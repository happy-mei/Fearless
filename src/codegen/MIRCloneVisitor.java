package codegen;

import id.Id;
import utils.Mapper;
import visitors.MIRVisitor;

import java.util.List;
import java.util.Map;

public class MIRCloneVisitor implements MIRVisitor<Object> {
  @Override public Map<String, List<MIR.Trait>> visitProgram(Map<String, List<MIR.Trait>> pkgs, Id.DecId entry) {
    return Mapper.of(res->pkgs.forEach((pkg, ds)->res.put(pkg, ds.stream().map(t->this.visitTrait(pkg,t)).toList())));
  }

  @Override public MIR.Trait visitTrait(String pkg, MIR.Trait trait) {
    return null;
  }

  @Override public MIR.Meth visitMeth(MIR.Meth meth, String selfName, boolean concrete) {
    return new MIR.Meth(
      meth.name(),
      meth.mdf(),
      meth.gens(),
      meth.xs(),
      meth.rt(),
      meth.body().map(e->(MIR)e.accept(this))
    );
  }

  @Override public MIR.X visitX(MIR.X x, boolean checkMagic) {
    return new MIR.X(x.name(), x.t(), x.capturer());
  }

  @Override public MIR.MCall visitMCall(MIR.MCall mCall, boolean checkMagic) {
    return new MIR.MCall(
      (MIR) mCall.recv().accept(this),
      mCall.name(),
      mCall.args().stream().map(arg->(MIR) arg.accept(this)).toList(),
      mCall.t(),
      mCall.mdf(),
      mCall.variant()
    );
  }

  @Override public MIR.Lambda visitLambda(MIR.Lambda newL, boolean checkMagic) {
    return new MIR.Lambda(
      newL.mdf(),
      newL.freshName(),
      newL.selfName(),
      newL.its(),
      newL.meths(),
      newL.methCaptures().stream().map(mCaps->mCaps.stream().map(x->(MIR.X)visitX(x)).toList()).toList(),
      newL.canSingleton()
    );
  }

  @Override public MIR.Unreachable visitUnreachable(MIR.Unreachable u) {
    return new MIR.Unreachable(u.t());
  }
}
