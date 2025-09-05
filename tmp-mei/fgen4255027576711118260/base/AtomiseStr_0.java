package base;
public interface AtomiseStr_0{
AtomiseStr_0 $self = new AtomiseStr_0Impl();
base.flows.Flow_1 $hash$imm(rt.Str raw_m$);
static base.flows.Flow_1 $hash$imm$fun(rt.Str raw_m$, base.AtomiseStr_0 $this) {
  return ((base.flows.Flow_1)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.flows.Flow_1) null;
}})
);
}
}