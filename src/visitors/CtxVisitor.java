package visitors;

import ast.E;
import codegen.MIR;

import java.util.Map;

public interface CtxVisitor<Ctx,R> {
  R visitMCall(String pkg, E.MCall e, Ctx ctx);
  R visitX(E.X e, Ctx ctx);
  R visitLambda(String pkg, E.Lambda e, Ctx ctx);
}
