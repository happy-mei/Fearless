package rt.flows.dataParallel.eod;

import base.OptMatch_2;
import base.flows.FlowOp_1;
import base.flows._Sink_1;
import rt.flows.dataParallel.BufferSink;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static rt.flows.dataParallel.eod.EODStrategies.PARALLELISM_POTENTIAL;

/**
 * EODWorker (Explosive Ordnance Disposal Worker) is a parallelism strategy that will optimistically parallelise tasks
 * until it runs out of capacity, blocking at that point or if there are a number of blocked workers, running
 * sequentially. This strategy aims to maximise parallelism while avoiding explosions of nested parallelism consuming
 * large amounts of memory.
 */
public final class EODWorker implements Runnable {
  public static void forRemaining(FlowOp_1 source, _Sink_1 downstream, int size) {
    var splitData = new FlowOp_1[PARALLELISM_POTENTIAL];
    splitData[0] = source;
    int i = 1;
    for (; i < PARALLELISM_POTENTIAL; ++i) {
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
    final var nTasks = i;
    // TODO: change parallelism strategy based on nTasks (i.e. if it's 2, do a classic fork-join and only run one in parallel)
    int realSize = size >= 0 ? size : nTasks;
    new EODStrategies(downstream, realSize, splitData, nTasks).run(nTasks);
  }


  private final FlowOp_1 source;
  private final BufferSink downstream;
  private final CountDownLatch doneSignal;
  private final AtomicInteger info;

  EODWorker(FlowOp_1 source, _Sink_1 downstream, int size, CountDownLatch doneSignal, AtomicInteger info) {
    this(
      source,
      downstream,
      doneSignal,
      info,
      // size isn't always going to be the correct answer here but in most cases it will be.
      size >= 0 ? new ArrayList<>(size) : new ArrayList<>()
    );
  }
  private EODWorker(FlowOp_1 source, _Sink_1 downstream, CountDownLatch doneSignal, AtomicInteger info, List<Object> buffer) {
    this.source = source;
    this.info = info;
    this.downstream = new BufferSink(downstream, buffer);
    this.doneSignal = doneSignal;
  }

  @SuppressWarnings("preview")
  @Override public void run() {
    ScopedValue
      .where(EODStrategies.INFO, info)
      .run(()->source.forRemaining$mut(downstream));
    doneSignal.countDown();
  }

  public void flush() {
    this.downstream.flush();
  }
//
//  public void release() {
//    if (!HAS_RELEASED.isBound()) {
//      AVAILABLE_PARALLELSIM.release(1);
//    }
//  }
}
