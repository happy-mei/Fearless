package base.json;
public interface _TopLevelParser_0 extends base.json._ParserActor_0{
_TopLevelParser_0 $self = new _TopLevelParser_0Impl();
base.flows.ActorRes_0 $hash$mut(base.flows._ActorSink_1 downstream_m$, base.Var_1 behaviour_m$, base.json.Token_0 token_m$);
static base.flows.ActorRes_0 $hash$mut$fun(base.flows._ActorSink_1 downstream_m$, base.Var_1 behaviour_m$, base.json.Token_0 token_m$, base.json._TopLevelParser_0 $this) {
  return ((base.flows.ActorRes_0)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.flows.ActorRes_0) null;
}})
);
}
}