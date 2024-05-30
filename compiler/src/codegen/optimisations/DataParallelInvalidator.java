package codegen.optimisations;

import codegen.MIR;
import codegen.MIR.MCall.CallVariant;
import codegen.MIRCloneVisitor;
import id.Id;
import id.Mdf;
import magic.Magic;
import utils.Bug;

import java.util.*;

public class DataParallelInvalidator implements MIRCloneVisitor, FlattenChain<Id.MethName, Void, MIR.MCall> {
  private static final Set<Id.MethName> SAFE_FLOW_MS = Set.of(
    new Id.MethName(".flow", 0),
    new Id.MethName(".map", 1),
    new Id.MethName(".filter", 1),
    new Id.MethName(".flatMap", 1),
    new Id.MethName(".let", 1), // TODO: test this in DP
    new Id.MethName(".first", 0),
    new Id.MethName(".findMap", 0),
    new Id.MethName(".fold", 2), // TODO: delete fold/3
    new Id.MethName(".find", 1),
    new Id.MethName(".any", 1),
    new Id.MethName(".all", 1),
    new Id.MethName(".for", 1),
    new Id.MethName(".list", 1),
    new Id.MethName(".max", 1),
    new Id.MethName(".min", 1),
    new Id.MethName(".join", 1)
  );

  @Override public MIR.E visitMCall(MIR.MCall call, boolean checkMagic) {
    return call;
    // TODO: no, this is only capturing one level deep-- need to go lower
//    if (!call.variant().contains(CallVariant.DataParallelFlow)) {
//      return MIRCloneVisitor.super.visitMCall(call, checkMagic);
//    }
//    var res = visitFluentCall(call, List.of(), Optional.empty());
//    if (res.isEmpty()) {
//      System.out.println(res);
//      var variants = call.variant().clone();
//      variants.remove(CallVariant.DataParallelFlow);
//      var safeCall = call.withVariants(variants);
//      return MIRCloneVisitor.super.visitMCall(safeCall, checkMagic);
//    }
//    return MIRCloneVisitor.super.visitMCall(res.get(), checkMagic);
  }

  @Override
  public Optional<MIR.MCall> visitFluentCall(
    MIR.MCall call,
    List<Void> _validEndings,
    Optional<MIR.X> self
  ) {
    var flowOps = new ArrayDeque<Id.MethName>();
    var res = flatten(call, flowOps, self);
    if (res == FlattenStatus.INVALID) { return Optional.empty(); }
    assert res == FlattenStatus.FLATTENED;
    return Optional.of(call);
  }

  @Override public FlattenStatus flatten(MIR.E expr, Deque<Id.MethName> ms, Optional<MIR.X> self) {
    return switch (expr) {
      case MIR.MCall call -> {
        var name = call.name();
        if (!SAFE_FLOW_MS.contains(name)) { yield FlattenStatus.INVALID; }
        if (!checkNaturalFolds(call)) { yield FlattenStatus.INVALID; }
        ms.offerFirst(name);
        yield flatten(call.recv(), ms, self);
      }
      case MIR.X x -> self.filter(x::equals).map(_->FlattenStatus.FLATTENED).orElse(FlattenStatus.INVALID);
      case MIR.CreateObj createObj -> FlattenStatus.FLATTENED;
      case MIR.BoolExpr boolExpr -> throw Bug.unreachable();
      case MIR.StaticCall staticCall -> throw Bug.unreachable();
      case MIR.Block block -> throw Bug.unreachable();
    };
  }

  private boolean checkNaturalFolds(MIR.MCall call) {
    if (call.name().equals(new Id.MethName(".join", 1))) {
      return validateNaturalFold(call, new MIR.MT.Plain(Mdf.imm, Magic.Str));
    }
    return true;
  }

  private boolean validateNaturalFold(MIR.MCall call, MIR.MT safeT) {
    assert call.args().size() == 1;
    return call.args().getFirst().t().equals(safeT);
  }
}
