package base.flows;
public interface TerminalOnInfiniteError_0{
TerminalOnInfiniteError_0 $self = new TerminalOnInfiniteError_0Impl();
base.Info_0 $hash$imm();
static base.Info_0 $hash$imm$fun(base.flows.TerminalOnInfiniteError_0 $this) {
  return ((base.Info_0)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.Info_0) null;
}})
);
}
}