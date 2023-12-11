package astFull;

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
    if (!e.name().name().isFresh()) {
      ds.add(new T.Dec(e.name().name(), e.name().gens(), e.name().bounds(), e, e.pos()));
    }
    return FullCollectorVisitor.super.visitLambda(e);
  }
}
