package base.flows;
public interface _DataParallelInvalidStateful_0{
_DataParallelInvalidStateful_0 $self = new _DataParallelInvalidStateful_0Impl();
Object $exclamation$imm();
static Object $exclamation$imm$fun(base.flows._DataParallelInvalidStateful_0 $this) {
  return (switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (Object) null;
}})
;
}
}