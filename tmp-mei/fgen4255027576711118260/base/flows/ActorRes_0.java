package base.flows;
public interface ActorRes_0 extends base.Sealed_0{
ActorRes_0 $self = new ActorRes_0Impl();
Object match$imm(base.flows.ActorResMatch_1 m_m$);

base.flows.ActorRes_0 stop$imm();

base.flows.ActorRes_0 continue$imm();
static Object match$imm$fun(base.flows.ActorResMatch_1 m_m$, base.flows.ActorRes_0 $this) {
  return (switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (Object) null;
}})
;
}

static base.flows.ActorRes_0 continue$imm$fun(base.flows.ActorRes_0 $this) {
  return ((base.flows.ActorRes_0)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.flows.ActorRes_0) null;
}})
);
}

static base.flows.ActorRes_0 stop$imm$fun(base.flows.ActorRes_0 $this) {
  return ((base.flows.ActorRes_0)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.flows.ActorRes_0) null;
}})
);
}
}