package base.flows;
public interface _FlowConverters_0 extends base.Sealed_0{
_FlowConverters_0 $self = new _FlowConverters_0Impl();
base.flows.Flow_1 range$imm(long start_m$);

base.flows.Flow_1 range$imm(long start_m$, long end_m$);
static base.flows.Flow_1 range$imm$fun(long start_m$, long end_m$, base.flows._FlowConverters_0 $this) {
  return ((base.flows.Flow_1)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.flows.Flow_1) null;
}})
);
}

static base.flows.Flow_1 range$imm$fun(long start_m$, base.flows._FlowConverters_0 $this) {
  return ((base.flows.Flow_1)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.flows.Flow_1) null;
}})
);
}
}