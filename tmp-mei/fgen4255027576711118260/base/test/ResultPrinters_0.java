package base.test;
public interface ResultPrinters_0 extends base.F_2{
ResultPrinters_0 $self = new ResultPrinters_0Impl();
base.test.ResultPrinter_0 $hash$read(Object a_m$);
static base.test.ResultPrinter_0 $hash$read$fun(base.caps.IO_0 io_m$, base.test.ResultPrinters_0 $this) {
  return ((base.test.ResultPrinter_0)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.test.ResultPrinter_0) null;
}})
);
}
}