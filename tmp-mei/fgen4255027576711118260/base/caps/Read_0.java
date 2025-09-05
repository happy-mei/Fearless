package base.caps;
public interface Read_0 extends base.caps.FileHandleMode_0{
Read_0 $self = new Read_0Impl();
rt.Str str$read();
static rt.Str str$read$fun(base.caps.Read_0 $this) {
  return ((rt.Str)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (rt.Str) null;
}})
);
}
}