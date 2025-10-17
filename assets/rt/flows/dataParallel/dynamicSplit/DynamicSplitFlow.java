package rt.flows.dataParallel.dynamicSplit;

import base.Info_0;
import base.OptMatch_2;
import base.True_0;
import base.Void_0;
import base.flows.FlowOp_1;
import base.flows._Sink_1;
import rt.FearlessError;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.Phaser;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static rt.flows.FlowCreator.IS_SEQUENTIALISED;

public final class DynamicSplitFlow {
  private final FlowOp_1 upstream;
  private final _Sink_1 originalDownstream;
  private final _Sink_1 downstream;
  private final Phaser sync;
  private final Thread.UncaughtExceptionHandler exceptionHandler;
  private final AtomicBoolean isRunning;
  private final Deque<Object> es;
  private final Deque<Thread> splitResults;
  private Thread prev;

  public DynamicSplitFlow(
    FlowOp_1 upstream,
    _Sink_1 originalDownstream,
    Phaser sync,
    AtomicBoolean isRunning,
    Thread.UncaughtExceptionHandler exceptionHandler,
    Thread prev
  ) {
    sync.register();
    this.upstream = upstream;
    this.originalDownstream = originalDownstream;
    this.sync = sync;
    this.isRunning = isRunning;
    this.exceptionHandler = exceptionHandler;
    this.es = new ArrayDeque<>();
    this.splitResults = new ArrayDeque<>();
    this.downstream = new _Sink_1() {
      @Override public Void_0 stopDown$mut() {
        return Void_0.$self;
      }
      @Override public Void_0 pushError$mut(Info_0 info_m$) {
        es.add(new Error(info_m$));
        return Void_0.$self;
      }
      @Override public Void_0 $hash$mut(Object x_m$) {
        es.add(x_m$);
        return Void_0.$self;
      }
    };
    this.prev = prev;
  }

  public static void for_(FlowOp_1 source, _Sink_1 downstream, AtomicBoolean isRunning) {
    AtomicReference<RuntimeException> exception = new AtomicReference<>();
    final Thread.UncaughtExceptionHandler handler = (_,err) -> {
      var message = err.getMessage();
      if (err instanceof StackOverflowError) { message = "Stack overflowed"; }
      exception.compareAndSet(null, new RuntimeException(message, err));
    };
    var sync = new Phaser(1);
    var res = new DynamicSplitFlow(source, downstream, sync, isRunning, handler, null);
    res.run();
    sync.arriveAndAwaitAdvance();
    flattenResults(res, downstream);
    downstream.stopDown$mut();
    var actualException = exception.get();
    if (actualException != null) {
      throw actualException;
    }
  }

  private static void flattenResults(DynamicSplitFlow res, _Sink_1 downstream) {
    while (!res.es.isEmpty()) {
      var e = res.es.removeFirst();
      if (e instanceof CheckSplitResult(DynamicSplitFlow split)) {
        var worker = res.splitResults.pollFirst();
        assert worker != null;
        try {
          worker.join();
        } catch (InterruptedException ex) {
          throw new RuntimeException(ex);
        }
        assert split != res;
        flattenResults(split, downstream);
        continue;
      }
      if (e instanceof Error(Info_0 info)) {
        downstream.pushError$mut(info);
        break;
      }
      try {
        downstream.$hash$mut(e);
      } catch (FearlessError err) {
        downstream.pushError$mut(err.info);
        break;
      } catch (ArithmeticException err) {
        downstream.pushError$mut(base.Infos_0.$self.msg$imm(rt.Str.fromJavaStr(err.getMessage())));
        break;
      }
    }
  }

  public void run() {
    try {
      if (!isRunning.getPlain()) {
        return;
      }
      var splitTokens = new ArrayDeque<CheckSplitResult>();
      while (upstream.isRunning$mut() == True_0.$self) {
        if (this.prev == null || prev.isAlive()) {
          var rhs = (FlowOp_1) upstream.split$mut().match$mut(new OptMatch_2() {
            @Override public Object some$mut(Object x_m$) { return x_m$; }
            @Override public Object empty$mut() { return null; }
          });
          if (rhs == null) {
            forSeq();
            es.addAll(splitTokens);
            return;
          }
          var rhsWorker = new DynamicSplitFlow(rhs, originalDownstream, sync, isRunning, exceptionHandler, this.prev);
          var rhsRes = Thread.ofVirtual().uncaughtExceptionHandler(exceptionHandler).start(rhsWorker::run);
          this.prev = rhsRes;
          var noFailures = stepSeq();
          splitTokens.addFirst(new CheckSplitResult(rhsWorker));
          splitResults.add(rhsRes);
          if (!noFailures) { break; }
        } else {
          ScopedValue.where(IS_SEQUENTIALISED, null).run(this::forSeq);
        }
      }
      es.addAll(splitTokens);
    } finally {
      sync.arriveAndDeregister();
    }
  }

  private void forSeq() {
    try {
      upstream.for$mut(downstream);
    } catch (FearlessError err) {
      downstream.pushError$mut(err.info);
    } catch (ArithmeticException err) {
      downstream.pushError$mut(base.Infos_0.$self.msg$imm(rt.Str.fromJavaStr(err.getMessage())));
    }
  }
  private boolean stepSeq() {
    try {
      upstream.step$mut(downstream);
      return true;
    } catch (FearlessError err) {
      downstream.pushError$mut(err.info);
      return false;
    } catch (ArithmeticException err) {
      downstream.pushError$mut(base.Infos_0.$self.msg$imm(rt.Str.fromJavaStr(err.getMessage())));
      return false;
    }
  }

  private record CheckSplitResult(DynamicSplitFlow split) {}
  private record Error(Info_0 info) {}
}
