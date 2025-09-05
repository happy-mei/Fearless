package base;
public interface Unreachable_0{
Unreachable_0 $self = new Unreachable_0Impl();
Object $exclamation$imm();

Object $exclamation$imm(rt.Str msg_m$);
static Object $exclamation$imm$fun(base.Unreachable_0 $this) {
  return (switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (Object) null;
}})
;
}

static Object $exclamation$imm$fun(rt.Str msg_m$, base.Unreachable_0 $this) {
  return (switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (Object) null;
}})
;
}
}