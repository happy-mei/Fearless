package astFull;

import ast.Mdf;
import id.Id.MethName;
import visitors.FullCloneVisitor;
import visitors.FullCollectorVisitor;

import java.util.List;
import java.util.Optional;

public interface E {
  E accept(FullCloneVisitor v);
  <R> Optional<R> accept(FullCollectorVisitor<R> v);
  T t();
  record Lambda(Mdf mdf, List<T.IT>its, String selfName, List<Meth>meths, T t) implements E{
    @Override public E accept(FullCloneVisitor v){return v.visitLambda(this);}
    @Override public <R>Optional<R> accept(FullCollectorVisitor<R> v){return v.visitLambda(this);}
  }
  record MCall(E receiver,MethName name,Optional<List<T>>ts,List<E>es, T t)implements E{
    @Override public E accept(FullCloneVisitor v){return v.visitMCall(this);}
    @Override public <R>Optional<R> accept(FullCollectorVisitor<R> v){return v.visitMCall(this);}
    @Override public String toString() {
      return String.format("%s %s%s(%s):%s", receiver, name, ts.map(Object::toString).orElse("[-]"), es, t);
    }
  }
  record X(String name, T t) implements E{
    private static int FRESH_N = 0;
    public static void reset() { FRESH_N = 0; }
    public X(T t){
      this("fear" + FRESH_N++ + "$", t);
    }
    @Override public E accept(FullCloneVisitor v){return v.visitX(this);}
    @Override public <R>Optional<R> accept(FullCollectorVisitor<R> v){return v.visitX(this);}
    @Override public String toString(){ return name+":"+t; }
  }
  record Meth(Optional<Sig> sig,Optional<MethName> name, List<String>xs,Optional<E> body){
    @Override public String toString() {
      return String.format("%s(%s): %s -> %s", name.map(Object::toString).orElse("[-]"), xs, sig.map(Object::toString).orElse("[-]"), body.map(Object::toString).orElse("[-]"));
    }
  }
  record Sig(Mdf mdf, List<T.GX> gens, List<T> ts, T ret){}
}

