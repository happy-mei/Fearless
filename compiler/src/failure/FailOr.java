package failure;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public sealed interface FailOr<T>{
  <R> FailOr<R> flatMap(Function<T,FailOr<R>> r);
  <R> FailOr<R> map(Function<T,R> r);
  FailOr<T> peek(Consumer<T> r);
  FailOr<T> or(Supplier<FailOr<T>> r);
  FailOr<T> mapErr(UnaryOperator<Supplier<CompileError>> u);
  default void ifRes(Consumer<T> r) {
    this.<Void>map(t -> {
      r.accept(t);
      return null;
    });
  }
  boolean isRes();
  default boolean isErr() { return !this.isRes(); }
  <R> FailOr<R> cast();
  T get();
  Optional<Supplier<CompileError>> asOpt();

  record Res<T>(T t) implements FailOr<T> {
    public <R> FailOr<R> flatMap(Function<T,FailOr<R>> r) { return r.apply(t); }
    public <R> FailOr<R> map(Function<T,R> r) { return new Res<>(r.apply(t)); }
    public FailOr<T> peek(Consumer<T> r) {
      r.accept(t);
      return this;
    }
    @Override public FailOr<T> or(Supplier<FailOr<T>> r) { return this; }

    public FailOr<T> mapErr(UnaryOperator<Supplier<CompileError>> u) {return this;}
    public boolean isRes(){ return true; }

    @Override public <R> FailOr<R> cast() {
      throw new UnsupportedOperationException("Only failures can be cast");
    }

    public T get(){ return t; }
    static final private Res<Void> ok= new Res<Void>(null);
    public Optional<Supplier<CompileError>> asOpt(){ return Optional.empty(); }
  }
  record Fail<T>(Supplier<CompileError> err) implements FailOr<T>{
    @SuppressWarnings("unchecked")
    private <R> Fail<R> self(){return (Fail<R>) this;}
    public <R> FailOr<R> flatMap(Function<T,FailOr<R>> r) { return self(); }
    public <R> FailOr<R> map(Function<T,R> r) { return self(); }
    public FailOr<T> peek(Consumer<T> r) { return self(); }
    @Override public FailOr<T> or(Supplier<FailOr<T>> r) { return r.get(); }

    public FailOr<T> mapErr(UnaryOperator<Supplier<CompileError>> u){ return new Fail<>(u.apply(err)); }
    public boolean isRes(){ return false; }

    @Override public <R> FailOr<R> cast() {
      return self();
    }

    public T get(){ throw err.get(); }
    public Optional<Supplier<CompileError>> asOpt(){ return Optional.of(err); }
  }
  static <T> Res<T> res(T t){ return new Res<T>(t); }
  static <T> Fail<T> err(Supplier<CompileError> err){ return new Fail<T>(err); }
  static Res<Void> ok(){ return Res.ok; }
  static FailOr<Void> opt(Optional<Supplier<CompileError>> opt){
    //big fail of Java inference here
    return opt.map(s->(FailOr<Void>)FailOr.<Void>err(s)).orElse(ok()); 
  }
  static <A,R> FailOr<List<R>> fold(Iterable<A> as, Function<A,FailOr<R>> f){
    List<R> res= new ArrayList<>();
    for(A a: as) {
      FailOr<R> ar= f.apply(a);
      if(ar instanceof Fail<?> err) { return err.self(); }
      res.add(ar.get());
    }
    return res(res);
  }
}


/*


public interface Res {
  default Res or(Supplier<Res> r) { return resMatch(e->this, i->r.get()); }
  default T orElse(Function<CompileError, T> f) { return resMatch(e->e, f); }
  <R> R resMatch(Function<T, R> ok, Function<CompileError, R> err);
  default T tOrThrow(){return resMatch(e->e,ce->{throw ce;}); }
  default CompileError errorOrThrow(){ return resMatch(e->{throw Bug.unreachable();}, ce->ce); }
  default Optional<T> t(){ return resMatch(Optional::of, i->Optional.empty()); }
  default Optional<CompileError> err(){ return resMatch(i->Optional.empty(),Optional::of); }
}
  public <R> R resMatch(Function<T, R> ok, Function<CompileError, R> err){ return err.apply(this); }
    public <R> R resMatch(Function<T,R> ok, Function<CompileError,R> err){ return ok.apply(this); }
*/