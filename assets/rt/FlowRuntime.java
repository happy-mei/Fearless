package rt;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SubmissionPublisher;
import java.util.function.Function;

public interface FlowRuntime {
  int BUFFER_SIZE = 256;
  ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

  static <E> SubmissionPublisher<Message<E>> spawnWorker() {
    return new SubmissionPublisher<>(executor, BUFFER_SIZE);
  }

  //  // TODO: I might not need this wrapper because I may always want to call ref.consume(..) where I would use signal.
  final class Subject<E> implements AutoCloseable {
    private final SubmissionPublisher<Message<E>> ref;
    private final CompletableFuture<Void> signal;
    private Throwable hasThrown;
    public Subject(SubmissionPublisher<Message<E>> ref, Function<Subject<E>, CompletableFuture<Void>> signal) {
      this.ref = ref;
      this.signal = signal.apply(this);
    }

    @Override
    public void close() {
      ref.close();
    }

    @SuppressWarnings("unchecked")
    public void stop() {
      ref.submit(Message.Stop.INSTANCE);
    }

    public SubmissionPublisher<Message<E>> ref() { return ref; }
    public CompletableFuture<Void> signal() { return signal; }

    public boolean hasThrown() { return hasThrown != null; }
    public void hasThrown(Throwable t) { this.hasThrown = t; }
  }

  sealed interface Message<E> {
    record Data<E>(E data) implements Message<E> {}
    record Stop<E>() implements Message<E> {
      @SuppressWarnings("rawtypes")
      static final Stop INSTANCE = new Stop<>();
    }
  }
}
