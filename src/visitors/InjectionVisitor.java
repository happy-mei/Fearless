package visitors;

import ast.T;
import astFull.E;
import id.Id;
import id.Mdf;
import utils.Bug;
import utils.Push;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class InjectionVisitor implements FullVisitor<ast.E>{
  public ast.E.MCall visitMCall(E.MCall e) {
    // TODO: WHYYYYYY
//    [-imm base.False[]-][base.False[]]{ } .or/1[]([[-imm base.Bool[]-][base.True[]]{ }]):imm base.Bool[] ?/1[imm base.Num[]]([[-mut base.ThenElse[imm X0/0$]-][]{
//    .then/0([]): Sig[mdf=mut,gens=[],ts=[],ret=infer] -> [-imm 42[]-][42[]]{ },
//    .else/0([]): Sig[mdf=mut,gens=[],ts=[],ret=infer] -> [-imm 0[]-][0[]]{ }}]):imm X0/0$
    assert e.ts().isPresent();
    return new ast.E.MCall(
      e.receiver().accept(this),
      e.name(),
      e.ts().orElseThrow().stream().map(this::visitT).toList(),
      e.es().stream().map(ei->ei.accept(this)).toList(),
      e.pos()
    );
  }

  public ast.E.X visitX(E.X e){
    return new ast.E.X(e.name(), e.pos());
  }

  public ast.E.Lambda visitLambda(E.Lambda e){
    var base = e.it().map(List::of).orElseGet(List::of);
    var its = Push.of(base, e.its().stream().filter(it->{
      if (base.isEmpty()) { return true; }
      return !it.equals(base.get(0));
    }).toList());
    return new ast.E.Lambda(
      e.mdf().orElseThrow(),
      its.stream().map(this::visitIT).toList(),
      Optional.ofNullable(e.selfName()).orElseGet(E.X::freshName),
      e.meths().stream().map(this::visitMeth).toList(),
      e.pos()
    );
  }

  public ast.T.Dec visitDec(astFull.T.Dec d){
    return new ast.T.Dec(
      d.name(),
      d.gxs().stream().map(gx->new Id.GX<ast.T>(gx.name())).toList(),
      this.visitLambda(d.lambda().withMdf(Mdf.mdf).withITs(Push.of(d.toIT(), d.lambda().its()))),
      d.pos()
    );
  }

  public ast.T visitT(astFull.T t){
    if (t.isInfer()) {
      // TODO: throw Fail.....
      throw Bug.todo();
    }
    return t.toAstT();
  }

  public Id.IT<ast.T> visitIT(Id.IT<astFull.T> t){
    return new Id.IT<>(
      t.name(),
      t.ts().stream().map(this::visitT).toList()
    );
  }

  public ast.E.Meth visitMeth(E.Meth m){
    return new ast.E.Meth(
      visitSig(m.sig().orElseThrow()),
      m.name().orElseThrow(),
      m.xs(),
      m.body().map(b->b.accept(this)),
      m.pos()
    );
  }

  public ast.E.Sig visitSig(E.Sig s){
    return new ast.E.Sig(
      s.mdf(),
      s.gens().stream().map(this::visitGX).toList(),
      s.ts().stream().map(this::visitT).toList(),
      this.visitT(s.ret()),
      s.pos()
    );
  }

  public ast.Program visitProgram(astFull.Program p){
    Map<Id.DecId, T.Dec> coreDs = p.ds().entrySet().stream()
      .collect(Collectors.toMap(kv->kv.getKey(), kv->visitDec(kv.getValue())));
    return new ast.Program(coreDs);
  }

  private Id.GX<ast.T> visitGX(Id.GX<astFull.T> gx){
    return new Id.GX<>(gx.name());
  }
}
