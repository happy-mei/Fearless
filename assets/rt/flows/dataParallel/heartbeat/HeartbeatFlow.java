package rt.flows.dataParallel.heartbeat;

import base.flows.FlowOp_1;
import base.flows._Sink_1;
import rt.flows.dataParallel.BufferSink;
import rt.vpf.VPF;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.IntStream;

public record HeartbeatFlow(_Sink_1 downstream, int size, List<FlowOp_1> splitData, BufferSink.FlushWorker flusher, CountDownLatch sync, Thread.UncaughtExceptionHandler exceptionHandler) implements Runnable {
  private static final int N_CPUS = Runtime.getRuntime().availableProcessors();
  private static final BlockingQueue<HeartbeatFlowWorker> tasks = new LinkedBlockingQueue<>();
  static { start(); }

  @Override public void run() {
    for (int i = 0; i < splitData.size(); ++i) {
      var worker = new HeartbeatFlowWorker(splitData().get(i), downstream, flusher, sync, exceptionHandler);
      if (splitData.size() == 1) {
        worker.run();
        return;
      }

      tasks.add(worker);
//
//      if (VPF.shouldSpawn()) {
//        var next = new HeartbeatFlow(downstream, size, splitData.subList(i+1, splitData.size()), flusher, sync, exceptionHandler);
//        VPF.spawnDirect(next, exceptionHandler);
//        worker.run();
//        return;
//      }
//
//      worker.run();
    }
  }

  public static void start() {
    Thread.ofVirtual().start(()->{
      while (true) {
        HeartbeatFlowWorker task;try{task = tasks.take();}
        catch (InterruptedException e) {throw new RuntimeException(e);}
        try {
          task.run();
        } catch (Throwable e) {
          task.exceptionHandler.uncaughtException(Thread.currentThread(), e);
        }
      }
    });
    IntStream.range(0, N_CPUS).forEach(_ -> {
      VPF.onHeartbeat(()->{
        if (tasks.isEmpty()) {
          Thread.onSpinWait();
          return;
        }
        var task = tasks.poll();
        if (task == null) {
          return;
        }
        VPF.spawnDirect(task, task.exceptionHandler);
      });
    });

//    Thread.ofVirtual().start(() -> {
//      while (true) {
//        if (!VPF.shouldSpawn()) {
//          Thread.onSpinWait();
//          continue;
//        }
//        var task = tasks.poll();
//        if (task == null) {
//          Thread.onSpinWait();
//          continue;
//        }
//        VPF.spawnDirect(task, task.exceptionHandler);
//      }
//    });
  }
}
