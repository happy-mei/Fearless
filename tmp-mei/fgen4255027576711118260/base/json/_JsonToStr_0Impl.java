package base.json;
public record _JsonToStr_0Impl() implements base.json._JsonToStr_0 {
  public rt.Str array$imm(base.List_1 a_m$) {
  return  base.json._JsonToStr_0.array$imm$fun(a_m$, this);
}

public rt.Str escape$imm(rt.Str s_m$) {
  return  base.json._JsonToStr_0.escape$imm$fun(s_m$, this);
}

public rt.Str object$imm(base.LinkedHashMap_2 o_m$) {
  return  base.json._JsonToStr_0.object$imm$fun(o_m$, this);
}

public rt.Str $hash$imm(base.json.Json_0 json_m$) {
  return  base.json._JsonToStr_0.$hash$imm$fun(json_m$, this);
}

  
}
