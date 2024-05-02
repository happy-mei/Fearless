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
    var name = e.id().id();
    var conflict = ds.get(name);
    if (conflict != null) {
      throw Fail.conflictingDecl(name, List.of(
        new Fail.Conflict(e.posOrUnknown(), name.toString()),
        new Fail.Conflict(conflict.posOrUnknown(), conflict.name().toString()))
      ).pos(e.pos());
    }
    var dec = new T.Dec(e.id().id(), e.id().gens(), e.id().bounds(), e, e.pos());
    ds.put(name, dec);
    return FullCollectorVisitor.super.visitLambda(e);
  }
  public void visitTrait(E.Lambda e) {
    FullCollectorVisitor.super.visitLambda(e);
  }
}
