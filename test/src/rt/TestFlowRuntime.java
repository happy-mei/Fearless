package rt;

import org.junit.jupiter.api.Test;

@SuppressWarnings("unchecked")
public class TestFlowRuntime {
  @Test void tbd() {
    try (var subject = FlowRuntime.<String>spawnSubject()) {
      subject.ref().submit(new FlowRuntime.Message.Data<>("Hello"));
      subject.ref().submit(new FlowRuntime.Message.Data<>("Bye"));
      System.out.println("after?");
      subject.ref().submit(FlowRuntime.Message.Stop.INSTANCE);
      System.out.println("am I blocked?");

      subject.signal().exceptionally(err -> {
        System.out.println("err" + err);
        return null;
      }).join();
    }
  }
}