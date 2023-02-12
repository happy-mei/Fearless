package visitors;

import astFull.E;
import astFull.T;
import id.Id;
import id.Mdf;
import utils.Err;
import utils.Push;

import java.util.Collections;
import java.util.List;

import static java.util.Objects.requireNonNull;

public record Env(List<String> xs,List<T> ts, List<Id.GX<T>> gxs, T decT) {
  public Env(){ this(List.of(), List.of(), List.of(), null); }
  public Env{
    assert xs.size()==ts.size();
    assert xs.stream().distinct().count()==xs.size();
    assert gxs.stream().distinct().count()==gxs.size();
    assert Err.ifMut(xs): "gxs should not be mutable";
    assert Err.ifMut(ts): "ts should not be mutable";
    assert Err.ifMut(gxs): "gxs should not be mutable";
  }
  public Env add(E.Meth m){
    return new Env(
      Push.of(xs,m.xs()),
      Push.of(ts,m.sig().map(E.Sig::ts)
        .orElseGet(()->Collections.nCopies(m.xs().size(), T.infer))),
      m.sig().map(gx->Push.of(gxs,gx.gens())).orElse(List.of()),
      decT.withMdf(m.sig().map(E.Sig::mdf).orElse(decT.mdf()))
    );
  }
  public Env add(List<Id.GX<T>>gxs){ return new Env(xs,ts,Push.of(gxs(),gxs),decT); }
  public Env add(E.X x,T t){ return add(x.name(),t); }
  public Env add(String x,T t){ return new Env(Push.of(xs,x),Push.of(ts,t),gxs,decT); }
  public Env add(Id.GX<T> gx){ return new Env(xs,ts,Push.of(gxs,gx),decT); }
  public Env add(T.Dec dec){ return new Env(xs,ts,Push.of(gxs,dec.gxs()),new T(Mdf.read, dec.toIT())); }
  public T get(E.X x){ return get(x.name()); }
  public T get(String x){
    if (x.equals("this")) { return requireNonNull(decT); }
    var res=xs.indexOf(x);
    assert res!=-1;
    return ts.get(res);
  }
  public boolean has(String x){return x.equals("this") || xs.contains(x); }
  public boolean has(E.X x){ return has(x.name()); }
  public boolean has(Id.GX<T> gx){ return gxs.contains(gx); }
}
