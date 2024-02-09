package magic;

import ast.T;
import codegen.MIR;
import id.Id;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface MagicTrait<E,R> {
  Id.IT<T> name();
  R instantiate();
  Optional<R> call(Id.MethName m, List<E> args, Map<E, T> gamma, EnumSet<MIR.MCall.CallVariant> variants);
}
