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
//    this:mut base.Ref[mdf X] .swap/1[-]([f:imm base.UpdateRef[mdf X] #/1[-]([this:mut base.Ref[recMdf X] */0[]([]):recMdf X]):infer]):mdf X

    // [-imm base.Let[]-][base.Let[]]{ } #/1[-]([[- infer-][]{ .var/0([]): [-] -> this:infer .swap/1[-]([x:infer]):infer,
    // .in/1([_]): [-] -> [-imm base.Void[]-][base.Void[]]{ }}]):imm base.Void[]

//    this:imm base.OptDo[imm T] #/1[infer]([x:imm T]):imm base.Void[]
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
    var its = Push.of(base, e.its());
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
