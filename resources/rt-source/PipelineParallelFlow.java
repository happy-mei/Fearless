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
  interface SubjectSubscriber<S,E,R> {
    userCode.FProgram.base$46flows.ActorRes_0 apply(
      S state,
      userCode.FProgram.base$46flows.$95Sink_1 downstream,
      FlowRuntime.Message.Data<E> msg
    );
  }

  static <S,E,R> FlowRuntime.Subject<E> getSubj(
    long subjectId,
    userCode.FProgram.base$46flows.$95Sink_1 downstream,
    S state,
    SubjectSubscriber<S,E,R> subscriber,
    Runnable stop
  ) {
    return FlowRuntime.<E>getSubject(subjectId)
      .orElseGet(() -> spawn(
        FlowRuntime.spawnWorker(),
        downstream,
        state,
        subscriber,
        stop
      ));
  }

  static <S,E,R> FlowRuntime.Subject<E> spawn(
    SubmissionPublisher<FlowRuntime.Message<E>> self,
    userCode.FProgram.base$46flows.$95Sink_1 downstream,
    S state,
    SubjectSubscriber<S,E,R> subscriber,
    Runnable stop
  ) {
    return new FlowRuntime.Subject<E>(self, self.consume(msg->{
      System.out.println("TODO: "+msg);
//      switch (msg) {
//        case FlowRuntime.Message.Data<E> data -> subscriber.apply(state, subject, data);
//      }
    }));
  };
}