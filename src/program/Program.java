package program;

import ast.T;
import astFull.E;
import failure.Res;
import id.Id;
import id.Mdf;
import id.Refresher;
import failure.Fail;
import program.inference.RefineTypes;
import program.typesystem.ETypeSystem;
import program.typesystem.Gamma;
import utils.Bug;
import utils.Pop;
import utils.Push;
import utils.Streams;
import visitors.CloneVisitor;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public interface Program {
  List<Id.IT<T>> itsOf(Id.IT<T> t);
  /** with t=C[Ts]  we do  C[Ts]<<Ms[Xs=Ts],*/
  List<CM> cMsOf(Id.IT<T> t);
  Set<Id.GX<ast.T>> gxsOf(Id.IT<T> t);
  Program withDec(T.Dec d);
  List<ast.E.Lambda> lambdas();

  static void reset() {
    methsCache.clear();
  }

  default void typeCheck() {
    var g = Gamma.empty();
    var checker = ETypeSystem.of(this, g, Optional.empty(), 0);
    lambdas().stream()
      .map(checker::visitLambda)
      .map(Res::err)
      .filter(Optional::isPresent)
      .map(Optional::get)
      .findAny()
      .ifPresent(err->{ throw err; });
  }

  default boolean isSubType(Mdf m1, Mdf m2) { //m1<m2
    if(m1==m2){ return true; }
    return switch(m1){
      case mut-> m2.isLikeMut();
      case lent, imm -> m2.isRead();
      case read-> false;
      case iso-> true;
      case recMdf, mdf -> throw Bug.unreachable();
    };
  }
  default boolean isSubType(astFull.T t1, astFull.T t2) { return isSubType(t1.toAstT(), t2.toAstT()); }
  default boolean isSubType(T t1, T t2) {
    if(!isSubType(t1.mdf(), t2.mdf())){ return false; }
    if(t1.rt().equals(t2.rt())){ return true; }
    if(!t1.isIt() || !t2.isIt()){ return false; }

    if (isTransitiveSubType(t1, t2)) { return true; }
    if (t1.itOrThrow().name().equals(t2.itOrThrow().name())) {
      return isAdaptSubType(t1, t2); // TODO: Depends on the rest of the type system
    }
    return false;

//    var isITOk = isTransitiveSubType(t1, t2);//isMdfOk && (isDirectSubType(t1, t2) || isAdaptSubType(t1, t2));
//    // TODO: more, transitive, etc.
//    return isITOk;
  }
  /*default boolean isDirectSubType(T t1, T t2) {
    //MDF C[T1..Tn]<MDF C'[Ts]
    //where
    //  C[X1..Xn]:ITs {_}
    //  C'[Ts] in ITs[X1..Xn=T1..Tn]
    return itsOf(t1.itOrThrow()).stream().anyMatch(it->it.equals(t2.itOrThrow()));
  }*/

  default boolean isAdaptSubType(T t1, T t2) {
  /*MDF C[T1..Tn]< MDF C[T1'..Tn']
    where
      adapterOk(MDF,C,T1..Tn,T1'..Tn')
  */
    assert t1.mdf() == t2.mdf();
    var mdf = t1.mdf();

    /*
    #Define adapterOk(MDF,C,Ts1,Ts2)
    adapterOk(MDF0,C,Ts1,Ts2)
      filterByMdf(MDF0, meths(C[Ts1]) = Ms1
      filterByMdf(MDF0, meths(C[Ts2]) = Ms2
      forall MDF m[Xs](G1):T1 _,MDF m[Xs](G2):T2 _ in mWisePairs(Ms1,Ms2)
        G2, this:MDF0 C[Ts1] |- this.m[Xs](G2.xs) : T2
     */
    var it1 = t1.itOrThrow();
    var it2 = t2.itOrThrow();
    assert it1.name().equals(it2.name());
    List<CM> cms1 = filterByMdf(mdf, meths(it1, 0));
    List<CM> cms2 = filterByMdf(mdf, meths(it2, 0));

    var methsByName = Stream.concat(cms1.stream(), cms2.stream())
      .collect(Collectors.groupingBy(CM::name))
      .values();
    return !methsByName.isEmpty() && methsByName.stream()
      .allMatch(ms->{
        assert ms.size() == 2;
        var m1 = ms.get(0);
        var m2 = ms.get(1);
        var recv = new ast.E.X("this", Optional.empty());
        var xs=Push.of(m1.xs(),"this");
        List<T> ts=Push.of(m2.sig().ts(),t1);

        var gxs = m2.sig().gens().stream().map(gx->new T(Mdf.mdf, gx)).toList();
        var e=new ast.E.MCall(recv, m1.name(), gxs, m1.xs().stream().<ast.E>map(x->new ast.E.X(x, Optional.empty())).toList(), Optional.empty());
        return isType(xs, ts, e, m2.sig().ret());
      });
  }

  default failure.Res typeOf(List<String>xs,List<ast.T>ts, ast.E e) {
    var g = Streams.zip(xs,ts).fold(Gamma::add, Gamma.empty());
    var v = ETypeSystem.of(this,g, Optional.empty(),0);
    return e.accept(v);
  }

  default boolean isType(List<String>xs,List<ast.T>ts, ast.E e, ast.T expected) {
    var g = Streams.zip(xs,ts).fold(Gamma::add, Gamma.empty());
    var v = ETypeSystem.of(this,g, Optional.of(expected),0);
    var res = e.accept(v);
    return res.resMatch(t->isSubType(t,expected),err->false);
  }

  default List<CM> filterByMdf(Mdf mdf, List<CM> cms) {
    if (cms.isEmpty()) { return List.of(); }
    var cm = cms.get(0);
    cms = Pop.left(cms);
    if (mdf.isIso() || mdf.isMut() || mdf.isLent()) {
      return Push.of(cm, filterByMdf(mdf, cms));
    }
    var sig = cm.sig();
    var baseMdfImmOrRead = mdf.isImm() || mdf.isRead();
    var methMdfImmOrRead = sig.mdf().isImm() || sig.mdf().isRead();
    if (baseMdfImmOrRead && methMdfImmOrRead) {
      return Push.of(cm, filterByMdf(mdf, cms));
    }
    return filterByMdf(mdf, cms);
  }

  record MWisePair(E.Meth a, E.Meth b){}
  default List<MWisePair> mWisePairs(){
    throw Bug.todo();
  }

  default Optional<E.Sig> fullSig(Id.IT<astFull.T> it, int depth, Predicate<CM> pred) {
    var freshGXs = Id.GX.standardNames(it.ts().size());
    var freshGXsSet = new HashSet<>(freshGXs);
    var freshGXsQueue = new ArrayDeque<>(freshGXs);
    var ts = it.ts().stream().map(t->new ast.T(Mdf.mdf, freshGXsQueue.poll())).toList();
    var myM_ = meths(new Id.IT<>(it.name(), ts), depth).stream()
      .filter(pred)
      .toList();
    if(myM_.isEmpty()){ return Optional.empty(); }
    assert myM_.size()==1;

    var cm = myM_.get(0);
    var sig = cm.sig().toAstFullSig();
    var restoredArgs = sig.ts().stream().map(t->RefineTypes.regenerateInfers(freshGXsSet, t)).toList();
    var restoredRt = RefineTypes.regenerateInfers(freshGXsSet, sig.ret());
    return Optional.of(new E.Sig(sig.mdf(), sig.gens(), restoredArgs, restoredRt, sig.pos()));
  }

  default Optional<CM> meths(Id.IT<T> it, Id.MethName name, int depth){
    var myM_ = meths(it, depth).stream().filter(mi->mi.name().equals(name)).toList();
    if(myM_.isEmpty()){ return Optional.empty(); }
    assert myM_.size()==1;
    return Optional.of(myM_.get(0));
  }

  default List<CM> meths(Id.IT<T> it, int depth) {

    return methsAux(it).stream().map(cm->freshenMethGens(cm, depth)).toList();
  }
  HashMap<Id.IT<T>, List<CM>> methsCache = new HashMap<>();
  default List<CM> methsAux(Id.IT<T> it) {
    // Can't use computeIfAbsent here because concurrent modification thanks to mutual recursion :-(
    if (methsCache.containsKey(it)) { return methsCache.get(it); }
    List<CM> cms = Stream.concat(
      cMsOf(it).stream(),
      itsOf(it).stream().flatMap(iti->methsAux(iti).stream())
    ).toList();
    var res = prune(cms);
    methsCache.put(it, res);
    return res;
  }

  static record RenameGens(Map<Id.GX<T>,Id.GX<T>> subst) implements CloneVisitor {
    public Id.GX<T> visitGX(Id.GX<T> t) {
      var thisSubst = subst.get(t);
      if (thisSubst != null) { return thisSubst; }
      return t;
    }
  }
  default CM norm(CM cm) {
    /*
    norm(CM) = CM' // Note: the (optional) body is not affected
    //Note, in CM ::= C[Ts].sig the class Xs are already replaced with Ts
    norm(C[Ts].m[X1..Xn](xTs):T->e) = C[Ts] (.m[](xTs):T->e)[X1=Par1..Xn=Parn]
      where we consistently select Par1..Parn globally so that
      it never happens that the current Ds contains Par1..Parn anywhere as a nested X
      Note: to compile with a pre compiled program we must add that
    norm(C[Ts].m[Par1 Xs](xTs):T->e) = C[Ts].m[Par1 Xs](xTs):T->e
     */
    //standardNames(n)->List.of(Par1..Parn)
    var gx=cm.sig().gens();
    List<Id.GX<ast.T>> names = new Refresher<ast.T>(0).freshNames(gx.size());
    Map<Id.GX<T>,Id.GX<T>> subst=IntStream.range(0,gx.size()).boxed()
      .collect(Collectors.toMap(gx::get, names::get));
    var newSig=new RenameGens(subst).visitSig(cm.sig());
    return cm.withSig(newSig);
  }

  /**
   * Normalised CMs are required for 5a, but the rest of the type system needs fresh names.
   */
  default CM freshenMethGens(CM cm, int depth) {
    var gxs=cm.sig().gens();
    var names = new Refresher<T>(depth).freshNames(gxs.size());
    Map<Id.GX<T>,Id.GX<T>> subst=IntStream.range(0,gxs.size()).boxed()
      .collect(Collectors.toMap(gxs::get, names::get));
    var newSig=new RenameGens(subst).visitSig(cm.sig());
    return cm.withSig(newSig);
  }

  default List<CM> prune(List<CM> cms) {
    /*
    prune(CMs) = pruneAux(CMs1)..pruneAux(CMsn)
      where CMs1..CMsn = groupByM(norm(CMs)) //groupByM(CMs)=CMss groups for the same m,n
     */
    var cmsMap = cms.stream()
      .distinct()
      .collect(Collectors.groupingBy(CM::name));
    return cmsMap.values().stream()
      .map(cmsi->pruneAux(cmsi,cmsi.size()+1))
      .toList();
  }

  default CM pruneAux(List<CM> cms,int limit) {
    if(limit==0){
      throw Fail.uncomposableMethods(cms.stream()
        .map(cm->Fail.conflict(cm.pos(), cm.toStringSimplified()))
        .toList()
      );
    }
    /*
    pruneAux(CM) = CM
    pruneAux(CM0..CMn)= pruneAux(CMs) where
      CMs=allCases(CM0..CMn)
      n > 0
      CMs.size < n //else error
     */
    assert cms.size() >= 1;
    var first=cms.get(0);
    if (cms.size() == 1) { return first; }
    var nextCms=cms.stream().skip(1)
      .filter(cmi->!firstIsMoreSpecific(first, cmi))
      .toList();

    return pruneAux(Push.of(nextCms,first),limit-1);
  }

  /*default List<CM> allCases(List<CM> cms) {

    allCases(CMs) = CMs.enumerate().permutations(2)
      .filter(<<i,m1>,<j,m2>> -> i < j)
      .map(<<i,m1>,<j,m2>>->selectMoreSpecific(m1,m2)).toList()

    var res = new LinkedHashSet<CM>();
    for (int i : Range.of(cms)) {
      for (int j : Range.of(cms)) {
        if (i <= j) { continue; }
        res.add(selectMoreSpecific(cms.get(i), cms.get(j)));
      }
    }
    return res.stream().toList();
  }*/

  // TODO: Write some tests to check that this is transitive
  /*default CM selectMoreSpecific(CM a, CM b) {
    return selectMoreSpecificAux(a,b)
      .or(()->selectMoreSpecificAux(b,a))
      .orElseThrow(()->Fail.uncomposableMethods(a.c(), b.c()));
  }*/
  default boolean firstIsMoreSpecific(CM a, CM b) {
      /*
      selectMoreSpecific(CM1,CM2) = CMi
        where
          CMi = Ci[Tsi] . MDF m[Xs](G):Ti e?i //Xs (not Xsi) requires the same (normed) Xs
          {j} = {1,2}\i
          not Ds|- Cj[Tsj]<=Ci[Tsi]
          either
           - Ds|- Ci[Tsi]<=Cj[Tsj] and Ds|- Ti<=Tj
           - e?j is empty and Ti = Tj//only not derm on syntactically eq
           - e?j is empty, Ds|- Ti<=Tj and not Ds|- Tj<=Ti
       */
    assert a.name().equals(b.name());
    var ta = new T(Mdf.mdf, a.c());
    var tb = new T(Mdf.mdf, b.c());
    if(isSubType(tb, ta)){ return false; }
    var ok=a.sig().gens().equals(b.sig().gens())
      && a.sig().ts().equals(b.sig().ts())
      && a.mdf()==b.mdf();
    if(!ok){ return false; }

    var isSubType = isSubType(ta, tb) && isSubType(a.ret(), b.ret());
    if(isSubType){ return true; }

    var is1AbsAndRetEq = b.isAbs() && a.ret().equals(b.ret());
    if(is1AbsAndRetEq){ return true; }

    var is1AbsAndRetSubtype = b.isAbs()
      && isSubType(a.ret(), b.ret())
      && !isSubType(b.ret(), a.ret());
    if(is1AbsAndRetSubtype){ return true; }

    return false;
  }

  default boolean isTransitiveSubType(T t1, T t3) {
  /*
  MDF IT1 < MDF IT3
  where
    MDF IT1 < MDF IT2
    MDF IT2 < MDF IT3
  */
    var mdf = t1.mdf();
    if (mdf != t3.mdf()) { return false; }
    return itsOf(t1.itOrThrow()).stream()
      .anyMatch(t2->isSubType(new T(mdf, t2), t3));
  }

  default Id.IT<ast.T> liftIT(Id.IT<astFull.T>it){
    var ts = it.ts().stream().map(astFull.T::toAstT).toList();
    return new Id.IT<>(it.name(), ts);
  }
}
//----

/* m(xs) is just using the length of xs and the name (m) to extract the method
  Ds,C[Xs]|-m(xs)->e => sig->e, // task 1
  where
    C[Xs] : ITs {_} in Ds //TODO: in docs
    exists sig such that
      forall IT in ITs such that m(xs) in dom(Ds,IT) //TODO: in docs
        sig.xs = xs
        Ds|-meths(IT)(m(xs)) compatible with sig //that is, equal except for sig.xs


#Define meths(C[Ts])=CMs   meths(C[Xs]:MDF B)=CMs

meths(C[Ts]) = prune(Ms[Xs=Ts], meths(IT1[Xs=Ts]),..,meths(ITn[Xs=Ts]))
  where C[Xs]: IT1..ITn {x, Ms} in Ds

meths(C[Xs]:MDF IT1..ITn {x, Ms}) = prune(Ms, meths(IT1),..,meths(ITn))


#Define prune(CMs) = CMs'   norm(CM)=CM'   allCases(CMs)=CMss  pruneAux(CMs)=CM

prune(CMs) = pruneAux(CMs1)..pruneAux(CMsn)
  where CMs1..CMsn = groupByM(norm(CMs)) //groupByM(CMs)=CMss groups for the same m,n
pruneAux(CM) = CM
pruneAux(CMs)= pruneAux(selectMoreSpecific(CMs1)..selectMoreSpecific(CMsn))
  where allCases(CMs) = CMs1..CMsn
  otherwise

allCases(CMs) = CMs.enumerate().permutations(2)
  .filter(<<i,m1>,<j,m2>> -> i < j)
  .map(<<i,m1>,<j,m2>> ->[m1,m2]).toList()

selectMoreSpecific(CM1,CM2) = CMi
  where
    CMi = Ci[Tsi] . MDF m[Xs](G):Ti e?i //Xs (not Xsi) requires the same (normed) Xs
    j = {1,2}\i
    either
     - Ds|- Ci[Tsi]<=Cj[Tsj] and Ds|- Ti<=Tj
     - e?j is empty and Ti = Tj//only not derm on syntactically eq
     - e?j is empty, Ds|- Ti<=Tj and not Ds|- Tj<=Ti

#Define T<T'

T<T

MDF X < MDF' X
  where MDF<MDF'

MDF IT < MDF' IT'
  where
    MDF<MDF'
    MDF IT < MDF IT'


MDF C[T1..Tn]<MDF C'[Ts]
  where
    C[X1..Xn]:ITs {_}
    C'[Ts] in ITs[X1..Xn=T1..Tn]

MDF C[T1..Tn]< MDF C[T1'..Tn']
  where
    adapterOk(MDF,C,T1..Tn,T1'..Tn')


#Define adapterOk(MDF,C,Ts1,Ts2)

adapterOk(MDF0,C,Ts1,Ts2)
filterByMdf(MDF0, meths(C[Ts1]) = Ms1
filterByMdf(MDF0, meths(C[Ts2]) = Ms2
forall MDF m[Xs](G1):T1 _,MDF m[Xs](G2):T2 _ in mWisePairs(Ms1,Ms2)
G2,inner:MDF0 C[Ts1] |- inner.m[Xs](G2.xs) : T2

_______
#Define filterByMdf(MDF,Ms) = Ms'

filterByMdf(MDF,empty) = empty
filterByMdf(MDF,M Ms) = M,filterByMdf(MDF,Ms)
  where MDF in {capsule,mut,lent}
filterByMdf(MDF,M Ms) = M,filterByMdf(MDF,Ms)
  where MDF in {imm,read} M.MDF in {imm,read}
filterByMdf(MDF,M Ms) = filterByMdf(MDF,Ms)
  otherwise


#Define MDF < MDF'

MDF < MDF
capsule < MDF
MDF < read
mut < lent
*/
