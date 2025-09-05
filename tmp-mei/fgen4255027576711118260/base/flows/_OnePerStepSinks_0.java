package base.flows;
public interface _OnePerStepSinks_0 extends base.flows._SinkDecorator_0{
_OnePerStepSinks_0 $self = new _OnePerStepSinks_0Impl();
base.flows._Sink_1 $hash$imm(base.flows._Sink_1 sink_m$);
static base.flows._Sink_1 $hash$imm$fun(base.flows._Sink_1 sink_m$, base.flows._OnePerStepSinks_0 $this) {
  return ((base.flows._Sink_1)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.flows._Sink_1) null;
}})
);
}
}