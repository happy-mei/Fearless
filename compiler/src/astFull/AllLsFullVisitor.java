package astFull;

import failure.Fail;
import id.Id;
import visitors.FullCollectorVisitor;

import java.util.*;

public class AllLsFullVisitor implements FullCollectorVisitor<Collection<T.Dec>> {
  private final Map<Id.DecId, T.Dec> ds = new HashMap<>();
  @Override public Collection<T.Dec> res() {
    return ds.values();
  }

  @Override public Void visitLambda(E.Lambda e) {
    var dec = new T.Dec(e.id().id(), e.id().gens(), e.id().bounds(), e, e.pos());
    var conflict = ds.get(dec.name());
    if (conflict != null) {
      throw Fail.conflictingDecl(dec.name(), List.of(new Fail.Conflict(conflict.posOrUnknown(), conflict.name().toString()))).pos(e.pos());
    }
    ds.put(dec.name(), dec);
    return FullCollectorVisitor.super.visitLambda(e);
  }
  public Void visitTrait(E.Lambda e) {
    return FullCollectorVisitor.super.visitLambda(e);
  }
}
