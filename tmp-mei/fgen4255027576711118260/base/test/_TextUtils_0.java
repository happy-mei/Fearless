package base.test;
public interface _TextUtils_0{
_TextUtils_0 $self = new _TextUtils_0Impl();
rt.Str status$imm(base.test.TestResultKind_0 kind_m$);

base.flows.Flow_1 results$imm(base.List_1 results_m$);

rt.Str suite$imm(base.test.TestResults_0 suite_m$, long depth_m$);

rt.Str heading$imm(rt.Str title_m$, long depth_m$);
static base.flows.Flow_1 results$imm$fun(base.List_1 results_m$, base.test._TextUtils_0 $this) {
  return ((base.flows.Flow_1)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.flows.Flow_1) null;
}})
);
}

static rt.Str suite$imm$fun(base.test.TestResults_0 suite_m$, long depth_m$, base.test._TextUtils_0 $this) {
  return ((rt.Str)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (rt.Str) null;
}})
);
}

static rt.Str heading$imm$fun(rt.Str title_m$, long depth_m$, base.test._TextUtils_0 $this) {
  return ((rt.Str)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (rt.Str) null;
}})
);
}

static rt.Str status$imm$fun(base.test.TestResultKind_0 kind_m$, base.test._TextUtils_0 $this) {
  return ((rt.Str)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (rt.Str) null;
}})
);
}
}