package base.json;
public interface LexNum_0{
LexNum_0 $self = new LexNum_0Impl();
base.flows.ActorRes_0 $hash$imm(base.flows._ActorSink_1 downstream_m$, base.json._LexerCtx_0 ctx_m$, rt.Str e_m$, base.flows.ActorImpl_3 parent_m$);
static base.flows.ActorRes_0 $hash$imm$fun(base.flows._ActorSink_1 downstream_m$, base.json._LexerCtx_0 ctx_m$, rt.Str e_m$, base.flows.ActorImpl_3 parent_m$, base.json.LexNum_0 $this) {
  return ((base.flows.ActorRes_0)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.flows.ActorRes_0) null;
}})
);
}
}