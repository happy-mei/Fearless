package base;
public interface _DecidedBlock_0{
_DecidedBlock_0 $self = new _DecidedBlock_0Impl();
base.Block_1 $hash$imm(Object res_m$);
static base.Block_1 $hash$imm$fun(Object res_m$, base._DecidedBlock_0 $this) {
  return ((base.Block_1)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.Block_1) null;
}})
);
}
}