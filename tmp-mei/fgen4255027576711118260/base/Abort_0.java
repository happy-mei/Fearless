package base;
public interface Abort_0 extends base.Sealed_0{
Abort_0 $self = new Abort_0Impl();
Object $exclamation$imm();
static Object $exclamation$imm$fun(base.Abort_0 $this) {
  return (switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (Object) null;
}})
;
}
}