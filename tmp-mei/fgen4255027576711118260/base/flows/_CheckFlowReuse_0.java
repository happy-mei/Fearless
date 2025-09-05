package base.flows;
public interface _CheckFlowReuse_0{
_CheckFlowReuse_0 $self = new _CheckFlowReuse_0Impl();
base.Void_0 $hash$imm(base.Var_1 isTail_m$);
static base.Void_0 $hash$imm$fun(base.Var_1 isTail_m$, base.flows._CheckFlowReuse_0 $this) {
  return ((base.Void_0)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.Void_0) null;
}})
);
}
}