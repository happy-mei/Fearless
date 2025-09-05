package base;
public interface Error_0{
Error_0 $self = new Error_0Impl();
Object $exclamation$imm(base.Info_0 info_m$);

Object msg$imm(rt.Str msg_m$);
static Object $exclamation$imm$fun(base.Info_0 info_m$, base.Error_0 $this) {
  return (switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (Object) null;
}})
;
}

static Object msg$imm$fun(rt.Str msg_m$, base.Error_0 $this) {
  return (switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (Object) null;
}})
;
}
}