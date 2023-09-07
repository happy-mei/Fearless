package program.typesystem;

import ast.E;
import ast.Program;
import ast.T;
import failure.CompileError;
import failure.Fail;
import id.Id;
import id.Mdf;
import utils.Bug;
import utils.Streams;

import java.util.Optional;
import java.util.Set;

public interface GenericBounds {
  static Optional<CompileError> validGenericLambda(Program p, XBs xbs, E.Lambda l) {
    return l.its().stream()
      .map(it->validGenericIT(p, xbs, it))
      .filter(Optional::isPresent)
      .map(Optional::get)
      .findAny();
  }

  static Optional<CompileError> validGenericMeth(Program p, XBs xbs, E.Meth m) {
    throw Bug.todo();
  }

  static Optional<CompileError> validGenericIT(Program p, XBs xbs, Id.IT<T> it) {
    var innerInvalid = it.ts().stream()
      .map(t->validGenericT(p, xbs, t))
      .filter(Optional::isPresent)
      .map(Optional::get)
      .findAny();
    if (innerInvalid.isPresent()) { return innerInvalid; }

    var dec = p.of(it.name());
    return Streams.zip(it.ts(), dec.gxs())
      .map((t, gx) -> {
        var bounds = dec.bounds().get(gx);
        return validGenericMdf(p, xbs, bounds.isEmpty() ? XBs.defaultBounds : bounds, t);
      })
      .filter(Optional::isPresent)
      .map(Optional::get)
      .findAny();
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
