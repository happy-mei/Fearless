package base.test;
public interface FTestRunner_0 extends base.F_2{
FTestRunner_0 $self = new FTestRunner_0Impl();
base.test.TestRunner_0 $hash$read(Object a_m$);
static base.test.TestRunner_0 $hash$read$fun(base.caps.System_0 sys_m$, base.test.FTestRunner_0 $this) {
  return ((base.test.TestRunner_0)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.test.TestRunner_0) null;
}})
);
}
}