package ast;

import id.Id;
import id.Id.MethName;
import id.Mdf;
import parser.Parser;
import visitors.CloneVisitor;
import visitors.Visitor;

import java.util.List;
import java.util.Optional;

public interface E {
  E accept(CloneVisitor v);
  <R>  R accept(Visitor<R> v);

  record Lambda(Mdf mdf, List<Id.IT<T>> its, String selfName, List<Meth> meths) implements E{
    public Lambda{
      assert mdf!=null;
      assert !its.isEmpty();
      assert X.validId(selfName);
      assert meths!=null;
    }
    @Override public E accept(CloneVisitor v){ return v.visitLambda(this); }
    @Override public <R> R accept(Visitor<R> v){return v.visitLambda(this);}
  }
  record MCall(E receiver, MethName name, List<T> ts, List<E> es)implements E{
    public MCall{ assert receiver!=null && name.num()==es.size() && ts!=null; }
    @Override public E accept(CloneVisitor v){return v.visitMCall(this);}
    @Override public <R> R accept(Visitor<R> v){return v.visitMCall(this);}
    @Override public String toString() {
      return String.format("%s %s%s(%s)", receiver, name, ts, es);
    }
  }
  record X(String name) implements E{
    public X{ assert validId(name); }
    
    public static boolean validId(String x){
      assert x!=null && !x.isEmpty();
      return new parser.Parser(Parser.dummy,x).parseX();      
    }
    @Override public E accept(CloneVisitor v){ return v.visitX(this); }
    @Override public <R> R accept(Visitor<R> v){ return v.visitX(this); }
    @Override public String toString(){ return name; }
  }
  record Meth(Sig sig, MethName name, List<String> xs, Optional<E> body){
    public Meth{ //noinspection OptionalAssignedToNull
      assert sig!= null && name.num()==xs.size() && body!=null; }
    public boolean isAbs(){ return body().isEmpty(); }
    @Override public String toString() {
      return String.format("%s(%s): %s -> %s", name, xs, sig, body.map(Object::toString).orElse("[-]"));
    }
  }
  record Sig(Mdf mdf, List<Id.GX<T>> gens, List<T> ts, T ret){
    public Sig{ assert mdf!=null && gens!=null && ts!=null && ret!=null; }
  }
}