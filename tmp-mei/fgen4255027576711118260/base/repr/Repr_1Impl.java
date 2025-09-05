package base.repr;
public record Repr_1Impl() implements base.repr.Repr_1 {
  public Object look$read(base.F_2 f_m$) {
  return  base.repr.Repr_1.look$read$fun(f_m$, this);
}

public Object cached$read(base.F_2 f_m$) {
  return  base.repr.Repr_1.cached$read$fun(f_m$, this);
}

public Object mutate$mut(base.F_2 f_m$) {
  return  base.repr.Repr_1.mutate$mut$fun(f_m$, this);
}

public base.Void_0 reset$mut() {
  return  base.repr.Repr_1.reset$mut$fun(this);
}

  
}
