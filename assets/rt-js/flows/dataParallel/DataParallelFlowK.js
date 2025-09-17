import { DataParallelFlow } from "./DataParallelFlow.js";
import { base$$flows$$_RestrictFlowReuse_0 } from "../../../base/flows/index.js";

export const DataParallelFlowK = (source_m$, size_m$) => {
  return base$$flows$$_RestrictFlowReuse_0.$self.$hash$imm(
    new DataParallelFlow(source_m$, size_m$, DataParallelFlowK.$self)
  );
};

// assign $self reference back to the factory
DataParallelFlowK.$self = DataParallelFlowK;
