package program.inference;

import ast.Program;
import astFull.T;
import program.typesystem.XBs;

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

  record MostUserWritten() implements BestITStrategy {
    public static final MostUserWritten $self = new MostUserWritten();
    @Override public T of(ast.T t1C, ast.T t2C, T iT1, T iT2) {
      return iT1;
    }
  }
}
