package base;
public interface OptMap_2 extends base.OptMatch_2{
base.Opt_1 some$mut(Object x_m$);

base.Opt_1 empty$mut();

Object $hash$mut(Object t_m$);
static base.Opt_1 some$mut$fun(Object x_m$, base.OptMap_2 $this) {
  return ((base.Opt_1)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.Opt_1) null;
}})
);
}

static base.Opt_1 empty$mut$fun(base.OptMap_2 $this) {
  return ((base.Opt_1)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.Opt_1) null;
}})
);
}
}