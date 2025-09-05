package base.caps;
public interface UnrestrictedIO_0 extends base.F_2,base.Sealed_0{
UnrestrictedIO_0 $self = new UnrestrictedIO_0Impl();
base.caps.IO_0 $hash$read(Object a_m$);
static base.caps.IO_0 $hash$read$fun(base.caps.System_0 sys_m$, base.caps.UnrestrictedIO_0 $this) {
  return ((base.caps.IO_0)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.caps.IO_0) null;
}})
);
}
}