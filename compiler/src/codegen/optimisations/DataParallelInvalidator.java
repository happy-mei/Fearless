package codegen.optimisations;

import codegen.MIR;
import codegen.MIR.MCall.CallVariant;
import codegen.MIRCloneVisitor;
import id.Id;
import magic.MagicImpls;
import utils.Bug;

import java.util.ArrayDeque;
import java.util.Deque;

public class DataParallelInvalidator implements MIRCloneVisitor {
  private final MagicImpls<?> magic;
  private final Deque<Id.MethName> flowStack;
  private DataParallelInvalidator(MagicImpls<?> magic) {
    this.magic = magic;
    this.flowStack = new ArrayDeque<>();
  }

  @Override public MIR.MCall visitMCall(MIR.MCall call, boolean checkMagic) {
    if (call.variant().contains(CallVariant.Standard) || call.variant().contains(CallVariant.PipelineParallelFlow)) {
      return call;
    }
    throw Bug.todo();
//    return MIRCloneVisitor.super.visitMCall(call, checkMagic);
  }
}
