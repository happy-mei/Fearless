package base.test;
public interface Main_0 extends base.Main_0{
base.List_1 testMain$imm(base.caps.System_0 system_m$, base.test.TestRunner_0 runner_m$);

base.Void_0 $hash$imm(base.caps.System_0 sys_m$);
static base.Void_0 $hash$imm$fun(base.caps.System_0 sys_m$, base.test.Main_0 $this) {
  return ((base.Void_0)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.Void_0) null;
}})
);
}
}