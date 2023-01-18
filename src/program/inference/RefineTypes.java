package program.inference;

import astFull.E;
import astFull.T;
import id.Id;
import program.Program;
import utils.Bug;
import utils.Streams;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

public record RefineTypes(astFull.Program p) {
  List<E> fixTypes(List<astFull.E> ies, List<astFull.T> iTs) {
    return Streams.zip(ies, iTs).map(this::fixType).toList();
  }
  astFull.E fixType(astFull.E ie, astFull.T iT) { return ie.withTP(best(ie.t(),iT)); }

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
    List<astFull.T> refinedTs = refined.stream().map(RP::iT).toList();
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
      .collect(Collectors.groupingBy(rp->rp.iT().match(Id.GX::name, it->null)));
    ArrayList<RP> res = new ArrayList<>(rps);
    for (var rpsi : byX.values()) {
      var ti = rpsi.stream().map(RP::iT1).reduce(this::best).orElseThrow();
      for (RP rpi : rpsi) {
        int index = rpi.pos();
        res.set(index, new RP(index, res.get(index).iT(), ti));
      }
    }
    return Collections.unmodifiableList(res);
  }
  List<RP> refineSigGens2(List<RP> rps){
    return rps.stream().map(this::killInfer).toList();
  }
  RP killInfer(RP rp){
    if(rp.iT().isInfer()){ return new RP(rp.pos(),rp.iT1(),rp.iT1()); }
    if(rp.iT1().isInfer()){ return new RP(rp.pos(),rp.iT(),rp.iT()); }
    return rp;
  }

  List<RP> refineSigGens3(List<RP> rps) {//expand
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
      if (rp.iT().isInfer() || rp.iT1().isInfer()) { return false; }
      if ((rp.iT().rt() instanceof Id.IT<astFull.T> it) && rp.iT1().rt() instanceof Id.IT<astFull.T> it1) {
        var mdfMatch = rp.iT().mdf().equals(rp.iT1().mdf());
        var nameAndGensMatch = it.name().equals(it1.name());
        return mdfMatch && nameAndGensMatch;
      }
      return false;
    }).toList();
    for (RP rp : expanded) {
      var it = rp.iT().itOrThrow();
      var it1 = rp.iT1().itOrThrow();
      rps.addAll(RP.of(rps.size(), it.ts(), it1.ts()));
    }
  }
  List<RP> refineSigGens4(List<RP> rps) {//replace
    throw Bug.todo();
    Map<Id.GX<T>,T>> ts=new ArrayList<>();
    ArrayList<Id.GX<T>> xs=new ArrayList<>();
    for(RP rp:rps){
      rp.iT.match(gx->{xs.add(gx);ts.add();}, it->{});
      rp.iT1.match(gx->{}, it->{});
    }
    fun=renameFunFull(Ts,Xs);
    rps.stream().map(rp->rename(rp,fun)).toList();
    p.rename(T, fun);
    new Program.RenameGens()
  }

  RP rename(RP rp, Function<Id.GX<T>, T> f){
    return new RP(rp.pos(), f.apply(rp.iT()), f.apply(rp.iT()));
//    return it.withTs(it.ts().stream().map(iti->rename(iti,f)).toList());
  }

  Function<Id.GX<T>, T> renameFunFull(List<T> ts, List<Id.GX<T>> gxs) {
    return gx->{
      int i = gxs.indexOf(gx);
      if(i==-1){ return null; }
      return ts.get(i);
    };
  }

  record RP(int pos, astFull.T iT, astFull.T iT1){
    static List<RP> of(int start,List<astFull.T> iTs,List<astFull.T> iTs1){
      int[] i = {start};
      return Streams.zip(iTs,iTs1).map((a,b)->new RP(i[0]++,a,b)).toList();
    }
  }
}
