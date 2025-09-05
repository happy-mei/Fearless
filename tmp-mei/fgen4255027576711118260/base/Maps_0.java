package base;
public interface Maps_0 extends base.Sealed_0{
Maps_0 $self = new Maps_0Impl();
base.LinkedHashMap_2 hashMap$imm(base.F_3 keyEq_m$, base.F_2 hash_m$);
static base.LinkedHashMap_2 hashMap$imm$fun(base.F_3 keyEq_m$, base.F_2 hash_m$, base.Maps_0 $this) {
  return ((base.LinkedHashMap_2)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.LinkedHashMap_2) null;
}})
);
}
}