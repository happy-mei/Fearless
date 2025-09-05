package base;
public interface Debug_0 extends base.Sealed_0{
Debug_0 $self = new Debug_0Impl();
base.Void_0 println$imm(Object x_m$);

rt.Str identify$imm(Object x_m$);

Object $hash$imm(Object x_m$);
static Object $hash$imm$fun(Object x_m$, base.Debug_0 $this) {
  return (switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (Object) null;
}})
;
}

static base.Void_0 println$imm$fun(Object x_m$, base.Debug_0 $this) {
  return ((base.Void_0)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.Void_0) null;
}})
);
}

static rt.Str identify$imm$fun(Object x_m$, base.Debug_0 $this) {
  return ((rt.Str)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (rt.Str) null;
}})
);
}
}