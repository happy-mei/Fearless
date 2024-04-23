package program.inference;

import ast.Program;
import id.Id;
import magic.MagicImpls;
import program.typesystem.XBs;

import java.util.Optional;

sealed interface BestITStrategy {
  astFull.T of(ast.T t1C, ast.T t2C, astFull.T iT1, astFull.T iT2);

  record MostSpecific(Program p) implements BestITStrategy {
    @Override public astFull.T of(ast.T t1C, ast.T t2C, astFull.T iT1, astFull.T iT2) {
      if (p.isSubType(XBs.empty(), t1C, t2C)) { return iT1; }
      if (p.isSubType(XBs.empty(), t2C, t1C)) { return iT2; }
      return iT1;
    }
  }
  record MostGeneral(Program p) implements BestITStrategy {
    @Override public astFull.T of(ast.T t1C, ast.T t2C, astFull.T iT1, astFull.T iT2) {
      if (p.isSubType(XBs.empty(), t1C, t2C)) { return iT2; }
      return iT1;
    }
  }

  /** Infer non-literals over literals */
  interface LiteralHack {
    static Optional<astFull.T> of(Program p, ast.T t1C, ast.T t2C, astFull.T iT1, astFull.T iT2) {
      if (!t1C.isIt() || !t2C.isIt()) { return Optional.empty(); }

      var lit1 = MagicImpls.getLiteral(p, t1C.name());
      var lit2 = MagicImpls.getLiteral(p, it2.name());
      assert lit1.isPresent() || lit2.isPresent();
      if (lit1.isPresent()) { return Optional.of(iT2); }
      return Optional.of(iT1);
    }
  }
}
