package base.flows;
public interface Flow_1 extends base.Extensible_1,base.Sealed_0,base.flows._NonTerminalOps_1,base.flows._TerminalOps_1{
base.Void_0 forEffect$mut(base.MF_2 f_m$);

base.flows.Flow_1 peek$mut(base.F_2 f_m$);

base.flows.Flow_1 peek$mut(base.ToIso_1 ctx_m$, base.F_3 f_m$);

base.flows.Flow_1 actorMut$mut(Object state_m$, base.flows.ActorImplMut_3 f_m$);

base.flows.Flow_1 self$imm();

base.flows.Flow_1 self$read();

base.flows.Flow_1 self$mut();

base.flows.Flow_1 assumeFinite$mut();

Object let$mut(base.F_2 x_m$, base.Continuation_3 cont_m$);

base.flows.Flow_1 actor$mut(Object state_m$, base.flows.ActorImpl_3 f_m$);

base.Opt_1 first$mut();

base.List_1 list$mut();

base.Bool_0 all$mut(base.F_2 predicate_m$);

base.flows.Flow_1 filter$mut(base.F_2 predicate_m$);

base.Bool_0 none$mut(base.F_2 predicate_m$);

base.flows.Flow_1 flatMap$mut(base.F_2 f_m$);

base.Opt_1 opt$mut();

base.Opt_1 findMap$mut(base.F_2 f_m$);

Long count$mut();

Object get$mut();

Object join$mut(base.flows.Joinable_1 j_m$);

base.Opt_1 last$mut();

base.flows.FlowOp_1 unwrapOp$mut(base.flows._UnwrapFlowToken_0 unwrap_m$);

Object fold$mut(base.MF_1 acc_m$, base.F_3 f_m$);

base.flows.Flow_1 mapFilter$mut(base.F_2 f_m$);

base.Action_1 only$mut();

base.Opt_1 first$mut(base.F_2 predicate_m$);

base.flows.Flow_1 map$mut(base.ToIso_1 ctx_m$, base.F_3 f_m$);

base.flows.Flow_1 map$mut(base.F_2 f_m$);

base.flows.Flow_1 limit$mut(long n_m$);

base.Bool_0 any$mut(base.F_2 predicate_m$);

base.Opt_1 find$mut(base.F_2 predicate_m$);

base.Opt_1 size$read();

base.Opt_1 max$mut(base.F_3 compare_m$);

base.flows.Flow_1 scan$mut(Object acc_m$, base.F_3 f_m$);

base.Void_0 for$mut(base.F_2 f_m$);

Object $hash$mut(base.Extension_2 ext_m$);

Object $hash$read(base.Extension_2 ext_m$);

Object $hash$imm(base.Extension_2 ext_m$);
static base.flows.Flow_1 self$mut$fun(base.flows.Flow_1 $this) {
  return ((base.flows.Flow_1)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.flows.Flow_1) null;
}})
);
}

static base.flows.Flow_1 self$read$fun(base.flows.Flow_1 $this) {
  return ((base.flows.Flow_1)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.flows.Flow_1) null;
}})
);
}

static base.flows.Flow_1 self$imm$fun(base.flows.Flow_1 $this) {
  return ((base.flows.Flow_1)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.flows.Flow_1) null;
}})
);
}

static Object let$mut$fun(base.F_2 x_m$, base.Continuation_3 cont_m$, base.flows.Flow_1 $this) {
  return (switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (Object) null;
}})
;
}

static Object join$mut$fun(base.flows.Joinable_1 j_m$, base.flows.Flow_1 $this) {
  return (switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (Object) null;
}})
;
}

static base.Action_1 only$mut$fun(base.flows.Flow_1 $this) {
  return ((base.Action_1)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.Action_1) null;
}})
);
}

static Object get$mut$fun(base.flows.Flow_1 $this) {
  return (switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (Object) null;
}})
;
}

static base.Opt_1 opt$mut$fun(base.flows.Flow_1 $this) {
  return ((base.Opt_1)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.Opt_1) null;
}})
);
}
}