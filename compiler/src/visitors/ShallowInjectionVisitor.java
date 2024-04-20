package visitors;

import astFull.E;
import astFull.T;
import id.Id;
import utils.Mapper;

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
    Map<Id.DecId, ast.T.Dec> coreDs = Mapper.of(res->p.ds().forEach((key, value) -> {
      var dec = visitDec(value);
      res.put(dec.id(), dec);
    }));
    Map<Id.DecId, ast.T.Dec> inlineDs = Mapper.of(res->p.inlineDs().forEach((key, value) -> {
      var dec = visitDec(removeInlineMs(value));
      res.put(dec.id(), dec);
    }));
    return new ast.Program(p.tsf(), coreDs, inlineDs);
  }

  /**
   * Remove all methods from fresh named lambdas because they are uncallable and may have methods
   * that do not have their signatures inferred yet.
   * @param dec The declaration for an inline fresh-named lambda
   * @return The same declaration with all methods removed
   */
  private static T.Dec removeInlineMs(T.Dec dec) {
//    if (!dec.name().isFresh()) { return dec; }
//    return dec.withLambda(dec.lambda().withMeths(List.of()));
    return dec.withLambda(dec.lambda().withMeths(dec.lambda().meths().stream().filter(m->m.sig().isPresent()).toList()));
  }
}
