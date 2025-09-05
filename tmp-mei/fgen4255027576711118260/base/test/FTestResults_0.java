package base.test;
public interface FTestResults_0 extends base.F_4{
FTestResults_0 $self = new FTestResults_0Impl();
base.test.TestResults_0 $hash$read(Object a_m$, Object b_m$, Object c_m$);
static base.test.TestResults_0 $hash$read$fun(rt.Str suiteTitle_m$, base.List_1 results_m$, base.List_1 nested_m$, base.test.FTestResults_0 $this) {
  return ((base.test.TestResults_0)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.test.TestResults_0) null;
}})
);
}
}