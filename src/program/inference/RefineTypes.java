package program.inference;

import astFull.E;
import astFull.T;
import id.Id;
import id.Mdf;
import program.TypeRename;
import utils.Bug;
import utils.Push;
import utils.Range;
import utils.Streams;

import java.util.*;
import java.util.function.Function;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public record RefineTypes(astFull.Program p) {
  List<E> fixTypes(List<astFull.E> ies, List<astFull.T> iTs) {
    return Streams.zip(ies, iTs).map(this::fixType).toList();
  }
  astFull.E fixType(astFull.E ie, astFull.T iT) { return ie.withTP(best(iT,ie.t())); }

  astFull.T best(astFull.T iT1, astFull.T iT2) {
    if(iT1.equals(iT2)){ return iT1; }
    if(iT1.isInfer()){ return iT2; }
    if(iT2.isInfer()){ return iT1; }
    if(iT1.rt() instanceof Id.GX<?> && iT2.rt() instanceof Id.IT<?>){ return iT2; }
    if(iT2.rt() instanceof Id.GX<?> && iT1.rt() instanceof Id.IT<?>){ return iT1; }
    if(iT1.rt() instanceof Id.GX<?> g1 && iT2.rt() instanceof Id.GX<?> g2){
      if(g1.equals(g2)){ return iT1; }
    }
    if(!(iT1.rt() instanceof Id.IT<astFull.T> c1) || !(iT2.rt() instanceof Id.IT<astFull.T> c2)){
      throw Bug.unreachable();
    }
    var notMatch=!c1.name().equals(c2.name()); //name includes gen size
    if(notMatch){ return iT1; }
    // TODO should we check the subtyping between C and C' instead?
    List<RP> refined = refineSigGens(new RP(c1.ts(),c2.ts()));
    List<astFull.T> refinedTs = refined.stream().map(RP::t1).toList();
    if(iT1.mdf()!=iT2.mdf()){ throw Bug.unreachable(); }
    //TODO: if the MDFs are different? take the most specific? not on iso?
    return new astFull.T(iT1.mdf(),c1.withTs(refinedTs));
  }
  record RP(astFull.T t1, astFull.T t2){}
  public static final TypeRename.FullTTypeRename renamer = new TypeRename.FullTTypeRename();
  List<RP> refineSigGens(List<RP>rps){
    Map<Id.GX<T>, T> map = toSub(collect(rps));
    return rps.stream().map(rp->renameRP(rp,map,renamer)).toList();
  }
  boolean isXX(RP rp){
    return rp.t1().rt() instanceof Id.GX<?> && rp.t2().rt() instanceof Id.GX<?>;
  }
  boolean isSameCDiffGens(int i, List<RP> rps) {
    var rp = rps.get(i);
    if ((rp.t1().rt() instanceof Id.IT<T> t1) && (rp.t2().rt() instanceof Id.IT<T> t2)) {
      return t1.ts().size() == t2.ts().size() && !t1.ts().equals(t2.ts());
    }
    return false;
  };
  record Sub(Id.GX<T> x,T t){}
  Sub collectXXOut(int index, ArrayList<RP> rps){
    var res = rps.remove(index);
    //collect(RPs, MDF X = _ X', RPs') =   X'=MDF X, collect(RPs[X = mdf X'], RPs'[X = mdf X'])
    var other = res.t2().withMdf(Mdf.mdf);
    var map = Map.of(res.t1().gxOrThrow(),other);
    Range.of(rps)
      .forEach(i->rps.set(i,renameRP(rps.get(i),map,renamer)));
    return new Sub(res.t2().gxOrThrow(),res.t1());
  }
  void collectSameCDiffGens(int index, ArrayList<RP> rps){
    var e = rps.remove(index);
    var its1=e.t1().itOrThrow().ts();
    var its2=e.t2().itOrThrow().ts();
    Range.of(its1).forEach(i->rps.add(index+i,new RP(its1.get(i),its2.get(i))));
  }
  void collectMulti_XC_XC(int index1, int index2, ArrayList<RP> rps){
    var e = rps.remove(index1);
    var its1=e.t2().itOrThrow().ts();
    var its2=rps.get(index2).t2().itOrThrow().ts();
    Range.of(its1).forEach(i->rps.add(index2+i,new RP(its1.get(i),its2.get(i))));
  }
  void collectMulti_XC_CX(int index1, int index2, ArrayList<RP> rps){
    var e = rps.remove(index1);
    var its1=e.t2().itOrThrow().ts();
    var its2=rps.get(index2).t1().itOrThrow().ts();
    Range.of(its1).forEach(i->rps.add(index2+i,new RP(its1.get(i),its2.get(i))));
  }
  void collectMulti_CX_XC(int index1, int index2, ArrayList<RP> rps){
    var e = rps.remove(index1);
    var its1=e.t1().itOrThrow().ts();
    var its2=rps.get(index2).t2().itOrThrow().ts();
    Range.of(its1).forEach(i->rps.add(index2+i,new RP(its1.get(i),its2.get(i))));
  }
  void collectMulti_CX_CX(int index1, int index2, ArrayList<RP> rps){
    var e = rps.remove(index1);
    var its1=e.t1().itOrThrow().ts();
    var its2=rps.get(index2).t1().itOrThrow().ts();
    Range.of(its1).forEach(i->rps.add(index2+i,new RP(its1.get(i),its2.get(i))));
  }
  boolean isInferRP(int index, ArrayList<RP> rps) {
    var rp = rps.get(index);
    return rp.t1().isInfer() || rp.t2().isInfer();
  }
  boolean isXC(int index, ArrayList<RP> rps) {
    var rp = rps.get(index);
    return (rp.t1().rt() instanceof Id.GX<?>) && (rp.t2().rt() instanceof Id.IT<?>);
  }
  boolean isXC(T t, int index2, ArrayList<RP> rps) {
    var eq = t.rt().equals(rps.get(index2).t1().rt());
    return eq && isXC(index2,rps);
  }
  boolean isCX(T t, int index2, ArrayList<RP> rps) {
    var eq = t.rt().equals(rps.get(index2).t2().rt());
    return eq && isCX(index2,rps);
  }
  boolean isCX(int index, ArrayList<RP> rps) {
    var rp = rps.get(index);
    return (rp.t1().rt() instanceof Id.IT<?>) && (rp.t2().rt() instanceof Id.GX<?>);
  }
  List<Sub> collect(List<RP>rps) { return collectRec(new ArrayList<>(rps)); }
  List<Sub> collectRec(ArrayList<RP> rps) {
    var optXX = IntStream.range(0, rps.size())
      .filter(i->isXX(rps.get(i))).findFirst();
    if (optXX.isPresent()) {
      Sub xx = collectXXOut(optXX.getAsInt(), rps);
      return Push.of(xx, collectRec(rps));
    }

    var optSameCDiffGens = IntStream.range(0, rps.size())
      .filter(i->isSameCDiffGens(i,rps)).findFirst();
    if (optSameCDiffGens.isPresent()) {
      collectSameCDiffGens(optSameCDiffGens.getAsInt(), rps);
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
    if(collectMulti_XC(rps, optXC1)){ return collectRec(rps); }
    var optCX1 = IntStream.range(0, rps.size()).boxed()
      .filter(i->isCX(i,rps)).findFirst();
    if(collectMulti_CX(rps, optCX1)){ return collectRec(rps); }
    throw Bug.todo();
  }

  private boolean collectMulti_XC(ArrayList<RP> rps, Optional<Integer> optXC1) {
    var optXC2 =  optXC1.flatMap(xc1->IntStream.range(xc1, rps.size()).boxed()
      .filter(i->isXC(rps.get(i).t1(),xc1, rps)).findFirst());
    if(optXC2.isEmpty()){ return false; }
    collectMulti_XC_XC(optXC1.get(),optXC2.get(), rps);
    return true;
  }
  private boolean collectMulti_CX(ArrayList<RP> rps, Optional<Integer> optCX1) {
    var optCX2 =  optCX1.flatMap(cx1->IntStream.range(cx1, rps.size()).boxed()
      .filter(i->isCX(rps.get(i).t2(), cx1, rps)).findFirst());
    if (optCX2.isEmpty()){ return false; }
    collectMulti_XC_CX(optCX1.get(),optCX2.get(), rps);
    return true;
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

  Map<Id.GX<T>, T> toSub(List<Sub> rps) {
    Map<Id.GX<T>, List<Sub>> res = rps.stream()
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
