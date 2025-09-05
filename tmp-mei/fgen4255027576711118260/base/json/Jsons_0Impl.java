package base.json;
public record Jsons_0Impl() implements base.json.Jsons_0 {
  public base.json.Json_0 bool$imm(base.Bool_0 b_m$) {
  return  base.json.Jsons_0.bool$imm$fun(b_m$, this);
}

public base.json.Json_0 number$imm(double n_m$) {
  return  base.json.Jsons_0.number$imm$fun(n_m$, this);
}

public base.json.Json_0 array$imm(base.List_1 a_m$) {
  return  base.json.Jsons_0.array$imm$fun(a_m$, this);
}

public base.json.Json_0 object$imm(base.LinkedHashMap_2 o_m$) {
  return  base.json.Jsons_0.object$imm$fun(o_m$, this);
}

public base.json.Json_0 null$imm() {
  return  base.json.Jsons_0.null$imm$fun(this);
}

public base.json.Json_0 string$imm(rt.Str s_m$) {
  return  base.json.Jsons_0.string$imm$fun(s_m$, this);
}

  
}
