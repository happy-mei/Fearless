package base;
public interface Regexs_0 extends base.Sealed_0{
Regexs_0 $self = new Regexs_0Impl();
base.Regex_0 $hash$imm(rt.Str pattern_m$);
static base.Regex_0 $hash$imm$fun(rt.Str pattern_m$, base.Regexs_0 $this) {
  return ((base.Regex_0)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.Regex_0) null;
}})
);
}
}