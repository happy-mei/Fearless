package base.caps;
public interface Create_0 extends base.caps.FileHandleMode_0{
Create_0 $self = new Create_0Impl();
rt.Str str$read();
static rt.Str str$read$fun(base.caps.Create_0 $this) {
  return ((rt.Str)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (rt.Str) null;
}})
);
}
}