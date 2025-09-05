package base.flows;
public record _Map_0Impl() implements base.flows._Map_0 {
  public base.flows.FlowOp_1 $hash$imm(base.flows._SinkDecorator_0 sinkDecorator_m$, base.flows.FlowOp_1 upstream_m$, base.F_2 f_m$) {
  return  base.flows._Map_0.$hash$imm$fun(sinkDecorator_m$, upstream_m$, f_m$, this);
}

public base.flows.FlowOp_1 $hash$imm(base.flows._SinkDecorator_0 sinkDecorator_m$, base.flows.FlowOp_1 upstream_m$, base.ToIso_1 ctx_m$, base.F_3 f_m$) {
  return  base.flows._Map_0.$hash$imm$fun(sinkDecorator_m$, upstream_m$, ctx_m$, f_m$, this);
}

public base.flows._Sink_1 impl$imm(base.flows._Sink_1 downstream_m$, base.ToIso_1 ctx_m$, base.F_3 f_m$, base.flows._SinkDecorator_0 sinkDecorator_m$) {
  return  base.flows._Map_0.impl$imm$fun(downstream_m$, ctx_m$, f_m$, sinkDecorator_m$, this);
}

public base.flows._Sink_1 impl$imm(base.flows._Sink_1 downstream_m$, base.F_2 f_m$, base.flows._SinkDecorator_0 sinkDecorator_m$) {
  return  base.flows._Map_0.impl$imm$fun(downstream_m$, f_m$, sinkDecorator_m$, this);
}

  
}
