package base;
public interface ControlFlow_0{
ControlFlow_0 $self = new ControlFlow_0Impl();
base.ControlFlow_1 continue$imm();

base.ControlFlow_1 return$imm(Object returnValue_m$);

base.ControlFlow_1 breakWith$imm();

base.ControlFlow_1 continueWith$imm();

base.ControlFlow_1 break$imm();
static base.ControlFlow_1 continue$imm$fun(base.ControlFlow_0 $this) {
  return ((base.ControlFlow_1)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.ControlFlow_1) null;
}})
);
}

static base.ControlFlow_1 break$imm$fun(base.ControlFlow_0 $this) {
  return ((base.ControlFlow_1)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.ControlFlow_1) null;
}})
);
}

static base.ControlFlow_1 continueWith$imm$fun(base.ControlFlow_0 $this) {
  return ((base.ControlFlow_1)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.ControlFlow_1) null;
}})
);
}

static base.ControlFlow_1 breakWith$imm$fun(base.ControlFlow_0 $this) {
  return ((base.ControlFlow_1)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.ControlFlow_1) null;
}})
);
}

static base.ControlFlow_1 return$imm$fun(Object returnValue_m$, base.ControlFlow_0 $this) {
  return ((base.ControlFlow_1)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.ControlFlow_1) null;
}})
);
}
}