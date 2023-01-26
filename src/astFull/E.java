package astFull;

import files.HasPos;
import files.Pos;
import id.Id;
import id.Id.MethName;
import id.Mdf;
import utils.Bug;
import visitors.FullCloneVisitor;
import visitors.FullVisitor;
import visitors.InjectionVisitor;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public sealed interface E extends HasPos {
  E accept(FullCloneVisitor v);
  <R> R accept(FullVisitor<R> v);
  T t();
  E withPos(Optional<Pos> pos);
  E withT(T t);

  record Lambda(Optional<Mdf> mdf, List<Id.IT<T>>its, String selfName, List<Meth>meths, Optional<Id.IT<T>> it, Optional<Pos> pos) implements E{
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

    @Override public Lambda withT(T t) {
      return new Lambda(mdf, its, selfName, meths, Optional.of(t.itOrThrow()), pos);
    }

    @Override public E accept(FullCloneVisitor v){return v.visitLambda(this);}
    @Override public <R> R accept(FullVisitor<R> v){return v.visitLambda(this);}
    @Override public String toString() {
      var mdf = this.mdf().map(Mdf::toString).orElse("");
      var type = mdf().isEmpty() && it().isEmpty() ? "infer" : it().map(Id.IT::toString).orElse("infer");
      var meths = meths().stream().map(Meth::toString).collect(Collectors.joining(",\n"));
      var selfName = Optional.ofNullable(selfName()).map(sn->"'"+sn).orElse("");
      return String.format("[-%s %s-]%s{%s %s}", mdf, type, its(), selfName, meths);
    }

    public Lambda withSelfName(String selfName) {
      return new Lambda(mdf, its, selfName, meths, it, pos);
    }
    public Lambda withIT(Optional<Id.IT<T>> it) {
      return new Lambda(mdf, its, selfName, meths, it, pos);
    }
    public Lambda withITs(List<Id.IT<T>> its) {
      return new Lambda(mdf, its, selfName, meths, it, pos);
    }
    public Lambda withMdf(Mdf mdf) {
      return new Lambda(Optional.of(mdf), its, selfName, meths, it, pos);
    }
    public Lambda withMeths(List<Meth> meths) {
      return new Lambda(mdf, its, selfName, meths, it, pos);
    }
    public Lambda withPos(Optional<Pos> pos) {
      return new Lambda(mdf, its, selfName, meths, it, pos);
    }
  }
  record MCall(E receiver,MethName name,Optional<List<T>>ts,List<E>es, T t, Optional<Pos> pos) implements E{
    public MCall { assert name.num() == es.size(); }
    public MCall withReceiver(E receiver) {
      return new MCall(receiver, name, ts, es, t, pos);
    }
    public MCall withEs(List<E> es) {
      return new MCall(receiver, name, ts, es, t, pos);
    }
    public MCall withTs(Optional<List<T>> ts) {
      return new MCall(receiver, name, ts, es, t, pos);
    }
    @Override public MCall withT(T t) {
      return new MCall(receiver, name, ts, es, t, pos);
    }
    @Override public MCall withPos(Optional<Pos> pos) {
      return new MCall(receiver, name, ts, es, t, pos);
    }
    @Override public E accept(FullCloneVisitor v){return v.visitMCall(this);}
    @Override public <R> R accept(FullVisitor<R> v){return v.visitMCall(this);}
    @Override public String toString() {
      return String.format("%s %s%s(%s):%s", receiver, name, ts.map(Object::toString).orElse("[-]"), es, t);
    }
  }
  record X(String name, T t, Optional<Pos> pos) implements E{
    private static int FRESH_N = 0;
    public static void reset() {
      if (FRESH_N > 100) { throw Bug.of("FRESH_N is larger than we expected for tests."); }
      FRESH_N = 0;
    }
    public X(T t){
      this("fear" + FRESH_N++ + "$", t, Optional.empty());
      if (FRESH_N == Integer.MAX_VALUE) { throw Bug.of("Maximum fresh identifier size reached"); }
    }
    public X withT(T t) {
      return new X(name, t, pos);
    }
    @Override public E withPos(Optional<Pos> pos) { return new X(name, t, pos); }

    @Override public E accept(FullCloneVisitor v){return v.visitX(this);}
    @Override public <R> R accept(FullVisitor<R> v){return v.visitX(this);}
    @Override public String toString(){ return name+":"+t; }
  }
  record Meth(Optional<Sig> sig,Optional<MethName> name, List<String>xs,Optional<E> body, Optional<Pos> pos) implements HasPos {
    public Meth {
      name.ifPresent(n->{ assert n.num() == xs.size(); });
    }
    public boolean isAbs(){ return body().isEmpty(); }
    public Meth withSig(Sig s) {
      return new Meth(Optional.of(s), name, xs, body, pos);
    }
    public Meth withName(MethName name) {
      return new Meth(sig, Optional.of(name), xs, body, pos);
    }
    public Meth withBody(Optional<E> body) {
      return new Meth(sig, name, xs, body, pos);
    }
    @Override public String toString() {
      return String.format("%s(%s): %s -> %s", name.map(Object::toString).orElse("[-]"), xs, sig.map(Object::toString).orElse("[-]"), body.map(Object::toString).orElse("[-]"));
    }
  }
  record Sig(Mdf mdf, List<Id.GX<T>> gens, List<T> ts, T ret, Optional<Pos> pos){
    public Sig{ assert mdf!=null && gens!=null && ts!=null && ret!=null; }
    public ast.E.Sig accept(InjectionVisitor visitor) {
      return visitor.visitSig(this);
    }
    @Override public String toString() {
      return "Sig[mdf="+mdf+",gens="+gens+",ts="+ts+",ret="+ret+"]";
    }
  }
}

