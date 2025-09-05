package base;
public interface ControlFlowContinue_0 extends base.ControlFlow_1,base.Sealed_0{
ControlFlowContinue_0 $self = new ControlFlowContinue_0Impl();
Object match$mut(base.ControlFlowMatch_2 m_m$);
static Object match$mut$fun(base.ControlFlowMatch_2 m_m$, base.ControlFlowContinue_0 $this) {
  return (switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (Object) null;
}})
;
}
}