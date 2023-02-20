package astFull;

import files.Pos;
import id.Id;
import id.Mdf;
import magic.Magic;
import failure.CompileError;
import failure.Fail;
import program.CM;
import program.TypeRename;
import utils.Bug;
import utils.OneOr;
import utils.Range;
import visitors.InjectionVisitor;
import visitors.ShallowInjectionVisitor;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Program implements program.Program{
  private final Map<Id.DecId, T.Dec> ds;
  public Program(Map<Id.DecId, T.Dec> ds) { this.ds = ds; }

  public List<ast.E.Lambda> lambdas() { throw Bug.unreachable(); }
  public program.Program withDec(ast.T.Dec d) {
    throw Bug.unreachable();
  }

  public Optional<Pos> posOf(Id.IT<ast.T> t) {
    return of(t).pos();
  }

  T.Dec of(Id.DecId d) {
    var res = ds.get(d);
    if (res == null) { res = Magic.getFullDec(this::of, d); }
    if (res == null) { throw Fail.traitNotFound(d); }
    return res;
  }

  T.Dec of(Id.IT<ast.T> t) {
    return of(t.name());
  }
  @Override public List<Id.IT<ast.T>> itsOf(Id.IT<ast.T> t){
    var d=of(t.name());
    assert t.ts().size()==d.gxs().size();
    var gxs=d.gxs().stream().map(gx->new Id.GX<ast.T>(gx.name())).toList();
    Function<Id.GX<ast.T>, ast.T> f = TypeRename.core().renameFun(t.ts(), gxs);
    return d.lambda().its().stream().map(ti->TypeRename.core().renameIT(liftIT(ti),f)).toList();
  }
  /** with t=C[Ts]  we do  C[Ts]<<Ms[Xs=Ts],*/
  @Override public List<CM> cMsOf(Id.IT<ast.T> t){
    var d=of(t.name());
    assert t.ts().size()==d.gxs().size();
    var gxs=d.gxs().stream().map(gx->new Id.GX<ast.T>(gx.name())).toList();
    Function<Id.GX<ast.T>, ast.T> f = TypeRename.core().renameFun(t.ts(), gxs);
    return d.lambda().meths().stream()
      .filter(mi->mi.sig().isPresent())
      .map(mi->cm(t,mi,f))
      .toList();
  }

  @Override public Set<Id.GX<ast.T>> gxsOf(Id.IT<ast.T> t) {
    return of(t).gxs().stream().map(Id.GX::toAstGX).collect(Collectors.toSet());
  }

  private CM cm(Id.IT<ast.T> t, astFull.E.Meth mi, Function<Id.GX<ast.T>, ast.T> f){
    // This is doing C[Ts]<<Ms[Xs=Ts] (hopefully)
    var sig=mi.sig().orElseThrow();
    var cm = CM.of(t, mi, TypeRename.coreRec().renameSig(new InjectionVisitor().visitSig(sig), f));
    return norm(cm);
  }
  public Map<Id.DecId, T.Dec> ds() { return this.ds; }

  private final HashMap<Id.DecId, Set<Id.DecId>> superDecIdsCache = new HashMap<>();
  public Set<Id.DecId> superDecIds(Id.DecId start) {
    if (superDecIdsCache.containsKey(start)) { return superDecIdsCache.get(start); }

    HashSet<Id.DecId> visited = new HashSet<>();
    superDecIds(visited, start);
    var res = Collections.unmodifiableSet(visited);
    superDecIdsCache.put(start, res);
    return res;
  }

  public void superDecIds(HashSet<Id.DecId> visited, Id.DecId current) {
    if (superDecIdsCache.containsKey(current)) {
      visited.addAll(superDecIdsCache.get(current));
      return;
    }

    var currentDec = of(current);
    for(var it : currentDec.lambda().its()) {
      var novel=visited.add(it.name());
      if(novel){ superDecIds(visited, it.name()); }
    }
  }

  /**
   * Applies inference 5a
   */
  public astFull.Program inferSignatures(){
    var is=new InferSignatures(this);
    for(int i: Range.of(is.decs)){
      var di = is.inferSignatures(is.decs.get(i));
      is.updateDec(di,i);
    }
    program.Program.reset();
    return is.p;
  }
  public ast.Program inferSignaturesToCore(){
    return new ShallowInjectionVisitor().visitProgram(inferSignatures());
  }
  public List<E.Meth> dom(Id.DecId id){ return this.of(id).lambda().meths(); }

  public static class InferSignatures {
    List<T.Dec> decs;
    Program p;
    InferSignatures(Program p){ this.p=p; this.decs = orderDecs(p.ds().values()); }
    private List<T.Dec> orderDecs(Collection<T.Dec>ds){
      return ds.stream().sorted(this::sortDec).collect(Collectors.toList());
    }
    private int sortDec(T.Dec d1,T.Dec d2){
      // TODO: This is not transitive (because we say == when there is no relation, not just on eq)
      // We may need to do a topological sort here or something
      System.out.println(d1.name()+" and "+d2.name());
      if (p.isSubType(new T(Mdf.mdf, d1.toIT()), new T(Mdf.mdf, d2.toIT()))) { return -1; }
      if (p.isSubType(new T(Mdf.mdf, d2.toIT()), new T(Mdf.mdf, d1.toIT()))) { return 1; }
//      if(p.superDecIds(d1.name()).contains(d2.name())) { return -1; }
//      if(p.superDecIds(d2.name()).contains(d1.name())) { return 1; }
      return 0;
    }
    private void updateDec(T.Dec d, int i) {
      decs.set(i,d);
      p=new Program(decs.stream().collect(Collectors.toMap(T.Dec::name, di->di)));
    }

    private T.Dec inferSignatures(T.Dec d) {
      return d.withLambda(inferSignatures(d, d.lambda().withSelfName("this")));
    }
    private E.Lambda inferSignatures(T.Dec dec, E.Lambda l) {
      if (l.selfName() == null) {
        l = l.withSelfName(new E.X(T.infer).name());
      }
      var ms = l.meths().stream().map(m->inferSignature(dec,m)).toList();
      return l.withMeths(ms);
    }
    Id.MethName onlyAbs(T.Dec dec){
      // depth doesn't matter here because we just extract the name
      var m = OneOr.of(()->Fail.cannotInferAbsSig(dec.name()), p.meths(dec.toAstT(), -1).stream().filter(CM::isAbs));
      return m.name();
    }
    E.Meth inferSignature(T.Dec dec, E.Meth m) {
      try {
        if(m.sig().isPresent()){ return m; }
        var name=m.name().orElseGet(() -> onlyAbs(dec));
        var namedMeth = m.withName(name);
        assert name.num()==namedMeth.xs().size();
        var inferred = p.meths(dec.toAstT(), name, 0)
          .orElseThrow(()->Fail.cannotInferSig(dec.name(), name));
        return namedMeth.withSig(inferred.sig().toAstFullSig());
      } catch (CompileError e) {
        throw e.pos(m.pos());
      }
    }
  }
  @Override public String toString() { return this.ds().toString(); }
}
