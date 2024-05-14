package program.typesystem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import ast.E;
import ast.Program;
import ast.T;
import id.Mdf;
import id.Id.IT;
import id.Id.MethName;
import program.CM;
import utils.Range;

record MultiSigBuilder(
    Mdf mdf0, List<T> expectedRes,
    ast.E.Sig s, int size, List<ArrayList<T>> tss,ArrayList<T> rets){
  //TODO: ignoring bounds now, we may need to add them as a field
  static MultiSig multiMethod(CM cm,Mdf mdf0,IT<T> it0, MethName m,List<T> expectedRes){
    var res= new MultiSigBuilder(
      mdf0,expectedRes,
      cm.sig(), cm.sig().ts().size(),
      cm.sig().ts().stream().map(t->new ArrayList<T>()).toList(),
      new ArrayList<T>());
    res.fillIsoHProm();
    res.fillIsoProm();
    res.fillBase();
    res.fillReadHProm();
    res.fillMutHPromRec();
    res.fillMutHPromPar();
    return new MultiSig(
      res.tss.stream().map(a->Collections.unmodifiableList(a)).toList(),
      Collections.unmodifiableList(res.rets));
  }
  boolean filterMdf(Function<Mdf,Mdf> f) {
    Mdf limit= f.apply(s.ts().get(0).mdf());
    return program.Program.isSubType(mdf0, limit);
  }
  boolean filterExpectedRes(Mdf retMdf) {
    return rets.stream().map(T::mdf).anyMatch(expectedMdf->
      program.Program.isSubType(retMdf, expectedMdf));
  }

  boolean filterRes() {
    return false;
  }
  void fillBase(){ fillProm(m->m); }
  void fillIsoProm(){ fillProm(this::mutIsoReadImm); }
  void fillIsoHProm(){ fillProm(this::mutIsoReadImmReadHImm); }  
  void fillReadHProm(){
    fillProm(this::mutIsoReadReadH,this::mutIsoReadReadH,this::toHyg);
    }
  void fillMutHPromRec(){//two version: one for receiver mut <->mutH
    if(!s.ts().get(0).mdf().isMut()){ return; }
    fillProm(this::mutMutH,this::mutIso,this::toHyg);
    }
  void fillMutHPromPar(){//one for parameter mut <->mutH
    int countMut= (int)s.ts().stream()
      .skip(1).filter(t->t.mdf().isMut()).count();
    for(int i:Range.of(0,countMut)){ fillMutHPromPar(i); }
    }
  void fillMutHPromPar(int specialMut){//one for parameter mut <->mutH
    if(!filterMdf(this::mutIso)){ return; }
    assert size == s.ts().size();//suspect is size+1 == s.ts().size()
    var addRet= fixR(s.ret(),this::toHyg);
    if(!filterExpectedRes(addRet.mdf())){ return; }
    rets.add(addRet);
    int count= 0;    
    for(var i : Range.of(0,size)){
      T currT= s.ts().get(i);//wrong use of s?
      var special= currT.mdf().isMut() && i==count;
      if(special){ count++; }
      var addT= fixP(currT,special?this::mutMutH:this::mutIso);
      tss.get(i).add(addT);
    }
  }
  void fillProm(Function<Mdf,Mdf> f){ fillProm(f,f,f); }  
  
  void fillProm(Function<Mdf,Mdf> rec,Function<Mdf,Mdf> p,Function<Mdf,Mdf> r){
    if(!filterMdf(rec)){ return; }
    assert size == s.ts().size();//suspect is size+1 == s.ts().size()
    var addRet= fixR(s.ret(),r);
    if(!filterExpectedRes(addRet.mdf())){ return; }
    rets.add(addRet);
    for(var i : Range.of(0,size)){
      tss.get(i).add(fixP(s.ts().get(i),p));//wrong use of s?
    }
  }
  T fixP(T t,Function<Mdf,Mdf> f){
    if(t.mdf().isMdf()){ return t.withMdf(Mdf.iso); }
    //t.isMdfX() == t.mdf().isMdf()
    //if(t.mdf().isReadImm()) {..}
    return t.withMdf(f.apply(t.mdf()));
    }
  /* TODO:
   with Bounds, take all RC of X, if single bounds, apply bounds and delegate  X == imm X
   If multiple bounds read, imm 
     select the worst and apply transformation?
     P mut,imm,read =>iso because mut is present
     R mut,imm,read =>imm because read is present
     what if iso/readH/mutH in bounds
     1-apply the transformation for all the bounds (include the read imm transformation. DO formalis?
     2-P- select the most specific
     2-R- select the most general
     Do we have a 'most specific/most general MDF function'? -NO
     
     -well formedness?
       read/imm X only well formed if
         X bounds contains imm? may be not needed?
         
         A[X:read,imm]:{}
         B[Y:read]:A[Y]{}
     location of default XBs.defaultBounds
     options for Toplas:
     -well formedness: no iso,readH,mutH to instantiate an X
     -have bounds
   */
  T fixR(T t,Function<Mdf,Mdf> f){
    if(t.mdf().isMdf()){ return t.withMdf(Mdf.imm); }
    return t.withMdf(f.apply(t.mdf()));
    }
  Mdf mutIso(Mdf m){
    assert m.isSyntaxMdf();
    return switch(m){
      case mut->Mdf.iso;
      default ->m;
    };
  }
  Mdf mutMutH(Mdf m){
    assert m.isSyntaxMdf();
    return switch(m){
      case mut->Mdf.lent;
      default ->m;
    };
  }

  Mdf mutIsoReadImm(Mdf m){
    assert m.isSyntaxMdf();
    return switch(m){
      case mut->Mdf.iso;
      case read->Mdf.imm;
      default ->m;
    };
  }
  Mdf mutIsoReadImmReadHImm(Mdf m){
    assert m.isSyntaxMdf();
    return switch(m){
      case mut->Mdf.iso;
      case read,readOnly->Mdf.imm;
      default ->m;
    };
  }
  Mdf mutIsoReadReadH(Mdf m){
    assert m.isSyntaxMdf();
    return switch(m){
      case mut->Mdf.iso;
      case read->Mdf.readOnly;
      default ->m;
    };
  }
  Mdf toHyg(Mdf m){
    return switch(m){
      case mut->Mdf.lent;
      case read,mdf->Mdf.readOnly;//TODO: add readImm when ready
      default ->m;
    };
  }
}