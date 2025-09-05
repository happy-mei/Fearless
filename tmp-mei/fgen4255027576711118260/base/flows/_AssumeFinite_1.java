package base.flows;
public interface _AssumeFinite_1 extends base.F_2{
_AssumeFinite_1 $self = new _AssumeFinite_1Impl();
base.flows.FlowOp_1 $hash$read(Object a_m$);
static base.flows.FlowOp_1 $hash$read$fun(base.flows.FlowOp_1 upstream_m$, base.flows._AssumeFinite_1 $this) {
  return ((base.flows.FlowOp_1)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.flows.FlowOp_1) null;
}})
);
}
}