package base;
public interface Try_0{
Try_0 $self = new Try_0Impl();
base.Action_1 $hash$imm(Object data_m$, base.F_2 $try);

base.Action_1 $hash$imm(base.F_1 $try);
static base.Action_1 $hash$imm$fun(base.F_1 $try, base.Try_0 $this) {
  return ((base.Action_1)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.Action_1) null;
}})
);
}

static base.Action_1 $hash$imm$fun(Object data_m$, base.F_2 $try, base.Try_0 $this) {
  return ((base.Action_1)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.Action_1) null;
}})
);
}
}