package rt;

import base.*;

import java.util.function.BiFunction;

public final class LinkedHashMap implements LinkedHashMap_2 {
  private final java.util.LinkedHashMap<Object, Object> inner = new java.util.LinkedHashMap<>();
  private final F_2 hashFn;
  private final F_3 keyEq;
  public LinkedHashMap(F_3 keyEq, F_2 hashFn) {
    this.hashFn = hashFn;
    this.keyEq = keyEq;
  }

  @Override public Opt_1 get$read(Object k_m$) {
    var res = inner.get(keyOf(k_m$));
    if (res == null) { return Opt_1.$self; }
    return Opts_0.$self.$hash$imm(res);
  }
  @Override public Opt_1 get$imm(Object k_m$) {
    return get$read(k_m$);
  }
  @Override public Opt_1 get$mut(Object k_m$) {
    return get$read(k_m$);
  }

  @Override public LinkedHashMap_2 $plus$mut(Object k_m$, Object v_m$) {
    inner.put(keyOf(k_m$), v_m$);
    return this;
  }
  @Override public Opt_1 remove$mut(Object k_m$) {
    var res = inner.remove(keyOf(k_m$));
    if (res == null) { return Opt_1.$self; }
    return Opts_0.$self.$hash$imm(res);
  }
  @Override public Void_0 clear$mut() {
    inner.clear();
    return Void_0.$self;
  }
  @Override public Bool_0 keyEq$read(Object k1_m$, Object k2_m$) {
    return (Bool_0)keyEq.$hash$read(k1_m$, k2_m$);
  }
  @Override public Bool_0 isEmpty$read() {
    return inner.isEmpty() ? True_0.$self : False_0.$self;
  }
  @Override public Void_0 put$mut(Object k_m$, Object v_m$) {
    return LinkedHashMap_2.put$mut$fun(k_m$, v_m$, this);
  }

  private Key keyOf(Object k) {
    return Key.of(k, hashFn, this::keyEq$read);
  }
  private record Key(Object k, ToHash_0 hashFn, BiFunction<Object,Object,Bool_0> keyEqFn) {
    public static Key of(Object k, F_2 hashFn, BiFunction<Object,Object,Bool_0> keyEqFn) {
      return new Key(k, (ToHash_0)hashFn.$hash$read(k), keyEqFn);
    }
    @Override public int hashCode() {
      return Math.toIntExact(hashFn.hash$read(new rt.CheapHash()).compute$mut());
    }
    @Override public boolean equals(Object o) {
      assert o instanceof Key;
      return keyEqFn.apply(this.k, ((Key)o).k) == True_0.$self;
    }
  }
}
