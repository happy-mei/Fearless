package program;

import ast.T;
import astFull.E;
import id.Id;
import id.Mdf;
import utils.Bug;
import utils.Pop;
import utils.Push;
import utils.Range;
import visitors.InjectionVisitor;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface Program {
  List<Id.IT<T>> itsOf(Id.IT<T> t);

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
    if (!t1.isIt() || !t2.isIt()) { return false; }
    var isMdfOk = isSubType(t1.mdf(), t2.mdf());
    var isITOk = isDirectSubType(t1, t2) || isAdaptSubType(t1, t2);
    // TODO: more, transitive, etc.
    return (isMdfOk && isITOk) || isTransitiveSubType(t1.itOrThrow(), t2.itOrThrow());
  }
  default boolean isDirectSubType(T t1, T t2) {
    /*
    MDF C[T1..Tn]<MDF C'[Ts]
    where
      C[X1..Xn]:ITs {_}
      C'[Ts] in ITs[X1..Xn=T1..Tn]
     */
    // can simplify to a DecId comparison because name and gen count is what matters here
    return itsOf(t2.itOrThrow()).stream()
      .anyMatch(it->it.name().equals(t1.itOrThrow().name()));
  }

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
      G2,inner:MDF0 C[Ts1] |- inner.m[Xs](G2.xs) : T2
     */
    var it1 = t1.itOrThrow();
    var it2 = t2.itOrThrow();
    var ms1 = filterByMdf(mdf, meths(it1).stream().map(cn->cn.m).toList());
    var ms2 = filterByMdf(mdf, meths(it2).stream().map(cn->cn.m).toList());
    throw Bug.todo();
  }

  default List<E.Meth> filterByMdf(Mdf mdf, List<E.Meth> ms) {
    // #Define filterByMdf(MDF,Ms) = Ms'
    // filterByMdf(MDF,empty) = empty
    if (ms.isEmpty()) { return List.of(); }
    var m = ms.get(0);
    ms = Pop.left(ms);
    /*
    filterByMdf(MDF,M Ms) = M,filterByMdf(MDF,Ms)
        where MDF in {iso,mut,lent}
     */
    if (mdf.isIso() || mdf.isMut() || mdf.isLent()) {
      return Push.of(m, filterByMdf(mdf, ms));
    }
    /*
    filterByMdf(MDF,M Ms) = M,filterByMdf(MDF,Ms)
      where MDF in {imm,read} M.MDF in {imm,read}
     */
    var sig = m.sig().orElseThrow();
    var baseMdfImmOrRead = mdf.isImm() || mdf.isRead();
    var methMdfImmOrRead = sig.mdf().isImm() || sig.mdf().isRead();
    if (baseMdfImmOrRead && methMdfImmOrRead) {
      return Push.of(m, filterByMdf(mdf, ms));
    }
    /*
    filterByMdf(MDF,M Ms) = filterByMdf(MDF,Ms)
      otherwise
     */
    return filterByMdf(mdf, ms);
  }

  record MWisePair(E.Meth a, E.Meth b){}
  default List<MWisePair> mWisePairs(){
    throw Bug.todo();
  }

  record CM(Id.IT<T> c, E.Meth m, ast.E.Sig sig) {
    public Id.MethName name() { return m().name().orElseThrow(); }
    public Mdf mdf() { return sig().mdf(); }
    public T ret() { return sig().ret(); }
    public boolean isAbs(){ return m.isAbs(); }
  }
  default List<CM> meths(Id.IT<T> it) {
    /*
    #Define meths(C[Ts])=CMs   meths(C[Xs]:MDF B)=CMs            IT<<Ms
      meths(C[Ts]) = prune(C[Ts]<<Ms[Xs=Ts], meths(IT1[Xs=Ts]),..,meths(ITn[Xs=Ts]))
        where C[Xs]: IT1..ITn {x, Ms} in Ds

      meths(C[Xs]:MDF IT1..ITn {x, Ms}) = prune(C[Ts]<<Ms, meths(IT1),..,meths(ITn))

      IT<<empty = empty
      IT<< m(xs)->e Ms = IT<<Ms  //it includes   IT<< SM Ms = IT<<Ms
      IT<< sig->e, Ms = norm(IT.sig->e), IT<<Ms
      IT<< sig, Ms = norm(IT.sig), IT<<Ms
     */
    // uh oh...
    throw Bug.todo();
  }

  default List<CM> toCM(Id.IT<ast.T> it,List<E.Meth>ms,List<Id.GX<ast.T>>gxs){
    return ms.stream()
      .filter(m->m.sig().isPresent())
      .map(m->new CM(
        it,
        m,
        norm(rename(
          m.sig().orElseThrow().accept(new InjectionVisitor()),
          renameFun(it.ts(), gxs)
        ))
      ))
      .toList();
    }

  default ast.E.Sig norm(ast.E.Sig s) {
    throw Bug.todo();
  }

  default ast.E.Sig updateGxs(E.Meth m, List<Id.GX<ast.T>> gxs, List<ast.T> ts){
    var sig=m.sig().orElseThrow();
    throw Bug.todo();
  }

  default List<CM> prune(List<CM> cms) {
    /*
    prune(CMs) = pruneAux(CMs1)..pruneAux(CMsn)
      where CMs1..CMsn = groupByM(norm(CMs)) //groupByM(CMs)=CMss groups for the same m,n
     */
    var cmsMap = cms.stream()
      .collect(Collectors.groupingBy(CM::name));
    return cmsMap.values().stream()
      .map(this::pruneAux)
      .toList();
  }

  default CM pruneAux(List<CM> cms) {
    /*
    pruneAux(CM) = CM
    pruneAux(CM0..CMn)= pruneAux(CMs) where
      CMs=allCases(CM0..CMn)
      n > 0
      CMs.size < n //else error
     */
    if (cms.size() == 1) { return cms.get(0); }
    var nextCms = allCases(cms);
    assert cms.size() > 1;
    assert nextCms.size() < cms.size();
    return pruneAux(nextCms);
  }

  default List<CM> allCases(List<CM> cms) {
    /*
    allCases(CMs) = CMs.enumerate().permutations(2)
      .filter(<<i,m1>,<j,m2>> -> i < j)
      .map(<<i,m1>,<j,m2>>->selectMoreSpecific(m1,m2)).toList()
     */
    var res = new LinkedHashSet<CM>();
    for (int i : Range.of(cms)) {
      for (int j : Range.of(cms)) {
        if (i < j) { continue; }
        res.add(selectMoreSpecific(cms.get(i), cms.get(j)));
      }
    }
    return res.stream().toList();
  }

  default CM selectMoreSpecific(CM a, CM b) {
    return selectMoreSpecificAux(a,b)
      .or(()->selectMoreSpecificAux(b,a))
      .orElseThrow(()->Bug.todo("better error"));
  }
  default Optional<CM> selectMoreSpecificAux(CM a, CM b) {
      /*
      selectMoreSpecific(CM1,CM2) = CMi
        where
          CMi = Ci[Tsi] . MDF m[Xs](G):Ti e?i //Xs (not Xsi) requires the same (normed) Xs
          {j} = {1,2}\i
          either
           - Ds|- Ci[Tsi]<=Cj[Tsj] and Ds|- Ti<=Tj
           - e?j is empty and Ti = Tj//only not derm on syntactically eq
           - e?j is empty, Ds|- Ti<=Tj and not Ds|- Tj<=Ti
       */
    assert a.name().equals(b.name());
    var ok=a.sig().gens().equals(b.sig().gens())
      && b.sig().ts().equals(b.sig().ts())
      && a.mdf()==b.mdf();
    if(!ok){ return Optional.empty(); }

    var isSubType = isSubType(new T(Mdf.mdf, a.c()), new T(Mdf.mdf, b.c()))
      && isSubType(a.ret(), b.ret());
    if(isSubType){ return Optional.of(a); }

    var is1AbsAndRetEq = b.isAbs() && a.ret().equals(b.ret());
    if(is1AbsAndRetEq){ return Optional.of(a); }

    var is1AbsAndRetSubtype = b.isAbs()
      && isSubType(a.ret(), b.ret())
      && !isSubType(b.ret(), a.ret());
    if(is1AbsAndRetSubtype){ return Optional.of(a); }

    return Optional.empty();
  }

  default boolean isTransitiveSubType(Id.IT<T> t1, Id.IT<T> t3) {
  /*
  MDF IT1 < MDF IT3
  where
    MDF IT1 < MDF IT2
    MDF IT2 < MDF IT3
  */
    return itsOf(t1).stream()
      .anyMatch(t2->
        isSubType(new T(Mdf.mdf, t1), new T(Mdf.mdf, t2))
        && isSubType(new T(Mdf.mdf, t2), new T(Mdf.mdf, t3))
      );
  }

  default Id.IT<ast.T> liftIT(Id.IT<astFull.T>it){
    var ts = it.ts().stream().map(astFull.T::toAstT).toList();
    return new Id.IT<>(it.name(), ts);
  }
  default Id.IT<ast.T> rename(Id.IT<ast.T> it, Function<Id.GX<T>, T> f){
    return it.withTs(it.ts().stream().map(iti->rename(iti,f)).toList());
  }

  default ast.E.Sig rename(ast.E.Sig sig, Function<Id.GX<ast.T>,ast.T>f){
    assert sig.gens().stream().allMatch(gx->f.apply(gx)==null);
    return new ast.E.Sig(
      sig.mdf(),
      sig.gens(),
      sig.ts().stream().map(t->rename(t,f)).toList(),
      rename(sig.ret(),f)
      );
    }
  default ast.T rename(ast.T t,Function<Id.GX<ast.T>,ast.T>f){
    return t.match(
      gx->propagateMdf(t.mdf(),f.apply(gx)),
      it->new ast.T(t.mdf(),rename(it,f))
    );
  }
  default ast.T propagateMdf(Mdf mdf, ast.T t){
    assert t!=null;
    assert !mdf.isRecMdf();
    if(mdf.isMdf()){ return t; }
    return t.withMdf(mdf);
  }

  default Function<Id.GX<ast.T>, ast.T> renameFun(List<ast.T> ts, List<Id.GX<ast.T>> gxs) {
    return gx->{
      int i = gxs.indexOf(gx);
      if(i==-1){ return null; }
      return ts.get(i);
    };
  }
}

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
