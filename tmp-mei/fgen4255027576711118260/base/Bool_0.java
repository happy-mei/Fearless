package base;
public interface Bool_0 extends base.Sealed_0,base.Stringable_0,base.ToImm_1{
rt.Str str$read();

Object if$imm(base.ThenElse_1 f_m$);

base.Bool_0 $ampersand$ampersand$imm(base.MF_1 b_m$);

Object $question$imm(base.ThenElse_1 f_m$);

base.Bool_0 $pipe$pipe$imm(base.MF_1 b_m$);

base.Bool_0 $pipe$imm(base.Bool_0 b_m$);

Object match$imm(base.BoolMatch_1 m_m$);

base.Bool_0 not$imm();

base.Bool_0 or$imm(base.Bool_0 b_m$);

base.Bool_0 $ampersand$imm(base.Bool_0 b_m$);

base.Bool_0 and$imm(base.Bool_0 b_m$);

base.Bool_0 toImm$read();
static base.Bool_0 $ampersand$imm$fun(base.Bool_0 b_m$, base.Bool_0 $this) {
  return ((base.Bool_0)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.Bool_0) null;
}})
);
}

static base.Bool_0 $pipe$imm$fun(base.Bool_0 b_m$, base.Bool_0 $this) {
  return ((base.Bool_0)(switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (base.Bool_0) null;
}})
);
}

static Object $question$imm$fun(base.ThenElse_1 f_m$, base.Bool_0 $this) {
  return (switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (Object) null;
}})
;
}

static Object match$imm$fun(base.BoolMatch_1 m_m$, base.Bool_0 $this) {
  return (switch (1) { default -> {
  System.err.println("Program aborted at:\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\n")));
  System.exit(1);
  yield (Object) null;
}})
;
}
}