package base;
public interface ResMatch_3 extends base.EitherMatch_3{
Object err$mut(Object x_m$);

Object ok$mut(Object x_m$);

Object b$mut(Object x_m$);

Object a$mut(Object x_m$);
static Object a$mut$fun(Object x_m$, base.ResMatch_3 $this) {
  return (switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (Object) null;
}})
;
}

static Object b$mut$fun(Object x_m$, base.ResMatch_3 $this) {
  return (switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (Object) null;
}})
;
}
}