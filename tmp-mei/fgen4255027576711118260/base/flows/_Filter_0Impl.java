package base.flows;
public record _Filter_0Impl() implements base.flows._Filter_0 {
  public base.flows.FlowOp_1 $hash$imm(base.flows._SinkDecorator_0 sinkDecorator_m$, base.flows.FlowOp_1 upstream_m$, base.F_2 predicate_m$) {
  return  base.flows._Filter_0.$hash$imm$fun(sinkDecorator_m$, upstream_m$, predicate_m$, this);
}

public base.flows._Sink_1 impl$imm(base.flows._Sink_1 downstream_m$, base.F_2 predicate_m$, base.flows._SinkDecorator_0 sinkDecorator_m$) {
  return  base.flows._Filter_0.impl$imm$fun(downstream_m$, predicate_m$, sinkDecorator_m$, this);
}

  
}
