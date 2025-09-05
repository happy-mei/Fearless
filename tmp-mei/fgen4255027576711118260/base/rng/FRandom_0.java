package base.rng;
public interface FRandom_0 extends base.F_2{
FRandom_0 $self = new FRandom_0Impl();
base.rng.Random_0 $hash$read(Object a_m$);
static base.rng.Random_0 $hash$read$fun(long seed_m$, base.rng.FRandom_0 $this) {
  return ((base.rng.Random_0)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.rng.Random_0) null;
}})
);
}
}