package rt;

import java.util.function.Consumer;

/*
  The plan:
  1. Magic .step in actor flow-ops to be a non blocking dispatch to a subject
  2. Magic .stop in actor flows to dispatch a stop message (and maybe .join on the signal?)
  ...
  Step ??: Profit!
 */

public interface PipelineParallelFlow {
  final class WrappedSinkK implements base.flows._PipelineParallelSink_0 {
    public static WrappedSinkK $self = new WrappedSinkK();
    // TODO: sink id is not actually used, just here to make debugging easier during development
    static long SINK_ID = 0;
    @Override public base.flows._Sink_1 $hash$imm(base.flows._Sink_1 original) {
//      return original;
      return new WrappedSink(SINK_ID++, original);
    }
  }
  final class WrappedSink implements base.flows._PipelineParallelSink_1 {
    final long subjectId;
    final base.flows._Sink_1 original;
    FlowRuntime.Subject<Object> subject;
    Throwable exception;
    public WrappedSink(long subjectId, base.flows._Sink_1 original) {
      this.subjectId = subjectId;
      this.original = original;

//      System.out.println("SPAWNING SUBJ: "+subjectId);
//      this.subject = spawn(msg -> {
//        System.out.println("SUBJ: "+subjectId+", Message received: "+msg);
//        original.$35$mut$(msg);
//      }, () -> {
//        System.out.println("Stop received (SUBJ "+subjectId+")");
//        original.stop$mut$();
//      });
      this.subject = spawn(original::$hash$mut, original::pushError$mut, original::stop$mut);
    }

    @Override public base.Void_0 stop$mut() {
//      System.out.println("Stopping subj "+subjectId);
      if (!subject.ref().isClosed()) {
        subject.stop();
        subject.signal()
          .exceptionally(t -> {
            exception = t;
            return null;
          })
          .join();
        if (exception != null) {
          var message = exception.getMessage();
          if (exception instanceof StackOverflowError) { message = "Stack overflowed"; }
          throw new RuntimeException(message, exception);
        }
      }
      return base.Void_0.$self;
    }
    @Override public base.Void_0 $hash$mut(Object x$) {
//      System.out.println("SUBJ: "+subjectId+" GOT MSG: "+x$);
      this.subject.ref().submit(new FlowRuntime.Message.Data<>(x$));
      return base.Void_0.$self;
    }
    @Override public base.Void_0 pushError$mut(base.Info_0 info$) {
      this.subject.ref().submit(new FlowRuntime.Message.Error<>(info$));
      return base.Void_0.$self;
    }
  }

  static <E> FlowRuntime.Subject<E> spawn(Consumer<E> downstreamData, Consumer<base.Info_0> downstreamErrors, Runnable stop) {
    var self = FlowRuntime.<E>spawnWorker();
    return new FlowRuntime.Subject<>(self, self.consume(msg->{
      switch (msg) {
        case FlowRuntime.Message.Data<E> data -> {
          try {
            downstreamData.accept(data.data());
          } catch (FearlessError err) {
            downstreamErrors.accept(err.info);
          } catch (Throwable t) {
            throw t;
          }
        }
        case FlowRuntime.Message.Error<E> info -> {
          downstreamErrors.accept(info.info());
        }
        case FlowRuntime.Message.Stop<E> ignored -> {
          stop.run();
          self.close();
        }
      }
    }));
  }
}