package utils;

import java.util.function.Function;

public class Box<T> {
  private T inner;

  public Box(T inner) { this.inner = inner; }

  public T get() { return inner; }
  public void set(T inner) { this.inner = inner; }
  public T update(Function<T, T> f) {
    this.inner = f.apply(this.inner);
    return this.inner;
  }
}
