import {
  base$$flows$$Flow_1,
  base$$flows$$_SeqFlow_0,
  base$$flows$$_TerminalOps_1,
  base$$flows$$_NonTerminalOps_1,
  base$$flows$$_With_0,
  base$$flows$$_Map_0,
  base$$flows$$_AssumeFinite_1,
  base$$flows$$_Filter_0,
  base$$flows$$_FlatMap_0,
} from "../../../base/flows/index.js";
import { base$$Opt_1 } from "../../../base/Opt_1.js";
import { base$$Extensible_1 } from "../../../base/Extensible_1.js";
import { ConvertFromDataParallel } from "../pipelineParallel/ConvertFromDataParallel.js";

// near the top of the file (after imports)
const ID_SINK_DECORATOR = {
  $hash$imm: s => s,
  $hash$read: s => s,
  $hash$mut: s => s
};

export class DataParallelFlow {
  constructor(source_m$, size_m$, $this) {
    this.source_m$ = source_m$;
    this.size_m$ = size_m$;
    this.$this = $this;

    this.size = this.size_m$.match$mut({
      some$mut: x => x,
      empty$mut: () => -1
    });
  }

  // Flow_1 helpers
  self$mut() { return base$$flows$$Flow_1.self$mut$fun(this); }
  self$read() { return base$$flows$$Flow_1.self$read$fun(this); }
  self$imm() { return base$$flows$$Flow_1.self$imm$fun(this); }
  let$mut(x_m$, cont_m$) { return base$$flows$$Flow_1.let$mut$fun(x_m$, cont_m$, this); }

  actorMut$mut(state_m$, f_m$) { return ConvertFromDataParallel.of(this, this.size_m$).actorMut$mut(state_m$, f_m$); }
  actor$mut(state_m$, f_m$) { return ConvertFromDataParallel.of(this, this.size_m$).actor$mut(state_m$, f_m$); }
  limit$mut(n_m$) { return ConvertFromDataParallel.of(this, this.size_m$).limit$mut(n_m$); }

  with$mut(other_m$) {
    const op = base$$flows$$_With_0.$self.$hash$imm(ID_SINK_DECORATOR, this.source_m$, other_m$.unwrapOp$mut(null));
    return this.$this.fromOp$imm(op, base$$Opt_1.$self);
  }

  first$mut(predicate_m$) {
    return base$$flows$$_SeqFlow_0.$self.fromOp$imm(this.getDataParallelSource(), this.size_m$).first$mut(predicate_m$);
  }
  last$mut() { return base$$flows$$_TerminalOps_1.last$mut$fun(this); }
  list$mut() { return base$$flows$$_TerminalOps_1.list$mut$fun(this); }
  all$mut(predicate_m$) {
    const res = this.filter$mut(x => predicate_m$.$hash$read(x).not$imm()).first$mut();
    return res.isEmpty$read();
  }
  none$mut(predicate_m$) { return base$$flows$$_TerminalOps_1.none$mut$fun(predicate_m$, this); }

  filter$mut(p_m$) { return this.$this.fromOp$imm(base$$flows$$_Filter_0.$self.$hash$imm(ID_SINK_DECORATOR, this.source_m$, p_m$), base$$Opt_1.$self); }
  flatMap$mut(f_m$) { return this.$this.fromOp$imm(base$$flows$$_FlatMap_0.$self.$hash$imm(ID_SINK_DECORATOR, this.source_m$, f_m$), base$$Opt_1.$self); }

  findMap$mut(f_m$) {
    return this
      .map$mut(f_m$)
      .filter$mut(res => res.isSome$read())
      .first$mut()
      .flatMap$mut({
        some$mut: x => OptFlatMap_2.some$mut$fun(x, this),
        empty$mut: () => OptFlatMap_2.empty$mut$fun(this),
        $hash$mut: t => t
      });
  }

  unwrapOp$mut() { return this.source_m$; }

  fold$mut(acc_m$, f_m$) {
    return base$$flows$$_SeqFlow_0.$self.fromOp$imm(this.getDataParallelSource(), this.size_m$).fold$mut(acc_m$, f_m$);
  }
  only$mut() { return base$$flows$$_SeqFlow_0.$self.fromOp$imm(this.getDataParallelSource(), this.size_m$).only$mut(); }
  get$mut() { return base$$flows$$_SeqFlow_0.$self.fromOp$imm(this.getDataParallelSource(), this.size_m$).get$mut(); }
  opt$mut() { return base$$flows$$_SeqFlow_0.$self.fromOp$imm(this.getDataParallelSource(), this.size_m$).opt$mut(); }

  map$mut(f_m$) { return this.$this.fromOp$imm(base$$flows$$_Map_0.$self.$hash$imm(s => s, this.source_m$, f_m$), this.size_m$); }
  map$mut_withConv(c, f_m$) { return ConvertFromDataParallel.of(this, this.size_m$).map$mut(c, f_m$); }

  mapFilter$mut(f_m$) { return base$$flows$$_NonTerminalOps_1.mapFilter$mut$fun(f_m$, this); }
  assumeFinite$mut() { return this.$this.fromOp$imm(base$$flows$$_AssumeFinite_1.$self.$hash$read(this.source_m$), this.size_m$); }
  peek$mut(f_m$) { return base$$flows$$_NonTerminalOps_1.peek$mut$fun(f_m$, this); }
  peek$mut_withConv(c, f_m$) { return ConvertFromDataParallel.of(this, this.size_m$).peek$mut(c, f_m$); }

  any$mut(predicate_m$) { return this.filter$mut(predicate_m$).first$mut().isSome$read(); }
  find$mut(predicate_m$) { return base$$flows$$_TerminalOps_1.find$mut$fun(predicate_m$, this); }
  max$mut(compare_m$) { return base$$flows$$_TerminalOps_1.max$mut$fun(compare_m$, this); }

  count$mut() { return this.size >= 0 ? this.size : this.fold$mut(() => 0, (acc, _) => acc + 1); }
  size$read() { return this.size_m$; }
  scan$mut(acc_m$, f_m$) { return base$$flows$$_NonTerminalOps_1.scan$mut$fun(acc_m$, f_m$, this); }

  $hash$imm(ext_m$) { return base$$Extensible_1.$hash$imm$fun(ext_m$, this); }
  $hash$read(ext_m$) { return base$$Extensible_1.$hash$read$fun(ext_m$, this); }
  $hash$mut(ext_m$) { return base$$Extensible_1.$hash$mut$fun(ext_m$, this); }

  join$mut(f) { return base$$flows$$Flow_1.join$mut$fun(f, this); }
  for$mut(f_m$) { return base$$flows$$_TerminalOps_1.for$mut$fun(f_m$, this); }
  forEffect$mut(f_m$) { return base$$flows$$_TerminalOps_1.forEffect$mut$fun(f_m$, this); }

  equals(obj) {
    if (obj === this) return true;
    if (!obj || obj.constructor !== DataParallelFlow) return false;
    return this.source_m$ === obj.source_m$ && this.size_m$ === obj.size_m$ && this.$this === obj.$this;
  }

  hashCode() {
    return JSON.stringify([this.source_m$, this.size_m$, this.$this]).split("").reduce((h, c) => ((h << 5) + h + c.charCodeAt(0)) | 0, 5381);
  }

  toString() {
    return `DataParallelFlow[source_m$=${this.source_m$}, size_m$=${this.size_m$}, $this=${this.$this}]`;
  }

  getDataParallelSource() {
    const outer = this;
    return {
      isFinite$mut: () => outer.source_m$.isFinite$mut(),
      step$mut: sink => outer.source_m$.step$mut(sink),
      stopUp$mut: () => outer.source_m$.stopUp$mut(),
      isRunning$mut: () => outer.source_m$.isRunning$mut(),
      for$mut: downstream => base$$flows$$_SeqFlow_0.$self.fromOp$imm(outer.source_m$, outer.size_m$).for$mut(downstream),
      split$mut: () => outer.source_m$.split$mut(),
      canSplit$read: () => outer.source_m$.canSplit$read()
    };
  }

}

DataParallelFlow.$self = new DataParallelFlow(null, base$$Opt_1.$self, null);
