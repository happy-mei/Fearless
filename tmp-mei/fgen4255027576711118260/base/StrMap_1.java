package base;
public interface StrMap_1 extends base.LinkedLens_2{
StrMap_1 $self = new StrMap_1Impl();
base.Opt_1 get$imm(Object k_m$);

base.Opt_1 get$mut(Object k_m$);

base.Opt_1 get$read(Object k_m$);

base.Bool_0 keyEq$read(Object k1_m$, Object k2_m$);

base.Bool_0 isEmpty$read();

base.LinkedLens_2 map$mut(base.MapMapImm_3 fImm_m$, base.MapMapMut_3 fMut_m$, base.MapMapRead_3 fRead_m$);

base.LinkedLens_2 put$imm(Object k_m$, Object v_m$);

base.LinkedLens_2 put$mut(Object k_m$, Object v_m$);

base.LinkedLens_2 put$read(Object k_m$, Object v_m$);

base.LinkedLens_2 map$imm(base.MapMapImm_3 fImm_m$, base.MapMapRead_3 fRead_m$);

base.LinkedLens_2 map$read(base.MapMapImm_3 fImm_m$, base.MapMapRead_3 fRead_m$);
static base.Bool_0 keyEq$read$fun(rt.Str k1_m$, rt.Str k2_m$, base.StrMap_1 $this) {
  return ((base.Bool_0)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.Bool_0) null;
}})
);
}
}