package base.caps;
public interface FEnv_0 extends base.F_2,base.Sealed_0{
FEnv_0 $self = new FEnv_0Impl();
base.caps.Env_0 io$imm(base.caps.IO_0 io_m$);

base.caps.Env_0 $hash$read(Object a_m$);
static base.caps.Env_0 $hash$read$fun(base.caps.System_0 s_m$, base.caps.FEnv_0 $this) {
  return ((base.caps.Env_0)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.caps.Env_0) null;
}})
);
}

static base.caps.Env_0 io$imm$fun(base.caps.IO_0 io_m$, base.caps.FEnv_0 $this) {
  return ((base.caps.Env_0)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.caps.Env_0) null;
}})
);
}
}