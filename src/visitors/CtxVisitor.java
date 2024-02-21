package visitors;

import ast.E;

public interface CtxVisitor<Ctx,R> {
  R visitMCall(E.MCall e, Ctx ctx);
  R visitX(E.X e, Ctx ctx);
  R visitLambda(E.Lambda e, Ctx ctx);
}
