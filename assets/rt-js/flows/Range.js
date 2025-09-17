import { base$$flows$$_SeqFlow_0 } from "../../../base/flows/index.js";
import { base$$Void_0, base$$Opt_1, base$$True_0, base$$False_0 } from "../../../base/index.js";

export class rt$$flows$$Range {
  static $self = new rt$$flows$$Range();

  $hash$imm(...args) {
    switch(args.length) {
      case 1: { // Infinite range: start
        const start_m$ = args[0];
        const op = new InfiniteRangeOp(start_m$);
        return base$$flows$$_SeqFlow_0.$self.fromOp$imm(op, base$$Opt_1.$self);
      }
      case 2: { // Finite range: start, end
        const [start_m$, end_m$] = args;
        const totalSize = end_m$ - start_m$;
        const op = new FiniteRangeOp(start_m$, end_m$);
        return base$$flows$$_SeqFlow_0.$self.fromOp$imm(op, base$$Opt_1.$self.$hash$imm(totalSize));
      }
      default:
        throw new Error(`No overload for $hash$imm with ${args.length} arguments`);
    }
  }
}

class FiniteRangeOp {
  constructor(start, end) {
    this.cursor = start;
    this.end = end;
  }

  isFinite$mut() { return base$$True_0.$self; }
  isRunning$mut() { return this.cursor < this.end ? base$$True_0.$self : base$$False_0.$self; }

  step$mut(sink_m$) {
    if (!this.isRunning()) { sink_m$.stopDown$mut(); return base$$Void_0.$self; }
    sink_m$.$hash$mut(this.cursor++);
    if (!this.isRunning()) { sink_m$.stopDown$mut(); }
    return base$$Void_0.$self;
  }

  stopUp$mut() { this.cursor = this.end; return base$$Void_0.$self; }

  for$mut(downstream_m$) {
    for (; this.cursor < this.end; this.cursor++) {
      downstream_m$.$hash$mut(this.cursor);
    }
    return downstream_m$.stopDown$mut();
  }

  split$mut() {
    const size = this.end - this.cursor;
    if (size <= 1) return base$$Opt_1.$self;
    const mid = this.cursor + Math.floor(size / 2);
    const end_ = this.end;
    this.end = mid;
    return base$$Opt_1.$self.$hash$imm(new FiniteRangeOp(mid, end_));
  }

  canSplit$read() { return (this.end - this.cursor) > 1 ? base$$True_0.$self : base$$False_0.$self; }
}

class InfiniteRangeOp {
  constructor(start) {
    this.cursor = start;
    this.isRunningFlag = true;
  }

  isFinite$mut() { return base$$False_0.$self; }
  isRunning$mut() { return this.isRunningFlag ? base$$True_0.$self : base$$False_0.$self; }

  step$mut(sink_m$) {
    if (!this.isRunningFlag) { sink_m$.stopDown$mut(); return base$$Void_0.$self; }
    sink_m$.$hash$mut(this.increment());
    if (!this.isRunningFlag) { sink_m$.stopDown$mut(); }
    return base$$Void_0.$self;
  }

  stopUp$mut() { this.isRunningFlag = false; return base$$Void_0.$self; }

  for$mut(downstream_m$) {
    while (this.isRunningFlag) {
      downstream_m$.$hash$mut(this.increment());
    }
    return downstream_m$.stopDown$mut();
  }

  split$mut() { return base$$Opt_1.$self; }
  canSplit$read() { return base$$False_0.$self; }

  increment() { return this.cursor++; }
}
