package ast;

import failure.Fail;
import id.Id;
import visitors.CollectorVisitor;

import java.util.*;

public class AllLsVisitor implements CollectorVisitor<Collection<T.Dec>> {
  private final Map<Id.DecId, T.Dec> ds = new HashMap<>();
  @Override public Collection<ast.T.Dec> res() {
    return Collections.unmodifiableCollection(ds.values());
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
    return CollectorVisitor.super.visitLambda(e);
  }
  public void visitTrait(E.Lambda e) {
    CollectorVisitor.super.visitLambda(e);
  }
}
