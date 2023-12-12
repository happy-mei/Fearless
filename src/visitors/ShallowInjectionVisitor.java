package visitors;

import astFull.E;
import astFull.T;
import id.Id;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ShallowInjectionVisitor extends InjectionVisitor implements FullVisitor<ast.E> {
  @Override public ast.E.Meth visitMeth(E.Meth m) {
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

  @Override public ast.Program visitProgram(astFull.Program p){
    Map<Id.DecId, ast.T.Dec> coreDs = p.ds().entrySet().stream()
      .collect(Collectors.toMap(Map.Entry::getKey, kv->visitDec(kv.getValue())));
    Map<Id.DecId, ast.T.Dec> inlineDs = p.inlineDs().entrySet().stream()
      .collect(Collectors.toMap(Map.Entry::getKey, kv->visitDec(removeInlineMs(kv.getValue()))));
    return new ast.Program(coreDs, inlineDs);
  }

  /**
   * Remove all methods from fresh named lambdas because they are uncallable and may have methods
   * that do not have their signatures inferred yet.
   * @param dec The declaration for an inline fresh-named lambda
   * @return The same declaration with all methods removed
   */
  private static T.Dec removeInlineMs(T.Dec dec) {
    if (!dec.name().isFresh()) { return dec; }
    return dec.withLambda(dec.lambda().withMeths(List.of()));
  }
}
