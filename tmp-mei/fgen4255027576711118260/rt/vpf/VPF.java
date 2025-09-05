package rt.vpf;

import rt.FearlessError;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public final class VPF {
  private static ExecutorService executor;
  private static volatile boolean heartbeat = true;
  private static final AtomicLong running = new AtomicLong(0);
  private static final ScheduledExecutorService heartbeatSideEffects = Executors.newSingleThreadScheduledExecutor();
  private static long HEARTBEAT_INTERVAL;
  private static boolean heartbeatEffectsEnabled;
//  private static final long MAX_TASKS = ConfigureVPF.getTasksPerCPU(Runtime.getRuntime().availableProcessors());

  public static Runnable start(long heartbeatInterval, boolean enableHBEffects) {
    assert running.get() == 0 : running.get();
    VPF.executor = Executors.newVirtualThreadPerTaskExecutor();
    HEARTBEAT_INTERVAL = heartbeatInterval;
    heartbeatEffectsEnabled = enableHBEffects;

    var scheduleExecutor = Executors.newSingleThreadScheduledExecutor();
    if (heartbeatInterval > 0) {
      scheduleExecutor.scheduleAtFixedRate(VPF::beat, heartbeatInterval, heartbeatInterval, TimeUnit.NANOSECONDS);
    }
    return () -> {
      scheduleExecutor.shutdown();
      heartbeatSideEffects.shutdown();
      VPF.executor.shutdown();
      heartbeat = false;
    };
  }
  public static void onHeartbeat(Runnable r) {
    if (!heartbeatEffectsEnabled || HEARTBEAT_INTERVAL <= 0) { return; }
    heartbeatSideEffects.scheduleAtFixedRate(r, HEARTBEAT_INTERVAL, HEARTBEAT_INTERVAL, TimeUnit.NANOSECONDS);
  }
  public static boolean shouldSpawn() {
    if (!heartbeat) {
      return false;
    }
    heartbeat = false;
    return true;
  }
  public static <R> Future<R> spawn(Callable<R> task) {
    var res = executor.submit(task);
//    if (MAX_TASKS >= 0) { running.incrementAndGet(); }
    return res;
  }
  public static void spawnDirect(Runnable task, Thread.UncaughtExceptionHandler handler) {
    Thread.ofVirtual().uncaughtExceptionHandler(handler).start(task);
//    if (MAX_TASKS >= 0) { running.incrementAndGet(); }
  }
  public static <R> R join(Future<R> future) {
    try {
//      if (MAX_TASKS >= 0) { running.decrementAndGet(); }
      return future.get();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    } catch (ExecutionException e) {
      switch (e.getCause()) {
        case FearlessError fe -> throw fe;
        case RuntimeException re -> throw re;
        default -> throw new RuntimeException(e.getCause());
      }
    }
  }

  private static void beat() {
//    if (MAX_TASKS >= 0 && running.getPlain() > MAX_TASKS) {
//      return;
//    }
    heartbeat = true;
  }
}
