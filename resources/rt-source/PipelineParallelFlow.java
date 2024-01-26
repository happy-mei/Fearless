package rt;

import userCode.FProgram;

import java.util.concurrent.SubmissionPublisher;

/*
  The plan:
  1. Magic .step in actor flow-ops to be a non blocking dispatch to a subject
  2. Magic .stop in actor flows to dispatch a stop message (and maybe .join on the signal?)
  ...
  Step ??: Profit!
 */

public interface PipelineParallelFlow {
  final class WrappedSinkK implements FProgram.base$46flows.$95PipelineParallelSink_0 {
    static long SINK_ID = 0;
    @Override public FProgram.base$46flows.$95PipelineParallelSink_1 $35$imm$(Object original) {
      return new WrappedSink(SINK_ID++, (FProgram.base$46flows.$95Sink_1) original);
    }
  }
  final class WrappedSink implements FProgram.base$46flows.$95PipelineParallelSink_1 {
    final long subjectId;
    final FProgram.base$46flows.$95Sink_1 original;
    FlowRuntime.Subject<Object> subject;
    Throwable exception;
    public WrappedSink(long subjectId, FProgram.base$46flows.$95Sink_1 original) {
      this.subjectId = subjectId;
      this.original = original;
    }

    @Override public FProgram.base.Void_0 stop$mut$() {
      System.out.println("STOPPING SUBJ: "+subjectId);
      if (subject != null && !subject.ref().isClosed()) {
        subject.stop();
        subject.signal()
          .exceptionally(t -> {
            exception = t;
            return null;
          })
          .join();
        if (exception != null) {
          throw new RuntimeException(exception.getLocalizedMessage(), exception);
        }
      }
      return FProgram.base.Void_0._$self;
    }
    @Override public FProgram.base.Void_0 $35$mut$(Object x$) {
      if (this.subject == null) {
        System.out.println("SPAWNING SUBJ: "+subjectId);
        System.out.println("SUBJ: "+subjectId+" GOT MSG: "+x$);
        this.subject = getSubject(subjectId, original::$35$mut$, original::stop$mut$);
      }
      this.subject.ref().submit(new FlowRuntime.Message.Data<>(x$));
      return FProgram.base.Void_0._$self;
    }
  }

  interface Subscriber<E> {
    void apply(E msg);
  }
//  interface ActorImpl<S,E> {
//    FProgram.base$46flows.ActorRes_0 apply(
//      SubmissionPublisher<FlowRuntime.Message<E>> self,
//      S state,
//      FProgram.base$46flows.$95Sink_1 downstream,
//      FlowRuntime.Message.Data<E> msg
//    );
//  }


  static <E> FlowRuntime.Subject<E> getSubject(
    long subjectId,
    Subscriber<E> subscriber,
    Runnable stop
  ) {
    return FlowRuntime.<E>getSubject(subjectId)
      .orElseGet(() -> spawn(
        FlowRuntime.spawnWorker(),
        subscriber,
        stop
      ));
  }
//  static <S,E> FlowRuntime.Subject<E> getActor(
//    long subjectId,
//    FProgram.base$46flows.$95Sink_1 downstream,
//    S state,
//    ActorImpl<S,E> subscriber,
//    Runnable stop
//  ) {
//    return FlowRuntime.<E>getSubject(subjectId)
//      .orElseGet(() -> spawnActor(
//        FlowRuntime.spawnWorker(),
//        downstream,
//        state,
//        subscriber,
//        stop
//      ));
//  }

  static <E> FlowRuntime.Subject<E> spawn(
    SubmissionPublisher<FlowRuntime.Message<E>> self,
    Subscriber<E> subscriber,
    Runnable stop
  ) {
    return new FlowRuntime.Subject<>(self, self.consume(msg->{
      System.out.println("Message received: "+msg);
      switch (msg) {
        case FlowRuntime.Message.Data<E> data -> subscriber.apply(data.data());
        case FlowRuntime.Message.Stop<E> ignored -> {
          stop.run();
          self.close();
        }
      }
    }));
  };

//  static <S,E,R> FlowRuntime.Subject<E> spawnActor(
//    SubmissionPublisher<FlowRuntime.Message<E>> self,
//    FProgram.base$46flows.$95Sink_1 downstream,
//    S state,
//    ActorImpl<S,E> subscriber,
//    Runnable stop
//  ) {
//    return new FlowRuntime.Subject<E>(self, self.consume(msg->{
//      System.out.println("Message received: "+msg);
//      switch (msg) {
//        case FlowRuntime.Message.Data<E> data -> subscriber.apply(self, state, downstream, data).match$imm$(new FProgram.base$46flows.ActorResMatch_1(){
//          @SuppressWarnings("unchecked")
//          public Object stop$mut$() {
//            self.submit(FlowRuntime.Message.Stop.INSTANCE);
//            return null;
//          }
//          public Object continue$mut$() {
//            return null;
//          }
//        });
//        case FlowRuntime.Message.Stop<E> ignored -> self.close();
//      }
//    }));
//  }
}