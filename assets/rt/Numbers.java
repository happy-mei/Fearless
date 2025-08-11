package rt;

import java.math.BigInteger;

public final class Numbers{
  private Numbers(){}
  public static long pow64(long base, long expBits /* unsigned */){
    long res = 1L;
    for (; expBits != 0; expBits >>>= 1){
      if ((expBits & 1L) != 0) res *= base;
      base *= base;
    }
  return res;
  }

  public static byte pow8(byte base, long expBits){
    return (byte) pow64(base, expBits);
  }

  public static long intSqrt(long x){
    if (x < 0){ throw new ArithmeticException("sqrt of negative Int"); }
    if (x <= 1){ return x; }
    long r= (long) Math.sqrt((double) x);
    for (;;){
      long q= x / r;
      long nr= (r + q) >>> 1;
      if (nr >= r){ break; }
      r = nr;
    }
    long rp1= r + 1;
    if (x / rp1 >= rp1){ r = rp1; }
    return r;
  }
  public static long natSqrt(long u){
    if (u == 0 || u == 1){ return u; }
    double du= (u & Long.MAX_VALUE) + (u < 0 ? 0x1.0p63 : 0.0);
    long r= (long) Math.sqrt(du);
    for (;;){
      long q= Long.divideUnsigned(u, r);
      long nr= (r + q) >>> 1;
      if (nr >= r){ break; }
      r = nr;
    }
    long rp1= r + 1;
    if (Long.compareUnsigned(Long.divideUnsigned(u, rp1), rp1) >= 0){ r = rp1; }
    return r;
  }
  public static byte byteSqrt(byte raw) {
    int x= Byte.toUnsignedInt(raw);
    int r= (int) Math.floor(Math.sqrt((double) x));
    return (byte) r;
  }
  public static base.Bool_0 toBool(boolean b) {
    return b ? base.True_0.$self : base.False_0.$self;
  }
}