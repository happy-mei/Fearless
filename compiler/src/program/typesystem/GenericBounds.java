package program.typesystem;

import ast.E;
import failure.FailOr;
import program.Program;
import ast.T;
import failure.CompileError;
import failure.Fail;
import id.Id;
import id.Mdf;
import program.CM;
import utils.Bug;
import utils.Streams;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

public interface GenericBounds {
  Set<Mdf> ALL_RCS = Set.of(Mdf.iso, Mdf.imm, Mdf.mut, Mdf.mutH, Mdf.read, Mdf.readH);

  static FailOr<Void> validGenericLambda(Program p, XBs xbs, E.Lambda l) {
    var boundsInference = new BoundsInference(p, xbs, ALL_RCS);
    var res = l.its().stream()
      .map(it->it.accept(boundsInference))
      .filter(FailOr::isErr)
      .<FailOr<Void>>map(FailOr::cast)
      .findFirst();
    return res.orElseGet(FailOr::ok);
  }

  static FailOr<Void> validGenericMeth(Program p, XBs xbs, CM cm, List<T> typeArgs) {
    var gensValid = typeArgs.stream()
      .map(t->validGenericT(p, xbs, t))
      .filter(Optional::isPresent)
      .map(Optional::get)
      .findAny();
    assert gensValid.isEmpty(); // can I remove this check?

    var typeParams = cm.sig().gens();
    if (typeArgs.size() != typeParams.size()) {
      return FailOr.err(()->Fail.genericMismatch(typeArgs, typeParams));
    }
    var res = Streams.zip(typeArgs, typeParams)
      .map((t, gx)->{
        var bounds = cm.bounds().getOrDefault(gx, XBs.defaultBounds);
        var inference = new BoundsInference(p, xbs, bounds);
        return t.accept(inference);
      })
      .filter(FailOr::isErr)
      .<FailOr<Void>>map(FailOr::cast)
      .findFirst();
    return res.orElseGet(FailOr::ok);
  }

  static Optional<Supplier<CompileError>> validGenericIT(Program p, XBs xbs, Id.IT<T> it) {
    var innerInvalid = it.ts().stream()
      .map(t->validGenericT(p, xbs, t))
      .filter(Optional::isPresent)
      .map(Optional::get)
      .findAny();
    if (innerInvalid.isPresent()) { return innerInvalid; }

    var dec = p.of(it.name());
    var gxs = switch (dec) {
      case T.Dec d -> d.gxs();
      case astFull.T.Dec d -> d.gxs();
    };
    var boundMap = switch (dec) {
      case T.Dec d -> d.bounds();
      case astFull.T.Dec d -> d.bounds();
    };
    return Streams.zip(it.ts(), gxs)
      .map((t, gx) -> {
        var bounds = boundMap.get(gx);
        return validGenericMdf(xbs, bounds == null || bounds.isEmpty() ? XBs.defaultBounds : bounds, t);
      })
      .filter(Optional::isPresent)
      .map(Optional::get)
      .findAny();
  }

  static Optional<Supplier<CompileError>> validGenericT(Program p, XBs xbs, T t) {
    return t.match(
      gx->Optional.empty(),
      it->validGenericIT(p, xbs, it)
    );
  }

  static Optional<Supplier<CompileError>> validGenericMdf(XBs xbs, Set<Mdf> bounds, T t) {
    Supplier<Optional<Supplier<CompileError>>> errMsg = ()->Optional.of(()->Fail.invalidMdfBound(t, bounds.stream().sorted().toList()));
    if (!t.mdf().is(Mdf.mdf, Mdf.recMdf, Mdf.readImm)) {
      return bounds.contains(t.mdf()) ? Optional.empty() : errMsg.get();
    }

    if (t.mdf().isReadImm()) {
      return bounds.containsAll(Set.of(Mdf.read, Mdf.imm)) ? Optional.empty() : errMsg.get();
    }

    if (t.mdf().isMdf()) {
      var bs = xbs.get(t.gxOrThrow());
      return bounds.containsAll(bs) ? Optional.empty() : errMsg.get();
    }

    if (t.mdf().isRecMdf()) {
      var isOk = t.match(
        gx->{
          var bs = xbs.get(gx);
          if (bs.contains(Mdf.mut) || bs.contains(Mdf.iso)) {
            return bounds.containsAll(XBs.defaultBounds);
          }
          if (!bs.contains(Mdf.mut) && !bs.contains(Mdf.iso) && bs.contains(Mdf.mutH)) {
            return bounds.containsAll(Set.of(Mdf.imm, Mdf.readH, Mdf.mutH));
          }
          if (!bs.contains(Mdf.mut) && !bs.contains(Mdf.iso) && !bs.contains(Mdf.mutH) && bs.contains(Mdf.readH)) {
            return bounds.containsAll(Set.of(Mdf.imm, Mdf.readH));
          }
          if (bs.size() == 1 && bs.contains(Mdf.imm)) { return bounds.contains(Mdf.imm); }
          throw Bug.unreachable();
        },
        it->bounds.containsAll(XBs.defaultBounds)
      );
      return isOk ? Optional.empty() : errMsg.get();
    }
    throw Bug.unreachable();
  }
}
