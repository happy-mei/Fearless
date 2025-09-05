package base.test;
public interface TestResults_0 extends base.Sealed_0{
TestResults_0 $self = new TestResults_0Impl();
base.List_1 results$read();

base.List_1 nestedResults$read();

rt.Str suiteTitle$read();
static rt.Str suiteTitle$read$fun(base.test.TestResults_0 $this) {
  return ((rt.Str)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (rt.Str) null;
}})
);
}

static base.List_1 results$read$fun(base.test.TestResults_0 $this) {
  return ((base.List_1)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.List_1) null;
}})
);
}

static base.List_1 nestedResults$read$fun(base.test.TestResults_0 $this) {
  return ((base.List_1)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.List_1) null;
}})
);
}
}