package rt;

import utils.Box;

import java.util.concurrent.*;
import java.util.stream.IntStream;

public final class FlowRuntime {
  public static final int BUFFER_SIZE = 256;
  private static final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
//  private

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
}
