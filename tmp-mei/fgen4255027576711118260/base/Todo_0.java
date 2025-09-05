package base;
public interface Todo_0{
Todo_0 $self = new Todo_0Impl();
Object $exclamation$imm();

Object $exclamation$imm(rt.Str msg_m$);
static Object $exclamation$imm$fun(base.Todo_0 $this) {
  return (switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (Object) null;
}})
;
}

static Object $exclamation$imm$fun(rt.Str msg_m$, base.Todo_0 $this) {
  return (switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (Object) null;
}})
;
}
}