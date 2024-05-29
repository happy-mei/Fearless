package rt.dataParallel;

import java.util.concurrent.locks.AbstractQueuedSynchronizer;

class ParallelismLimiter {
  private final Sync sync;

  public ParallelismLimiter(int maxParallelism) {
    this.sync = new Sync(maxParallelism);
  }

  public void acquire() {
    acquire(1);
  }

  public void release() {
    release(1);
  }

  public void acquire(int workersNeeded) {
    sync.acquireShared(workersNeeded);
  }

  public void release(int workersUsed) {
    sync.releaseShared(workersUsed);
  }

  private static final class Sync extends AbstractQueuedSynchronizer {
    public Sync(int maxParallelism) {
      setState(maxParallelism);
    }

    @Override protected boolean tryAcquire(int permits) {
      return tryAcquireShared(permits) >= 0;
    }

    @Override protected boolean tryRelease(int permits) {
      return tryReleaseShared(permits);
    }

    @Override protected int tryAcquireShared(int permits) {
      while (true) {
        int available = getState();
        int remaining = available - permits;
        if (remaining < 0 || compareAndSetState(available, remaining)) {
          return remaining;
        }
      }
    }

    @Override protected boolean tryReleaseShared(int permits) {
      for (; ; ) {
        int current = getState();
        int next = current + permits;
        if (next < current) {
          throw new RuntimeException("Maximum permit count exceeded");
        }
        if (compareAndSetState(current, next))
          return true;
      }
    }
  }
}
