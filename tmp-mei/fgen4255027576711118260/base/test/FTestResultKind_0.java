package base.test;
public interface FTestResultKind_0{
FTestResultKind_0 $self = new FTestResultKind_0Impl();
base.test.TestResultKind_0 passed$imm();

base.test.TestResultKind_0 failed$imm(rt.Str details_m$);

base.test.TestResultKind_0 skipped$imm();

base.test.TestResultKind_0 errored$imm(rt.Str details_m$);
static base.test.TestResultKind_0 passed$imm$fun(base.test.FTestResultKind_0 $this) {
  return ((base.test.TestResultKind_0)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.test.TestResultKind_0) null;
}})
);
}

static base.test.TestResultKind_0 skipped$imm$fun(base.test.FTestResultKind_0 $this) {
  return ((base.test.TestResultKind_0)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.test.TestResultKind_0) null;
}})
);
}

static base.test.TestResultKind_0 failed$imm$fun(rt.Str details_m$, base.test.FTestResultKind_0 $this) {
  return ((base.test.TestResultKind_0)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.test.TestResultKind_0) null;
}})
);
}

static base.test.TestResultKind_0 errored$imm$fun(rt.Str details_m$, base.test.FTestResultKind_0 $this) {
  return ((base.test.TestResultKind_0)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.test.TestResultKind_0) null;
}})
);
}
}