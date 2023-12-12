package ast;

import failure.Fail;
import visitors.CollectorVisitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AllLsVisitor implements CollectorVisitor<List<T.Dec>> {
  private final List<ast.T.Dec> ds = new ArrayList<>();
  @Override public List<ast.T.Dec> res() {
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
    return CollectorVisitor.super.visitLambda(e);
  }
  public void visitTrait(E.Lambda e) {
    CollectorVisitor.super.visitLambda(e);
  }
}
