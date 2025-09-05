package base;
public interface Res_0{
Res_0 $self = new Res_0Impl();
base.Res_2 err$imm(Object x_m$);

base.Res_2 ok$imm(Object x_m$);

base.Res_1 $hash$imm(Object x_m$);
static base.Res_1 $hash$imm$fun(Object x_m$, base.Res_0 $this) {
  return ((base.Res_1)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.Res_1) null;
}})
);
}

static base.Res_2 ok$imm$fun(Object x_m$, base.Res_0 $this) {
  return ((base.Res_2)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.Res_2) null;
}})
);
}

static base.Res_2 err$imm$fun(Object x_m$, base.Res_0 $this) {
  return ((base.Res_2)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.Res_2) null;
}})
);
}
}