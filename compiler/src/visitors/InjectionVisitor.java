package visitors;

import ast.T;
import astFull.E;
import failure.Fail;
import id.Id;
import id.Mdf;
import program.typesystem.XBs;
import utils.Mapper;

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
    List<ast.T> ts;
    try {ts= e.ts().get().stream().map(this::visitTInGens).toList();}
    catch (astFull.T.MatchOnInfer err) {
      throw Fail.inferFailed("Could not infer:\n"+e).pos(e.pos());
    }
    return new ast.E.MCall(recv,e.name(),ts,       
      e.es().stream().map(ei->ei.accept(this)).toList(),
      e.pos());
  }
  public ast.E.X visitX(E.X e){
    return new ast.E.X(e.name(), e.pos());
  }
  public ast.E.Lambda visitLambda(E.Lambda e){
    var inferredType= e.it().map(List::of).orElse(List.of());
    var its= e.its().isEmpty() ? inferredType : e.its();
    var gxs= e.id().gens().stream().map(this::visitGX).toList();
    var lambdaId= new ast.E.Lambda.LambdaId(
        e.id().id(),
        gxs,completeBounds(gxs,e.id().bounds())
      );
    Mdf lambdaMdf=e.mdf()
      //.or(()->e.it().map(..)) TODO: No mdf in e.it(). Where do we infer it? 
      .orElse(Mdf.imm); 
    return new ast.E.Lambda(
      lambdaId, lambdaMdf,
      its.stream().map(this::visitIT).toList(),
      Optional.ofNullable(e.selfName()).orElseGet(E.X::freshName),
      e.meths().stream().map(this::visitMeth).toList(),
      e.pos()
    );
  }  
  Map<Id.GX<T>, Set<Mdf>> completeBounds(
      List<Id.GX<T>> gxs, Map<Id.GX<astFull.T>, Set<Mdf>> bounds){
    return Mapper.of(xbs->gxs.forEach(xi->
      xbs.put(xi,bounds.getOrDefault(xi,XBs.defaultBounds))));
    //Note: it works across generic Id.GX<astFull.T> vs Id.GX<ast.T>
    //because of erasure + legacy get(Object)
  }
  public ast.T.Dec visitDec(astFull.T.Dec d){
    var lambda= this.visitLambda(d.lambda());
    return new ast.T.Dec(lambda);
  }

  public ast.T visitT(astFull.T t){
    return t.toAstT();//throws MatchOnInfer if t == ?
  }

  public ast.T visitTInGens(astFull.T t){
    return t.toAstT();//throws MatchOnInfer if t == ?
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
    var gxs= s.gens().stream().map(this::visitGX).toList();
    return new ast.E.Sig(
      gxs, completeBounds(gxs,s.bounds()),
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