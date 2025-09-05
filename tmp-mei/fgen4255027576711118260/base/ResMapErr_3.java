package base;
public interface ResMapErr_3 extends base.ResMatch_3{
base.Res_2 err$mut(Object x_m$);

base.Res_2 b$mut(Object x_m$);

base.Res_2 ok$mut(Object x_m$);

base.Res_2 a$mut(Object x_m$);

Object $hash$mut(Object x_m$);
static base.Res_2 ok$mut$fun(Object x_m$, base.ResMapErr_3 $this) {
  return ((base.Res_2)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.Res_2) null;
}})
);
}

static base.Res_2 err$mut$fun(Object x_m$, base.ResMapErr_3 $this) {
  return ((base.Res_2)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.Res_2) null;
}})
);
}
}