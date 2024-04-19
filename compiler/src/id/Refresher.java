package id;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Refresher<TT extends Id.Ty> {
  private final int depth;
  public Refresher(int depth) { this.depth = depth; }

  public Id.GX<TT> freshName(Id.GX<TT> original) {
    var originalName = original.name();
    if (originalName.contains("$")) {
      originalName = originalName.split("\\$")[0];
    }
//    assert !originalName.contains("$") : originalName;
    return new Id.GX<>(originalName+"$"+depth);
  }
  public Map<Id.GX<TT>,Id.GX<TT>> substitutes(List<Id.GX<TT>> gxs) {
    return gxs.stream().collect(Collectors.toMap(Function.identity(), this::freshName));
  }
}
