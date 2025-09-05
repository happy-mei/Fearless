package base;
public record Block_1Impl() implements base.Block_1 {
  public base.BlockIf_1 if$mut(base.Condition_0 p_m$) {
  return  base.Block_1.if$mut$fun(p_m$, this);
}

public base.Block_1 _do$mut(base.Void_0 v_m$) {
  return  base.Block_1._do$mut$fun(v_m$, this);
}

public base.Block_1 loop$mut(base.LoopBody_1 body_m$) {
  return  base.Block_1.loop$mut$fun(body_m$, this);
}

public Object openIso$mut(Object x_m$, base.Continuation_3 cont_m$) {
  return  base.Block_1.openIso$mut$fun(x_m$, cont_m$, this);
}

public Object var$mut(base.ReturnStmt_1 x_m$, base.Continuation_3 cont_m$) {
  return  base.Block_1.var$mut$fun(x_m$, cont_m$, this);
}

public base.Void_0 done$mut() {
  return  base.Block_1.done$mut$fun(this);
}

public Object isoPod$mut(base.ReturnStmt_1 x_m$, base.Continuation_3 cont_m$) {
  return  base.Block_1.isoPod$mut$fun(x_m$, cont_m$, this);
}

public base.Block_1 assert$mut(base.Condition_0 p_m$) {
  return  base.Block_1.assert$mut$fun(p_m$, this);
}

public base.Block_1 assert$mut(base.Condition_0 p_m$, rt.Str failMsg_m$) {
  return  base.Block_1.assert$mut$fun(p_m$, failMsg_m$, this);
}

public Object return$mut(base.ReturnStmt_1 a_m$) {
  return  base.Block_1.return$mut$fun(a_m$, this);
}

public Object let$mut(base.ReturnStmt_1 x_m$, base.Continuation_3 cont_m$) {
  return  base.Block_1.let$mut$fun(x_m$, cont_m$, this);
}

public base.Block_1 do$mut(base.ReturnStmt_1 r_m$) {
  return  base.Block_1.do$mut$fun(r_m$, this);
}

  
}
