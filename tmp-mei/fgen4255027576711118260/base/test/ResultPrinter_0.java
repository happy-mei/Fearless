package base.test;
public interface ResultPrinter_0 extends base.Sealed_0,base.test.ResultReporter_0{
ResultPrinter_0 $self = new ResultPrinter_0Impl();
base.Void_0 $hash$mut(base.List_1 results_m$);
static base.Void_0 $hash$mut$fun(base.List_1 results_m$, base.test.ResultPrinter_0 $this) {
  return ((base.Void_0)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.Void_0) null;
}})
);
}
}