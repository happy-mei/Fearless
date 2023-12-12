package astFull;

import failure.Fail;
import visitors.FullCollectorVisitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AllLsFullVisitor implements FullCollectorVisitor<List<T.Dec>> {
  private final List<T.Dec> ds = new ArrayList<>();
  @Override public List<T.Dec> res() {
    return Collections.unmodifiableList(ds);
  }

  @Override public Void visitLambda(E.Lambda e) {
    var dec = new T.Dec(e.name().id(), e.name().gens(), e.name().bounds(), e, e.pos());
    var idx = ds.indexOf(dec);
    if (idx != -1) {
      var conflict = ds.get(idx);
      throw Fail.conflictingDecl(dec.name(), List.of(new Fail.Conflict(conflict.posOrUnknown(), conflict.name().toString()))).pos(e.pos());
    }
    ds.add(dec);
    return FullCollectorVisitor.super.visitLambda(e);
  }
  public Void visitTrait(E.Lambda e) {
    return FullCollectorVisitor.super.visitLambda(e);
  }
}
