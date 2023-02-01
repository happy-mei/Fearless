package program.inference;

import astFull.E;
import astFull.T;
import id.Id;
import id.Mdf;
import program.CM;
import utils.Streams;
import visitors.FullShortCircuitVisitor;
import visitors.InjectionVisitor;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public record InferBodies(ast.Program p) {
  ast.Program inferAll(astFull.Program fullProgram){
    return new ast.Program(inferDecs(fullProgram));
  }

  Map<Id.DecId, ast.T.Dec> inferDecs(astFull.Program fullProgram){
    return fullProgram.ds().values().stream()
      .map(this::inferDec)
      .collect(Collectors.toMap(d->d.name(), d->d));
  }
  ast.T.Dec inferDec(astFull.T.Dec d){
    var coreDecl = p.ds().get(d.name());
    var l = coreDecl.lambda();
    return coreDecl.withLambda(l.withMeths(
      Streams.zip(d.lambda().meths(),l.meths())
        .map((fullMeth, coreMeth)->fullMeth.body().map(b->inferMethBody(coreDecl,b, coreMeth)).orElse(coreMeth))
        .toList()
    ));
  }
  ast.E.Meth inferMethBody(ast.T.Dec dec, E e, ast.E.Meth coreMeth) {
    var refiner = new RefineTypes(p);
    var iV = new InjectionVisitor();
    var type = refiner.fixType(e,coreMeth.sig().toAstFullSig().ret());
    var newBody = fixInferStep(iGOf(dec, coreMeth), type).accept(iV);
    return coreMeth.withBody(Optional.of(newBody));
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
    System.out.println(e);
    assert next.map(ei->!ei.equals(e)).orElse(true);
    if (next.isEmpty()) { return e; }
//    if (e.equals(next.get())) { return e; }
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
  Optional<E> refineLambda(E.Lambda e, E.Lambda baseLambda){
    var anyNoSig = e.meths().stream().anyMatch(mi->mi.sig().isEmpty());
    if(anyNoSig){ return Optional.of(baseLambda); }
    return Optional.of(new RefineTypes(p).fixLambda(baseLambda));
  }

  Optional<E> bProp(Map<String, T> gamma, E.Lambda e) {
    if (e.it().isEmpty()) { return Optional.empty(); }
    boolean[] done = {false};
    List<E.Meth> newMs = e.meths().stream().map(mi->done[0]
      ? mi
      : bProp(gamma,mi,e).map(mj->{done[0]=true;return mj;}).orElse(mi))
    .toList();
    if(!done[0]){ return Optional.empty(); }
    var res = refineLambda(e, e.withMeths(newMs));
    return res.flatMap(e1->!e1.equals(e) ? res : Optional.empty());
  }
  Optional<E.Meth> bProp(Map<String, T> gamma, E.Meth m, E.Lambda e) {
    var res = bPropWithSig(gamma,m,e)
      .or(()->bPropGetSigM(gamma,m,e))
      .or(()->bPropGetSig(gamma,m,e));
    assert res.map(m1->!m.equals(m1)).orElse(true);
    return res;
  }
  Optional<E.Meth> bPropWithSig(Map<String, T> gamma, E.Meth m, E.Lambda e) {
    var anyNoSig = e.meths().stream().anyMatch(mi->mi.sig().isEmpty());
    if(anyNoSig){ return Optional.empty(); }
    if(m.body().isEmpty()){ return Optional.empty(); }
    if(m.sig().isEmpty()){ return Optional.empty(); }
    Map<String, T> richGamma = new HashMap<>(gamma);
    richGamma.put(e.selfName(),e.t());
    var sig = m.sig().orElseThrow();
    Streams.zip(m.xs(), sig.ts()).forEach(richGamma::put);
    richGamma = Collections.unmodifiableMap(richGamma);
    var refiner = new RefineTypes(p);
    var e1 = m.body().get();
    var e2 = refiner.fixType(e1,sig.ret());
    var optBody = inferStep(richGamma,e2);
    var res = optBody.map(b->m.withBody(Optional.of(b)).withSig(refiner.fixTypes(sig, b.t())));
    var finalRes = res.or(()->e1==e2
      ? Optional.empty()
      : Optional.of(m.withBody(Optional.of(e2))));
    return finalRes.map(m1->!m.equals(m1)).orElse(true) ? finalRes : Optional.empty();
//    assert finalRes.map(m1->!m.equals(m1)).orElse(true);
//    return finalRes;
  }
  Optional<E.Meth> bPropGetSigM(Map<String, T> gamma, E.Meth m, E.Lambda e) {
    assert !e.it().isEmpty();
    if(m.sig().isPresent()){ return Optional.empty(); }
    var res = onlyAbs(e.it().get()).map(m::withSig);
    assert res.map(m1->!m.equals(m1)).orElse(true);
    return res;
  }

  Optional<E.Sig> onlyAbs(Id.IT<astFull.T> it){ return p.fullSig(it,CM::isAbs); }

  Optional<E.Sig> onlyMName(Id.IT<astFull.T> it, Id.MethName name){
    return p.fullSig(it,cm->cm.name().equals(name));
  }

  Optional<E.Meth> bPropGetSig(Map<String, T> gamma, E.Meth m, E.Lambda e) {
    assert !e.it().isEmpty();
    if(m.sig().isPresent()){ return Optional.empty(); }
    var sig = onlyMName(e.it().get(), m.name().orElseThrow());
    if(sig.isEmpty()){ return Optional.empty(); }
    var res = sig.map(m::withSig);
    assert res.map(m1->!m.equals(m1)).orElse(true);
    return res;
  }

  Optional<E> methCall(Map<String, T> gamma, E.MCall e) {
    var res = methCallProp(gamma, e)
      .or(()->methArgProp(gamma,e))
      .or(()->methCallHasGens(gamma, e))
      .or(()->methCallNoGens(gamma, e));
    assert res.map(e1->!e.equals(e1)).orElse(true);
    return res;
  }
  Optional<E> methCallProp(Map<String, T> gamma, E.MCall e) {
    Optional<E> res = inferStep(gamma,e.receiver()).map(e::withReceiver);
    assert res.map(e1->!e.equals(e1)).orElse(true);
    return res;
  }
  Optional<E> methArgProp(Map<String, T> gamma, E.MCall e) {
    boolean[] done = {false};
    var newEs = e.es().stream().map(ei->done[0]
      ? ei
      : inferStep(gamma,ei).map(ej->{ done[0]=true; return ej; }
    ).orElse(ei)).toList();
    if(!done[0]){ return Optional.empty(); }
    //Sub s = new Sub(res.t2().gxOrThrow(),res.t1);
    //Sub sMdf = new Sub(res.t2().gxOrThrow(),res.t1.withMdf(Mdf.mdf));
    Optional<E> res = Optional.of(e.withEs(newEs));
    assert res.map(e1->!e.equals(e1)).orElse(true);
    return res;
  }
  Optional<E> methCallHasGens(Map<String, T> gamma, E.MCall e) {
    if (e.ts().isEmpty()) { return Optional.empty(); }
    var gens = e.ts().get();
    var c = e.receiver().t();
    var iTs = typesOf(e.es());
    if (c.isInfer() || (!(c.rt() instanceof Id.IT<T> recv))) { return Optional.empty(); }

    var refiner = new RefineTypes(p);
    var baseSig = new RefineTypes.RefinedSig(Mdf.mdf, e.name(), gens, iTs, e.t());
    var refined = refiner.refineSigMassive(c.mdf(), recv, List.of(baseSig));
    var refinedSig = refined.sigs().get(0);
    var fixedRecv = refiner.fixType(e.receiver(), new T(e.receiver().t().mdf(), refined.c()));
    var fixedArgs = refiner.fixTypes(e.es(), refinedSig.args());
    var fixedGens = e.ts().map(userGens -> replaceOnlyInfers(userGens, refinedSig.gens())).orElse(refinedSig.gens());

    assert refinedSig.name().equals(e.name());
    var res = new E.MCall(
      fixedRecv,
      refinedSig.name(),
      Optional.of(fixedGens),
      fixedArgs,
      refiner.best(refinedSig.rt(), e.t()),
      e.pos()
    );
    return e.equals(res) ? Optional.empty() : Optional.of(res);
  }
  public static T replaceOnlyInfers(T user, T inferred) {
    if (user.isInfer()) { return inferred; }
    if (!(user.rt() instanceof Id.IT<T> userIT
      && inferred.rt() instanceof Id.IT<T> inferredIT)) { return user; }
    if (!userIT.name().equals(inferredIT.name())) { return user; }
    return new T(user.mdf(), userIT.withTs(replaceOnlyInfers(userIT.ts(), inferredIT.ts())));
  }
  public static List<T> replaceOnlyInfers(List<T> userGens, List<T> inferredGens) {
    return Streams.zip(userGens, inferredGens)
      .map(InferBodies::replaceOnlyInfers)
      .toList();
  }
  Optional<E> methCallNoGens(Map<String, T> gamma, E.MCall e) {
    if (e.ts().isPresent()) { return Optional.empty(); }
    var c = e.receiver().t();
    // cond: e.name().name.equals("#") && c.itOrThrow().name().name().equals("base.UpdateRef")
    if (c.isInfer() || (!(c.rt() instanceof Id.IT<T> recv))) { return Optional.empty(); }

    var sig = p.fullSig(recv, cm->cm.name().equals(e.name())).orElseThrow();
    var k = sig.gens().size();
    var infers = Collections.nCopies(k, T.infer);
    return Optional.of(e.withTs(Optional.of(infers)));
  }

  Optional<E> var(Map<String, T> gamma, E.X e) {
    if (!e.t().isInfer()) { return Optional.empty(); }
    Optional<E> res = Optional.ofNullable(gamma.get(e.name())).map(e::withT);
    assert res.map(e1->!e1.equals(e)).orElse(true);
    return res;
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