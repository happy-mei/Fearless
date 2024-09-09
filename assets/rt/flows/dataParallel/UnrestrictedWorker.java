package rt.flows.dataParallel;

import base.OptMatch_2;
import base.flows.FlowOp_1;
import base.flows._Sink_1;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

final class UnrestrictedWorker implements Runnable {
  static void forRemaining(FlowOp_1 source, _Sink_1 downstream, int size) {
    // Try to split up the source N_CPU times
    // TODO: we probably want to just do this on some of the dataset to try it out first
    var splitData = new ArrayList<FlowOp_1>();
    int i = 1;
    splitData.add(source);
    for (;; ++i) {
      var split = (FlowOp_1) splitData.get(i - 1).split$mut().match$mut(new OptMatch_2() {
        @Override public Object some$mut(Object split_) {
          return split_;
        }
        @Override public Object empty$mut() {
          return null;
        }
      });
      if (split == null) { break; }
      splitData.add(split);
    }
    int realSize = size >= 0 ? size : i;
    int perWorkerSize = realSize / i;

    var doneSignal = new CountDownLatch(i);
    var workers = new UnrestrictedWorker[i];
    for (int j = 0; j < i; ++j) {
      var subSource = splitData.get(j);
      assert subSource != null;
      var worker = new UnrestrictedWorker(subSource, downstream, perWorkerSize, doneSignal);
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
  public UnrestrictedWorker(FlowOp_1 source, _Sink_1 downstream, int size, CountDownLatch doneSignal) {
    this(
      source,
      downstream,
      size,
      doneSignal,
      // size isn't always going to be the correct answer here but in most cases it will be.
      size >= 0 ? new ArrayList<>(size) : new ArrayList<>()
    );
  }
  public UnrestrictedWorker(FlowOp_1 source, _Sink_1 downstream, int size, CountDownLatch doneSignal, List<Object> buffer) {
    this.source = source;
    this.downstream = new BufferSink(downstream);
    this.doneSignal = doneSignal;
  }

  @Override public void run() {
    source.forRemaining$mut(downstream);
    doneSignal.countDown();
  }
}
