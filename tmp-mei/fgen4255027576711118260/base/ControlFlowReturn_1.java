package base;
public interface ControlFlowReturn_1 extends base.ControlFlow_1,base.Sealed_0{
ControlFlowReturn_1 $self = new ControlFlowReturn_1Impl();
Object match$mut(base.ControlFlowMatch_2 m_m$);

Object value$mut();
static Object match$mut$fun(base.ControlFlowMatch_2 m_m$, base.ControlFlowReturn_1 $this) {
  return (switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (Object) null;
}})
;
}

static Object value$mut$fun(base.ControlFlowReturn_1 $this) {
  return (switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (Object) null;
}})
;
}
}