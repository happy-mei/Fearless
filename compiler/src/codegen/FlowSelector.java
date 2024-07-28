package codegen;

import id.Id;
import magic.Magic;

import java.util.Optional;

public interface FlowSelector {
  static Optional<Id.DecId> bestParallelConstr(MIR.MCall call) {
    if (!call.canParallelise()) { return Optional.empty(); }
//    var res = call.variant().contains(MIR.MCall.CallVariant.DataParallelFlow)
//      ? Magic.DataParallelFlowK
//      : Magic.PipelineParallelFlowK;
    var res = Magic.PipelineParallelFlowK; // TODO: restore when data parallel flows are stable
    return Optional.of(res);
  }
}
