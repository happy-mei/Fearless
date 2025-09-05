package base;
public interface Let_0{
Let_0 $self = new Let_0Impl();
Object $hash$imm(base.Let_2 l_m$);
static Object $hash$imm$fun(base.Let_2 l_m$, base.Let_0 $this) {
  return (switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (Object) null;
}})
;
}
}