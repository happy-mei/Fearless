package main;

public final class Timer {
  private long time = System.currentTimeMillis();
  public long duration() {
    var old = time;
    this.time = System.currentTimeMillis();
    return this.time - old;
  }
}
