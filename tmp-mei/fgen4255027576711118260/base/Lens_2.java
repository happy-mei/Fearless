package base;
public interface Lens_2 extends base.Map_2{
base.Opt_1 get$imm(Object k_m$);

base.Opt_1 get$read(Object k_m$);

base.Bool_0 keyEq$read(Object k1_m$, Object k2_m$);

base.Bool_0 isEmpty$read();

base.Lens_2 put$imm(Object k_m$, Object v_m$);

base.Lens_2 map$imm(base.MapMapImm_3 f_m$);
static base.Lens_2 put$imm$fun(Object k_m$, Object v_m$, base.Lens_2 $this) {
  return ((base.Lens_2)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.Lens_2) null;
}})
);
}

static base.Lens_2 map$imm$fun(base.MapMapImm_3 f_m$, base.Lens_2 $this) {
  return ((base.Lens_2)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.Lens_2) null;
}})
);
}
}