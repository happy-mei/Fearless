package magic;

import ast.T;
import codegen.MIR;
import id.Id;

import java.util.List;
import java.util.Map;

public interface MagicTrait<R> {
  Id.IT<T> name();
  MIR.Lambda instance();
  R instantiate();
  R call(Id.MethName m, List<MIR> args, Map<MIR, T> gamma);
}
