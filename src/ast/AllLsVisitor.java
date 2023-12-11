package ast;

import failure.Fail;
import visitors.CollectorVisitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AllLsVisitor implements CollectorVisitor<List<T.Dec>> {
  private final List<ast.T.Dec> ds = new ArrayList<>();
  @Override public List<ast.T.Dec> res() {
    return Collections.unmodifiableList(ds);
  }

  public AllLsVisitor() {
    super();
    System.out.println("new");
  }

  @Override public Void visitLambda(E.Lambda e) {
    if (!e.name().name().isFresh()) {
      var dec = new T.Dec(e.name().name(), e.name().gens(), e.name().bounds(), e, e.pos());
      var idx = ds.indexOf(dec);
      if (idx != -1) {
        var conflict = ds.get(idx);
        // TODO: WHYYYYY
//        throw Fail.conflictingDecl(dec.name(), List.of(new Fail.Conflict(conflict.posOrUnknown(), conflict.name().toString()))).pos(e.pos());
      }
      System.out.println("adding");
      ds.add(dec);
    }
    return CollectorVisitor.super.visitLambda(e);
  }
  public Void visitTrait(E.Lambda e) {
    return CollectorVisitor.super.visitLambda(e);
  }
}
