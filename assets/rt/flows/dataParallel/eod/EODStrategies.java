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
  public static final Semaphore AVAILABLE_PARALLELISM = new Semaphore(PARALLELISM_POTENTIAL);

  @Override public void seqOnly() {
    var sink = new DelayedStopSink(downstream);
    for (var data : splitData) {
      if (data == null) { break; }
      data.forRemaining$mut(sink);
    }
    sink.stop();
  }

  @Override public void oneParOneSeq() {
    manyPar();
  }

  @Override public void manyPar() {
    assert nTasks > 1;
    int perWorkerSize = size / nTasks;

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
      var actualException = exception.get();
      if (actualException != null) {
        throw actualException;
      }
      var subSource = splitData.get(i);
      var worker = new EODWorker(subSource, downstream, perWorkerSize, flusher);
      if (i == nTasks - 1) {
        worker.run();
        workers[i] = worker;
        break;
      }

      var willParallelise = AVAILABLE_PARALLELISM.tryAcquire();
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
        AVAILABLE_PARALLELISM.release();
      }

      var actualException = exception.get();
      if (actualException != null) {
        throw actualException;
      }
    }

    flusher.stop(downstream);
    var actualException = exception.get();
    if (actualException != null) {
      throw actualException;
    }
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
