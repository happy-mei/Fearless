package base;
public interface Regex_0 extends base.Sealed_0,base.Stringable_0{
Regex_0 $self = new Regex_0Impl();
rt.Str str$read();

base.Bool_0 isMatch$imm(rt.Str haystack_m$);
static rt.Str str$read$fun(base.Regex_0 $this) {
  return ((rt.Str)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (rt.Str) null;
}})
);
}

static base.Bool_0 isMatch$imm$fun(rt.Str haystack_m$, base.Regex_0 $this) {
  return ((base.Bool_0)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.Bool_0) null;
}})
);
}
}