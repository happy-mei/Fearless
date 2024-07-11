package rt.flows.dataParallel;

import base.Info_0;
import base.OptMatch_2;
import base.Void_0;
import base.flows.FlowOp_1;
import base.flows._Sink_1;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.RecursiveAction;

final class ForkJoinWorker extends RecursiveAction {
  private final FlowOp_1 source;
  private final _Sink_1 downstream;
  private final _Sink_1 original;
  private final ConcurrentLinkedQueue<Object> es;

  public ForkJoinWorker(FlowOp_1 source, _Sink_1 downstream) {
    this(source, downstream, new ConcurrentLinkedQueue<>());
  }

  private ForkJoinWorker(FlowOp_1 source, _Sink_1 downstream, ConcurrentLinkedQueue<Object> es) {
    this.source = source;
    this.original = downstream;
    this.downstream = new _Sink_1() {
      @Override public Void_0 stop$mut() {
        return downstream.stop$mut();
      }
      @Override public Void_0 pushError$mut(Info_0 info_m$) {
        return downstream.pushError$mut(info_m$);
      }
      @Override public Void_0 $hash$mut(Object x_m$) {
        es.offer(x_m$);
        return Void_0.$self;
      }
    };
    this.es = es;
  }

  public void forRemaining() {
    this.compute();
    for (var e : es) {
      original.$hash$mut(e);
    }
  }

  @Override protected void compute() {
    source.split$mut().match$mut(new OptMatch_2() {
      @Override public Object some$mut(Object split_) {
        var split = (FlowOp_1) split_;
        var rhsData = new ConcurrentLinkedQueue<>();
        var lhs = new ForkJoinWorker(source, downstream, es);
        var rhs = new ForkJoinWorker(split, downstream, rhsData);
        rhs.fork();
        lhs.compute();
        rhs.join();
        es.addAll(rhsData);
        return null;
      }
      @Override public Object empty$mut() {
        source.forRemaining$mut(downstream);
        return null;
      }
    });
  }
}
