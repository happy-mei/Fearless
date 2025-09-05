package base.flows;
public interface Enumerated_1 extends base.Sealed_0{
Enumerated_1 $self = new Enumerated_1Impl();
Long i$imm();

Object e$imm();
static Long i$imm$fun(base.flows.Enumerated_1 $this) {
  return ((long)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (long) 0;
}})
);
}

static Object e$imm$fun(base.flows.Enumerated_1 $this) {
  return (switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (Object) null;
}})
;
}
}