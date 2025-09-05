package base;
public record Assert_0Impl() implements base.Assert_0 {
  public base.Void_0 $exclamation$imm(base.Bool_0 assertion_m$) {
  return  base.Assert_0.$exclamation$imm$fun(assertion_m$, this);
}

public Object $exclamation$imm(base.Bool_0 assertion_m$, base.AssertCont_1 cont_m$) {
  return  base.Assert_0.$exclamation$imm$fun(assertion_m$, cont_m$, this);
}

public Object $exclamation$imm(base.Bool_0 assertion_m$, rt.Str msg_m$, base.AssertCont_1 cont_m$) {
  return  base.Assert_0.$exclamation$imm$fun(assertion_m$, msg_m$, cont_m$, this);
}

public Object _fail$imm() {
  return  base.Assert_0._fail$imm$fun(this);
}

public Object _fail$imm(rt.Str msg_m$) {
  return  base.Assert_0._fail$imm$fun(msg_m$, this);
}

  
}
