package base.rng;
public record FRandom_0Impl() implements base.rng.FRandom_0 {
  public base.rng.Random_0 $hash$read(Object a_m$) {
  return this.$hash$read$Delegate((long) a_m$);
}

public base.rng.Random_0 $hash$read$Delegate(long seed_m$) {
  return  base.rng.FRandom_0.$hash$read$fun(seed_m$, this);
}


  
}
