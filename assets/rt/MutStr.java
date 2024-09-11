package rt;

import base.Bool_0;
import base.False_0;
import base.True_0;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public final class MutStr implements Str {
  private ByteBuffer buffer = ByteBuffer.allocateDirect(16);
  private int[] graphemes = null;

  public MutStr(Str str) {
    assert str != null;
    put(str);
  }

  @Override public ByteBuffer utf8() {
    return buffer.slice(0, buffer.position());
  }

  @Override public int[] graphemes() {
    var graphemes = this.graphemes;
    if (graphemes != null) {
      return graphemes;
    }
    graphemes = NativeRuntime.indexString(utf8());
    this.graphemes = graphemes;
    return graphemes;
  }

  @Override public Bool_0 isEmpty$read() {
    return buffer.position() == 0 ? True_0.$self : False_0.$self;
  }

  @Override public MutStr $plus$mut(base.Stringable_0 other$) {
    var other = other$.str$read();
    assert other != null;
    put(other);
    return this;
  }

  @Override public base.Void_0 append$mut(base.Stringable_0 other$) {
    this.$plus$mut(other$);
    return base.Void_0.$self;
  }

  @Override public base.Void_0 clear$mut() {
    buffer.clear();
    buffer.position(0);
    return base.Void_0.$self;
  }

  private void put(Str str) {
    var toAdd = str.utf8();
    if (buffer.remaining() < toAdd.capacity()) {
      buffer.position(0);
      var newBuffer = ByteBuffer.allocateDirect((buffer.capacity() * 3) / 2 + 1);
      buffer = newBuffer.put(buffer);
    }
    buffer.put(toAdd);
    toAdd.position(0);
  }

//  private Str freeze() {
//    freezeLock.lock();
//    try {
//      // if we lost the lock race, use the answer from the winner
//      if (immStr != null) { return immStr; }
//
//      var bufferStream = buffer.size() > 32 ? buffer.parallelStream() : buffer.stream();
////      byte[] utf8 = new byte[bufferStream.mapToInt(s -> s.utf8().length).sum()];
//      var capacity = bufferStream.mapToInt(s -> s.utf8().remaining()).sum();
//      var utf8 = ByteBuffer.allocateDirect(capacity);
//      for (var str : buffer) {
//        utf8.put(str.utf8().duplicate());
//      }
//      utf8.position(0);
////      utf8 = utf8.asReadOnlyBuffer();
//      var res = Str.fromTrustedUtf8(utf8);
//      buffer.clear(); // cant just set to null, because we use .freeze for just normal reading too (not just for ->imm)
//      buffer.add(res);
//      return res;
//    } finally {
//      freezeLock.unlock();
//    }
//  }
}
