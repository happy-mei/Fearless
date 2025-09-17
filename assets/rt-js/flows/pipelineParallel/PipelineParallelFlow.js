import { FearlessError } from "../../FearlessError.js";

/* ---- Message sealed interface ---- */
export const Message = {
  Error: class {
    constructor(info) { this.info = info; }
  },
  Stop: (function () {
    class StopClass {}
    StopClass.INSTANCE = new StopClass();
    return StopClass;
  })()
};

/* ---- DeterministicFearlessError ---- */
export class DeterministicFearlessError extends FearlessError {
  constructor(info) { super(info); }
}

/* ---- WrappedSink and WrappedSinkK ---- */
export class WrappedSink {
  /**
   * @param {base$$flows$$_Sink_1} original
   */
  constructor(original) {
    this.original = original;
    this.subject = new Subject(original);
    this.softClosed = false;
  }

  stopDown$mut() {
    try {
      this.subject.submit(Message.Stop.INSTANCE);
      return this.subject.join();
    } catch (err) {
      // Mirror Java behavior: rethrow deterministic errors directly, wrap others
      if (err instanceof DeterministicFearlessError) throw err;
      if (err instanceof FearlessError) throw err;
      if (err instanceof Error) {
        const message = err.message && /stack overflow/i.test(err.message) ? "Stack overflowed" : err.message;
        throw new Error(message, { cause: err });
      }
      throw err;
    } finally {
      return base.Void_0.$self;
    }
  }

  $hash$mut(x$) {
    // submit message for processing
    this.subject.submit(x$);
    return base.Void_0.$self;
  }

  pushError$mut(info$) {
    this.subject.submit(new Message.Error(info$));
    return base.Void_0.$self;
  }

  softClose() {
    this.subject.softClosed = true;
  }
}

export class WrappedSinkK {
  static $self = new WrappedSinkK();
  $hash$imm(original) {
    return new WrappedSink(original);
  }
}

/* ---- Bounded async queue (Array + waiters) ---- */
class BoundedAsyncQueue {
  constructor(capacity) {
    this.capacity = capacity;
    this.buf = [];
    this.takers = []; // resolve functions waiting to take when empty
    this.spaceWaiters = []; // resolve functions waiting for space when full
  }

  // push item, resolving a take waiter if any
  _pushImmediate(item) {
    if (this.takers.length > 0) {
      const takeResolve = this.takers.shift();
      takeResolve({ value: item });
    } else {
      this.buf.push(item);
    }
  }

  // offer: try to push; returns true if succeeded, false if full
  offer(item) {
    if (this.buf.length < this.capacity) {
      this._pushImmediate(item);
      return true;
    }
    return false;
  }

  // push with waiting for space (returns a promise that resolves when pushed)
  async push(item, timeoutMs = 50) {
    // fast path
    if (this.offer(item)) return true;

    // otherwise loop until we can push; wait a short timeout then retry
    while (true) {
      // Wait for a short time (matches Java behaviour that waits on onEmpty with timeout)
      await new Promise(resolve => setTimeout(resolve, timeoutMs));
      if (this.offer(item)) return true;
    }
  }

  // take: returns a promise that resolves with the item
  take() {
    if (this.buf.length > 0) {
      const v = this.buf.shift();
      return Promise.resolve(v);
    }
    return new Promise(resolve => this.takers.push(resolve));
  }

  // non-blocking poll
  poll() {
    return this.buf.length > 0 ? this.buf.shift() : null;
  }
}

/* ---- Subject: worker that consumes buffer and dispatches to downstream ---- */
export class Subject {
  /**
   * @param {import('../../base/flows/_Sink_1.js').default} downstream
   */
  constructor(downstream) {
    this.downstream = downstream;
    this.buffer = new BoundedAsyncQueue(512);
    this.softClosed = false;
    this.exception = null;
    // start worker
    this._workerPromise = (async () => {
      try {
        await this._runLoop();
      } catch (e) {
        // capture exception for join
        this.exception = e;
      }
    })();
  }

  /**
   * Submit a message to the subject. This mirrors the Java submit behaviour:
   * - if softClosed and msg != Stop, silently drop
   * - otherwise attempt to offer; if full, wait briefly and retry
   */
  submit(msg) {
    if (this.softClosed && msg !== Message.Stop.INSTANCE) {
      return;
    }
    // try to offer fast path
    if (this.buffer.offer(msg)) return;
    // otherwise wait until space and push (this is async but we intentionally don't await so submit stays non-blocking)
    // this mirrors Java's blocking loop but without blocking the main thread
    this.buffer.push(msg).catch(err => { throw err; });
  }

  // private run loop
  async _runLoop() {
    while (true) {
      let msg;
      // Use poll to decide whether to immediately complete onEmpty semantics
      msg = this.buffer.poll();
      if (msg == null) {
        // no item - wait for next
        msg = await this.buffer.take();
      }

      if (msg === Message.Stop.INSTANCE) {
        try {
          this.downstream.stopDown$mut();
        } catch (err) {
          // If stopDown throws, capture as exception to be rethrown on join
          throw err;
        }
        break;
      }

      if (msg instanceof Message.Error) {
        this._processError(msg);
        continue;
      }

      await this._processDataMsg(msg);
    }
  }

  async join() {
    // wait for worker to finish
    await this._workerPromise;
    // ensure downstream stop is called (Java also calls downstream.stopDown in join)
    try {
      this.downstream.stopDown$mut();
    } catch (err) {
      // ignore - will be handled below
    }

    if (this.exception) {
      const e = this.exception;
      if (e instanceof DeterministicFearlessError) throw e;
      if (e instanceof FearlessError) throw e;
      if (e instanceof Error) throw e;
      throw new Error(String(e));
    }

    return base.Void_0.$self;
  }

  _processError(msg) {
    if (this.softClosed) return;
    this.softClosed = true;
    try {
      this.downstream.pushError$mut(msg.info);
    } catch (err) {
      if (err instanceof FearlessError) {
        // propagate as deterministic
        throw new DeterministicFearlessError(err.info);
      }
      throw err;
    }
  }

  async _processDataMsg(data) {
    if (this.softClosed) return;
    try {
      this.downstream.$hash$mut(data);
    } catch (err) {
      // Keep accepting messages but soft close and push error downstream
      this.softClosed = true;
      if (err instanceof FearlessError) {
        try {
          this.downstream.pushError$mut(err.info);
        } catch (err2) {
          throw new DeterministicFearlessError(err2.info);
        }
      } else if (err instanceof Error && /ArithmeticException|RangeError|DivideByZero|Overflow/i.test(err.message || "")) {
        // map arithmetic-like errors to Infos_0.msg$imm(rt.Str)
        // best effort: if base.Infos_0 exists, use it; else push plain message
        try {
          const msg = (err && err.message) ? err.message : String(err);
          if (base && base.Infos_0 && base.Infos_0.$self && base.Infos_0.$self.msg$imm) {
            this.downstream.pushError$mut(base.Infos_0.$self.msg$imm(/* rt.Str.fromJavaStr(msg) */ msg));
          } else {
            this.downstream.pushError$mut(msg);
          }
        } catch (err2) {
          if (err2 instanceof FearlessError) throw new DeterministicFearlessError(err2.info);
          throw err2;
        }
      } else {
        // generic rethrow
        throw err;
      }
    }
  }
}

/* ---- Export default container object to match Java top-level interface usage ---- */
export const PipelineParallelFlow = {
  WrappedSinkK,
  WrappedSink,
  Message,
  Subject,
  DeterministicFearlessError
};
