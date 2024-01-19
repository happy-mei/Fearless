package rt;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SubmissionPublisher;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public final class FlowRuntime {
  public static final int BUFFER_SIZE = 256;
  private static final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

  // TODO: I might not need this wrapper because I may always want to call ref.consume(..) where I would use signal.
  public record Subject<E>(SubmissionPublisher<Message<E>> ref, CompletableFuture<Void> signal) implements AutoCloseable {
    @Override public void close() {
      ref.close();
    }
  }
  public static <E> Subject<E> spawnSubject() {
    var subject = new SubmissionPublisher<Message<E>>(executor, BUFFER_SIZE);
    // TODO: The default consumer subscriber stops processing new things on exception. I might want to make my own to have different behaviour.
    var signal = subject.consume(msg->{
      switch (msg) {
        case Message.Stop<E> ignored -> {
          System.out.println("stopped");
          subject.close();
        }
        case Message.Data<E> data -> {
          System.out.println("onNext: "+data.data);
          var idk = new Box<>(0L);
          IntStream.range(0, 200_000_000).forEach(i -> idk.update(j -> i + j));
//          throw new Error("throws");
        }
      }
    });
    return new Subject<>(subject, signal);
  }

  public sealed interface Message<E> {
    record Data<E>(E data) implements Message<E> {}
    record Stop<E>() implements Message<E> {
      @SuppressWarnings("rawtypes")
      static final Stop INSTANCE = new Stop<>();
    }
  }

  // TODO remove:
  public static class Box<T> {
    private T inner;

    public static <T> Box<T> of(Supplier<T> f) { return new Box<>(f.get()); }

    public Box(T inner) { this.inner = inner; }

    public T get() { return inner; }
    public void set(T inner) { this.inner = inner; }
    public T update(Function<T, T> f) {
      this.inner = f.apply(this.inner);
      return this.inner;
    }
  }
}
