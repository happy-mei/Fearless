package rt.dataParallel;

import base.Info_0;
import base.Void_0;
import base.flows._Sink_1;

import java.util.List;

final class BufferSink implements _Sink_1 {
  public final _Sink_1 original;
  private final List<Object> buffer;

  public BufferSink(_Sink_1 original, List<Object> buffer) {
    this.original = original;
    this.buffer = buffer;
  }

  @Override public Void_0 stop$mut() {
    return original.stop$mut();
  }
  @Override public Void_0 pushError$mut(Info_0 info_m$) {
    return original.pushError$mut(info_m$);
  }
  @Override public Void_0 $hash$mut(Object x_m$) {
    buffer.add(x_m$);
    return Void_0.$self;
  }

  public void flush() {
    for (var e : buffer) {
      original.$hash$mut(e);
    }
  }
}
