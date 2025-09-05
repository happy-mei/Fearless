package base.rng;
public interface Random_0 extends base.ToIso_1{
Double float$mut();

base.rng.Random_0 iso$mut();

base.rng.Random_0 self$mut();

Long nat$mut();

Long nat$mut(long minInclusive_m$, long maxExclusive_m$);
static Long nat$mut$fun(long minInclusive_m$, long maxExclusive_m$, base.rng.Random_0 $this) {
  return ((long)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (long) 0;
}})
);
}

static Double float$mut$fun(base.rng.Random_0 $this) {
  return ((double)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (double) 0;
}})
);
}
}