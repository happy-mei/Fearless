package ast;

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
    if (!e.name().name().isFresh()) {
      ds.add(new T.Dec(e.name().name(), e.name().gens(), e.name().bounds(), e, e.pos()));
    }
    return CollectorVisitor.super.visitLambda(e);
  }
}
