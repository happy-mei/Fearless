package base;
public record UTF16_0Impl() implements base.UTF16_0 {
  public rt.Str fromSurrogatePair$imm(long high_m$, long low_m$) {
  return  base.UTF16_0.fromSurrogatePair$imm$fun(high_m$, low_m$, this);
}

public base.Bool_0 isSurrogate$imm(long codePoint_m$) {
  return  base.UTF16_0.isSurrogate$imm$fun(codePoint_m$, this);
}

public rt.Str fromCodePoint$imm(long codePoint_m$) {
  return  base.UTF16_0.fromCodePoint$imm$fun(codePoint_m$, this);
}

  
}
