package base;
public interface Vars_0{
Vars_0 $self = new Vars_0Impl();
base.Var_1 $hash$imm(Object x_m$);
static base.Var_1 $hash$imm$fun(Object x_m$, base.Vars_0 $this) {
  return ((base.Var_1)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.Var_1) null;
}})
);
}
}