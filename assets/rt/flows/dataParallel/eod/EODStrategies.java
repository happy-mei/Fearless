package rt.flows.dataParallel.eod;

import base.flows.FlowOp_1;
import base.flows._Sink_1;
import rt.flows.dataParallel.BufferSink;
import rt.flows.dataParallel.DelayedStopSink;
import rt.flows.dataParallel.ParallelStrategies;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public record EODStrategies(_Sink_1 downstream, int size, List<FlowOp_1> splitData, int nTasks) implements ParallelStrategies {
  private static final int TASKS_PER_CORE = 5;
  private static final int N_CPUS = Runtime.getRuntime().availableProcessors();
  public static final int PARALLELISM_POTENTIAL = TASKS_PER_CORE * N_CPUS;
  //  private static final int PARALLELISM_POTENTIAL = 4;
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

  @Override public void oneParOneSeq() {
    var lhsSink = new BufferSink(downstream, new ArrayList<>(1));
    var rhsSink = new BufferSink(downstream, new ArrayList<>(1));
    var lhs = Thread.ofVirtual().start(()->splitData.getFirst().forRemaining$mut(lhsSink));
    splitData.stream()
      .skip(1)
      .forEachOrdered(rhs->rhs.forRemaining$mut(rhsSink));
    try {
      lhs.join();
    } catch (InterruptedException e) {
      throw new RuntimeException(e); // should never happen with a virtual thread
    }
    lhsSink.flush();
    rhsSink.flush();
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

    var doneSignal = new CountDownLatch(nTasks);
    var workers = new EODWorker[nTasks];
    for (int j = 0; j < nTasks; ++j) {
      var subSource = splitData.get(j);
      var worker = new EODWorker(subSource, downstream, perWorkerSize, doneSignal, permits);
      if (willParallelise) {
        Thread.ofVirtual().start(worker);
      } else {
        worker.run();
      }
      workers[j] = worker;
    }
    try {
      doneSignal.await();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
    while (permits.get() > 0) {
      tryReleaseAll(permits);
    }
    for (var worker : workers) {
      if (worker == null) { break; }
      worker.flush();
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
