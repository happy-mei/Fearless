package visitors;

import astFull.E;
import astFull.T;

import java.util.Optional;

public class ShallowInjectionVisitor extends InjectionVisitor implements FullVisitor<ast.E> {
  public ast.E.Meth visitMeth(E.Meth m) {
    if (m.body().isPresent()) {
      m = m.withBody(Optional.of(new E.X("this", T.infer, m.pos())));
    }

    // keep if we don't want to mess with pos for now call super
//    return new ast.E.Meth(
//      visitSig(m.sig().orElseThrow()),
//      m.name().orElseThrow(),
//      m.xs(),
//      new ast.E.X()
//    );
    return super.visitMeth(m);
  }
}
