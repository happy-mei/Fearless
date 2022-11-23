package astFull;

import id.Id;
import id.Id.MethName;
import id.Mdf;
import visitors.FullCloneVisitor;
import visitors.FullCollectorVisitor;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public interface E {
  E accept(FullCloneVisitor v);
  <R> Optional<R> accept(FullCollectorVisitor<R> v);
  T t();
  record Lambda(Optional<Mdf> mdf, List<Id.IT<T>>its, String selfName, List<Meth>meths, Optional<Id.IT<T>> it) implements E{
    public Lambda {
      Objects.requireNonNull(mdf);
      Objects.requireNonNull(meths);
      Objects.requireNonNull(it);
    }
    @Override public T t() {
      if (mdf().isEmpty() && it().isEmpty()) { return T.infer; }
      assert mdf().isPresent() && it().isPresent();
      return new T(mdf().get(), it().get());
    }
    @Override public E accept(FullCloneVisitor v){return v.visitLambda(this);}
    @Override public <R>Optional<R> accept(FullCollectorVisitor<R> v){return v.visitLambda(this);}
    @Override public String toString() {
      var mdf = this.mdf().map(Mdf::toString).orElse("");
      var type = mdf().isEmpty() && it().isEmpty() ? "infer" : it.map(Id.IT::toString).orElse("infer");
      var meths = meths().stream().map(Meth::toString).collect(Collectors.joining(",\n"));

      return String.format("[-%s %s-]%s{[%s] %s}", mdf, type, its(), selfName(), meths);
    }
  }
  record MCall(E receiver,MethName name,Optional<List<T>>ts,List<E>es, T t)implements E{
    public MCall { assert name.num() == es.size(); }
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
    public Meth {
      name.ifPresent(n->{ assert n.num() == xs.size(); });
    }
    @Override public String toString() {
      return String.format("%s(%s): %s -> %s", name.map(Object::toString).orElse("[-]"), xs, sig.map(Object::toString).orElse("[-]"), body.map(Object::toString).orElse("[-]"));
    }
  }
  record Sig(Mdf mdf, List<Id.GX<T>> gens, List<T> ts, T ret){}
}

