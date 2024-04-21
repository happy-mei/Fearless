package failure;
import ast.T;
import utils.Bug;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
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
*/