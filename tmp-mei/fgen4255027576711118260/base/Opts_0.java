package base;
public interface Opts_0{
Opts_0 $self = new Opts_0Impl();
base.Opt_1 $hash$imm(Object x_m$);
static base.Opt_1 $hash$imm$fun(Object x_m$, base.Opts_0 $this) {
  return ((base.Opt_1)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.Opt_1) null;
}})
);
}
}