package base.caps;
public interface _System_0 extends base.Sealed_0,base.caps.System_0{
_System_0 $self = new _System_0Impl();
base.ToIso_1 iso$mut();

base.caps.System_0 self$mut();

base.caps.RandomSeed_0 rng$mut();

base.caps.CapTry_0 try$mut();

base.caps.IO_0 io$mut();
static base.ToIso_1 iso$mut$fun(base.caps._System_0 $this) {
  return ((base.ToIso_1)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.ToIso_1) null;
}})
);
}

static base.caps.System_0 self$mut$fun(base.caps._System_0 $this) {
  return ((base.caps.System_0)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.caps.System_0) null;
}})
);
}

static base.caps.IO_0 io$mut$fun(base.caps._System_0 $this) {
  return ((base.caps.IO_0)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.caps.IO_0) null;
}})
);
}

static base.caps.RandomSeed_0 rng$mut$fun(base.caps._System_0 $this) {
  return ((base.caps.RandomSeed_0)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.caps.RandomSeed_0) null;
}})
);
}

static base.caps.CapTry_0 try$mut$fun(base.caps._System_0 $this) {
  return ((base.caps.CapTry_0)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.caps.CapTry_0) null;
}})
);
}
}