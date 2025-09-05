package base.flows;
public interface _EmptyCtx_0 extends base.ToIso_1{
_EmptyCtx_0 $self = new _EmptyCtx_0Impl();
base.ToIso_1 iso$mut();

base.flows._EmptyCtx_0 self$mut();
static base.ToIso_1 iso$mut$fun(base.flows._EmptyCtx_0 $this) {
  return ((base.ToIso_1)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.ToIso_1) null;
}})
);
}

static base.flows._EmptyCtx_0 self$mut$fun(base.flows._EmptyCtx_0 $this) {
  return ((base.flows._EmptyCtx_0)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.flows._EmptyCtx_0) null;
}})
);
}
}