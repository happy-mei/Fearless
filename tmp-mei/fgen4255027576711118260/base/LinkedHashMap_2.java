package base;
public interface LinkedHashMap_2 extends base.Map_2{
base.Opt_1 get$read(Object k_m$);

base.Opt_1 get$imm(Object k_m$);

base.Opt_1 get$mut(Object k_m$);

base.flows.Flow_1 flow$read();

base.flows.Flow_1 flow$imm();

base.Bool_0 isEmpty$read();

base.flows.Flow_1 keys$read();

base.Void_0 clear$mut();

base.flows.Flow_1 flowMut$mut();

base.flows.Flow_1 values$read();

base.flows.Flow_1 values$imm();

base.flows.Flow_1 values$mut();

base.LinkedHashMap_2 $plus$mut(Object k_m$, Object v_m$);

base.Bool_0 keyEq$read(Object k1_m$, Object k2_m$);

base.Opt_1 remove$mut(Object k_m$);

base.Void_0 put$mut(Object k_m$, Object v_m$);
static base.Void_0 put$mut$fun(Object k_m$, Object v_m$, base.LinkedHashMap_2 $this) {
  return ((base.Void_0)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.Void_0) null;
}})
);
}
}