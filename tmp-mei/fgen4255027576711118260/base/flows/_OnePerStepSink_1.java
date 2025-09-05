package base.flows;
public interface _OnePerStepSink_1 extends base.Sealed_0,base.flows._Sink_1{
_OnePerStepSink_1 $self = new _OnePerStepSink_1Impl();
base.Void_0 stopDown$mut();

base.Bool_0 isEmpty$read();

base.Void_0 pushError$mut(base.Info_0 info_m$);

base.Void_0 stepOnce$mut();

base.Void_0 $hash$mut(Object e_m$);
static base.Void_0 $hash$mut$fun(Object e_m$, base.flows._OnePerStepSink_1 $this) {
  return ((base.Void_0)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.Void_0) null;
}})
);
}

static base.Void_0 stopDown$mut$fun(base.flows._OnePerStepSink_1 $this) {
  return ((base.Void_0)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.Void_0) null;
}})
);
}

static base.Void_0 pushError$mut$fun(base.Info_0 info_m$, base.flows._OnePerStepSink_1 $this) {
  return ((base.Void_0)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.Void_0) null;
}})
);
}

static base.Bool_0 isEmpty$read$fun(base.flows._OnePerStepSink_1 $this) {
  return ((base.Bool_0)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.Bool_0) null;
}})
);
}

static base.Void_0 stepOnce$mut$fun(base.flows._OnePerStepSink_1 $this) {
  return ((base.Void_0)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.Void_0) null;
}})
);
}
}