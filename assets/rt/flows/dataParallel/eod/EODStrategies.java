package rt.flows.dataParallel.eod;

import base.flows.FlowOp_1;
import base.flows._Sink_1;
import rt.flows.dataParallel.BufferSink;
import rt.flows.dataParallel.DelayedStopSink;
import rt.flows.dataParallel.ParallelStrategies;

import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public record EODStrategies(_Sink_1 downstream, int size, List<FlowOp_1> splitData, int nTasks) implements ParallelStrategies {
  private static final int TASKS_PER_CORE = 5;
  private static final int N_CPUS = Runtime.getRuntime().availableProcessors();
  public static final int PARALLELISM_POTENTIAL = TASKS_PER_CORE * N_CPUS;
//  public static final int PARALLELISM_POTENTIAL = 4;
  @SuppressWarnings("preview")
  public static final ScopedValue<AtomicInteger> INFO = ScopedValue.newInstance();
  private static final Semaphore AVAILABLE_PARALLELISM = new Semaphore(PARALLELISM_POTENTIAL);
  private static final AtomicLong waitingTasks = new AtomicLong();

  @Override public void seqOnly() {
    var sink = new DelayedStopSink(downstream);
    for (var data : splitData) {
      if (data == null) { break; }
      data.forRemaining$mut(sink);
    }
    sink.stop();
  }

  // TODO: exception handling and potentially make this bounded
  @Override public void oneParOneSeq() {
    throw new RuntimeException("TODO");
//    var lhsSink = new BufferSink(downstream);
//    var rhsSink = new BufferSink(downstream);
//    var lhs = Thread.ofVirtual().start(()->splitData.getFirst().forRemaining$mut(lhsSink));
//    splitData.stream()
//      .skip(1)
//      .forEachOrdered(rhs->rhs.forRemaining$mut(rhsSink));
//    try {
//      lhs.join();
//    } catch (InterruptedException e) {
//      throw new RuntimeException(e); // should never happen with a virtual thread
//    }
//    lhsSink.flush();
//    rhsSink.flush();
  }

  @Override public void manyPar() {
    int perWorkerSize = size / nTasks;
    if (Thread.currentThread().isVirtual()) {
      releaseParentPermit();
    }

    var willParallelise = true;
    if (!AVAILABLE_PARALLELISM.tryAcquire(nTasks)) {
      //TODO: for some reason if we ever run seq here instead of blocking we can end up having unbounded concurrency when availability > 1
      if (waitingTasks.getPlain() < 10) {
        waitingTasks.getAndIncrement();
        AVAILABLE_PARALLELISM.acquireUninterruptibly(nTasks);
        waitingTasks.getAndDecrement();
      } else {
        willParallelise = false;
      }
    }
    final var parallelTasks = willParallelise ? nTasks: 0;
    var permits = new AtomicInteger(parallelTasks);

    var workers = new EODWorker[nTasks];
    var spawned = new Thread[nTasks];
    AtomicReference<RuntimeException> exception = new AtomicReference<>();
    final Thread.UncaughtExceptionHandler handler = (_,err) -> {
      var message = err.getMessage();
      if (err instanceof StackOverflowError) { message = "Stack overflowed"; }
      exception.compareAndSet(null, new RuntimeException(message, err));
    };
    var flusher = BufferSink.FlushWorker.start(handler);
    for (int i = 0; i < nTasks; ++i) {
      var actualException = exception.getAcquire();
      if (actualException != null) {
        throw actualException;
      }
      var subSource = splitData.get(i);
      var worker = new EODWorker(subSource, downstream, permits, perWorkerSize, flusher);
      if (willParallelise) {
        spawned[i] = Thread.ofVirtual().uncaughtExceptionHandler(handler).start(worker);
      } else {
        worker.run();
      }
      workers[i] = worker;
    }

    for (int i = 0; i < nTasks; ++i) {
      var worker = workers[i];
      if (worker == null) { break; }
      var thread = spawned[i];

      if (thread != null) {
        try {
          thread.join();
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      }

      var actualException = exception.get();
      if (actualException != null) {
        throw actualException;
      }
    }

    while (permits.get() > 0) {
      tryReleaseAll(permits);
    }

    flusher.stop(downstream);
    var actualException = exception.get();
    if (actualException != null) {
      throw actualException;
    }
  }

  /**
   * If we're entering new data-parallelism within a DP worker already, we need to release our parents permits
   * to prevent deadlocks.
   */
  @SuppressWarnings("preview")
  private static void releaseParentPermit() {
    assert Thread.currentThread().isVirtual();
    if (!INFO.isBound()) { return; }
    var info = INFO.get();
    tryReleaseAll(info);
  }

  private static void tryReleaseAll(AtomicInteger permits) {
    int remaining;
    do {
      remaining = permits.get();
      assert remaining >= 0;
      if (remaining == 0) { return; }
    } while (!permits.compareAndSet(remaining, 0));
    AVAILABLE_PARALLELISM.release(remaining);
  }
}
