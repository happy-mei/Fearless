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
    var dec = new T.Dec(e.name().id(), e.name().gens(), e.name().bounds(), e, e.pos());
    var conflict = ds.get(dec.name());
    if (conflict != null) {
      throw Fail.conflictingDecl(dec.name(), List.of(new Fail.Conflict(conflict.posOrUnknown(), conflict.name().toString()))).pos(e.pos());
    }
    ds.put(dec.name(), dec);
    return CollectorVisitor.super.visitLambda(e);
  }
  public void visitTrait(E.Lambda e) {
    CollectorVisitor.super.visitLambda(e);
  }
}
