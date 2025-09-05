package base.flows;
public interface FlowOp_1{
base.Bool_0 isFinite$mut();

base.Void_0 step$mut(base.flows._Sink_1 sink_m$);

base.Void_0 stopUp$mut();

base.Bool_0 isRunning$mut();

base.Opt_1 split$mut();

base.Bool_0 canSplit$read();

base.Void_0 for$mut(base.flows._Sink_1 downstream_m$);
static base.Void_0 for$mut$fun(base.flows._Sink_1 downstream_m$, base.flows.FlowOp_1 $this) {
  return ((base.Void_0)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.Void_0) null;
}})
);
}

static base.Bool_0 isFinite$mut$fun(base.flows.FlowOp_1 $this) {
  return ((base.Bool_0)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.Bool_0) null;
}})
);
}

static base.Opt_1 split$mut$fun(base.flows.FlowOp_1 $this) {
  return ((base.Opt_1)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.Opt_1) null;
}})
);
}

static base.Bool_0 canSplit$read$fun(base.flows.FlowOp_1 $this) {
  return ((base.Bool_0)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.Bool_0) null;
}})
);
}
}