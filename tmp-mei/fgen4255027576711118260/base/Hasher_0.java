package base;
public interface Hasher_0{
base.Hasher_0 hash$mut(base.ToHash_0 x_m$);

base.Hasher_0 byte$mut(byte x_m$);

base.Hasher_0 str$mut(rt.Str x_m$);

base.Hasher_0 nat$mut(long x_m$);

base.Hasher_0 float$mut(double x_m$);

base.Hasher_0 int$mut(long x_m$);

Long compute$mut();
static base.Hasher_0 hash$mut$fun(base.ToHash_0 x_m$, base.Hasher_0 $this) {
  return ((base.Hasher_0)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.Hasher_0) null;
}})
);
}
}