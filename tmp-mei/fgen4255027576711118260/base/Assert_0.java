package base;
public interface Assert_0 extends base.Sealed_0{
Assert_0 $self = new Assert_0Impl();
base.Void_0 $exclamation$imm(base.Bool_0 assertion_m$);

Object $exclamation$imm(base.Bool_0 assertion_m$, base.AssertCont_1 cont_m$);

Object $exclamation$imm(base.Bool_0 assertion_m$, rt.Str msg_m$, base.AssertCont_1 cont_m$);

Object _fail$imm();

Object _fail$imm(rt.Str msg_m$);
static base.Void_0 $exclamation$imm$fun(base.Bool_0 assertion_m$, base.Assert_0 $this) {
  return ((base.Void_0)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.Void_0) null;
}})
);
}

static Object $exclamation$imm$fun(base.Bool_0 assertion_m$, base.AssertCont_1 cont_m$, base.Assert_0 $this) {
  return (switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (Object) null;
}})
;
}

static Object $exclamation$imm$fun(base.Bool_0 assertion_m$, rt.Str msg_m$, base.AssertCont_1 cont_m$, base.Assert_0 $this) {
  return (switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (Object) null;
}})
;
}

static Object _fail$imm$fun(base.Assert_0 $this) {
  return (switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (Object) null;
}})
;
}

static Object _fail$imm$fun(rt.Str msg_m$, base.Assert_0 $this) {
  return (switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (Object) null;
}})
;
}
}