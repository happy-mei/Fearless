package base.flows;
public interface _SafeSource_0{
_SafeSource_0 $self = new _SafeSource_0Impl();
base.flows.Flow_1 fromList$imm(base.List_1 list_m$);

base.flows.FlowOp_1 fromList$apostrophe$imm(base.List_1 list_m$, long start_m$, long end_m$);
static base.flows.Flow_1 fromList$imm$fun(base.List_1 list_m$, base.flows._SafeSource_0 $this) {
  return ((base.flows.Flow_1)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.flows.Flow_1) null;
}})
);
}

static base.flows.FlowOp_1 fromList$apostrophe$imm$fun(base.List_1 list_m$, long start_m$, long end_m$, base.flows._SafeSource_0 $this) {
  return ((base.flows.FlowOp_1)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.flows.FlowOp_1) null;
}})
);
}
}