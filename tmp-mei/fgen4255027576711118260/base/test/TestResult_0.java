package base.test;
public interface TestResult_0{
rt.Str title$read();

base.test.TestResultKind_0 kind$read();

base.Opt_1 info$read();
static base.Opt_1 info$read$fun(base.test.TestResult_0 $this) {
  return ((base.Opt_1)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.Opt_1) null;
}})
);
}
}