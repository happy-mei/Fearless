package base.flows;
public record _First_1Impl() implements base.flows._First_1 {
  public base.Opt_1 $hash$read(Object a_m$, Object b_m$) {
  return this.$hash$read$Delegate((base.flows.FlowOp_1) a_m$, (base.flows._SinkDecorator_0) b_m$);
}

public base.Opt_1 $hash$read$Delegate(base.flows.FlowOp_1 upstream_m$, base.flows._SinkDecorator_0 sinkDecorator_m$) {
  return  base.flows._First_1.$hash$read$fun(upstream_m$, sinkDecorator_m$, this);
}


  
}
