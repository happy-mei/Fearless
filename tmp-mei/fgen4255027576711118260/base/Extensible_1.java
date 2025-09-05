package base;
public interface Extensible_1{
Object self$imm();

Object self$read();

Object self$mut();

Object $hash$imm(base.Extension_2 ext_m$);

Object $hash$read(base.Extension_2 ext_m$);

Object $hash$mut(base.Extension_2 ext_m$);
static Object $hash$mut$fun(base.Extension_2 ext_m$, base.Extensible_1 $this) {
  return (switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (Object) null;
}})
;
}

static Object $hash$read$fun(base.Extension_2 ext_m$, base.Extensible_1 $this) {
  return (switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (Object) null;
}})
;
}

static Object $hash$imm$fun(base.Extension_2 ext_m$, base.Extensible_1 $this) {
  return (switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (Object) null;
}})
;
}
}