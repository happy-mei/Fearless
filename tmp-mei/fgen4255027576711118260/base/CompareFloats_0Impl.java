package base;
public record CompareFloats_0Impl() implements base.CompareFloats_0 {
  public base.Ordering_0 $hash$read(Object a_m$, Object b_m$) {
  return this.$hash$read$Delegate((double) a_m$, (double) b_m$);
}

public base.Ordering_0 $hash$read$Delegate(double a_m$, double b_m$) {
  return  base.CompareFloats_0.$hash$read$fun(a_m$, b_m$, this);
}


  
}
