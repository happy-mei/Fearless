package base;
public interface LList_0{
LList_0 $self = new LList_0Impl();
base.LList_1 $hash$imm();
static base.LList_1 $hash$imm$fun(base.LList_0 $this) {
  return ((base.LList_1)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.LList_1) null;
}})
);
}
}