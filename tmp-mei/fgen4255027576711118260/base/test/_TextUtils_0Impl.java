package base.test;
public record _TextUtils_0Impl() implements base.test._TextUtils_0 {
  public rt.Str status$imm(base.test.TestResultKind_0 kind_m$) {
  return  base.test._TextUtils_0.status$imm$fun(kind_m$, this);
}

public base.flows.Flow_1 results$imm(base.List_1 results_m$) {
  return  base.test._TextUtils_0.results$imm$fun(results_m$, this);
}

public rt.Str suite$imm(base.test.TestResults_0 suite_m$, long depth_m$) {
  return  base.test._TextUtils_0.suite$imm$fun(suite_m$, depth_m$, this);
}

public rt.Str heading$imm(rt.Str title_m$, long depth_m$) {
  return  base.test._TextUtils_0.heading$imm$fun(title_m$, depth_m$, this);
}

  
}
