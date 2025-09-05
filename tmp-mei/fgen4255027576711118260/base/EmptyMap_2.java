package base;
public interface EmptyMap_2 extends base.Map_2{
EmptyMap_2 $self = new EmptyMap_2Impl();
base.Opt_1 get$read(Object k_m$);

base.Opt_1 get$imm(Object k_m$);

base.Bool_0 keyEq$read(Object k1_m$, Object k2_m$);

base.Bool_0 isEmpty$read();
static base.Bool_0 keyEq$read$fun(Object k1_m$, Object k2_m$, base.EmptyMap_2 $this) {
  return ((base.Bool_0)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.Bool_0) null;
}})
);
}

static base.Opt_1 get$imm$fun(Object k_m$, base.EmptyMap_2 $this) {
  return ((base.Opt_1)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.Opt_1) null;
}})
);
}

static base.Opt_1 get$read$fun(Object k_m$, base.EmptyMap_2 $this) {
  return ((base.Opt_1)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.Opt_1) null;
}})
);
}
}