package rt;

import org.junit.jupiter.api.Test;

@SuppressWarnings("unchecked")
public class TestFlowRuntime {
//  @Test void tbd() {
//    try (
//      var subject = FlowRuntime.<String>spawnSubject();
//      var subject2 = FlowRuntime.<String>spawnSubject()
//    ) {
//      subject.ref().submit(new FlowRuntime.Message.Data<>("Hello"));
//      subject2.ref().submit(new FlowRuntime.Message.Data<>("Hello2"));
//      subject.ref().submit(new FlowRuntime.Message.Data<>("Bye"));
//      subject2.ref().submit(new FlowRuntime.Message.Data<>("Bye2"));
//      System.out.println("after?");
//      subject.ref().submit(FlowRuntime.Message.Stop.INSTANCE);
//      subject2.ref().submit(FlowRuntime.Message.Stop.INSTANCE);
//      System.out.println("am I blocked?");
//
//      subject.signal().exceptionally(err -> {
//        System.out.println("err" + err);
//        return null;
//      }).join();
//    }
//  }
}