package base;
public record EmptyMap_2Impl() implements base.EmptyMap_2 {
  public base.Opt_1 get$read(Object k_m$) {
  return  base.EmptyMap_2.get$read$fun(k_m$, this);
}

public base.Opt_1 get$imm(Object k_m$) {
  return  base.EmptyMap_2.get$imm$fun(k_m$, this);
}

public base.Bool_0 keyEq$read(Object k1_m$, Object k2_m$) {
  return  base.EmptyMap_2.keyEq$read$fun(k1_m$, k2_m$, this);
}

public base.Bool_0 isEmpty$read() {
  return  base.Map_2.isEmpty$read$fun(this);
}

  
}
