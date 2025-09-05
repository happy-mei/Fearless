package base.flows;
public record _FlatMap_0Impl() implements base.flows._FlatMap_0 {
  public base.flows._Sink_1 impl$imm(base.flows._Sink_1 downstream_m$, base.F_2 f_m$, base.flows.FlowOp_1 op_m$, base.Var_1 isRunning_m$, base.flows._SinkDecorator_0 sinkDecorator_m$) {
  return  base.flows._FlatMap_0.impl$imm$fun(downstream_m$, f_m$, op_m$, isRunning_m$, sinkDecorator_m$, this);
}

public base.flows.FlowOp_1 $hash$imm(base.flows._SinkDecorator_0 sinkDecorator_m$, base.flows.FlowOp_1 upstream_m$, base.F_2 f_m$) {
  return  base.flows._FlatMap_0.$hash$imm$fun(sinkDecorator_m$, upstream_m$, f_m$, this);
}

public base.Void_0 flatten$imm(base.flows.FlowOp_1 toFlatten_m$, base.flows._Sink_1 downstream_m$, base.flows.FlowOp_1 op_m$, base.flows._SinkDecorator_0 sinkDecorator_m$) {
  return  base.flows._FlatMap_0.flatten$imm$fun(toFlatten_m$, downstream_m$, op_m$, sinkDecorator_m$, this);
}

  
}
