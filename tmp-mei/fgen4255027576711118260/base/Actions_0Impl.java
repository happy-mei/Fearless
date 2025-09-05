package base;
public record Actions_0Impl() implements base.Actions_0 {
  public base.Action_1 info$imm(base.Info_0 info_m$) {
  return  base.Actions_0.info$imm$fun(info_m$, this);
}

public base.Action_1 ok$imm(Object x_m$) {
  return  base.Actions_0.ok$imm$fun(x_m$, this);
}

public base.Action_1 lazy$imm(base.MF_1 f_m$) {
  return  base.Actions_0.lazy$imm$fun(f_m$, this);
}

  
}
