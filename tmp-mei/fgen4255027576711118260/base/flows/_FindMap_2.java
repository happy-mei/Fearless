package base.flows;
public interface _FindMap_2 extends base.F_4{
_FindMap_2 $self = new _FindMap_2Impl();
base.Opt_1 $hash$read(Object a_m$, Object b_m$, Object c_m$);
static base.Opt_1 $hash$read$fun(base.flows.FlowOp_1 upstream_m$, base.F_2 f_m$, base.flows._SinkDecorator_0 sinkDecorator_m$, base.flows._FindMap_2 $this) {
  return ((base.Opt_1)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.Opt_1) null;
}})
);
}
}