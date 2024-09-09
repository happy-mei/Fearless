package rt.flows.dataParallel;

import base.Info_0;
import base.Void_0;
import base.flows._Sink_1;
import rt.FearlessError;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;

public final class BufferSink implements _Sink_1 {
  private final static BlockingQueue<FlusherElement> toFlush = new LinkedBlockingQueue<>();
  private static AtomicReference<RuntimeException> exception = new AtomicReference<>();
  public static final class FlushWorker implements Runnable {
    private FlushWorker() {}
    public static final CompletableFuture<Void> doneSignal = new CompletableFuture<>();
    private static Thread thread;
    public static FlushWorker start(Thread.UncaughtExceptionHandler exceptionHandler) {
      var worker = new FlushWorker();
      thread = Thread.ofVirtual().uncaughtExceptionHandler((_,err) -> {
        err.printStackTrace();
        var message = err.getMessage();
        if (err instanceof StackOverflowError) { message = "Stack overflowed"; }
        exception.compareAndSet(null, new RuntimeException(message, err));
      }).start(worker);
      return worker;
    }
    public void stop() {
      toFlush.add(FlusherElement.StopToken.$self);
      try {
        thread.join();
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
      var actualException = exception.get();
      if (actualException != null) {
        throw actualException;
      }
    }
    @Override public void run() {
      while (!toFlush.isEmpty() || !doneSignal.isDone()) {
        FlusherElement e;try{e = toFlush.take();}
        catch (InterruptedException ex) {throw new RuntimeException(ex);}
        if (e == FlusherElement.StopToken.$self) {
          assert toFlush.isEmpty();
          break;
        }
        if (!(e instanceof FlusherElement.Sink sink)) {
          throw new UnsupportedOperationException("Only stop tokens and buffer sinks are supported.");
        }
        this.flush(sink.sink);
      }
    }
    private void flush(BufferSink sink) {
      // keep flushing if there are elements to flush, or if there might be more elements in the future.
      while (!sink.buffer.isEmpty() || !sink.noMoreElementsSignal.isDone()) {
        Object e;try{e = sink.buffer.take();}
        catch (InterruptedException ex) {throw new RuntimeException(ex);}
        if (e == FlusherElement.StopToken.$self) {
          assert sink.buffer.isEmpty();
          break;
        }

        if (e instanceof Error err) {
          sink.original.pushError$mut(err.info);
          continue;
        }
        try {
          sink.original.$hash$mut(e);
        } catch (FearlessError err) {
          sink.original.pushError$mut(err.info);
        } catch (ArithmeticException err) {
          sink.original.pushError$mut(base.Infos_0.$self.msg$imm(rt.Str.fromJavaStr(err.getMessage())));
        }
      }
      sink.original.stop$mut();
    }
  }

  public final _Sink_1 original;
  private final BlockingQueue<Object> buffer;
  private final CompletableFuture<Void> noMoreElementsSignal = new CompletableFuture<>();
//  private final Thread flusher;

  private record Error(Info_0 info) {}

  public BufferSink(_Sink_1 original) {
    this.original = original;
    this.buffer = new LinkedBlockingQueue<>(5_000_000);
    toFlush.add(new FlusherElement.Sink(this));
//    var onReady = new CompletableFuture<Void>();
//    this.flusher = Thread.ofVirtual().start(new FlushWorker(this, onReady));
//    onReady.join();
//    assert flushLock.isFair();
//    flushLock.lo;
  }

  @Override public Void_0 stop$mut() {
//    return original.stop$mut();
    return Void_0.$self;
  }

  void safePut(Object e) {
    noMoreElementsSignal.complete(null);
    var actualException = exception.get();
    if (actualException != null) {
      throw actualException;
    }
    try {
      buffer.put(e);
    } catch (InterruptedException ex) {
      throw new RuntimeException(ex);
    }
  }

  @Override public Void_0 pushError$mut(Info_0 info_m$) {
    safePut(new Error(info_m$));
    return Void_0.$self;
  }
  @Override public Void_0 $hash$mut(Object x_m$) {
    safePut(x_m$);
    return Void_0.$self;
  }

  public void flush() {
    safePut(FlusherElement.StopToken.$self);
//    try {
//      flusher.join();
//    } catch (InterruptedException e) {
//      throw new RuntimeException(e);
//    }
  }
  sealed interface FlusherElement {
    record StopToken() implements FlusherElement {
      public static final StopToken $self = new StopToken();
    }
    record Sink(BufferSink sink) implements FlusherElement {}
  }
}
