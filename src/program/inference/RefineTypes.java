package program.inference;

import astFull.T;
import astFull.E;
import id.Id;
import id.Mdf;
import program.CM;
import program.TypeRename;
import utils.*;
import visitors.FullShortCircuitVisitor;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public record RefineTypes(ast.Program p) {
  List<E> fixTypes(List<E> ies, List<T> iTs) {
    return Streams.zip(ies, iTs).map(this::fixType).toList();
  }
  E fixType(E ie, T iT) { return ie.withT(best(iT,ie.t())); }

  T best(T iT1, T iT2) {
    if(iT1.equals(iT2)){ return iT1; }
    if(iT1.isInfer()){ return iT2; }
    if(iT2.isInfer()){ return iT1; }
    if(iT1.rt() instanceof Id.GX<?> && iT2.rt() instanceof Id.IT<?>){ return iT2; }
    if(iT2.rt() instanceof Id.GX<?> && iT1.rt() instanceof Id.IT<?>){ return iT1; }
    if(iT1.rt() instanceof Id.GX<?> g1 && iT2.rt() instanceof Id.GX<?> g2){
      if(g1.equals(g2)){ return iT1; }
    }
    if( !(iT1.rt() instanceof Id.IT<T> c1)
     || !(iT2.rt() instanceof Id.IT<T> c2)){
      throw Bug.unreachable();
    }
    var notMatch=!c1.name().equals(c2.name()); //name includes gen size
    if(notMatch){ return iT1; }
    // TODO should we check the subtyping between C and C' instead?
    List<RP> refined = refineSigGens(RP.of(c1.ts(),c2.ts()));
    if(refined.isEmpty()){ return iT1; }
    List<T> refinedTs = refined.stream().map(RP::t1).toList();
    if(iT1.mdf()!=iT2.mdf()){ throw Bug.unreachable(); }
    //TODO: if the MDFs are different? take the most specific? not on iso?
    return new T(iT1.mdf(),c1.withTs(refinedTs));
  }
  record RP(T t1, T t2){
    static List<RP> of(List<T> iTs, List<T> iTs1){
      return Streams.zip(iTs,iTs1).map(RP::new).toList();
    }
    static List<RP> ofCore(List<ast.T> iTs, List<ast.T> iTs1){
      return Streams.zip(iTs,iTs1).map((iT,iT1)->new RP(iT.toAstFullT(), iT1.toAstFullT())).toList();
    }

  }
  public static final TypeRename.FullTTypeRename renamer = new TypeRename.FullTTypeRename();

  record RefinedSig(Id.MethName name, List<T> gens, List<T> args, T rt){}
  List<T> renameSigPart(Set<Id.GX<ast.T>> fresh, List<RP> refined, int start, int end) {
    return IntStream.range(start, end)
      .mapToObj(i->refined.get(i).t1())
      .map(t->renamer.renameT(t, gx->{
        if (fresh.contains(gx)) { return T.infer; }
        return new T(t.mdf(), gx);
      }))
      .toList();
  }

  RefinedSig refineSig(Id.IT<T> c, RefinedSig sig) {
    var freshGXs = Id.GX.standardNames(c.ts().size() + sig.gens().size());
    var freshGXsSet = new HashSet<>(freshGXs);
    var freshGXsQueue = new ArrayDeque<>(freshGXs);
    var ts = c.ts().stream().map(t->new ast.T(Mdf.mdf, freshGXsQueue.poll())).toList();    // Xs
    var gxs = sig.gens().stream().map(gx->freshGXsQueue.poll()).toList();    // Xs'
    var ms = p.meths(new Id.IT<>(c.name(), ts));
    var freshSig = freshXs(ms, sig.name(), gxs);
    var freshGens = freshSig.gens();
    List<RP> rps = Streams.of(
      RP.of(sig.gens(), freshGens).stream(),
      RP.of(sig.args(), freshSig.args()).stream(),
      Stream.of(new RP(freshSig.rt(), sig.rt()))
    ).toList();
    var refined = refineSigGens(rps);//BIG call
    var refinedGens = renameSigPart(freshGXsSet, refined, 0, sig.gens().size());
    var refinedArgs = renameSigPart(freshGXsSet, refined, sig.gens().size(), sig.gens().size() + sig.args().size());
    var refinedRT = renameSigPart(freshGXsSet, refined, refined.size() - 1, refined.size()).get(0);

    return new RefinedSig(sig.name(), refinedGens, refinedArgs, refinedRT);
  }

  RefinedSig freshXs(List<CM> ms, Id.MethName m, List<Id.GX<ast.T>> gxs) {
    var meth = OneOr.of(
      "More than one valid method found for "+m,
      ms.stream().filter(mi->mi.name().equals(m))
    );
    var sig = meth.sig().toAstFullSig();
    assert meth.sig().gens().size() == gxs.size();
    return new RefinedSig(meth.name(),
      gxs.stream().map(gx->new T(Mdf.mdf, gx.toFullAstGX())).toList(),
      sig.ts(),
      sig.ret()
    );
  }

  List<RP> refineSigGens(List<RP>rps){
    List<Sub> subs = collect(rps);
    Map<Id.GX<T>, T> map = toSub(subs);
    return rps.stream().map(rp->renameRP(rp,map,renamer)).toList();
  }
  boolean isXX(RP rp){
    return rp.t1().rt() instanceof Id.GX<?> && rp.t2().rt() instanceof Id.GX<?>;
  }
  boolean sameC(int i, List<RP> rps) {
    var rp = rps.get(i);
    if ((rp.t1().rt() instanceof Id.IT<T> t1) && (rp.t2().rt() instanceof Id.IT<T> t2)) {
      return t1.name().equals(t2.name());
    }
    return false;
  }
  record Sub(Id.GX<T> x,T t){
    boolean isCircular() {
      if (t.isInfer() || t.rt() instanceof Id.GX<?>) { return false; }
      assert t.rt() instanceof Id.IT<?>;
      var hasXVisitor = new FullShortCircuitVisitor<Boolean>(){
        @Override
        public Optional<Boolean> visitGX(Id.GX<T> t) {
          if (t.equals(x)) { return Optional.of(true); }
          return Optional.empty();
        }
      };
      return hasXVisitor.visitT(t).orElse(false);
    }
  }
  Sub collectXXOut(int index, ArrayList<RP> rps){
    var res = rps.remove(index);
    //collect(RPs, MDF X = _ X', RPs') =   X'=MDF X, collect(RPs[X = mdf X'], RPs'[X = mdf X'])
    var other = res.t2().withMdf(Mdf.mdf);
    rename(rps,new Sub(res.t1().gxOrThrow(),other));
    return new Sub(res.t2().gxOrThrow(),res.t1());
  }
  void collectSameC(int index, ArrayList<RP> rps){
    var e = rps.remove(index);
    var its1=e.t1().itOrThrow().ts();
    var its2=e.t2().itOrThrow().ts();
    Range.of(its1).forEach(i->rps.add(index+i,new RP(its1.get(i),its2.get(i))));
  }
  Sub collectMulti(int index1, int index2, ArrayList<RP> rps){
    var e = rps.remove(index1);
    index2 -= 1;
    insertGens(rps, index2, gensOf(e), gensOf(rps.get(index2)));
    return toSub(e).orElseThrow();
  }
  List<T> gensOf(RP rp){
    if(rp.t1().rt() instanceof Id.GX<?>){ return rp.t2().itOrThrow().ts(); }
    assert rp.t2().rt() instanceof Id.GX<?>;
    return rp.t1().itOrThrow().ts();
  }
  void insertGens(ArrayList<RP> rps, int index, List<T> its1, List<T> its2) {
    Range.of(its1).forEach(i->rps.add(index+i,new RP(its1.get(i),its2.get(i))));
  }
  boolean isInferRP(int index, ArrayList<RP> rps) {
    var rp = rps.get(index);
    return rp.t1().isInfer() || rp.t2().isInfer();
  }
  boolean isSameXC(RP rp1, RP rp2) {
    Id.GX<?> x1 = rp1.t1().match(gx->gx,it->rp1.t2().gxOrThrow());
    Id.DecId c1 =  rp1.t1().match(gx->rp1.t2().itOrThrow(),it->it).name();
    Optional<Id.IT<?>> c21 = rp2.t1().match(gx->Optional.empty(), Optional::of);
    Optional<Id.IT<?>> c22 = rp2.t2().match(gx->Optional.empty(), Optional::of);
    if(c21.isPresent() && c22.isPresent()){ return false; }
    if(c21.isEmpty() && c22.isEmpty()){ return false; }
    Id.DecId c2=c21.or(()->c22).get().name();
    if(!c1.equals(c2)){ return false; }
    return rp2.t1().match(
      x2->x1.equals(x2),
      it->rp2.t2().match(x1::equals, it_->false)
    );
  }
  boolean isXC(int index, ArrayList<RP> rps) {
    var rp = rps.get(index);
    var leftX = (rp.t1().rt() instanceof Id.GX<?>) && (rp.t2().rt() instanceof Id.IT<?>);
    var rightX = (rp.t2().rt() instanceof Id.GX<?>) && (rp.t1().rt() instanceof Id.IT<?>);
    return leftX || rightX;
  }
  List<Sub> collect(List<RP>rps) { return collectRec(new ArrayList<>(rps)); }
  void rename(ArrayList<RP>rps,Sub s){
    if(s.isCircular()){ return; }
    var map = Map.of(s.x(),s.t());
    Range.of(rps).forEach(i->rps.set(i,renameRP(rps.get(i),map,renamer)));
  }
  List<Sub> collectRec(ArrayList<RP> rps) {
    if(rps.isEmpty()){ return List.of(); }

    var optXX = IntStream.range(0, rps.size())
      .filter(i->isXX(rps.get(i))).findFirst();
    if (optXX.isPresent()) {
      Sub xx = collectXXOut(optXX.getAsInt(), rps);
      return Push.of(xx, collectRec(rps));
    }

    var optSameC = IntStream.range(0, rps.size())
      .filter(i->sameC(i,rps)).findFirst();
    if (optSameC.isPresent()) {
      collectSameC(optSameC.getAsInt(), rps);
      return collectRec(rps);
    }
    var optInfer = IntStream.range(0, rps.size())
      .filter(i->isInferRP(i, rps)).findFirst();
    if (optInfer.isPresent()){
      rps.remove(optInfer.getAsInt());
      return collectRec(rps);
    }
    var optXC1 = IntStream.range(0, rps.size()).boxed()
      .filter(i->isXC(i,rps)).findFirst();
    var optXC2 = optXC1.flatMap(xc1->Streams
      .firstPos(xc1+1,rps,i->isSameXC(rps.get(i), rps.get(xc1))));
    if(optXC2.isPresent()){
      Sub xc = collectMulti(optXC1.get(),optXC2.get(), rps);
      return Push.of(xc, collectRec(rps));
    }
    // otherwise...
    assert !rps.isEmpty();
    RP head = rps.remove(0);
    var sub=toSub(head);
    if (sub.isEmpty()){ return collectRec(rps); }
    rename(rps,sub.get());
    return Push.of(sub.get(), collectRec(rps));
  }
  private Optional<Sub> toSub(RP rp){
    var t1=rp.t1();
    var t2=rp.t2();
    var leftX = (t1.rt() instanceof Id.GX<?>) && (t2.rt() instanceof Id.IT<?>);
    var rightX = (t2.rt() instanceof Id.GX<?>) && (t1.rt() instanceof Id.IT<?>);
    if(leftX){ return Optional.of(new Sub(t1.gxOrThrow(), t2.withMdf(t1.mdf()))); }
    if(rightX){ return Optional.of(new Sub(t2.gxOrThrow(), t1)); }
    return Optional.empty();
  }

  /*
//In the rules below we always select the smallest RPs possible
collect(RPs, MDF X = _ X', RPs') =   X'=MDF X, collect(RPs[X = mdf X'], RPs'[X = mdf X'])
collect(RPs, _ C[iTs] = _ C[iTs'], RPs') = collect(RPs, iTs = iTs', RPs)
collect(RPs, infer = _, RPs') = collect(RPs')
collect(RPs, _ = infer, RPs') = collect(RPs')
collect(RPs, _ X = _ C[iTs], RPs', MDF X = MDF' C[iTs'], RPs")
  = collect(RPs,RPs', MDF X = MDF' C[iTs'], iTs = ITs', RPs")
collect(RPs, _ X = _ C[iTs], RPs', MDF' C[iTs'] = MDF X, RPs")
  = collect(RPs,RPs',MDF' C[iTs'] = MDF X, iTs = iTs', RPs")
collect(RPs, _ C[iTs] = _ X, RPs', MDF' C[iTs'] = MDF X, RPs")
   = collect(RPs,RPs',MDF' C[iTs'] = MDF X, iTs = iTs', RPs')
otherwise, if no other rule is applicable
collect(MDF X = MDF' C[iTs], RPs) =     X=MDF C[iTs] collect(RPs)
collect(MDF C[iTs] = MDF' X, RPs) =     X=MDF C[iTs] collect(RPs)
collect(empty) = empty
  */

  Map<Id.GX<T>, T> toSub(List<Sub> subs) {
    Map<Id.GX<T>, List<Sub>> res = subs.stream()
      .filter(si->!si.isCircular())
      .filter(si->!si.x().equals(si.t().rt()))
      .collect(Collectors.groupingBy(Sub::x));
    return res.values().stream().collect(Collectors.toMap(
      si->si.get(0).x(),
      si->bestAll(si.stream().map(Sub::t))
    ));
  }
  T bestAll(Stream<T> ts){ return ts.reduce(this::best).orElseThrow(); }

  public RP renameRP(RP rp, Map<Id.GX<T>, T> map, TypeRename.FullTTypeRename rename){
    var t1 = rename.renameT(rp.t1(),map::get);
    var t2 = rename.renameT(rp.t2(),map::get);
    return new RP(t1, t2);
  }
}
