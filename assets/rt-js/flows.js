import { FlowCreator } from "./flows/FlowCreator.js";
import { DataParallelFlowK } from "./flows/dataParallel/DataParallelFlowK.js";
import { pipelineParallelFlow } from "./flows/pipelineParallel/pipelineParallelFlow.js";
import { Range } from "./flows/Range.js";

export const rt$$flows = {
  FlowCreator,
  pipelineParallel: {
    pipelineParallelFlow
  },
  dataParallel: {
    DataParallelFlowK
  },
  Range,
};
