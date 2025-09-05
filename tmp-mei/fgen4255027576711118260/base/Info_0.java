package base;
public interface Info_0 extends base.Sealed_0{
rt.Str str$imm();

rt.Str msg$imm();

base.List_1 list$imm();

Object accept$imm(base.InfoVisitor_1 visitor_m$);

base.LinkedHashMap_2 map$imm();
static rt.Str str$imm$fun(base.Info_0 $this) {
  return ((rt.Str)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (rt.Str) null;
}})
);
}
}