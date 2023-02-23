package visitors;

import codegen.MIR;
import id.Id;

import java.util.List;
import java.util.Map;

public interface MIRVisitor<R> {
  // Structure
  R visitProgram(Map<String, List<MIR.Trait>> pkgs, Id.DecId entry);
  R visitTrait(String pkg, MIR.Trait trait);
  R visitMeth(MIR.Meth meth, String selfName, boolean concrete);

  // Expressions/values
  default R visitX(MIR.X x) { return visitX(x, true); }
  R visitX(MIR.X x, boolean checkMagic);
  default R visitMCall(MIR.MCall mCall) { return visitMCall(mCall, true); }
  R visitMCall(MIR.MCall mCall, boolean checkMagic);
  default R visitLambda(MIR.Lambda newL) { return visitLambda(newL, true); }
  R visitLambda(MIR.Lambda newL, boolean checkMagic);
//  R visitNewDynLambda(MIR.NewDynLambda newL);
//  R visitNewStaticLambda(MIR.NewStaticLambda newL);
//  R visitShare(MIR.Share s);

  // TODO: Ref stuff
}
