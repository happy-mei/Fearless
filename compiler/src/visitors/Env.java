package visitors;

import ast.E;
import ast.T;
import id.Id;
import id.Mdf;
import program.typesystem.XBs;
import utils.Err;
import utils.Mapper;
import utils.Push;

import java.util.HashMap;
import java.util.List;

import static java.util.Objects.requireNonNull;

public record Env(List<String> xs, List<T> ts, List<Id.GX<T>> gxs, XBs xbs, T decT, List<Id.MethName> ms, HashMap<String, Integer> usages) {
  public Env(){ this(List.of(), List.of(), List.of(), XBs.empty(), null, List.of(), new HashMap<>()); }
  public Env {
    assert xs.size()==ts.size();
    assert xs.stream().distinct().count()==xs.size();
    assert gxs.stream().distinct().count()==gxs.size();
    assert Err.ifMut(xs): "gxs should not be mutable";
    assert Err.ifMut(ts): "ts should not be mutable";
    assert Err.ifMut(gxs): "gxs should not be mutable";
  }
  public Env add(E.Meth m){
    return new Env(
      Push.of(xs, m.xs().stream().map(x->x.equals("_") ? astFull.E.X.freshName() : x).toList()),
      Push.of(ts,m.sig().ts()),
      Push.of(gxs, m.sig().gens()),
      xbs.addBounds(m.sig().gens(), m.sig().bounds()),
      decT.withMdf(m.sig().mdf()),
      Push.of(ms, m.name()),
      Mapper.ofMut(c->{
        c.putAll(usages);
        m.xs().forEach(x->c.put(x, 0));
      })
    );
  }
  public Env add(E.Lambda l, List<Id.MethName> ms){
    return new Env(
      Push.of(xs,l.selfName()),
      Push.of(ts,new T(l.mdf(), new Id.IT<>(Id.GX.fresh().name(), List.of()))),
      gxs,
      xbs.addBounds(l.name().gens(), l.name().bounds()),
      decT,
      Push.of(this.ms, ms),
      new HashMap<>()
    );
  }
  public Env add(List<Id.GX<T>>gxs){ return new Env(xs,ts,Push.of(gxs(),gxs),xbs,decT,ms,usages); }
  public Env add(E.X x, T t){ return add(x.name(),t); }
  public Env add(String x, T t){ return new Env(Push.of(xs,x),Push.of(ts,t),gxs,xbs,decT,ms,Mapper.ofMut(c->{
    c.putAll(usages);
    c.put(x, 0);
  })); }
  public Env add(Id.GX<T> gx){ return new Env(xs,ts,Push.of(gxs,gx),xbs,decT,ms,usages); }
  public Env add(T.Dec dec){ return new Env(xs,ts,Push.of(gxs,dec.gxs()),xbs.addBounds(dec.gxs(), dec.bounds()),new T(Mdf.readOnly, dec.toIT()),ms,usages); }
  public void addUsage(String x) { usages.computeIfPresent(x, (x_,n)->n+1); }
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
