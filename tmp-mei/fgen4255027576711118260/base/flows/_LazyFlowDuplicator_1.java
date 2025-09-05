package base.flows;
public interface _LazyFlowDuplicator_1 extends base.MF_1,base.Sealed_0{
_LazyFlowDuplicator_1 $self = new _LazyFlowDuplicator_1Impl();
base.Opt_1 collected$mut();

base.flows.Flow_1 $hash$mut();
static base.flows.Flow_1 $hash$mut$fun(base.flows._LazyFlowDuplicator_1 $this) {
  return ((base.flows.Flow_1)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.flows.Flow_1) null;
}})
);
}

static base.Opt_1 collected$mut$fun(base.flows._LazyFlowDuplicator_1 $this) {
  return ((base.Opt_1)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.Opt_1) null;
}})
);
}
}