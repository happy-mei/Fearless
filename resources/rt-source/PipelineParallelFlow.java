package rt;

import java.util.Optional;
import java.util.concurrent.SubmissionPublisher;

/*
  The plan:
  1. Magic .step in actor flow-ops to be a non blocking dispatch to a subject
  2. Magic .stop in actor flows to dispatch a stop message (and maybe .join on the signal?)
  ...
  Step ??: Profit!
 */

public interface PipelineParallelFlow {
  interface Subscriber<E,R> {
    void apply(
      userCode.FProgram.base$46flows.$95Sink_1 downstream,
      FlowRuntime.Message.Data<E> msg
    );
  }
  interface ActorImpl<S,E,R> {
    userCode.FProgram.base$46flows.ActorRes_0 apply(
      S state,
      userCode.FProgram.base$46flows.$95Sink_1 downstream,
      FlowRuntime.Message.Data<E> msg
    );
  }


  static <E,R> FlowRuntime.Subject<E> getSubject(
    long subjectId,
    userCode.FProgram.base$46flows.$95Sink_1 downstream,
    Subscriber<E,R> subscriber,
    Runnable stop
  ) {
    return FlowRuntime.<E>getSubject(subjectId)
      .orElseGet(() -> spawn(
        FlowRuntime.spawnWorker(),
        downstream,
        subscriber,
        stop
      ));
  }
  static <S,E,R> FlowRuntime.Subject<E> getActor(
    long subjectId,
    userCode.FProgram.base$46flows.$95Sink_1 downstream,
    S state,
    ActorImpl<S,E,R> subscriber,
    Runnable stop
  ) {
    return FlowRuntime.<E>getSubject(subjectId)
      .orElseGet(() -> spawnActor(
        FlowRuntime.spawnWorker(),
        downstream,
        state,
        subscriber,
        stop
      ));
  }

  static <E,R> FlowRuntime.Subject<E> spawn(
    SubmissionPublisher<FlowRuntime.Message<E>> self,
    userCode.FProgram.base$46flows.$95Sink_1 downstream,
    Subscriber<E,R> subscriber,
    Runnable stop
  ) {
    return new FlowRuntime.Subject<E>(self, self.consume(msg->{
      System.out.println("Message received: "+msg);
      switch (msg) {
        case FlowRuntime.Message.Data<E> data -> subscriber.apply(downstream, data);
        case FlowRuntime.Message.Stop<E> ignored -> self.close();
      }
    }));
  };

  static <S,E,R> FlowRuntime.Subject<E> spawnActor(
    SubmissionPublisher<FlowRuntime.Message<E>> self,
    userCode.FProgram.base$46flows.$95Sink_1 downstream,
    S state,
    ActorImpl<S,E,R> subscriber,
    Runnable stop
  ) {
    return new FlowRuntime.Subject<E>(self, self.consume(msg->{
      System.out.println("Message received: "+msg);
      switch (msg) {
        case FlowRuntime.Message.Data<E> data -> subscriber.apply(state, downstream, data).match$imm$(new userCode.FProgram.base$46flows.ActorResMatch_1(){
          @SuppressWarnings("unchecked")
          public Object stop$mut$() {
            self.submit(FlowRuntime.Message.Stop.INSTANCE);
            return null;
          }
          public Object continue$mut$() {
            return null;
          }
        });
        case FlowRuntime.Message.Stop<E> ignored -> self.close();
      }
    }));
  };
}