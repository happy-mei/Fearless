package base.flows;
public interface _PipelineParallelFlow_0 extends base.flows._FlowFactory_0{
_PipelineParallelFlow_0 $self = new _PipelineParallelFlow_0Impl();
base.flows.Flow_1 fromOp$imm(base.flows.FlowOp_1 source_m$, base.Opt_1 size_m$);

base.flows.Flow_1 $hash$imm(Object e_m$);
static base.flows.Flow_1 fromOp$imm$fun(base.flows.FlowOp_1 source_m$, base.Opt_1 size_m$, base.flows._PipelineParallelFlow_0 $this) {
  return ((base.flows.Flow_1)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.flows.Flow_1) null;
}})
);
}

static base.flows.Flow_1 $hash$imm$fun(Object e_m$, base.flows._PipelineParallelFlow_0 $this) {
  return ((base.flows.Flow_1)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.flows.Flow_1) null;
}})
);
}
}