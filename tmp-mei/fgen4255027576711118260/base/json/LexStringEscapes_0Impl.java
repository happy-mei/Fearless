package base.json;
public record LexStringEscapes_0Impl() implements base.json.LexStringEscapes_0 {
  public base.flows.ActorRes_0 $hash$read(base.flows._ActorSink_1 downstream_m$, Object state_m$, Object e_m$) {
  return this.$hash$read$Delegate((base.flows._ActorSink_1) downstream_m$, (base.json._LexerCtx_0) state_m$, (rt.Str) e_m$);
}

public base.flows.ActorRes_0 $hash$read$Delegate(base.flows._ActorSink_1 downstream_m$, base.json._LexerCtx_0 ctx_m$, rt.Str e_m$) {
  return  base.json.LexStringEscapes_0.$hash$read$fun(downstream_m$, ctx_m$, e_m$, this);
}


  
}
