import { base$$True_0, base$$False_0, base$$Void_0, base$$Opt_1, base$$Opts_0 } from "../../base/index.js";

export const SpliteratorFlowOp = {
  of(iterable) {
    // Convert iterable to an array for splitting & tracking
    const arr = Array.from(iterable);
    let index = 0;
    let hasStopped = false;

    return {
      isFinite$mut() {
        return base$$True_0.$self;
      },

      step$mut(sink_m$) {
        if (hasStopped || index >= arr.length) {
          hasStopped = true;
          sink_m$.stopDown$mut();
        } else {
          sink_m$.$hash$mut(arr[index++]);
        }
        return base$$Void_0.$self;
      },

      stopUp$mut() {
        hasStopped = true;
        return base$$Void_0.$self;
      },

      isRunning$mut() {
        return hasStopped ? base$$False_0.$self : base$$True_0.$self;
      },

      for$mut(downstream_m$) {
        while (index < arr.length) {
          downstream_m$.$hash$mut(arr[index++]);
        }
        downstream_m$.stopDown$mut();
        hasStopped = true;
        return base$$Void_0.$self;
      },

      split$mut() {
        const remaining = arr.length - index;
        if (remaining <= 1) return base$$Opt_1.$self;
        const mid = index + Math.floor(remaining / 2);
        const firstHalf = arr.slice(index, mid);
        index = mid; // leave second half for this iterator
        return base$$Opts_0.$self.$hash$imm(SpliteratorFlowOp.of(firstHalf));
      },

      canSplit$read() {
        return (arr.length - index) > 1 ? base$$True_0.$self : base$$False_0.$self;
      }
    };
  }
};
