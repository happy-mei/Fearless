package base;
public interface As_1 extends base.Sealed_0{
As_1 $self = new As_1Impl();
Object $hash$imm(Object x_m$);
static Object $hash$imm$fun(Object x_m$, base.As_1 $this) {
  return (switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (Object) null;
}})
;
}
}