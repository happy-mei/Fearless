package astFull;

import files.Pos;
import utils.Bug;

import java.net.URI;
import java.util.IdentityHashMap;
import java.util.Optional;

public class PosMap {
  private static final IdentityHashMap<Object, Pos> map = new IdentityHashMap<>();
  public static void add(Object node, Pos pos) {
    var res = map.put(node, pos);
    if (res != null && res != pos) {Bug.err("A Pos for "+node+" already exists.");}
  }
  public static Optional<Pos> get(Object node) {return Optional.ofNullable(map.get(node));}
  public static Pos getOrUnknown(Object node) {
    return get(node).orElse(Pos.of(URI.create("unknown"), 0, 0));
  }
}
