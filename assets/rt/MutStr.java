package rt;

import java.util.ArrayList;
import java.util.List;

public final class MutStr implements Str {
  private final List<Str> buffer = new ArrayList<>();
  private Str immStr;

  public MutStr(Str str) {
    buffer.add(str);
    immStr = str;
  }

  @Override public byte[] utf8() {
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

  @Override public base.Void_0 add$mut(Str other$) {
    buffer.add(other$);
    immStr = null;
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
    byte[] utf8 = new byte[buffer.stream().mapToInt(s -> s.utf8().length).sum()];
    int idx = 0;
    for (var str : buffer) {
      System.arraycopy(str.utf8(), 0, utf8, idx, str.utf8().length);
      idx = idx + str.utf8().length;
    }
    return Str.fromTrustedUtf8(utf8);
  }
}
