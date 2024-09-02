package rt.flows.pipelineParallel;

import rt.FearlessError;
import rt.FlowRuntime;

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
      this.subject = spawn(original);
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
          if (exception instanceof FearlessError fe) { throw fe; } // TODO: check this with flow semantics
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

  static <E> FlowRuntime.Subject<E> spawn(base.flows._Sink_1 downstream) {
    var self = FlowRuntime.<E>spawnWorker();
    return new FlowRuntime.Subject<>(self, self.consume(new Consumer<FlowRuntime.Message<E>>() {
      private boolean softClosed = false;
      @Override public void accept(FlowRuntime.Message<E> msg) {
        switch (msg) {
          case FlowRuntime.Message.Data<E> data -> {
            if (softClosed) { return; }
            try {
              downstream.$hash$mut(data.data());
            } catch (FearlessError err) {
              // Keep "accepting" new messages, but don't actually do anything with them because we're in an error state
              softClosed = true;
              downstream.pushError$mut(err.info);
            } catch (ArithmeticException err) {
              softClosed = true;
              downstream.pushError$mut(base.Infos_0.$self.msg$imm(rt.Str.fromJavaStr(err.getMessage())));
            }
          }
          case FlowRuntime.Message.Error<E> info -> {
            if (softClosed) { return; }
            softClosed = true;
            downstream.pushError$mut(info.info());
          }
          case FlowRuntime.Message.Stop<E> ignored -> {
            downstream.stop$mut();
            self.close();
          }
        }
      }
    }));
  }
}