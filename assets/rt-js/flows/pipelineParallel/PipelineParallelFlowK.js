import { base$$Void_0, base$$Infos_0 } from "../../../base/index.js";
import { base$$flows$$_PipelineParallelFlow_0 } from "../../../base/flows/index.js";
import { PipelineParallelFlow } from "./PipelineParallelFlow.js";
import { FearlessError } from "../../FearlessError.js";

export class PipelineParallelFlowK {
  /**
   * @param {base$$flows$$FlowOp_1} source_m$
   * @param {base$$Opt_1} size_m$
   * @returns {base$$flows$$Flow_1}
   */
  fromOp$imm(source_m$, size_m$) {
    const source = new SafeSource(source_m$);
    return base$$flows$$_PipelineParallelFlow_0.fromOp$imm$fun(source, size_m$, this);
  }

  /**
   * Delegates to generated helper
   * @param {any} e_m$
   */
  $hash$imm(e_m$) {
    return base$$flows$$_PipelineParallelFlow_0.$hash$imm$fun(e_m$, this);
  }

  // singleton for compiler/runtime compatibility
  static $self = new PipelineParallelFlowK();
}

/**
 * SafeSource: wraps a FlowOp_1 and catches FearlessError / arithmetic exceptions
 * so they are forwarded to the downstream sink via pushError$mut, mirroring the Java behavior.
 */
class SafeSource {
  /**
   * @param {base$$flows$$FlowOp_1} original
   */
  constructor(original) {
    this.original = original;
    /** @type {PipelineParallelFlow.WrappedSink | null} */
    this.sink = null;
  }

  isFinite$mut() {
    return this.original.isFinite$mut();
  }

  step$mut(sink_m$) {
    // keep reference to wrapped sink when observed
    if (sink_m$ instanceof PipelineParallelFlow.WrappedSink) {
      this.sink = sink_m$;
    }
    try {
      return this.original.step$mut(sink_m$);
    } catch (err) {
      // rethrow deterministic wrapper errors unchanged
      if (err instanceof PipelineParallelFlow.DeterministicFearlessError) {
        throw err;
      }
      if (err instanceof FearlessError) {
        // forward the Info to the sink as pushError$mut
        try {
          sink_m$.pushError$mut(err.info);
        } catch (inner) {
          // If pushError itself throws, bubble the original err as deterministic
          throw new PipelineParallelFlow.DeterministicFearlessError(err.info);
        }
        return base$$Void_0.$self;
      }
      // treat certain JS errors as arithmetic-like exceptions
      if (err instanceof Error) {
        const msg = err.message || String(err);
        try {
          if (base$$Infos_0 && base$$Infos_0.$self && typeof base$$Infos_0.$self.msg$imm === "function") {
            sink_m$.pushError$mut(base$$Infos_0.$self.msg$imm(msg));
          } else {
            // best-effort fallback
            sink_m$.pushError$mut(msg);
          }
        } catch (inner) {
          // convert to deterministic if pushError throws FearlessError
          if (inner instanceof FearlessError) {
            throw new PipelineParallelFlow.DeterministicFearlessError(inner.info);
          }
          // otherwise rethrow inner
          throw inner;
        }
        return base$$Void_0.$self;
      }
      // unknown throwables: rethrow
      throw err;
    }
  }

  stopUp$mut() {
    if (this.sink != null) {
      try {
        this.sink.softClose();
      } catch (e) {
        // ignore softClose errors; proceed to stop original
      }
    }
    return this.original.stopUp$mut();
  }

  isRunning$mut() {
    return this.original.isRunning$mut();
  }

  for$mut(downstream_m$) {
    if (downstream_m$ instanceof PipelineParallelFlow.WrappedSink) {
      this.sink = downstream_m$;
    }
    try {
      return this.original.for$mut(downstream_m$);
    } catch (err) {
      if (err instanceof PipelineParallelFlow.DeterministicFearlessError) {
        throw err;
      }
      if (err instanceof FearlessError) {
        try {
          downstream_m$.pushError$mut(err.info);
        } catch (inner) {
          throw new PipelineParallelFlow.DeterministicFearlessError(inner.info);
        }
        return base$$Void_0.$self;
      }
      if (err instanceof Error) {
        const msg = err.message || String(err);
        try {
          if (base$$Infos_0 && base$$Infos_0.$self && typeof base$$Infos_0.$self.msg$imm === "function") {
            downstream_m$.pushError$mut(base$$Infos_0.$self.msg$imm(msg));
          } else {
            downstream_m$.pushError$mut(msg);
          }
        } catch (inner) {
          if (inner instanceof FearlessError) {
            throw new PipelineParallelFlow.DeterministicFearlessError(inner.info);
          }
          throw inner;
        }
        return base$$Void_0.$self;
      }
      throw err;
    }
  }

  split$mut() {
    return this.original.split$mut();
  }

  canSplit$read() {
    return this.original.canSplit$read();
  }
}
