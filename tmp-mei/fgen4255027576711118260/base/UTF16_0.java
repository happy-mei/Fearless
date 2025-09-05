package base;
public interface UTF16_0 extends base.Sealed_0{
UTF16_0 $self = new UTF16_0Impl();
rt.Str fromSurrogatePair$imm(long high_m$, long low_m$);

base.Bool_0 isSurrogate$imm(long codePoint_m$);

rt.Str fromCodePoint$imm(long codePoint_m$);
static rt.Str fromCodePoint$imm$fun(long codePoint_m$, base.UTF16_0 $this) {
  return ((rt.Str)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (rt.Str) null;
}})
);
}

static base.Bool_0 isSurrogate$imm$fun(long codePoint_m$, base.UTF16_0 $this) {
  return ((base.Bool_0)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.Bool_0) null;
}})
);
}

static rt.Str fromSurrogatePair$imm$fun(long high_m$, long low_m$, base.UTF16_0 $this) {
  return ((rt.Str)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (rt.Str) null;
}})
);
}
}