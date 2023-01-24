package program.inference;

import astFull.E;
import astFull.T;
import id.Id;
import program.TypeRename;
import utils.Bug;
import utils.Streams;
import utils.Mapper;

import java.util.*;
import java.util.stream.Collectors;

public record RefineTypesOldBoo(astFull.Program p) {
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
    List<RP> refined = refineSigGens(RP.of(0,c1.ts(),c2.ts()));
    List<astFull.T> refinedTs = refined.stream().map(RP::t1).toList();
    if(iT1.mdf()!=iT2.mdf()){ throw Bug.unreachable(); }
    //TODO: if the MDFs are different? take the most specific? not on iso?
    return new astFull.T(iT1.mdf(),c1.withTs(refinedTs));
  }
  boolean wellNumbered(List<RP> rps){
    for(int i=0;i<rps.size();i++){ assert rps.get(i).pos()==i; }
    return true;
  }
  List<RP> refineSigGens(List<RP> rps) {
    int size=rps.size();
    rps = refineSigGens3(rps);//expand could be longer now (recursive)
    rps = refineSigGens2(rps);//killInfer
    rps = refineSigGens1(rps);//x norm
    rps = refineSigGens4(rps);//replace
    return rps.subList(0,size);//cutToSize
  }
  List<RP> refineSigGens1(List<RP> rps) {
    assert wellNumbered(rps);
    var byX = rps.stream()
      .collect(Collectors.groupingBy(rp->rp.t1().match(Id.GX::name, it->Id.GX.intrinsicStandardGX())));
    ArrayList<RP> res = new ArrayList<>(rps);
    for (var rpsi : byX.values()) {
      var ti = rpsi.stream().map(RP::t2).reduce(this::best).orElseThrow();
      for (RP rpi : rpsi) {
        int index = rpi.pos();
        res.set(index, new RP(index, res.get(index).t1(), ti));
      }
    }
    return Collections.unmodifiableList(res);
  }
  List<RP> refineSigGens2(List<RP> rps){
    return rps.stream().map(this::killInfer).toList();
  }
  RP killInfer(RP rp){
    if(rp.t1().isInfer()){ return new RP(rp.pos(),rp.t2(),rp.t2()); }
    if(rp.t2().isInfer()){ return new RP(rp.pos(),rp.t1(),rp.t1()); }
    return rp;
  }

  List<RP> refineSigGens3(List<RP> rps) {//expand the list of pairs with new matches if the decls match
    ArrayList<RP>res = new ArrayList<>(rps);
    int precSize=0;
    int currSize = res.size();
    while(currSize!=precSize){
      refineSigGens3(precSize,res);
      precSize=currSize;
      currSize=res.size();
      }
    return Collections.unmodifiableList(res);
    }
  void refineSigGens3(int start, ArrayList<RP> rps) {//expand
    List<RP> expanded = rps.stream().skip(start).filter(rp->{
      if (rp.t1().isInfer() || rp.t2().isInfer()) { return false; }
      if ((rp.t1().rt() instanceof Id.IT<astFull.T> it) && rp.t2().rt() instanceof Id.IT<astFull.T> it1) {
        var mdfMatch = rp.t1().mdf().equals(rp.t2().mdf());
        var nameAndGensMatch = it.name().equals(it1.name());
        return mdfMatch && nameAndGensMatch;
      }
      return false;
    }).toList();
    for (RP rp : expanded) {
      var it = rp.t1().itOrThrow();
      var it1 = rp.t2().itOrThrow();
      rps.addAll(RP.of(rps.size(), it.ts(), it1.ts()));
    }
  }
  List<RP> refineSigGens4(List<RP> rps) {//replace gens
    /*
    get a rename map X->t1
    navigate and find all pairs like X=C[] // do we need to also handle C[]=X
    X=C[  Y ]  X=C[]  Y =D[] k = K[X]
     */
    //Map.of(c->stream.forall(c.put(..))); // Flow[Pair[A,B]] -> Map[A,B]
    Map<Id.GX<T>,T> map =Mapper.of(c->rps.forEach(rp->{
      if ((rp.t1.rt() instanceof Id.GX<T> gx) && (rp.t2.rt() instanceof Id.IT<T>)) {
        c.put(gx, rp.t2);
      } else if ((rp.t2.rt() instanceof Id.GX<T> gx) && (rp.t1.rt() instanceof Id.IT<T>)) {
        c.put(gx, rp.t1);
      }
    }));
    return rps.stream().map(rp->renameRP(rp,map)).toList();
  }

  public RefineTypesOldBoo.RP renameRP(RefineTypesOldBoo.RP rp, Map<Id.GX<astFull.T>, astFull.T> map){
    var rename = new TypeRename.FullTTypeRename();
    var t1 = rename.renameT(rp.t1(),map::get);
    var t2 = rename.renameT(rp.t2(),map::get);
    return new RefineTypesOldBoo.RP(rp.pos(), t1, t2);
  }

  public record RP(int pos, astFull.T t1, astFull.T t2){
    static List<RP> of(int start,List<astFull.T> iTs,List<astFull.T> iTs1){
      int[] i = {start};
      return Streams.zip(iTs,iTs1).map((a,b)->new RP(i[0]++,a,b)).toList();
    }
  }
}
