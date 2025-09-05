package base.json;
public interface LexJson_0 extends base.flows.ActorImpl_3{
LexJson_0 $self = new LexJson_0Impl();
base.flows.ActorRes_0 $hash$read(base.flows._ActorSink_1 downstream_m$, Object state_m$, Object e_m$);
static base.flows.ActorRes_0 $hash$read$fun(base.flows._ActorSink_1 downstream_m$, base.json._LexerCtx_0 ctx_m$, rt.Str e_m$, base.json.LexJson_0 $this) {
  return ((base.flows.ActorRes_0)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.flows.ActorRes_0) null;
}})
);
}
}