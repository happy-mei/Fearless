package program.typesystem;

import ast.T;
import failure.CompileError;
import failure.Fail;
import id.Id;
import id.Mdf;
import program.Program;

import java.util.Optional;

public interface XTsMap {
  default T get(Id.GX<?> x) {
    return getO(x).orElseThrow();
  }
  default T get(String s) {
    return getO(s).orElseThrow();
  }
  default Optional<T> getO(Id.GX<?> x){ return getO(x.name()); }
  Optional<T> getO(String s);
  static XTsMap empty(){ return x->Optional.empty(); }
  default XTsMap add(String s, T t) {
    return x->x.equals(s) ? Optional.of(t) : this.getO(x);
  }
}
