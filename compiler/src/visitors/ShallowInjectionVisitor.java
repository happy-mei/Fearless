package visitors;

import astFull.E;
import astFull.T;
import id.Id;
import id.Mdf;
import id.Id.GX;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class ShallowInjectionVisitor extends InjectionVisitor implements FullVisitor<ast.E> {
  static public ShallowInjectionVisitor of(){ return of(Map.of()); } 
  static public ShallowInjectionVisitor of(Map<Id.GX<ast.T>, Set<Mdf>> allBounds){
    return new ShallowInjectionVisitor(allBounds){
      ShallowInjectionVisitor renew(Map<GX<ast.T>, Set<Mdf>> allBounds) {
        return of(allBounds);
      }};
  }
  protected ShallowInjectionVisitor(Map<Id.GX<ast.T>, Set<Mdf>> allBounds){super(allBounds);}
  @Override public ast.E.Meth visitMeth(E.Meth m) {
    if (m.body().isPresent()) {
      m = m.withBody(Optional.of(new E.X("this", T.infer, m.pos())));
    }
    return super.visitMeth(m);
  }

  @Override public ast.Program visitProgram(astFull.Program p){
    Map<Id.DecId, ast.T.Dec> coreDs = p.ds().entrySet().stream()
      .collect(Collectors.toMap(Map.Entry::getKey, kv->visitDec(kv.getValue())));
    Map<Id.DecId, ast.T.Dec> inlineDs = p.inlineDs().entrySet().stream()
      .collect(Collectors.toMap(Map.Entry::getKey, kv->visitDec(removeInlineMs(kv.getValue()))));
    return new ast.Program(p.tsf(), coreDs, inlineDs);
  }

  // TODO: this is a bad assumption. We can definitely define a callable method on an inline lambda.
  /**
   * Remove all methods from fresh named lambdas because they are uncallable and may have methods
   * that do not have their signatures inferred yet.
   * @param dec The declaration for an inline fresh-named lambda
   * @return The same declaration with all methods removed
   */
  private static T.Dec removeInlineMs(T.Dec dec) {
    return dec.withLambda(
      dec.lambda()
        .withMeths(dec.lambda().meths().stream().filter(m->m.sig().isPresent()).toList())
    );
  }
}
