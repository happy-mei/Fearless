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
    var dec = new T.Dec(e.id().id(), e.id().gens(), e.id().bounds(), e, e.pos());
    var conflict = ds.get(dec.id());
    if (conflict != null) {
      throw Fail.conflictingDecl(dec.id(), List.of(new Fail.Conflict(conflict.posOrUnknown(), conflict.id().toString()))).pos(e.pos());
    }
    ds.put(dec.id(), dec);
    return CollectorVisitor.super.visitLambda(e);
  }
  public void visitTrait(E.Lambda e) {
    CollectorVisitor.super.visitLambda(e);
  }
}
