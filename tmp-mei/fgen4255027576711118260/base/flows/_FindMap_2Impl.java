package base.flows;
public record _FindMap_2Impl() implements base.flows._FindMap_2 {
  public base.Opt_1 $hash$read(Object a_m$, Object b_m$, Object c_m$) {
  return this.$hash$read$Delegate((base.flows.FlowOp_1) a_m$, (base.F_2) b_m$, (base.flows._SinkDecorator_0) c_m$);
}

public base.Opt_1 $hash$read$Delegate(base.flows.FlowOp_1 upstream_m$, base.F_2 f_m$, base.flows._SinkDecorator_0 sinkDecorator_m$) {
  return  base.flows._FindMap_2.$hash$read$fun(upstream_m$, f_m$, sinkDecorator_m$, this);
}


  
}
