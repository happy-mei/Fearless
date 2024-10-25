package id;

import utils.Bug;

import java.util.Arrays;

public class Normaliser<TT extends Id.Ty> {
  private final int depth;
  private int n = 0;
  public Normaliser(int depth) { this.depth = depth; }

  @SuppressWarnings("unchecked")
  public Id.GX<TT>[] normalisedNames(int n) {
    var res = new Id.GX[n];
    Arrays.setAll(res, this::normalisedName);
    return (Id.GX<TT>[]) res;
  }
  public Id.GX<TT> normalisedName(int ignored) {
    // yes, adding an ignored argument to make this a method reference instead of a lambda has a real
    // performance impact
    if (n + 1 == Integer.MAX_VALUE) { throw Bug.of("Maximum fresh identifier size reached"); }
    return new Id.GX<>("X" + depth + "/" + n++ + "$"); // i.e. X0/2$ for top level, X1/0$ for a nested one
  }
}
