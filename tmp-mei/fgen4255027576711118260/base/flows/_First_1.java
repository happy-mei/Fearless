package base.flows;
public interface _First_1 extends base.F_3{
_First_1 $self = new _First_1Impl();
base.Opt_1 $hash$read(Object a_m$, Object b_m$);
static base.Opt_1 $hash$read$fun(base.flows.FlowOp_1 upstream_m$, base.flows._SinkDecorator_0 sinkDecorator_m$, base.flows._First_1 $this) {
  return ((base.Opt_1)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.Opt_1) null;
}})
);
}
}