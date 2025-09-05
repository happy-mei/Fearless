package base.flows;
public interface _ActorSink_1 extends base.Sealed_0{
_ActorSink_1 $self = new _ActorSink_1Impl();
base.Void_0 pushError$mut(base.Info_0 info_m$);

base.Void_0 $hash$mut(Object x_m$);
static base.Void_0 $hash$mut$fun(Object x_m$, base.flows._ActorSink_1 $this) {
  return ((base.Void_0)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.Void_0) null;
}})
);
}

static base.Void_0 pushError$mut$fun(base.Info_0 info_m$, base.flows._ActorSink_1 $this) {
  return ((base.Void_0)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.Void_0) null;
}})
);
}
}