package astFull;

import files.Pos;
import utils.Bug;

import java.net.URI;
import java.util.IdentityHashMap;
import java.util.Optional;

// alternatively to this, we can have Pos in the AST with a hashcode/equals set to not impact anything

public class PosMap {
  private static final Pos Unknown = Pos.of(URI.create("unknown"), 0, 0);
  public static void reset(){ map.clear(); }
  private static final IdentityHashMap<Object, Pos> map = new IdentityHashMap<>();
  public static <N> N add(N node, Pos pos) {
    if (pos == Unknown){ return node; }
    var res = map.put(node, pos);
    if (res != null && res != pos) {Bug.err("A Pos for "+node+" already exists.");}
    return node;
  }
  public static <ON,N> N replace(ON oldNode, N node) {
    var oldPos = map.remove(oldNode);
    return add(node, oldPos);
  }
  public static Optional<Pos> get(Object node) {return Optional.ofNullable(map.get(node));}
  public static Pos getOrUnknown(Object node) {
    return get(node).orElse(Unknown);
  }
}
