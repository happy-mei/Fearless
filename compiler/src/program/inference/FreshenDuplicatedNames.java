package program.inference;

import astFull.E;
import id.Id;
import visitors.FullCloneVisitor;

import java.util.HashSet;

/**
 * When doing inference we can copy method bodies for multi-mdf method overloads. However, because every lambda has a
 * unique name, this means that we can get errors from duplicated names. For explicitly named lambdas this is fine,
 * but if we're inferring the name of the lambda, we should really not run into this problem. This visitor regenerates
 * all fresh lambda names if they have been seen before.
 */
public class FreshenDuplicatedNames implements FullCloneVisitor {
  private final HashSet<Id.DecId> seen = new HashSet<>();
  @Override public E.Lambda visitLLambda(E.Lambda e) {
    var res = FullCloneVisitor.super.visitLLambda(e);
    var id = res.id().id();
    if (seen.add(id)) {
      return res;
    }
    return res.withLambdaId(new E.Lambda.LambdaId(Id.DecId.fresh(id.pkg(), id.gen()), res.id().gens(), res.id().bounds()));
  }
}
