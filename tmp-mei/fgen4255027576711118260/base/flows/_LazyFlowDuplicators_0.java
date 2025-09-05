package base.flows;
public interface _LazyFlowDuplicators_0{
_LazyFlowDuplicators_0 $self = new _LazyFlowDuplicators_0Impl();
base.flows._LazyFlowDuplicator_1 $hash$imm(base.flows.Flow_1 flow_m$);
static base.flows._LazyFlowDuplicator_1 $hash$imm$fun(base.flows.Flow_1 flow_m$, base.flows._LazyFlowDuplicators_0 $this) {
  return ((base.flows._LazyFlowDuplicator_1)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.flows._LazyFlowDuplicator_1) null;
}})
);
}
}