package base.test;
public record FTestResults_0Impl() implements base.test.FTestResults_0 {
  public base.test.TestResults_0 $hash$read(Object a_m$, Object b_m$, Object c_m$) {
  return this.$hash$read$Delegate((rt.Str) a_m$, (base.List_1) b_m$, (base.List_1) c_m$);
}

public base.test.TestResults_0 $hash$read$Delegate(rt.Str suiteTitle_m$, base.List_1 results_m$, base.List_1 nested_m$) {
  return  base.test.FTestResults_0.$hash$read$fun(suiteTitle_m$, results_m$, nested_m$, this);
}


  
}
