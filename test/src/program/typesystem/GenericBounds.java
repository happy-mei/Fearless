package program.typesystem;

import ast.E;
import ast.Program;
import ast.T;
import failure.CompileError;
import failure.Fail;
import id.Id;
import id.Mdf;
import utils.Bug;

import java.util.Optional;
import java.util.Set;

public interface GenericBounds {
  static Optional<CompileError> validGenericLambda(Program p, XBs xbs, E.Lambda l) {
    throw Bug.todo();
  }

  static Optional<CompileError> validGenericMeth(Program p, XBs xbs, E.Meth m) {
    throw Bug.todo();
  }

  static Optional<CompileError> validGenericIT(Program p, XBs xbs, Id.IT<T> it) {
    throw Bug.todo();
  }

  static Optional<CompileError> validGenericT(Program p, XBs xbs, T t) {
    return t.match(
      gx->Optional.empty(),
      it->validGenericIT(p, xbs, it)
    );
  }

  static Optional<CompileError> validGenericMdf(Program p, XBs xbs, Set<Mdf> bounds, T t) {
    if (t.mdf().isMdf()) {
      var bs = xbs.get(t.gxOrThrow());
      return bounds.containsAll(bs) ? Optional.empty() : Optional.of(Fail.invalidMdfBound(t, bounds.stream().sorted().toList()));
    }
    return bounds.contains(t.mdf()) ? Optional.empty() : Optional.of(Fail.invalidMdfBound(t, bounds.stream().sorted().toList()));
  }
}
