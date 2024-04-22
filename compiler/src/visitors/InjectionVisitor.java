package visitors;

import ast.T;
import astFull.E;
import failure.Fail;
import id.Id;
import id.Mdf;
import utils.Mapper;
import utils.Push;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class InjectionVisitor implements FullVisitor<ast.E>{
  public ast.E.MCall visitMCall(E.MCall e) {
    var recv = e.receiver().accept(this);
    if (e.ts().isEmpty()) {
      throw Fail.couldNotInferCallGenerics(e.name()).pos(e.pos());
    }
    try {
      return new ast.E.MCall(
        recv,
        e.name(),
        e.ts().get().stream().map(this::visitTInGens).toList(),
        e.es().stream().map(ei->ei.accept(this)).toList(),
        e.pos()
      );
    } catch (astFull.T.MatchOnInfer err) {
      throw Fail.inferFailed("Could not infer:\n"+e).pos(e.pos());
    }
  }

  public ast.E.X visitX(E.X e){
    return new ast.E.X(e.name(), e.pos());
  }

  public ast.E.Lambda visitLambda(E.Lambda e){
    var base = e.it().map(List::of).orElse(List.of());
    var its = Push.of(base, e.its().stream().filter(it->{
      if (base.isEmpty()) { return true; }
      return !it.name().equals(base.get(0).name());
    }).toList());//TODO: why the base could already be
    //in the its and not in the first place?
    var gxs= e.id().gens().stream().map(this::visitGX).toList();
    return new ast.E.Lambda(
      new ast.E.Lambda.LambdaId(
        e.id().id(),
        gxs,completeBounds(gxs,e.id().bounds())
      ),
      e.mdf().orElse(Mdf.mdf),
      its.stream().map(this::visitIT).toList(),
      Optional.ofNullable(e.selfName()).orElseGet(E.X::freshName),
      e.meths().stream().map(this::visitMeth).toList(),
      e.pos()
    );
  }
  
  Map<Id.GX<T>, Set<Mdf>> completeBounds(List<Id.GX<T>> gxs, Map<Id.GX<astFull.T>, Set<Mdf>> bounds){
    //TODO: when it works, remove comment below
    //gxs= gxs.stream().map(gx->new Id.GX<T>(gx.name())).toList();
    //Map<Id.GX<T>, Set<Mdf>> boundsGiven= Mapper.of(xbs->
    //  bounds.forEach((gx, bs)->xbs.put(new Id.GX<>(gx.name()), bs)));
    return Mapper.of(xbs->gxs.forEach(xi->{
      var currentB= bounds.getOrDefault(xi,defaultBounds);
      //yes it works across generic Id.GX<astFull.T> vs Id.GX<ast.T>
      //because of erasure + legacy get(Object)
      xbs.put(xi,currentB);
      }));
  }
  //TODO: below must be duplicated code. Find the place where they exists already
  private static final Set<Mdf> defaultBounds= Set.of(Mdf.mut,Mdf.imm,Mdf.read);
  public ast.T.Dec visitDec(astFull.T.Dec d){
    var gxs= d.gxs().stream().map(this::visitGX).toList();
    var lambda= this.visitLambda(d.lambda()//TODO: remove the line below when removing mess with its?
        .withITs(Push.of(d.toIT(), d.lambda().its())));
    return new ast.T.Dec(
      d.name(),
      gxs,completeBounds(gxs,d.bounds()),
      lambda,
      d.pos()
    );
  }

  public ast.T visitT(astFull.T t){
    if (t.isInfer()) {
      throw new astFull.T.MatchOnInfer();
    }
    return t.toAstT();
  }

  public ast.T visitTInGens(astFull.T t){
    if (t.isInfer()) {
      throw new astFull.T.MatchOnInfer();
    }
    return t.toAstT();
  }

  public Id.IT<ast.T> visitIT(Id.IT<astFull.T> t){
    return new Id.IT<>(
      t.name(),
      t.ts().stream().map(this::visitTInGens).toList()
    );
  }

  public ast.E.Meth visitMeth(E.Meth m){
    if (m.sig().isEmpty() || m.name().isEmpty()) {
      throw Fail.inferFailed(m.toString()).pos(m.pos());
    }
    ast.E.Sig sig; try { sig = visitSig(m.sig().orElseThrow()); }
    catch (astFull.T.MatchOnInfer err) {
      throw Fail.inferFailed(m.toString()).pos(m.pos());
    }
    return new ast.E.Meth(
      sig,
      m.name().orElseThrow(),
      m.xs(),
      m.body().map(b->b.accept(this)),
      m.pos()
    );
  }

  public ast.E.Sig visitSig(E.Sig s){
    return new ast.E.Sig(
      s.gens().stream().map(this::visitGX).toList(),
      Mapper.of(bounds->s.bounds().forEach((gx,bs)->bounds.put(visitGX(gx), bs))),
      s.ts().stream().map(this::visitT).toList(),
      this.visitT(s.ret()),
      s.pos()
    );
  }

  public ast.Program visitProgram(astFull.Program p){
    Map<Id.DecId, T.Dec> coreDs = p.ds().entrySet().stream()
      .collect(Collectors.toMap(Map.Entry::getKey, kv->visitDec(kv.getValue())));
    return new ast.Program(p.tsf(), coreDs, Map.of());
  }

  private Id.GX<ast.T> visitGX(Id.GX<? extends Id.Ty> gx){
    return new Id.GX<>(gx.name());
  }
}
