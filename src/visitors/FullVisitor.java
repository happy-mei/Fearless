package visitors;

import astFull.E;

public interface FullVisitor<R> {
  R visitMCall(E.MCall e);
  R visitX(E.X e);
  R visitLambda(E.Lambda e);
}