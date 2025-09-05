package base.flows;
public record _Fold_2Impl() implements base.flows._Fold_2 {
  public Object $hash$read(Object a_m$, Object b_m$, Object c_m$, Object d_m$) {
  return this.$hash$read$Delegate((base.flows.FlowOp_1) a_m$, (Object) b_m$, (base.F_3) c_m$, (base.flows._SinkDecorator_0) d_m$);
}

public Object $hash$read$Delegate(base.flows.FlowOp_1 upstream_m$, Object acc_m$, base.F_3 f_m$, base.flows._SinkDecorator_0 sinkDecorator_m$) {
  return  base.flows._Fold_2.$hash$read$fun(upstream_m$, acc_m$, f_m$, sinkDecorator_m$, this);
}


  
}
