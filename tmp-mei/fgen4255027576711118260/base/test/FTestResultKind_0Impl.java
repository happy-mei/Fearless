package base.test;
public record FTestResultKind_0Impl() implements base.test.FTestResultKind_0 {
  public base.test.TestResultKind_0 passed$imm() {
  return  base.test.FTestResultKind_0.passed$imm$fun(this);
}

public base.test.TestResultKind_0 failed$imm(rt.Str details_m$) {
  return  base.test.FTestResultKind_0.failed$imm$fun(details_m$, this);
}

public base.test.TestResultKind_0 skipped$imm() {
  return  base.test.FTestResultKind_0.skipped$imm$fun(this);
}

public base.test.TestResultKind_0 errored$imm(rt.Str details_m$) {
  return  base.test.FTestResultKind_0.errored$imm$fun(details_m$, this);
}

  
}
