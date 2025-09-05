package base.flows;
public interface _PipelineParallelSink_0 extends base.flows._SinkDecorator_0{
_PipelineParallelSink_0 $self = new _PipelineParallelSink_0Impl();
base.flows._Sink_1 $hash$imm(base.flows._Sink_1 s_m$);
static base.flows._Sink_1 $hash$imm$fun(base.flows._Sink_1 s_m$, base.flows._PipelineParallelSink_0 $this) {
  return ((base.flows._Sink_1)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.flows._Sink_1) null;
}})
);
}
}