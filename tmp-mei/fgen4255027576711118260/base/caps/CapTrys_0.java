package base.caps;
public interface CapTrys_0 extends base.F_2{
CapTrys_0 $self = new CapTrys_0Impl();
base.caps.CapTry_0 $hash$read(Object a_m$);
static base.caps.CapTry_0 $hash$read$fun(base.caps.System_0 sys_m$, base.caps.CapTrys_0 $this) {
  return ((base.caps.CapTry_0)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.caps.CapTry_0) null;
}})
);
}
}