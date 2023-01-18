package program.inference;

import astFull.E;
import id.Id;
import utils.Bug;
import utils.Streams;
import visitors.InjectionVisitor;

import java.util.*;
import java.util.stream.Collectors;

public record InferBodies(astFull.Program p) {
  ast.Program inferAll() {
    var injectionVisitor = new InjectionVisitor();
    Map<Id.DecId, ast.T.Dec> coreDs = inferDecs().entrySet().stream()
      .collect(Collectors.toMap(Map.Entry::getKey, kv->injectionVisitor.visitDec(kv.getValue())));
    return new ast.Program(coreDs);
  }

  Map<Id.DecId, astFull.T.Dec> inferDecs() {
    return p.ds().values().stream()
      .map(d->d.withLambda(d.lambda().withMethsP(
        d.lambda().meths().stream()
          .map(m->m.withBodyP(m.body().map(e->fixInferStep(iGOf(d, m), e))))
          .toList()
      )))
      .collect(Collectors.toMap(d->d.name(), d->d));
  }

  private Map<String, astFull.T> iGOf(astFull.T.Dec dec, astFull.E.Meth m) {
    assert dec.lambda().selfName() != null;
    return iGOf(dec.lambda().selfName(), new astFull.T(m.sig().orElseThrow().mdf(), dec.toIT()), m);
  }

  private Map<String, astFull.T> iGOf(String selfName, astFull.T lambdaT, astFull.E.Meth m) {
    Map<String, astFull.T> gamma = new HashMap<>();
    gamma.put(selfName, lambdaT);
    var sig = m.sig().orElseThrow();
    Streams.zip(m.xs(), sig.ts()).forEach(gamma::put);
    return Collections.unmodifiableMap(gamma);
  }

  //TODO: this may have to become iterative if the recursion gets out of control
  E fixInferStep(Map<String, astFull.T> gamma, E e) {
    var next = inferStep(gamma, e);
    if (next.isEmpty()) { return e; }
    return fixInferStep(gamma, next.get());
  }

  // rule name version
  Optional<E> inferStep(Map<String, astFull.T> gamma, E e){
    return switch(e){
      case E.X e1->var(gamma, e1);
      case E.MCall e1->methCall(gamma, e1);
      case E.Lambda e1->bProp(gamma, e1);
      };
  }

  // propagation
  Optional<E> methCallProp(Map<String, astFull.T> gamma, E.MCall e) {
    return inferStep(gamma,e.receiver()).map(e::withReceiverP);
  }
  Optional<E> methArgProp(Map<String, astFull.T> gamma, E.MCall e) {
    boolean[] done = {false};
    var newEs = e.es().stream().map(ei->done[0]
      ? ei
      :inferStep(gamma,ei).map(ej->{done[0]=true;return ej;}
      ).orElse(ei)).toList();
    if(!done[0]){ return Optional.empty(); }
    return Optional.of(e.withEsP(newEs));
  }
  Optional<E> bProp(Map<String, astFull.T> gamma, E.Lambda e) {
    boolean[] done = {false};
    List<astFull.E.Meth> newMs = e.meths().stream().map(mi->done[0]
      ? mi
      :bProp(gamma,mi,e).map(mj->{done[0]=true;return mj;}
    ).orElse(mi)).toList();
    if(!done[0]){ return Optional.empty(); }
    return Optional.of(e.withMethsP(newMs));
  }
  Optional<astFull.E.Meth> bProp(Map<String, astFull.T> gamma, astFull.E.Meth m, E.Lambda e) {
    return bPropWithSig(gamma,m,e)
      .or(()->bPropGetSigM(gamma,m,e))
      .or(()->bPropGetSig(gamma,m,e));
  }
  Optional<astFull.E.Meth> bPropWithSig(Map<String, astFull.T> gamma, astFull.E.Meth m, E.Lambda e) {
    if(m.sig().isEmpty() || m.body().isEmpty()){ return Optional.empty(); }
    Map<String, astFull.T> richGamma=new HashMap<>(gamma);
    richGamma.put(e.selfName(),e.t());
    var sig = m.sig().orElseThrow();
    Streams.zip(m.xs(), sig.ts()).forEach(richGamma::put);
    richGamma = Collections.unmodifiableMap(richGamma);
    var e1 = m.body().get();
    var e2 = new RefineTypes(this.p).fixType(e1,sig.ret());
    var optBody = inferStep(richGamma,e2);
    var res = optBody.map(b->m.withBodyP(Optional.of(b)));
    return res.or(()->e1==e2
      ? Optional.empty()
      : Optional.of(m.withBodyP(Optional.of(e2))));
  }
  Optional<astFull.E.Meth> bPropGetSigM(Map<String, astFull.T> gamma, astFull.E.Meth m, E.Lambda e) {
    throw Bug.todo();
  }
  Optional<astFull.E.Meth> bPropGetSig(Map<String, astFull.T> gamma, astFull.E.Meth m, E.Lambda e) {
    throw Bug.todo();
  }

  // inference
  Optional<E> methCall(Map<String, astFull.T> gamma, E.MCall e) {
    return methCallProp(gamma, e)
      .or(()->methArgProp(gamma,e))
      .or(()->methCallHasGens(gamma, e))
      .or(()->methCallNoGens(gamma, e));
  }
  Optional<E> methCallNoGens(Map<String, astFull.T> gamma, E.MCall e) {
    throw Bug.todo();
  }
  Optional<E> methCallHasGens(Map<String, astFull.T> gamma, E.MCall e) {
    if (e.ts().isEmpty()) { return Optional.empty(); }
    throw Bug.todo();
  }

  Optional<E> var(Map<String, astFull.T> gamma, E.X e) {
    if (!e.t().isInfer()) { return Optional.empty(); }
    return Optional.ofNullable(gamma.get(e.name())).map(e::withTP);
  }

  // helpers

  /** extracts the annotated types for all the ie in ies */
  List<astFull.T> typesOf(List<astFull.E> ies) {
    throw Bug.todo();
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