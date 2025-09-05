package base.json;
public record Tokens_0Impl() implements base.json.Tokens_0 {
  public base.json.Token_0 true$imm(long line_m$, long col_m$) {
  return  base.json.Tokens_0.true$imm$fun(line_m$, col_m$, this);
}

public base.json.Token_0 quoted$imm(long line_m$, long col_m$, rt.Str chars_m$) {
  return  base.json.Tokens_0.quoted$imm$fun(line_m$, col_m$, chars_m$, this);
}

public base.json.Token_0 comma$imm(long line_m$, long col_m$) {
  return  base.json.Tokens_0.comma$imm$fun(line_m$, col_m$, this);
}

public base.json.Token_0 cc$imm(long line_m$, long col_m$) {
  return  base.json.Tokens_0.cc$imm$fun(line_m$, col_m$, this);
}

public base.json.Token_0 cs$imm(long line_m$, long col_m$) {
  return  base.json.Tokens_0.cs$imm$fun(line_m$, col_m$, this);
}

public base.json.Token_0 oc$imm(long line_m$, long col_m$) {
  return  base.json.Tokens_0.oc$imm$fun(line_m$, col_m$, this);
}

public base.json.Token_0 os$imm(long line_m$, long col_m$) {
  return  base.json.Tokens_0.os$imm$fun(line_m$, col_m$, this);
}

public base.json.Token_0 false$imm(long line_m$, long col_m$) {
  return  base.json.Tokens_0.false$imm$fun(line_m$, col_m$, this);
}

public base.json.Token_0 null$imm(long line_m$, long col_m$) {
  return  base.json.Tokens_0.null$imm$fun(line_m$, col_m$, this);
}

public base.json.Token_0 numeric$imm(long line_m$, long col_m$, double num_m$) {
  return  base.json.Tokens_0.numeric$imm$fun(line_m$, col_m$, num_m$, this);
}

public base.json.Token_0 unknownFragment$imm(long line_m$, long col_m$, rt.Str bufferContents_m$) {
  return  base.json.Tokens_0.unknownFragment$imm$fun(line_m$, col_m$, bufferContents_m$, this);
}

public base.json.Token_0 colon$imm(long line_m$, long col_m$) {
  return  base.json.Tokens_0.colon$imm$fun(line_m$, col_m$, this);
}

  
}
