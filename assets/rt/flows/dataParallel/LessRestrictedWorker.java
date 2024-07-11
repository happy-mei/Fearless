package rt.flows.dataParallel;

import base.OptMatch_2;
import base.flows.FlowOp_1;
import base.flows._Sink_1;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

final class LessRestrictedWorker implements Runnable {
  private static final int N_CPUS = Runtime.getRuntime().availableProcessors();
  private static final int TASK_SIZE = N_CPUS * 5;

  static void forRemaining(FlowOp_1 source, _Sink_1 downstream, int size) {
    // Try to split up the source N_CPU times
    // TODO: we probably want to just do this on some of the dataset to try it out first
    var splitData = new FlowOp_1[TASK_SIZE];
    int i = 1;
    splitData[0] = source;
    for (; i < TASK_SIZE; ++i) {
      var split = (FlowOp_1) splitData[i - 1].split$mut().match$mut(new OptMatch_2() {
        @Override public Object some$mut(Object split_) {
          return split_;
        }
        @Override public Object empty$mut() {
          return null;
        }
      });
      if (split == null) { break; }
      splitData[i] = split;
    }
    int realSize = size >= 0 ? size : i;
    int perWorkerSize = realSize / i;

    var doneSignal = new CountDownLatch(i);
    var workers = new LessRestrictedWorker[i];
    for (int j = 0; j < i; ++j) {
      var subSource = splitData[j];
      assert subSource != null;
      var worker = new LessRestrictedWorker(subSource, downstream, perWorkerSize, doneSignal);
      Thread.ofVirtual().start(worker);
      workers[j] = worker;
    }
    try {
      doneSignal.await();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
    for (var worker : workers) {
      if (worker == null) { break; }
      worker.downstream.flush();
    }
  }

  private final FlowOp_1 source;
  private final BufferSink downstream;
  private final CountDownLatch doneSignal;
  public LessRestrictedWorker(FlowOp_1 source, _Sink_1 downstream, int size, CountDownLatch doneSignal) {
    this(
      source,
      downstream,
      size,
      doneSignal,
      // size isn't always going to be the correct answer here but in most cases it will be.
      size >= 0 ? new ArrayList<>(size) : new ArrayList<>()
    );
  }
  public LessRestrictedWorker(FlowOp_1 source, _Sink_1 downstream, int size, CountDownLatch doneSignal, List<Object> buffer) {
    this.source = source;
    this.downstream = new BufferSink(downstream, buffer);
    this.doneSignal = doneSignal;
  }

  @Override public void run() {
    source.forRemaining$mut(downstream);
    doneSignal.countDown();
  }
}
