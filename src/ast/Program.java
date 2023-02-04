package ast;

import files.Pos;
import id.Id;
import magic.Magic;
import failure.Fail;
import program.CM;
import program.TypeRename;
import utils.Bug;
import visitors.InjectionVisitor;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Program implements program.Program  {
  private final Map<Id.DecId, T.Dec> ds;
  public Program(Map<Id.DecId, T.Dec> ds) { this.ds = ds; }

  public Map<Id.DecId, T.Dec> ds() { return this.ds; }
  public List<ast.E.Lambda> lambdas() { return this.ds().values().stream().map(T.Dec::lambda).toList(); }

  public Program withDec(T.Dec d) {
    var ds = new HashMap<>(ds());
    assert !ds.containsKey(d.name());
    ds.put(d.name(), d);
    return new Program(Collections.unmodifiableMap(ds));
  }

  public Optional<Pos> posOf(Id.IT<ast.T> t) {
    return of(t.name()).pos();
  }

  T.Dec of(Id.DecId d) {
    var res = ds.get(d);
    if (res == null) { res = new InjectionVisitor().visitDec(Magic.getDec(d)); }
    if (res == null) { throw Fail.traitNotFound(d); }
    return res;
  }

  @Override public List<Id.IT<T>> itsOf(Id.IT<T> t) {
    var d=of(t.name());
    assert t.ts().size()==d.gxs().size();
    var gxs=d.gxs().stream().map(gx->new Id.GX<ast.T>(gx.name())).toList();
    Function<Id.GX<T>, T> f = TypeRename.core().renameFun(t.ts(), gxs);
    return d.lambda().its().stream()
      .filter(ti->!ti.name().equals(t.name()))
      .map(ti->TypeRename.core().renameIT(ti,f))
      .toList();
  }
  @Override
  public List<CM> cMsOf(Id.IT<T> t) {
    var d=of(t.name());
    assert t.ts().size()==d.gxs().size();
    var gxs=d.gxs().stream().map(gx->new Id.GX<ast.T>(gx.name())).toList();
    Function<Id.GX<ast.T>, ast.T> f = TypeRename.core().renameFun(t.ts(), gxs);
    return d.lambda().meths().stream()
      .map(mi->cm(t,mi,f))
      .toList();
  }
  @Override public Set<Id.GX<T>> gxsOf(Id.IT<T> t) {
    return of(t.name()).gxs().stream().map(Id.GX::toAstGX).collect(Collectors.toSet());
  }

  @Override public String toString() { return this.ds.toString(); }

  private CM cm(Id.IT<ast.T> t, E.Meth mi, Function<Id.GX<ast.T>, ast.T> f){
    // This is doing C[Ts]<<Ms[Xs=Ts] (hopefully)
    var cm = CM.of(t, mi, TypeRename.coreRec().renameSig(mi.sig(), f));
    return norm(cm);
  }
}
