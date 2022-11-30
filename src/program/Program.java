package program;

import ast.T;
import astFull.E;
import astFull.PosMap;
import id.Id;
import id.Mdf;
import main.Fail;
import utils.Bug;
import utils.Pop;
import utils.Push;
import utils.Range;
import visitors.InjectionVisitor;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface Program {
  List<Id.IT<T>> itsOf(Id.IT<T> t);
  List<CM> cMsOf(Id.IT<T> t);

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

    var isITOk = isTransitiveSubType(t1, t2);//isMdfOk && (isDirectSubType(t1, t2) || isAdaptSubType(t1, t2));
    // TODO: more, transitive, etc.
    return isITOk;
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

    public String toStringSimplified() {
      return c+", "+name();
    }
    @Override public String toString() {
      return c+","+mdf()+" "+name()+"("+String.join(",", m.xs())+")"
        +sig.gens()+sig.ts()+":"+ret()
        +(isAbs()?"abs":"impl");
    }
  }
  default List<CM> meths(Id.IT<T> it) {
    List<CM> cms = Stream.concat(
      cMsOf(it).stream(),
      itsOf(it).stream().flatMap(iti->meths(iti).stream())
    ).toList();
    return prune(cms);
  }
  default List<CM> meths(astFull.T.Dec dec) {
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

  default ast.E.Sig norm(ast.E.Sig s) {
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
      .map(cmsi->pruneAux(cmsi,cmsi.size()+1))
      .toList();
  }

  default CM pruneAux(List<CM> cms,int limit) {
    if(limit==0){
      throw Fail.uncomposableMethods(cms.stream()
        .map(cm->Fail.conflict(PosMap.getOrUnknown(cm.m()), cm.toStringSimplified()))
        .toList()
      );
    }
    // TODO: I think CMs should be dedup'd see TestMeths.t15. We lost that when we left allCases
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
      .distinct()
      .toList();

    if (nextCms.size() == 1 && nextCms.get(0).equals(first)) { return pruneAux(List.of(first),limit-1); }
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
