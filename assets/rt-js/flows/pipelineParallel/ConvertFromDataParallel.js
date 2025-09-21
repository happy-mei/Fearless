import { base$$flows$$_PipelineParallelFlow_0, base$$flows$$FlowOp_1 } from "../../../base/flows/index.js";
import { base$$True_0, base$$False_0, base$$Void_0, base$$Opt_1 } from "../../../base/index.js";
import { PipelineParallelFlow } from "./PipelineParallelFlow.js";

export const ConvertFromDataParallel = {
  /**
   * @param {DataParallelFlow} source
   * @param {base$$Opt_1} size
   */
  of(source, size) {
    const dpSource = source.getDataParallelSource();
    const dpConsumer = new DPSource(dpSource);

    return base$$flows$$_PipelineParallelFlow_0.$self.fromOp$imm$2(dpConsumer, size);
  }
};

/**
 * Async queue helper (replacement for Java BlockingDeque)
 */
class AsyncQueue {
  constructor() {
    this.items = [];
    this.waiters = [];
  }

  push(item) {
    if (this.waiters.length > 0) {
      const waiter = this.waiters.shift();
      waiter({ value: item, done: false });
    } else {
      this.items.push(item);
    }
  }

  async take() {
    if (this.items.length > 0) {
      return this.items.shift();
    }
    return new Promise(resolve => {
      this.waiters.push(resolve);
    }).then(r => r.value);
  }
}

/**
 * DPSource implements base$$flows$$FlowOp_1
 */
class DPSource {
  #buffer = new AsyncQueue();
  #isRunning = true;
  #hasStarted = false;
  #source;

  /**
   * @param {any} source DataParallelFlow.DataParallelSource
   */
  constructor(source) {
    this.#source = source;
  }

  start() {
    if (this.#hasStarted) throw new Error("start called twice on DPSource");
    this.#hasStarted = true;

    // simulate async "thread"
    (async () => {
      await this.#source.for$mut$1({
        stopDown$mut$0: () => {
          this.#buffer.push(PipelineParallelFlow.Message.Stop.INSTANCE);
          return base$$Void_0.$self;
        },
        pushError$mut$1: (info_m$) => {
          this.#buffer.push(new PipelineParallelFlow.Message.Error(info_m$));
          return base$$Void_0.$self;
        },
        $hash$mut$1: (x_m$) => {
          this.#buffer.push(x_m$);
          return base$$Void_0.$self;
        }
      });
    })();
  }

  isFinite$mut$0() {
    return this.#source.isFinite$mut();
  }

  async step$mut$1(sink_m$) {
    if (this.isRunning$mut() === base$$False_0.$self) {
      return base$$Void_0.$self;
    }
    if (!this.#hasStarted) {
      this.start();
    }

    const sink = /** @type {PipelineParallelFlow.WrappedSink} */ (sink_m$);

    const msg = await this.#buffer.take();
    sink.subject.submit(msg);

    if (msg === PipelineParallelFlow.Message.Stop.INSTANCE) {
      this.#isRunning = false;
    }

    if (!this.#isRunning) {
      sink.stopDown$mut$0();
      this.stopUp$mut$0();
    }

    return base$$Void_0.$self;
  }

  stopUp$mut$0() {
    this.#isRunning = false;
    this.#source.stopUp$mut$0();
    return base$$Void_0.$self;
  }

  isRunning$mut$0() {
    return this.#isRunning ? base$$True_0.$self : base$$False_0.$self;
  }

  for$mut$1(downstream_m$) {
    return base$$flows$$FlowOp_1.for$mut$2$fun(downstream_m$, this);
  }

  split$mut$0() {
    return base$$Opt_1.$self;
  }

  canSplit$read$0() {
    return base$$False_0.$self;
  }
}
