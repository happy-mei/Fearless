package program.inference;

import astFull.T;
import astFull.E;
import id.Id;
import program.CM;
import utils.Bug;
import utils.Streams;
import visitors.InjectionVisitor;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record InferBodies(ast.Program p) {
  ast.Program inferAll(astFull.Program fullProgram){
    return new ast.Program(inferDecs(fullProgram));
  }

  Map<Id.DecId, ast.T.Dec> inferDecs(astFull.Program fullProgram){
    var iV = new InjectionVisitor();
    return fullProgram.ds().values().stream()
      .map(d->{
        var coreDecl = p.ds().get(d.name());
        var l=coreDecl.lambda();
        return coreDecl.withLambda(l.withMethsP(
          Streams.zip(d.lambda().meths(),l.meths())
            .map((fM,cM)->cM.withBody(fM.body().map(e->fixInferStep(iGOf(coreDecl, cM), e).accept(iV))))
            .toList()
        ));
      })
      .collect(Collectors.toMap(d->d.name(), d->d));
  }

  private Map<String, astFull.T> iGOf(ast.T.Dec dec, ast.E.Meth m) {
    assert dec.lambda().selfName() != null;
    var t = new astFull.T(m.sig().mdf(), dec.toIT().toFullAstIT(ast.T::toAstFullT));
    return iGOf(dec.lambda().selfName(), t, m);
  }

  private Map<String, astFull.T> iGOf(String selfName, astFull.T lambdaT, ast.E.Meth m) {
    Map<String, astFull.T> gamma = new HashMap<>();
    gamma.put(selfName, lambdaT);
    var sig = m.sig();
    Streams.zip(m.xs(), sig.ts()).forEach((k,t)->gamma.put(k, t.toAstFullT()));
    return Collections.unmodifiableMap(gamma);
  }

  //TODO: this may have to become iterative if the recursion gets out of control
  E fixInferStep(Map<String, T> gamma, E e) {
    var next = inferStep(gamma, e);
    if (next.isEmpty()) { return e; }
    if (e.equals(next.get())) { return e; }
    return fixInferStep(gamma, next.get());
  }

  // rule name version
  Optional<E> inferStep(Map<String, T> gamma, E e){
    return switch(e){
      case E.X e1->var(gamma, e1);
      case E.MCall e1->methCall(gamma, e1);
      case E.Lambda e1->bProp(gamma, e1);
      };
  }

  // propagation
  Optional<E> methCallProp(Map<String, T> gamma, E.MCall e) {
    return inferStep(gamma,e.receiver()).map(e::withReceiver);
  }
  Optional<E> methArgProp(Map<String, T> gamma, E.MCall e) {
    boolean[] done = {false};
    var newEs = e.es().stream().map(ei->done[0]
      ? ei
      :inferStep(gamma,ei).map(ej->{done[0]=true;return ej;}
      ).orElse(ei)).toList();
    if(!done[0]){ return Optional.empty(); }
    return Optional.of(e.withEs(newEs));
  }
  Optional<E> bProp(Map<String, T> gamma, E.Lambda e) {
    boolean[] done = {false};
    List<E.Meth> newMs = e.meths().stream().map(mi->done[0]
      ? mi
      :bProp(gamma,mi,e).map(mj->{done[0]=true;return mj;}
    ).orElse(mi)).toList();
    if(!done[0]){ return Optional.empty(); }
    return Optional.of(e.withMeths(newMs));
  }
  Optional<E.Meth> bProp(Map<String, T> gamma, E.Meth m, E.Lambda e) {
    return bPropWithSig(gamma,m,e)
      .or(()->bPropGetSigM(gamma,m,e))
      .or(()->bPropGetSig(gamma,m,e));
  }
  Optional<E.Meth> bPropWithSig(Map<String, T> gamma, E.Meth m, E.Lambda e) {
    if(m.body().isEmpty()){ return Optional.empty(); }
    if(m.sig().isEmpty()){ return Optional.empty(); }
    Map<String, T> richGamma=new HashMap<>(gamma);
    richGamma.put(e.selfName(),e.t()); // TODO: Nick: selfName can be null
    var sig = m.sig().orElseThrow();
    Streams.zip(m.xs(), sig.ts()).forEach(richGamma::put);
    richGamma = Collections.unmodifiableMap(richGamma);
    var e1 = m.body().get();
    var e2 = new RefineTypes(p).fixType(e1,sig.ret());
    var optBody = inferStep(richGamma,e2);
    var res = optBody.map(b->m.withBody(Optional.of(b)));
    return res.or(()->e1==e2
      ? Optional.empty()
      : Optional.of(m.withBody(Optional.of(e2))));
  }
  Optional<E.Meth> bPropGetSigM(Map<String, T> gamma, E.Meth m, E.Lambda e) {
    if (e.it().isEmpty()) { return Optional.empty(); }

//    var iT = new Id.IT<ast.T>("FearFreshC$", List.of());
//    var parentMs = e.its().stream()
//      .flatMap(it->p.meths(it.toAstIT(T::toAstT)).stream());
//    var localMs = e.meths().stream()
//      .filter(mi->mi.sig().isPresent())
//      .map(mi->CM.of(iT, mi, new InjectionVisitor().visitSig(mi.sig().get())));
//    var ms = p.prune(Stream.concat(localMs, parentMs).toList());

    List<CM> ms = p.meths(e.it().get().toAstIT(T::toAstT));
    if (ms.size() > 1) { return Optional.empty(); }
    var sig = ms.get(0).sig();

    return Optional.of(m.withSig(sig.toAstFullSig()));
  }
  Optional<E.Meth> bPropGetSig(Map<String, T> gamma, E.Meth m, E.Lambda e) {
    throw Bug.todo();
  }

  // inference
  Optional<E> methCall(Map<String, T> gamma, E.MCall e) {
    return methCallProp(gamma, e)
      .or(()->methArgProp(gamma,e))
      .or(()->methCallHasGens(gamma, e))
      .or(()->methCallNoGens(gamma, e));
  }
  Optional<E> methCallHasGens(Map<String, T> gamma, E.MCall e) {
    if (e.ts().isEmpty()) { return Optional.empty(); }
    var gens = e.ts().get();
    var c = e.receiver().t();
    var iTs = typesOf(e.es());
    if (c.isInfer() || (!(c.rt() instanceof Id.IT<T> recv))) { return Optional.empty(); }

    var refiner = new RefineTypes(p);
    var refined = refiner.refineSig(recv,
      new RefineTypes.RefinedSig(e.name(), gens, iTs, e.t())
    );
    var fixedArgs = refiner.fixTypes(e.es(), refined.args());

    assert refined.name().equals(e.name());
    var res = new E.MCall(
      e.receiver(),
      refined.name(),
      Optional.of(refined.gens()),
      fixedArgs,
      refiner.best(refined.rt(), e.t()),
      e.pos()
    );
    return Optional.of(res);
  }
  Optional<E> methCallNoGens(Map<String, T> gamma, E.MCall e) {
    assert e.ts().isEmpty();
    var c = e.receiver().t();
    if (c.isInfer() || (!(c.rt() instanceof Id.IT<T> recv))) { return Optional.empty(); }
    var m = p.meths(recv.toAstIT(T::toAstT)).stream()
      .filter(mi->mi.name().equals(e.name()))
      .findFirst().orElseThrow();
    var k = m.sig().gens().size();
    var infers = Collections.nCopies(k, T.infer);
    return Optional.of(e.withTs(Optional.of(infers)));
  }

  Optional<E> var(Map<String, T> gamma, E.X e) {
    if (!e.t().isInfer()) { return Optional.empty(); }
    return Optional.ofNullable(gamma.get(e.name())).map(e::withT);
  }

  // helpers

  /** extracts the annotated types for all the ie in ies */
  List<T> typesOf(List<E> ies) {
    return ies.stream().map(E::t).toList();
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