package program.typesystem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;

import ast.T;
import id.Mdf;
import id.Id.IT;
import id.Id.MethName;
import program.CM;

record MultiSigBuilder(ast.E.Sig s, int size, List<ArrayList<T>> tss,ArrayList<T> rets){
  //TODO: ignoring bounds now, we may need to add them as a field
  static MultiSig of(CM cm,Mdf mdf0,IT<T> it0, MethName m,List<T> ts1){
    var res= new MultiSigBuilder(
      cm.sig(), cm.sig().ts().size(),
      cm.sig().ts().stream().map(t->new ArrayList<T>()).toList(),
      new ArrayList<T>());
    res.fillIsoHProm();
    res.fillIsoProm();
    res.fillBase();
    //res.fillReadHProm();
    //res.fillMutHProm();    
    return new MultiSig(
      res.tss.stream().map(a->Collections.unmodifiableList(a)).toList(),
      Collections.unmodifiableList(res.rets));
  }
  void fillBase(){ fillProm(t->t,t->t); }
  void fillIsoProm(){ fillProm(
    t->fixP(t,this::mutIsoReadImm),
    t->fixR(t,this::mutIsoReadImm));
  }
  void fillIsoHProm(){ fillProm(
      t->fixP(t,this::mutIsoReadImmReadHImm),
      t->fixR(t,this::mutIsoReadImmReadHImm));
    }
  /* TODO: not sure how fixP/fixR should work here.
  void fillReadHProm(){ fillProm(
      t->fixP(t,this::mutIsoReadReadH),
      t->fixR(t,this::toHyg));
    }
    //fillMutHProm still missing
  */
  void fillProm(Function<T,T> p,Function<T,T> r){
    IntStream.range(0, size).forEach(i->p.apply(s.ts().get(i)));
    rets.add(r.apply(s.ret()));    
  }
  
  T fixP(T t,Function<Mdf,Mdf> f){
    if(t.mdf().isMdf()){ return t.withMdf(Mdf.iso); }
    return t.withMdf(f.apply(t.mdf()));
    }
  T fixR(T t,Function<Mdf,Mdf> f){
    if(t.mdf().isMdf()){ return t.withMdf(Mdf.imm); }
    return t.withMdf(f.apply(t.mdf()));
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