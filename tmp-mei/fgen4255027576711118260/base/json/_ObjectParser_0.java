package base.json;
public interface _ObjectParser_0{
_ObjectParser_0 $self = new _ObjectParser_0Impl();
base.json._ParserActor_0 $hash$imm(base.Consumer_1 collector_m$, base.json._ParserActor_0 old_m$);
static base.json._ParserActor_0 $hash$imm$fun(base.Consumer_1 collector_m$, base.json._ParserActor_0 old_m$, base.json._ObjectParser_0 $this) {
  return ((base.json._ParserActor_0)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.json._ParserActor_0) null;
}})
);
}
}