package base.flows;
public record EmptyFlow_1Impl() implements base.flows.EmptyFlow_1 {
  public base.Void_0 forEffect$mut(base.MF_2 f_m$) {
  return  base.flows._TerminalOps_1.forEffect$mut$fun(f_m$, this);
}

public base.flows.Flow_1 peek$mut(base.F_2 f_m$) {
  return  base.flows._NonTerminalOps_1.peek$mut$fun(f_m$, this);
}

public base.flows.Flow_1 peek$mut(base.ToIso_1 ctx_m$, base.F_3 f_m$) {
  return  base.flows._NonTerminalOps_1.peek$mut$fun(ctx_m$, f_m$, this);
}

public base.flows.Flow_1 actorMut$mut(Object state_m$, base.flows.ActorImplMut_3 f_m$) {
  return  base.flows.EmptyFlow_1.actorMut$mut$fun(state_m$, f_m$, this);
}

public base.flows.Flow_1 self$mut() {
  return  base.flows.Flow_1.self$mut$fun(this);
}

public base.flows.Flow_1 self$read() {
  return  base.flows.Flow_1.self$read$fun(this);
}

public base.flows.Flow_1 self$imm() {
  return  base.flows.Flow_1.self$imm$fun(this);
}

public Object let$mut(base.F_2 x_m$, base.Continuation_3 cont_m$) {
  return  base.flows.Flow_1.let$mut$fun(x_m$, cont_m$, this);
}

public base.flows.Flow_1 assumeFinite$mut() {
  return  base.flows.EmptyFlow_1.assumeFinite$mut$fun(this);
}

public base.flows.Flow_1 actor$mut(Object state_m$, base.flows.ActorImpl_3 f_m$) {
  return  base.flows.EmptyFlow_1.actor$mut$fun(state_m$, f_m$, this);
}

public base.Opt_1 first$mut() {
  return  base.flows.EmptyFlow_1.first$mut$fun(this);
}

public base.List_1 list$mut() {
  return  base.flows._TerminalOps_1.list$mut$fun(this);
}

public base.Bool_0 all$mut(base.F_2 p_m$) {
  return  base.flows.EmptyFlow_1.all$mut$fun(p_m$, this);
}

public base.flows.Flow_1 filter$mut(base.F_2 p_m$) {
  return  base.flows.EmptyFlow_1.filter$mut$fun(p_m$, this);
}

public base.Bool_0 none$mut(base.F_2 predicate_m$) {
  return  base.flows._TerminalOps_1.none$mut$fun(predicate_m$, this);
}

public base.flows.Flow_1 flatMap$mut(base.F_2 f_m$) {
  return  base.flows.EmptyFlow_1.flatMap$mut$fun(f_m$, this);
}

public base.Opt_1 opt$mut() {
  return  base.flows.Flow_1.opt$mut$fun(this);
}

public Object get$mut() {
  return  base.flows.Flow_1.get$mut$fun(this);
}

public Long count$mut() {
  return  base.flows.EmptyFlow_1.count$mut$fun(this);
}

public base.Opt_1 findMap$mut(base.F_2 fear2199$_m$) {
  return  base.flows.EmptyFlow_1.findMap$mut$fun(fear2199$_m$, this);
}

public Object join$mut(base.flows.Joinable_1 j_m$) {
  return  base.flows.Flow_1.join$mut$fun(j_m$, this);
}

public base.Opt_1 last$mut() {
  return  base.flows._TerminalOps_1.last$mut$fun(this);
}

public base.flows.FlowOp_1 unwrapOp$mut(base.flows._UnwrapFlowToken_0 fear2200$_m$) {
  return  base.flows.EmptyFlow_1.unwrapOp$mut$fun(fear2200$_m$, this);
}

public base.flows.Flow_1 mapFilter$mut(base.F_2 f_m$) {
  return  base.flows._NonTerminalOps_1.mapFilter$mut$fun(f_m$, this);
}

public Object fold$mut(base.MF_1 acc_m$, base.F_3 fear2201$_m$) {
  return  base.flows.EmptyFlow_1.fold$mut$fun(acc_m$, fear2201$_m$, this);
}

public base.Action_1 only$mut() {
  return  base.flows.Flow_1.only$mut$fun(this);
}

public base.Opt_1 first$mut(base.F_2 predicate_m$) {
  return  base.flows._TerminalOps_1.first$mut$fun(predicate_m$, this);
}

public base.flows.Flow_1 map$mut(base.ToIso_1 fear2202$_m$, base.F_3 fear2203$_m$) {
  return  base.flows.EmptyFlow_1.map$mut$fun(fear2202$_m$, fear2203$_m$, this);
}

public base.flows.Flow_1 map$mut(base.F_2 f_m$) {
  return  base.flows.EmptyFlow_1.map$mut$fun(f_m$, this);
}

public base.flows.Flow_1 limit$mut(long fear2204$_m$) {
  return  base.flows.EmptyFlow_1.limit$mut$fun(fear2204$_m$, this);
}

public base.Bool_0 any$mut(base.F_2 p_m$) {
  return  base.flows.EmptyFlow_1.any$mut$fun(p_m$, this);
}

public base.Opt_1 find$mut(base.F_2 fear2205$_m$) {
  return  base.flows.EmptyFlow_1.find$mut$fun(fear2205$_m$, this);
}

public base.Opt_1 max$mut(base.F_3 compare_m$) {
  return  base.flows._TerminalOps_1.max$mut$fun(compare_m$, this);
}

public base.Opt_1 size$read() {
  return  base.flows.EmptyFlow_1.size$read$fun(this);
}

public base.flows.Flow_1 scan$mut(Object acc_m$, base.F_3 f_m$) {
  return  base.flows._NonTerminalOps_1.scan$mut$fun(acc_m$, f_m$, this);
}

public Object $hash$imm(base.Extension_2 ext_m$) {
  return  base.Extensible_1.$hash$imm$fun(ext_m$, this);
}

public Object $hash$read(base.Extension_2 ext_m$) {
  return  base.Extensible_1.$hash$read$fun(ext_m$, this);
}

public Object $hash$mut(base.Extension_2 ext_m$) {
  return  base.Extensible_1.$hash$mut$fun(ext_m$, this);
}

public base.Void_0 for$mut(base.F_2 f_m$) {
  return  base.flows._TerminalOps_1.for$mut$fun(f_m$, this);
}

  
}
