package main.java;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import ast.T;
import ast.E.Lambda;
import ast.E.MCall;
import ast.E.X;
import ast.E.Lambda.LambdaId;
import ast.T.Dec;
import id.Id;
import id.Mdf;
import id.Id.DecId;
import id.Id.GX;

class DecTypeInfo implements visitors.Visitor<Void>{
  StringBuilder res= new StringBuilder();
  List<Lambda> nested= new ArrayList<>();
  String get() { 
    var tmp= res.toString();
    res.setLength(0);
    return tmp;
    }
  void c(Object s){ res.append(s); }
  
  <E> void seq(List<E> es,Consumer<E> f,String sep){
    boolean first= true;
    for(var e:es) {
      if(first){ first = false; }
      else {c(sep);}
      f.accept(e);
    }
  }
  String bounds(GX<T> gx, Map<Id.GX<T>, Set<Mdf>> map){
    var bounds= map.get(gx);
    if(bounds==null){ return ""; }
    return ":"+bounds.stream()
      .map(m->m.toString())
      .collect(Collectors.joining(", "));
  }
  public void stringLambda(Lambda l) {
    LambdaId id= l.name();
    c(id.id().shortName());
    c("[");
    seq(id.gens(),gx->c(gx+bounds(gx,id.bounds())),", ");
    c("]:");
    seq(l.its(),it->{it.match(
      gx->{c(gx); return null;},
      t->{
        stringName(t.name());
        c("[");
        seq(t.ts(),this::stringType,", ");
        c("]");
        return null;
        });},",");
    c("{\n");
    l.meths().stream().forEach(this::stringMeth);
    c("}\n");
  }
  public void stringName(DecId decId) {
    c(decId.pkg());
    c(".");
    c(decId.shortName());
  }
  public void stringType(ast.T t) {
    c(t.mdf());
    c(" ");
    t.match(
      gx->{c(gx.name());return null;},
      it->{
        stringName(it.name());
        c("[");
        seq(it.ts(),this::stringType,", ");
        c("]");
        return null;
      });
  }
  public void stringMeth(ast.E.Meth m) {
    var s=m.sig();
    c("  ");
    c(s.mdf());
    //c(m.name().mdf().orElse(Mdf.imm));//TODO: refactor it away
    c(" ");
    c(m.name().name());
    c("(");
    assert m.xs().size()==s.ts().size();
    seq(IntStream.range(0,m.xs().size()).boxed().toList(),i->{
      c(m.xs().get(i));
      c(":");
      stringType(s.ts().get(i));
      },", ");
    c("):");
    stringType(s.ret());
    if(m.isAbs()) {c("\n");return;}
    c("->xxx\n");
  }
  public String stringLambdaGet(Lambda l) {
    stringLambda(l);
    return get();
  }
  public String visitDec(Dec dec) {
    LambdaId id= dec.lambda().name();
    assert id.bounds().equals(dec.bounds());
    assert id.id().equals(dec.name());
    assert id.gens().equals(dec.gxs());
    String main= stringLambdaGet(dec.lambda());
    return main+nested.stream()
      .map(this::stringLambdaGet)
      .collect(Collectors.joining("\n"));
  }

  @Override public Void visitMCall(MCall e) {
    e.receiver().accept(this);
    e.es().stream().forEach(ei->ei.accept(this));
    return null;
  }

  @Override public Void visitX(X e) {
    return null;
  }

  @Override public Void visitLambda(Lambda e) {
    nested.add(e);
    return null;
  }
  
}