package base;
public interface LList_1 extends base.Collection_0,base.Sealed_0{
LList_1 $self = new LList_1Impl();
base.Opt_1 tryGet$imm(long i_m$);

base.Opt_1 tryGet$read(long i_m$);

base.Opt_1 tryGet$mut(long i_m$);

base.iter.Iter_1 iter$read();

base.iter.Iter_1 iter$imm();

base.iter.Iter_1 iter$mut();

base.Bool_0 isEmpty$read();

base.LList_1 tail$imm();

base.LList_1 tail$read();

base.LList_1 tail$mut();

base.Opt_1 head$imm();

base.Opt_1 head$read();

base.Opt_1 head$mut();

base.List_1 list$mut();

base.flows.FlowOp_1 _flowread$read();

base.LList_1 $plus$plus$imm(base.LList_1 l1_m$);

base.LList_1 $plus$plus$read(base.LList_1 l1_m$);

base.LList_1 $plus$plus$mut(base.LList_1 l1_m$);

Object get$imm(long i_m$);

Object get$read(long i_m$);

Object get$mut(long i_m$);

Object match$read(base.LListMatchRead_2 m_m$);

Object match$mut(base.LListMatch_2 m_m$);

base.flows.Flow_1 flow$imm();

base.flows.Flow_1 flow$read();

base.flows.Flow_1 flow$mut();

base.flows.FlowOp_1 _flowimm$imm();

Long size$read();

base.LList_1 $plus$imm(Object e_m$);

base.LList_1 $plus$read(Object e_m$);

base.LList_1 $plus$mut(Object e_m$);

base.LList_1 pushFront$read(Object e_m$);

base.LList_1 pushFront$mut(Object e_m$);
static Object match$mut$fun(base.LListMatch_2 m_m$, base.LList_1 $this) {
  return (switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (Object) null;
}})
;
}

static Object match$read$fun(base.LListMatchRead_2 m_m$, base.LList_1 $this) {
  return (switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (Object) null;
}})
;
}

static Object get$mut$fun(long i_m$, base.LList_1 $this) {
  return (switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (Object) null;
}})
;
}

static Object get$read$fun(long i_m$, base.LList_1 $this) {
  return ((Object)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (Object) null;
}})
);
}

static Object get$imm$fun(long i_m$, base.LList_1 $this) {
  return ((Object)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (Object) null;
}})
);
}

static base.Opt_1 tryGet$mut$fun(long i_m$, base.LList_1 $this) {
  return ((base.Opt_1)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.Opt_1) null;
}})
);
}

static base.Opt_1 tryGet$read$fun(long i_m$, base.LList_1 $this) {
  return ((base.Opt_1)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.Opt_1) null;
}})
);
}

static base.Opt_1 tryGet$imm$fun(long i_m$, base.LList_1 $this) {
  return ((base.Opt_1)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.Opt_1) null;
}})
);
}

static base.Opt_1 head$mut$fun(base.LList_1 $this) {
  return ((base.Opt_1)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.Opt_1) null;
}})
);
}

static base.Opt_1 head$read$fun(base.LList_1 $this) {
  return ((base.Opt_1)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.Opt_1) null;
}})
);
}

static base.Opt_1 head$imm$fun(base.LList_1 $this) {
  return ((base.Opt_1)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.Opt_1) null;
}})
);
}

static base.LList_1 tail$mut$fun(base.LList_1 $this) {
  return ((base.LList_1)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.LList_1) null;
}})
);
}

static base.LList_1 tail$read$fun(base.LList_1 $this) {
  return ((base.LList_1)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.LList_1) null;
}})
);
}

static base.LList_1 tail$imm$fun(base.LList_1 $this) {
  return ((base.LList_1)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.LList_1) null;
}})
);
}

static base.LList_1 $plus$plus$mut$fun(base.LList_1 l1_m$, base.LList_1 $this) {
  return ((base.LList_1)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.LList_1) null;
}})
);
}

static base.LList_1 $plus$plus$read$fun(base.LList_1 l1_m$, base.LList_1 $this) {
  return ((base.LList_1)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.LList_1) null;
}})
);
}

static base.LList_1 $plus$plus$imm$fun(base.LList_1 l1_m$, base.LList_1 $this) {
  return ((base.LList_1)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.LList_1) null;
}})
);
}

static base.LList_1 $plus$mut$fun(Object e_m$, base.LList_1 $this) {
  return ((base.LList_1)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.LList_1) null;
}})
);
}

static base.LList_1 $plus$read$fun(Object e_m$, base.LList_1 $this) {
  return ((base.LList_1)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.LList_1) null;
}})
);
}

static base.LList_1 $plus$imm$fun(Object e_m$, base.LList_1 $this) {
  return ((base.LList_1)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.LList_1) null;
}})
);
}

static base.iter.Iter_1 iter$mut$fun(base.LList_1 $this) {
  return ((base.iter.Iter_1)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.iter.Iter_1) null;
}})
);
}

static base.iter.Iter_1 iter$imm$fun(base.LList_1 $this) {
  return ((base.iter.Iter_1)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.iter.Iter_1) null;
}})
);
}

static base.iter.Iter_1 iter$read$fun(base.LList_1 $this) {
  return ((base.iter.Iter_1)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.iter.Iter_1) null;
}})
);
}

static base.flows.Flow_1 flow$mut$fun(base.LList_1 $this) {
  return ((base.flows.Flow_1)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.flows.Flow_1) null;
}})
);
}

static base.flows.Flow_1 flow$read$fun(base.LList_1 $this) {
  return ((base.flows.Flow_1)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.flows.Flow_1) null;
}})
);
}

static base.flows.FlowOp_1 _flowread$read$fun(base.LList_1 $this) {
  return ((base.flows.FlowOp_1)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.flows.FlowOp_1) null;
}})
);
}

static base.flows.Flow_1 flow$imm$fun(base.LList_1 $this) {
  return ((base.flows.Flow_1)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.flows.Flow_1) null;
}})
);
}

static base.flows.FlowOp_1 _flowimm$imm$fun(base.LList_1 $this) {
  return ((base.flows.FlowOp_1)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.flows.FlowOp_1) null;
}})
);
}

static base.Bool_0 isEmpty$read$fun(base.LList_1 $this) {
  return ((base.Bool_0)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.Bool_0) null;
}})
);
}

static Long size$read$fun(base.LList_1 $this) {
  return ((long)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (long) 0;
}})
);
}

static base.List_1 list$mut$fun(base.LList_1 $this) {
  return ((base.List_1)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.List_1) null;
}})
);
}

static base.LList_1 pushFront$mut$fun(Object e_m$, base.LList_1 $this) {
  return ((base.LList_1)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.LList_1) null;
}})
);
}

static base.LList_1 pushFront$read$fun(Object e_m$, base.LList_1 $this) {
  return ((base.LList_1)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.LList_1) null;
}})
);
}
}