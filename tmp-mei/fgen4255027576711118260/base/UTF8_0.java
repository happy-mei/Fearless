package base;
public interface UTF8_0 extends base.Sealed_0{
UTF8_0 $self = new UTF8_0Impl();
base.Action_1 fromBytes$imm(base.List_1 utf8Bytes_m$);
static base.Action_1 fromBytes$imm$fun(base.List_1 utf8Bytes_m$, base.UTF8_0 $this) {
  return ((base.Action_1)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.Action_1) null;
}})
);
}
}