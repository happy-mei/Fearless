package program.inference;

import ast.T;
import astFull.E;
import astFull.PosMap;
import id.Id;
import main.Fail;
import utils.Bug;
import visitors.InjectionVisitor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public record InferBodies(astFull.Program p) {
  ast.Program inferAll() {
    var injectionVisitor = new InjectionVisitor();
    Map<Id.DecId, ast.T.Dec> ds = p.ds().values().stream()
      .map(d->injectionVisitor.visitDec(d.withLambda(d.lambda().withMeths(
        d.lambda().meths().stream()
          .map(m->m.withBody(
            m.body()
              .map(e->fixInferStep(
                Map.of(d.lambda().selfName(), new astFull.T(m.sig().orElseThrow().mdf(), d.toIT())),
                e
                )))
            )
          .toList()
        ))))
      .collect(Collectors.toMap(T.Dec::name, d->d));
    return new ast.Program(ds);
  }

  private Map<String, astFull.T> buildGamma(astFull.T.Dec dec, astFull.E.Meth m) {
    Map<String, astFull.T> gamma = new HashMap<>();
    gamma.put(dec.lambda().selfName(), new astFull.T(m.sig().orElseThrow().mdf(), dec.toIT()));
    var sig = m.sig().orElseThrow()E
    // TODO: add params to gamma

    return gamma;
  }

  E fixInferStep(Map<String, astFull.T> gamma, E e) {
    var next = inferStep(gamma, e);
    if (next.isEmpty()) {
      if (e.t().isInfer()) {
        throw Fail.inferFailed(e).pos(PosMap.getOrUnknown(e));
      }
      return e;
    }
    return fixInferStep(gamma, next.get());
  }

  // rule name version
  Optional<E> inferStep(Map<String, astFull.T> gamma, E e) {
    if (e instanceof E.X e1) { return var(gamma, e1); }
    if (e instanceof E.MCall e1) { return methCall(gamma, e1); }
    if (e instanceof E.Lambda e1) { return bProp(gamma, e1); }
    throw Bug.unreachable();
  }
  Optional<E> methCall(Map<String, astFull.T> gamma, E.MCall e) {
    if (e.ts().isEmpty()) {
      return methCallNoGens(gamma, e);
    }
    return methCallHasGens(gamma, e);
  }
  Optional<E> methCallNoGens(Map<String, astFull.T> gamma, E.MCall e) {
    throw Bug.todo();
  }
  Optional<E> methCallHasGens(Map<String, astFull.T> gamma, E.MCall e) {
    throw Bug.todo();
  }

  Optional<E> bProp(Map<String, astFull.T> gamma, E.Lambda e) {
    throw Bug.todo();
  }

  Optional<E> var(Map<String, astFull.T> gamma, E.X e) {
    if (!e.t().isInfer()) { return Optional.empty(); }
    return Optional.ofNullable(gamma.get(e.name())).map(t->new E.X(e.name(), t));
  }
}

/*
1 Currerent Ds -->For all classes in Ds, for all methods with a body, infer the body
-->new Ds (core) with a core program
by running fixInferStep and finally injectionToCore

2 fixInferStep(iG,e)=e' //runs inferStep(iG,E)=Optional<E> until e' is not 'empty',
the one just before none is the final result

3
iG |- e=>e'
inferStep(iG,E)=Optional<E> so that on 'empty' it 'could not be done'

every rule should be a method with the rule name

 */