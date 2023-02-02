package id;

import utils.Bug;

import java.util.List;
import java.util.stream.IntStream;

public class Refresher<TT> {
  private final int depth;
  private int n = 0;
  public Refresher(int depth) { this.depth = depth; }

  public List<Id.GX<TT>> freshNames(int n) {
    return IntStream.range(0, n).mapToObj(unused->freshName()).toList();
  }
  public Id.GX<TT> freshName() {
    if (n + 1 == Integer.MAX_VALUE) { throw Bug.of("Maximum fresh identifier size reached"); }
    return new Id.GX<>("X" + depth + "/" + n++ + "$"); // i.e. X0/2$ for top level, X1/0$ for a nested one
  }
}
