package base;
public interface Infos_0{
Infos_0 $self = new Infos_0Impl();
base.Info_0 list$imm(base.List_1 list_m$);

base.Info_0 msg$imm(rt.Str msg_m$);

base.Info_0 map$imm(base.LinkedHashMap_2 map_m$);
static base.Info_0 msg$imm$fun(rt.Str msg_m$, base.Infos_0 $this) {
  return ((base.Info_0)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.Info_0) null;
}})
);
}

static base.Info_0 list$imm$fun(base.List_1 list_m$, base.Infos_0 $this) {
  return ((base.Info_0)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.Info_0) null;
}})
);
}

static base.Info_0 map$imm$fun(base.LinkedHashMap_2 map_m$, base.Infos_0 $this) {
  return ((base.Info_0)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.Info_0) null;
}})
);
}
}