package base;
public interface Block_1 extends base.Sealed_0{
Block_1 $self = new Block_1Impl();
base.BlockIf_1 if$mut(base.Condition_0 p_m$);

base.Block_1 _do$mut(base.Void_0 v_m$);

base.Block_1 loop$mut(base.LoopBody_1 body_m$);

Object openIso$mut(Object x_m$, base.Continuation_3 cont_m$);

Object var$mut(base.ReturnStmt_1 x_m$, base.Continuation_3 cont_m$);

base.Void_0 done$mut();

Object isoPod$mut(base.ReturnStmt_1 x_m$, base.Continuation_3 cont_m$);

base.Block_1 assert$mut(base.Condition_0 p_m$);

base.Block_1 assert$mut(base.Condition_0 p_m$, rt.Str failMsg_m$);

Object return$mut(base.ReturnStmt_1 a_m$);

Object let$mut(base.ReturnStmt_1 x_m$, base.Continuation_3 cont_m$);

base.Block_1 do$mut(base.ReturnStmt_1 r_m$);
static base.Void_0 done$mut$fun(base.Block_1 $this) {
  return ((base.Void_0)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.Void_0) null;
}})
);
}

static Object return$mut$fun(base.ReturnStmt_1 a_m$, base.Block_1 $this) {
  return (switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (Object) null;
}})
;
}

static base.Block_1 do$mut$fun(base.ReturnStmt_1 r_m$, base.Block_1 $this) {
  return ((base.Block_1)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.Block_1) null;
}})
);
}

static base.Block_1 _do$mut$fun(base.Void_0 v_m$, base.Block_1 $this) {
  return ((base.Block_1)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.Block_1) null;
}})
);
}

static base.Block_1 assert$mut$fun(base.Condition_0 p_m$, base.Block_1 $this) {
  return ((base.Block_1)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.Block_1) null;
}})
);
}

static base.Block_1 assert$mut$fun(base.Condition_0 p_m$, rt.Str failMsg_m$, base.Block_1 $this) {
  return ((base.Block_1)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.Block_1) null;
}})
);
}

static Object let$mut$fun(base.ReturnStmt_1 x_m$, base.Continuation_3 cont_m$, base.Block_1 $this) {
  return (switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (Object) null;
}})
;
}

static Object openIso$mut$fun(Object x_m$, base.Continuation_3 cont_m$, base.Block_1 $this) {
  return (switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (Object) null;
}})
;
}

static Object var$mut$fun(base.ReturnStmt_1 x_m$, base.Continuation_3 cont_m$, base.Block_1 $this) {
  return (switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (Object) null;
}})
;
}

static Object isoPod$mut$fun(base.ReturnStmt_1 x_m$, base.Continuation_3 cont_m$, base.Block_1 $this) {
  return (switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (Object) null;
}})
;
}

static base.BlockIf_1 if$mut$fun(base.Condition_0 p_m$, base.Block_1 $this) {
  return ((base.BlockIf_1)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.BlockIf_1) null;
}})
);
}

static base.Block_1 loop$mut$fun(base.LoopBody_1 body_m$, base.Block_1 $this) {
  return ((base.Block_1)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.Block_1) null;
}})
);
}
}