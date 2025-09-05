package base;
public record CheapHash_0Impl() implements base.CheapHash_0 {
  public base.Hasher_0 hash$mut(base.ToHash_0 x_m$) {
  return  base.Hasher_0.hash$mut$fun(x_m$, this);
}

public base.Hasher_0 byte$mut(byte x_m$) {
  return  base.CheapHash_0.byte$mut$fun(x_m$, this);
}

public base.Hasher_0 str$mut(rt.Str x_m$) {
  return  base.CheapHash_0.str$mut$fun(x_m$, this);
}

public base.Hasher_0 nat$mut(long x_m$) {
  return  base.CheapHash_0.nat$mut$fun(x_m$, this);
}

public base.Hasher_0 float$mut(double x_m$) {
  return  base.CheapHash_0.float$mut$fun(x_m$, this);
}

public base.Hasher_0 int$mut(long x_m$) {
  return  base.CheapHash_0.int$mut$fun(x_m$, this);
}

public Long compute$mut() {
  return  base.CheapHash_0.compute$mut$fun(this);
}

  
}
