package visitors;

import astFull.E;
import id.Id;

public class InjectionVisitor implements FullVisitor<ast.E> {
  public ast.E visitMCall(E.MCall e) {
    return new ast.E.MCall(
      e.receiver().accept(this),
      e.name(),
      e.ts().orElseThrow().stream().map(this::visitT).toList(),
      e.es().stream().map(ei->ei.accept(this)).toList()
    );
  }

  public ast.E visitX(E.X e) {
    return new ast.E.X(e.name());
  }

  public ast.E visitLambda(E.Lambda e) {
    return new ast.E.Lambda(
      e.mdf().orElseThrow(),
      e.its().stream().map(this::visitIT).toList(),
      e.selfName(),
      e.meths().stream().map(this::visitMeth).toList()
    );
  }

  public ast.T visitT(astFull.T t) {
    return t.toAstT();
  }

  public Id.IT<ast.T> visitIT(Id.IT<astFull.T> t) {
    return new Id.IT<>(
      t.name(),
      t.ts().stream().map(this::visitT).toList()
    );
  }

  public ast.E.Meth visitMeth(E.Meth m) {
    return new ast.E.Meth(
      visitSig(m.sig().orElseThrow()),
      m.name().orElseThrow(),
      m.xs(),
      m.body().map(b->b.accept(this))
    );
  }

  public ast.E.Sig visitSig(E.Sig s) {
    return new ast.E.Sig(
      s.mdf(),
      s.gens().stream().map(this::visitGX).toList(),
      s.ts().stream().map(this::visitT).toList(),
      this.visitT(s.ret())
    );
  }

  private Id.GX<ast.T> visitGX(Id.GX<astFull.T> gx) {
    return new Id.GX<>(gx.name());
  }
}
