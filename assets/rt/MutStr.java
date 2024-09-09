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
  private final List<Str> buffer = new ArrayList<>();
  private Str immStr;
  // technically this object can be promoted to iso, then sub-typed to imm and used in parallel!
  // we need to make sure we don't freeze concurrently.
  private final ReentrantLock freezeLock = new ReentrantLock();

  public MutStr(Str str) {
    assert str != null;
    buffer.add(str);
    immStr = str;
  }

  @Override public ByteBuffer utf8() {
    if (immStr == null) {
      immStr = freeze();
    }
    return immStr.utf8();
  }

  @Override public int[] graphemes() {
    if (immStr == null) {
      immStr = freeze();
    }
    return immStr.graphemes();
  }

  @Override public Bool_0 isEmpty$read() {
    for (var str : buffer) {
      if (str.isEmpty$read() == False_0.$self) {
        return False_0.$self;
      }
    }
    return True_0.$self;
  }

  @Override public MutStr $plus$mut(base.Stringable_0 other$) {
    var other = other$.str$read();
    assert other != null;
    buffer.add(other);
    immStr = null;
    return this;
  }

  @Override public base.Void_0 append$mut(base.Stringable_0 other$) {
    this.$plus$mut(other$);
    return base.Void_0.$self;
  }

  @Override public base.Void_0 clear$mut() {
    buffer.clear();
    immStr = Str.EMPTY;
    return base.Void_0.$self;
  }

  @Override public rt.Str str$read() {
    if (immStr == null) {
      immStr = freeze();
    }
    return immStr;
  }

  private Str freeze() {
    freezeLock.lock();
    try {
      // if we lost the lock race, use the answer from the winner
      if (immStr != null) { return immStr; }

      var bufferStream = buffer.size() > 32 ? buffer.parallelStream() : buffer.stream();
//      byte[] utf8 = new byte[bufferStream.mapToInt(s -> s.utf8().length).sum()];
      var capacity = bufferStream.mapToInt(s -> s.utf8().remaining()).sum();
      var utf8 = ByteBuffer.allocateDirect(capacity);
      for (var str : buffer) {
        utf8.put(str.utf8().duplicate());
      }
      utf8.position(0);
      utf8 = utf8.asReadOnlyBuffer();
      var res = Str.fromTrustedUtf8(utf8);
      buffer.clear(); // cant just set to null, because we use .freeze for just normal reading too (not just for ->imm)
      buffer.add(res);
      return res;
    } finally {
      freezeLock.unlock();
    }
  }
}
