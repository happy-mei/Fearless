package base.flows;
public interface _Fold_2 extends base.F_5{
_Fold_2 $self = new _Fold_2Impl();
Object $hash$read(Object a_m$, Object b_m$, Object c_m$, Object d_m$);
static Object $hash$read$fun(base.flows.FlowOp_1 upstream_m$, Object acc_m$, base.F_3 f_m$, base.flows._SinkDecorator_0 sinkDecorator_m$, base.flows._Fold_2 $this) {
  return (switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (Object) null;
}})
;
}
}