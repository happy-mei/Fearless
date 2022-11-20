package ast;

import java.util.List;
import java.util.Optional;

import parser.Parser;
import visitors.CloneVisitor;
import visitors.CollectorVisitor;


//TODO: discuss: may be MethName and DecId does not need duplication

public interface E {
  E accept(CloneVisitor v);
  <R> Optional<R> accept(CollectorVisitor<R> v);

  record Lambda(Mdf mdf, List<T.IT> its, String selfName, List<Meth> meths) implements E{
    public Lambda{
      assert mdf!=null;
      assert !its.isEmpty();
      assert X.validId(selfName);
      assert meths!=null;
    }
    @Override public E accept(CloneVisitor v){ return v.visitLambda(this); }
    @Override public <R>Optional<R> accept(CollectorVisitor<R> v){return v.visitLambda(this);}
  }
  record MCall(E receiver, MethName name, List<T> ts, List<E> es)implements E{
    public MCall{ assert receiver!=null && name.num()==es.size() && ts!=null; }
    @Override public E accept(CloneVisitor v){return v.visitMCall(this);}
    @Override public <R>Optional<R> accept(CollectorVisitor<R> v){return v.visitMCall(this);}
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
    @Override public <R>Optional<R> accept(CollectorVisitor<R> v){ return v.visitX(this); }
    @Override public String toString(){ return name; }
  }
  record MethName(String name, int num){
    public MethName{ assert validM(name) && num>=0; }
    public static boolean validM(String m){
      assert m!=null && !m.isEmpty();
      return new parser.Parser(Parser.dummy,m).parseM();      
    }
    @Override public String toString(){ return name; }
  }
  record Meth(Sig sig, MethName name, List<String> xs, Optional<E> body){
    public Meth{ assert sig!= null && name.num()==xs.size() && body!=null; }
    @Override public String toString() {
      return String.format("%s(%s): %s -> %s", name, xs, sig, body.map(Object::toString).orElse("[-]"));
    }
  }
  record Sig(Mdf mdf, List<T.GX> gens, List<T> ts, T ret){
    public Sig{ assert mdf!=null && gens!=null && ts!=null && ret!=null; }
  }
}