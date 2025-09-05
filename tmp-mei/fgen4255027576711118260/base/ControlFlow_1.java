package base;
public interface ControlFlow_1{
ControlFlow_1 $self = new ControlFlow_1Impl();
Object match$mut(base.ControlFlowMatch_2 m_m$);
static Object match$mut$fun(base.ControlFlowMatch_2 m_m$, base.ControlFlow_1 $this) {
  return (switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (Object) null;
}})
;
}
}