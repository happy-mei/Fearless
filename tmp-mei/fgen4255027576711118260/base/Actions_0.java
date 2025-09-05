package base;
public interface Actions_0{
Actions_0 $self = new Actions_0Impl();
base.Action_1 info$imm(base.Info_0 info_m$);

base.Action_1 ok$imm(Object x_m$);

base.Action_1 lazy$imm(base.MF_1 f_m$);
static base.Action_1 lazy$imm$fun(base.MF_1 f_m$, base.Actions_0 $this) {
  return ((base.Action_1)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.Action_1) null;
}})
);
}

static base.Action_1 ok$imm$fun(Object x_m$, base.Actions_0 $this) {
  return ((base.Action_1)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.Action_1) null;
}})
);
}

static base.Action_1 info$imm$fun(base.Info_0 info_m$, base.Actions_0 $this) {
  return ((base.Action_1)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.Action_1) null;
}})
);
}
}