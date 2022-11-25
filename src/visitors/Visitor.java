package visitors;

import ast.E;

public interface Visitor<R> {
  R visitMCall(E.MCall e);
  R visitX(E.X e);
  R visitLambda(E.Lambda e);
}
