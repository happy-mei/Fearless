package rt.flows.dataParallel;

import base.Info_0;
import base.Void_0;
import base.flows._Sink_1;
import rt.FearlessError;

import java.util.List;

public final class BufferSink implements _Sink_1 {
  public final _Sink_1 original;
  private final List<Object> buffer;

  private record Error(Info_0 info) {}

  public BufferSink(_Sink_1 original, List<Object> buffer) {
    this.original = original;
    this.buffer = buffer;
  }

  @Override public Void_0 stop$mut() {
//    return original.stop$mut();
    return Void_0.$self;
  }
  @Override public Void_0 pushError$mut(Info_0 info_m$) {
    buffer.add(new Error(info_m$));
    return Void_0.$self;
  }
  @Override public Void_0 $hash$mut(Object x_m$) {
    buffer.add(x_m$);
    return Void_0.$self;
  }

  public void flush() {
    for (var e : buffer) {
      if (e instanceof Error err) {
        original.pushError$mut(err.info);
        continue;
      }
      try {
        original.$hash$mut(e);
      } catch (FearlessError err) {
        original.pushError$mut(err.info);
      } catch (ArithmeticException err) {
        original.pushError$mut(base.Infos_0.$self.msg$imm(rt.Str.fromJavaStr(err.getMessage())));
      }
    }
    original.stop$mut();
  }
}
