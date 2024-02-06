package visitors;

import codegen.mir2.MIR;
import id.Id;

public interface MIRVisitor2<R> {
//  R visitProgram(MIR.Program p, Id.DecId entry);
//  R visitPackage(MIR.Package pkg);
//  R visitTypeDef(String pkg, MIR.TypeDef def);
//  R visitMeth(String pkg, MIR.Meth meth, boolean signatureOnly);
//  R visitObjLit(String pkg, MIR.ObjLit lit, boolean checkMagic);
  R visitCreateObj(MIR.CreateObj createObj, boolean checkMagic);
  R visitX(MIR.X x, boolean checkMagic);
  R visitMCall(MIR.MCall call, boolean checkMagic);
  R visitUnreachable(MIR.Unreachable unreachable);
}
