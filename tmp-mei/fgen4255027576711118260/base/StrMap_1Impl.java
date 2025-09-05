package base;
public record StrMap_1Impl() implements base.StrMap_1 {
  public base.Opt_1 get$imm(Object k_m$) {
  return this.get$imm$Delegate((rt.Str) k_m$);
}

public base.Opt_1 get$imm$Delegate(rt.Str k_m$) {
  return  base.LinkedLens_2.get$imm$fun(k_m$, this);
}


public base.Opt_1 get$mut(Object k_m$) {
  return this.get$mut$Delegate((rt.Str) k_m$);
}

public base.Opt_1 get$mut$Delegate(rt.Str k_m$) {
  return  base.LinkedLens_2.get$mut$fun(k_m$, this);
}


public base.Opt_1 get$read(Object k_m$) {
  return this.get$read$Delegate((rt.Str) k_m$);
}

public base.Opt_1 get$read$Delegate(rt.Str k_m$) {
  return  base.LinkedLens_2.get$read$fun(k_m$, this);
}


public base.Bool_0 keyEq$read(Object k1_m$, Object k2_m$) {
  return this.keyEq$read$Delegate((rt.Str) k1_m$, (rt.Str) k2_m$);
}

public base.Bool_0 keyEq$read$Delegate(rt.Str k1_m$, rt.Str k2_m$) {
  return  base.StrMap_1.keyEq$read$fun(k1_m$, k2_m$, this);
}


public base.Bool_0 isEmpty$read() {
  return  base.Map_2.isEmpty$read$fun(this);
}

public base.LinkedLens_2 map$mut(base.MapMapImm_3 fImm_m$, base.MapMapMut_3 fMut_m$, base.MapMapRead_3 fRead_m$) {
  return  base.LinkedLens_2.map$mut$fun(fImm_m$, fMut_m$, fRead_m$, this);
}

public base.LinkedLens_2 put$imm(Object k_m$, Object v_m$) {
  return this.put$imm$Delegate((rt.Str) k_m$, (Object) v_m$);
}

public base.LinkedLens_2 put$imm$Delegate(rt.Str k_m$, Object v_m$) {
  return  base.LinkedLens_2.put$imm$fun(k_m$, v_m$, this);
}


public base.LinkedLens_2 put$mut(Object k_m$, Object v_m$) {
  return this.put$mut$Delegate((rt.Str) k_m$, (Object) v_m$);
}

public base.LinkedLens_2 put$mut$Delegate(rt.Str k_m$, Object v_m$) {
  return  base.LinkedLens_2.put$mut$fun(k_m$, v_m$, this);
}


public base.LinkedLens_2 put$read(Object k_m$, Object v_m$) {
  return this.put$read$Delegate((rt.Str) k_m$, (Object) v_m$);
}

public base.LinkedLens_2 put$read$Delegate(rt.Str k_m$, Object v_m$) {
  return  base.LinkedLens_2.put$read$fun(k_m$, v_m$, this);
}


public base.LinkedLens_2 map$imm(base.MapMapImm_3 fImm_m$, base.MapMapRead_3 fRead_m$) {
  return  base.LinkedLens_2.map$imm$fun(fImm_m$, fRead_m$, this);
}

public base.LinkedLens_2 map$read(base.MapMapImm_3 fImm_m$, base.MapMapRead_3 fRead_m$) {
  return  base.LinkedLens_2.map$read$fun(fImm_m$, fRead_m$, this);
}

  
}
