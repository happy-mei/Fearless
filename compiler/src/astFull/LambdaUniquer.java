package astFull;

import id.Id;
import visitors.FullCloneVisitor;

import java.util.Objects;

/** Occasionally we duplicate method bodies (i.e. RC overloading). We need to make sure that the fresh names for
 * all object literals are unique because otherwise they'll conflict. */
class LambdaUniquer implements FullCloneVisitor {
  private static LambdaUniquer INSTANCE = null;
  private LambdaUniquer() { INSTANCE = this; }
  public static LambdaUniquer get() { return Objects.requireNonNullElseGet(INSTANCE, LambdaUniquer::new); }

  @Override public E.Lambda visitLLambda(E.Lambda e) {
    if (!e.id().id().isFresh()) { return FullCloneVisitor.super.visitLLambda(e); }
    var renamed = FullCloneVisitor.super.visitLLambda(e);
    Id.DecId old = e.id().id();
    return renamed
      .withLambdaId(new E.Lambda.LambdaId(Id.DecId.fresh(old.pkg(), old.gen()), e.id().gens(), e.id().bounds()));
  }
}
