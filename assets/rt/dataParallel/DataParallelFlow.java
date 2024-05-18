package rt.dataParallel;

import base.*;
import base.flows.*;

public record DataParallelFlow(base.flows.FlowOp_1 source_m$, base.Opt_1 size_m$, DataParallelFlowK $this) implements base.flows.Flow_1 {
  public base.flows.Flow_1 self$mut() {
    return  base.flows.Flow_1.self$mut$fun(this);
  }
  public base.flows.Flow_1 self$read() {
    return  base.flows.Flow_1.self$read$fun(this);
  }
  public base.flows.Flow_1 self$imm() {
    return  base.flows.Flow_1.self$imm$fun(this);
  }

  public Object let$mut(base.F_2 x_m$, base.Continuation_3 cont_m$) {
    return  base.flows.Flow_1.let$mut$fun(x_m$, cont_m$, this);
  }

  public base.flows.Flow_1 actorMut$mut(Object state_m$, base.flows.ActorImplMut_3 f_m$) {
    return (base.flows.Flow_1) _DataParallelInvalidStateful_0.$self.$exclamation$imm();
  }
  public base.flows.Flow_1 actor$mut(Object state_m$, base.flows.ActorImpl_3 f_m$) {
    return (base.flows.Flow_1) _DataParallelInvalidStateful_0.$self.$exclamation$imm();
  }
  public base.flows.Flow_1 limit$mut(long n_m$) {
    return (base.flows.Flow_1) _DataParallelInvalidStateful_0.$self.$exclamation$imm();
  }

  public base.Opt_1 first$mut() {
    return _SeqFlow_0.$self.fromOp$imm(source_m$, Opt_1.$self).first$mut();
  }

  public base.List_1 list$mut() {
    return base.flows._TerminalOps_1.list$mut$fun(this);
  }

  public base.Bool_0 all$mut(base.F_2 predicate_m$) {
    return  base.flows._TerminalOps_1.all$mut$fun(predicate_m$, this);
  }

  public base.flows.Flow_1 filter$mut(base.F_2 p_m$) {
    return $this.fromOp$imm(_Filter_0.$self.$hash$imm(_Sink_0.$self, source_m$, p_m$), Opt_1.$self);
  }

  public base.flows.Flow_1 flatMap$mut(base.F_2 f_m$) {
    return $this.fromOp$imm(_FlatMap_0.$self.$hash$imm(_Sink_0.$self, source_m$, f_m$), Opt_1.$self);
  }

  public base.Opt_1 findMap$mut(base.F_2 f_m$) {
    return _SeqFlow_0.$self.fromOp$imm(source_m$, size_m$).findMap$mut(f_m$);
  }

  public base.flows.FlowOp_1 unwrapOp$mut(base.flows._UnwrapFlowToken_0 fear55$_m$) {
    return source_m$;
  }

  public Object fold$mut(Object acc_m$, base.F_3 f_m$) {
    return _SeqFlow_0.$self.fromOp$imm(new ParallelSource(), Opt_1.$self).fold$mut(acc_m$, f_m$);
  }

  public Object fold$mut(Object acc_m$, base.F_3 f_m$, base.F_3 combine_m$) {
    return _SeqFlow_0.$self.fromOp$imm(new ParallelSource(), Opt_1.$self).fold$mut(acc_m$, f_m$, combine_m$);
  }

  public base.flows.Flow_1 map$mut(base.F_2 f_m$) {
    return $this.fromOp$imm(_Map_0.$self.$hash$imm(_Sink_0.$self, source_m$, f_m$), Opt_1.$self);
  }

  public base.Bool_0 any$mut(base.F_2 predicate_m$) {
    return base.flows._TerminalOps_1.any$mut$fun(predicate_m$, this);
  }

  public base.Opt_1 find$mut(base.F_2 predicate_m$) {
    return base.flows._TerminalOps_1.find$mut$fun(predicate_m$, this);
  }

  public base.Opt_1 max$mut(base.F_3 compare_m$) {
    return  base.flows._TerminalOps_1.max$mut$fun(compare_m$, this);
  }

  public Long size$mut() {
    return (Long) size_m$.match$read(new OptMatch_2() {
      @Override public Object some$mut(Object x_m$) {
        return x_m$;
      }
      @Override public Object empty$mut() {
        return fold$mut(0, (acc,n)->((long)acc) + ((long)n), (a,b) -> ((long)a) + ((long)b));
      }
    });
  }

  public base.flows.Flow_1 scan$mut(Object acc_m$, base.F_3 f_m$) {
    return  base.flows._NonTerminalOps_1.scan$mut$fun(acc_m$, f_m$, this);
  }

  public Object $hash$imm(base.Extension_2 ext_m$) {
    return  base.Extensible_1.$hash$imm$fun(ext_m$, this);
  }

  public Object $hash$read(base.Extension_2 ext_m$) {
    return  base.Extensible_1.$hash$read$fun(ext_m$, this);
  }

  public Object $hash$mut(base.Extension_2 ext_m$) {
    return  base.Extensible_1.$hash$mut$fun(ext_m$, this);
  }

  public base.Void_0 for$mut(base.F_2 f_m$) {
    return  base.flows._TerminalOps_1.for$mut$fun(f_m$, this);
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
      new ForRemaining(source_m$, downstream_m$).forRemaining();
      return Void_0.$self;
    }
    @Override public Opt_1 split$mut() {
      return source_m$.split$mut();
    }
  }
}
