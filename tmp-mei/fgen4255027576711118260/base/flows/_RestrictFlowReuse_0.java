package base.flows;
public interface _RestrictFlowReuse_0{
_RestrictFlowReuse_0 $self = new _RestrictFlowReuse_0Impl();
base.flows.Flow_1 $hash$imm(base.flows.Flow_1 flow_m$);
static base.flows.Flow_1 $hash$imm$fun(base.flows.Flow_1 flow_m$, base.flows._RestrictFlowReuse_0 $this) {
  return ((base.flows.Flow_1)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.flows.Flow_1) null;
}})
);
}
}