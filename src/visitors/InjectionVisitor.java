package visitors;

import ast.T;
import astFull.E;
import astFull.PosMap;
import id.Id;
import id.Mdf;
import utils.Push;

import java.util.Map;
import java.util.stream.Collectors;

public class InjectionVisitor implements FullVisitor<ast.E>{
  public ast.E.MCall visitMCall(E.MCall e) {
    return PosMap.replace(e, new ast.E.MCall(
      e.receiver().accept(this),
      e.name(),
      e.ts().orElseThrow().stream().map(this::visitT).toList(),
      e.es().stream().map(ei->ei.accept(this)).toList()
    ));
  }

  public ast.E.X visitX(E.X e){
    return new ast.E.X(e.name());
  }

  public ast.E.Lambda visitLambda(E.Lambda e){
    return PosMap.replace(e, new ast.E.Lambda(
      e.mdf().orElseThrow(),
      e.its().stream().map(this::visitIT).toList(),
      e.selfName(),
      e.meths().stream().map(this::visitMeth).toList()
    ));
  }

  public ast.T.Dec visitDec(astFull.T.Dec d){
    return PosMap.replace(d, new ast.T.Dec(
      d.name(),
      d.gxs().stream().map(gx->new Id.GX<ast.T>(gx.name())).toList(),
      this.visitLambda(d.lambda().withMdfP(Mdf.mdf).withITsP(Push.of(d.toIT(), d.lambda().its())))
    ));
  }

  public ast.T visitT(astFull.T t){
    return t.toAstT();
  }

  public Id.IT<ast.T> visitIT(Id.IT<astFull.T> t){
    return new Id.IT<>(
      t.name(),
      t.ts().stream().map(this::visitT).toList()
    );
  }

  public ast.E.Meth visitMeth(E.Meth m){
    return PosMap.replace(m, new ast.E.Meth(
      visitSig(m.sig().orElseThrow()),
      m.name().orElseThrow(),
      m.xs(),
      m.body().map(b->b.accept(this))
    ));
  }

  public ast.E.Sig visitSig(E.Sig s){
    return new ast.E.Sig(
      s.mdf(),
      s.gens().stream().map(this::visitGX).toList(),
      s.ts().stream().map(this::visitT).toList(),
      this.visitT(s.ret())
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
