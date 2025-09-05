package base.flows;
public interface _Limit_0{
_Limit_0 $self = new _Limit_0Impl();
base.flows.FlowOp_1 $hash$imm(base.flows._SinkDecorator_0 sinkDecorator_m$, base.flows.FlowOp_1 upstream_m$, long n_m$);
static base.flows.FlowOp_1 $hash$imm$fun(base.flows._SinkDecorator_0 sinkDecorator_m$, base.flows.FlowOp_1 upstream_m$, long n_m$, base.flows._Limit_0 $this) {
  return ((base.flows.FlowOp_1)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.flows.FlowOp_1) null;
}})
);
}
}