package base.test;
public record FTestResult_0Impl() implements base.test.FTestResult_0 {
  public base.test.TestResult_0 $hash$read(Object a_m$, Object b_m$) {
  return this.$hash$read$Delegate((rt.Str) a_m$, (base.test.TestResultKind_0) b_m$);
}

public base.test.TestResult_0 $hash$read$Delegate(rt.Str title_m$, base.test.TestResultKind_0 kind_m$) {
  return  base.test.FTestResult_0.$hash$read$fun(title_m$, kind_m$, this);
}


public base.test.TestResult_0 withInfo$imm(rt.Str title_m$, base.test.TestResultKind_0 kind_m$, base.Opt_1 info_m$) {
  return  base.test.FTestResult_0.withInfo$imm$fun(title_m$, kind_m$, info_m$, this);
}

  
}
