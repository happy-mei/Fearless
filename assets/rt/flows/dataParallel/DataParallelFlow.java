package rt.flows.dataParallel;

import base.*;
import base.flows.*;
import rt.flows.dataParallel.eod.EODWorker;

import java.util.Objects;

public final class DataParallelFlow implements Flow_1 {
  private final FlowOp_1 source_m$;
  private final Opt_1 size_m$;
  private final DataParallelFlowK $this;
  private final long size;

  public DataParallelFlow(FlowOp_1 source_m$, Opt_1 size_m$, DataParallelFlowK $this) {
    this.source_m$ = source_m$;
    this.size_m$ = size_m$;
    this.$this = $this;
    this.size = (Long) size_m$.match$mut(new OptMatch_2() {
      @Override public Object some$mut(Object x_m$) {
        return x_m$;
      }
      @Override public Object empty$mut() {
        return -1L;
      }
    });
  }

  public Flow_1 self$mut() {
    return Flow_1.self$mut$fun(this);
  }

  public Flow_1 self$read() {
    return Flow_1.self$read$fun(this);
  }

  public Flow_1 self$imm() {
    return Flow_1.self$imm$fun(this);
  }

  public Object let$mut(F_2 x_m$, Continuation_3 cont_m$) {
    return Flow_1.let$mut$fun(x_m$, cont_m$, this);
  }

  public Flow_1 actorMut$mut(Object state_m$, ActorImplMut_3 f_m$) {
    return _PipelineParallelFlow_0.$self.fromOp$imm(source_m$, Opt_1.$self).actorMut$mut(state_m$, f_m$);
  }

  public Flow_1 actor$mut(Object state_m$, ActorImpl_3 f_m$) {
    return _PipelineParallelFlow_0.$self.fromOp$imm(source_m$, Opt_1.$self).actor$mut(state_m$, f_m$);
  }

  public Flow_1 limit$mut(long n_m$) {
    return _PipelineParallelFlow_0.$self.fromOp$imm(source_m$, (Opt_1)size_m$.match$imm(new OptMatch_2() {
      @Override public Opt_1 some$mut(Object x_m$) {
        return Opts_0.$self.$hash$imm(Math.min(n_m$, (long) x_m$));
      }
      @Override public Opt_1 empty$mut() {
        return Opt_1.$self;
      }
    })).limit$mut(n_m$);
  }

  public Opt_1 first$mut() {
    return _SeqFlow_0.$self.fromOp$imm(source_m$, size_m$).first$mut();
  }

  public List_1 list$mut() {
    return _TerminalOps_1.list$mut$fun(this);
  }

  public Bool_0 all$mut(F_2 predicate_m$) {
    return _TerminalOps_1.all$mut$fun(predicate_m$, this);
  }

  public Flow_1 filter$mut(F_2 p_m$) {
    return $this.fromOp$imm(_Filter_0.$self.$hash$imm(_Sink_0.$self, source_m$, p_m$), Opt_1.$self);
  }

  public Flow_1 flatMap$mut(F_2 f_m$) {
    return $this.fromOp$imm(_FlatMap_0.$self.$hash$imm(_Sink_0.$self, source_m$, f_m$), Opt_1.$self);
  }

  public Opt_1 findMap$mut(F_2 f_m$) {
    return _SeqFlow_0.$self.fromOp$imm(source_m$, size_m$).findMap$mut(f_m$);
  }

  public FlowOp_1 unwrapOp$mut(_UnwrapFlowToken_0 fear55$_m$) {
    return new ParallelSource();
  }

  public Object fold$mut(Object acc_m$, F_3 f_m$) {
    return _SeqFlow_0.$self.fromOp$imm(new ParallelSource(), size_m$).fold$mut(acc_m$, f_m$);
  }

  public Flow_1 map$mut(F_2 f_m$) {
    return $this.fromOp$imm(_Map_0.$self.$hash$imm(_Sink_0.$self, source_m$, f_m$), this.size_m$);
  }

  public Bool_0 any$mut(F_2 predicate_m$) {
    return _TerminalOps_1.any$mut$fun(predicate_m$, this);
  }

  public Opt_1 find$mut(F_2 predicate_m$) {
    return _TerminalOps_1.find$mut$fun(predicate_m$, this);
  }

  public Opt_1 max$mut(F_3 compare_m$) {
    return _TerminalOps_1.max$mut$fun(compare_m$, this);
  }

  public Long size$mut() {
    return (Long) size_m$.match$read(new OptMatch_2() {
      @Override public Object some$mut(Object x_m$) {
        return x_m$;
      }

      @Override public Object empty$mut() {
        return fold$mut(0L, (acc, _) -> ((long) acc) + 1);
      }
    });
  }

  public Flow_1 scan$mut(Object acc_m$, F_3 f_m$) {
    return _NonTerminalOps_1.scan$mut$fun(acc_m$, f_m$, this);
  }

  public Object $hash$imm(Extension_2 ext_m$) {
    return Extensible_1.$hash$imm$fun(ext_m$, this);
  }

  public Object $hash$read(Extension_2 ext_m$) {
    return Extensible_1.$hash$read$fun(ext_m$, this);
  }

  public Object $hash$mut(Extension_2 ext_m$) {
    return Extensible_1.$hash$mut$fun(ext_m$, this);
  }

  public Object join$mut(base.flows.Joinable_1 f) {
    return Flow_1.join$mut$fun(f, this);
  }

  public Void_0 for$mut(F_2 f_m$) {
    return _TerminalOps_1.for$mut$fun(f_m$, this);
  }

  public FlowOp_1 source_m$() {
    return source_m$;
  }

  public Opt_1 size_m$() {
    return size_m$;
  }

  public DataParallelFlowK $this() {
    return $this;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) return true;
    if (obj == null || obj.getClass() != this.getClass()) return false;
    var that = (DataParallelFlow) obj;
    return Objects.equals(this.source_m$, that.source_m$) &&
      Objects.equals(this.size_m$, that.size_m$) &&
      Objects.equals(this.$this, that.$this);
  }

  @Override
  public int hashCode() {
    return Objects.hash(source_m$, size_m$, $this);
  }

  @Override
  public String toString() {
    return "DataParallelFlow[" +
      "source_m$=" + source_m$ + ", " +
      "size_m$=" + size_m$ + ", " +
      "$this=" + $this + ']';
  }


  private class ParallelSource implements FlowOp_1 {
    @Override public Bool_0 isFinite$mut() {
      return source_m$.isFinite$mut();
    }

    @Override public Void_0 step$mut(_Sink_1 sink_m$) {
      return source_m$.step$mut(sink_m$);
    }

    @Override public Void_0 stop$mut() {
      return source_m$.stop$mut();
    }

    @Override public Bool_0 isRunning$mut() {
      return source_m$.isRunning$mut();
    }

    @Override public Void_0 forRemaining$mut(_Sink_1 downstream_m$) {
      EODWorker.forRemaining(source_m$, downstream_m$, (int) size);
//      nestLevel.incrementAndGet();
//      if (stats == null) {
//        stats = size >= 0 ? new Stats(size) : new Stats();
//      }
//      new ForRemaining(source_m$, downstream_m$).forRemaining();
//      if (nestLevel.decrementAndGet() == 0) {
////        stats = null;
//      }
      return Void_0.$self;
    }

    @Override public Opt_1 split$mut() {
      return source_m$.split$mut();
    }

    @Override public Bool_0 canSplit$read() {
      return source_m$.canSplit$read();
    }
  }
}
