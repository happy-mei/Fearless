import { base$$Opt_1, base$$Opts_0, base$$False_0 } from "../../base/index.js";
import { base$$flows$$_SeqFlow_0, base$$flows$$_UnwrapFlowToken_0, base$$flows$$Flow_1, base$$flows$$FlowOp_1, base$$flows$$_FlowFactory_0 } from "../../base/flows/index.js";
import { DataParallelFlowK } from "./dataParallel/DataParallelFlowK.js";
import { PipelineParallelFlowK } from "./pipelineParallel/PipelineParallelFlowK.js";

// Fake "ScopedValue" for sequentialisation toggle
let IS_SEQUENTIALISED = { bound: false };

export const FlowCreator = {
  IS_SEQUENTIALISED,

  /**
   * @param {base$$flows$$_FlowFactory_0} intended The flow factory the compiler intends for us to use
   * @param {base$$flows$$Flow_1} original The original flow that we are trying to promote
   * @return {base$$flows$$Flow_1} A flow that may or may not have been created with intended.
   *                  Only ever degrades flows (DP -> Seq), never upgrades.
   */
  fromFlow(intended, original) {
    const op = original.unwrapOp$mut(base$$flows$$_UnwrapFlowToken_0.$self);

    // size = long, or -1 if empty
    const size = original.size$read().match$imm({
      some$mut: (x) => x,
      empty$mut: () => -1n, // bigint for long
    });

    return FlowCreator.fromFlowOp(intended, op, size);
  },

  /**
   * @param {base$$flows$$_FlowFactory_0} intended
   * @param {base$$flows$$FlowOp_1} op
   * @param {bigint|number} size
   */
  fromFlowOp(intended, op, size) {
    const isSequentialised = IS_SEQUENTIALISED.bound;

    if (isSequentialised) {
      const optSize = size < 0 ? base$$Opt_1.$self : base$$Opts_0.$self.$hash$imm(size);
      return base$$flows$$_SeqFlow_0.$self.fromOp$imm(op, optSize);
    }

    if (op.isFinite$mut() === base$$False_0.$self && intended instanceof DataParallelFlowK) {
      return PipelineParallelFlowK.$self.fromOp$imm(op, base$$Opt_1.$self);
    }

    if (size < 0) {
      return intended.fromOp$imm(op, base$$Opt_1.$self);
    }

    const optSize = base$$Opts_0.$self.$hash$imm(size);

    // Special-case fork/join attempt
    if (size === 2n) {
      return intended.fromOp$imm(op, optSize);
    }

    if (size <= 1n) {
      return base$$flows$$_SeqFlow_0.$self.fromOp$imm(op, optSize);
    }

    if (size < 4n && intended instanceof DataParallelFlowK) {
      return PipelineParallelFlowK.$self.fromOp$imm(op, optSize);
    }

    return intended.fromOp$imm(op, optSize);
  },
};
