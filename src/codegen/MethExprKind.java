package codegen;

import utils.Streams;

import java.util.stream.Stream;

public sealed interface MethExprKind {
  Kind kind();
  enum Kind implements MethExprKind {
    RealExpr, Delegate, Unreachable, Delegator;
    @Override public Kind kind() { return this; }
  }
  record Delegator(MIR.Sig original, MIR.Sig delegate) implements MethExprKind {
    public Stream<MIR.X> xs() {
      return Streams.zip(delegate.xs(), original.xs()).map((o, d)->new MIR.X(o.name(), d.t()));
    }
    @Override public Kind kind() {
      return Kind.Delegator;
    }
  }
}
