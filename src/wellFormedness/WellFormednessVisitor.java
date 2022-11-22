package wellFormedness;

import astFull.E;
import astFull.PosMap;
import astFull.T;
import id.Id;
import id.Mdf;
import main.CompileError;
import main.Fail;
import program.Program;
import visitors.FullCollectorVisitorWithEnv;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/*
  ✅Actual generic parameters can not be iso //checked in Alias, Lambda, mCall, and Meth
  ✅No explicitly declared this
  ✅Arguments list disjoints
  ✅No shadowing:done x in meth decl, x in lambda, done Xs in meth decl for other Xs in scope
  ✅(done in package) Disjoint class names //overloading on generic arity
  ✅Disjoint method names //overloading on parameter arity
  ✅'mdf' only in front of X
  recMdf only for read/lent methods
    TODO: we should reundestand why. For example if I have a mut method returning recMdf Foo
    TODO: would this return a capsule Foo when called on a capsule receiver?
  X in base.NoMutHyg[X] is a generic parameter on the interface implementing it
  ✅(done in parser) If the lambda type is inferred then at least "{}" must be present
  ✅(done in parser) In a B a SM of form ()->e is ill formed (but ok as an SM)
  Implements relation is acyclic //ok to implement same trait with different generics
    that is, forall n,C C[mdf X1..mdf Xn] notin supertypes(Ds, C[mdf X1..mdf Xn]) where X1..Xn are fresh.
 */
public class WellFormednessVisitor extends FullCollectorVisitorWithEnv<CompileError> {

  @Override public Optional<CompileError> visitMCall(E.MCall e) {
    return e.ts().flatMap(this::noIsoParams)
      .or(()->super.visitMCall(e))
      .map(err->err.pos(PosMap.getOrUnknown(e)));
  }

  @Override public Optional<CompileError> visitAlias(T.Alias a) {
    return super.visitAlias(a)
      .map(err->err.pos(PosMap.getOrUnknown(a)));
  }

  @Override public Optional<CompileError> visitLambda(E.Lambda e) {
    return Optional.ofNullable(e.selfName())
      .flatMap(x->
        noExplicitThis(List.of(x))
        .or(()->noShadowingVar(List.of(x)))
      )
      .or(()->hasNonDisjoingMs(e))
      .or(()->super.visitLambda(e))
      .map(err->err.pos(PosMap.getOrUnknown(e)));
  }

  @Override public Optional<CompileError> visitMeth(E.Meth e) {
    return hasNonDisjointXs(e.xs(), e)
      .or(()->noExplicitThis(e.xs()))
      .or(()->noShadowingVar(e.xs()))
      .or(()->noShadowingGX(e.sig().map(E.Sig::gens).orElse(List.of())))
      .or(()->super.visitMeth(e))
      .map(err->err.pos(PosMap.getOrUnknown(e)));
  }
  @Override public Optional<CompileError> visitT(T t){
    return mdfOnlyOnGX(t)
      .or(()->super.visitT(t))
      .map(err->err.pos(PosMap.getOrUnknown(t)));
  }
  @Override public Optional<CompileError> visitIT(Id.IT<T> t) {
    return noIsoParams(t.ts())
      .or(()->super.visitIT(t))
      .map(err->err.pos(PosMap.getOrUnknown(t)));
  }

  @Override public Optional<CompileError> visitProgram(Program p){
    return noCyclicImplRelations(p)
      .or(()->super.visitProgram(p));
  }

  private Optional<CompileError> noIsoParams(List<T> genArgs) {
    return genArgs.stream()
      .flatMap(T::flatten)
      .dropWhile(t->t.mdf() != Mdf.iso)
      .map(Fail::isoInTypeArgs)
      .findFirst();
  }
  private Optional<CompileError> mdfOnlyOnGX(T t) {
    if(t.isInfer() || !t.mdf().isMdf()){ return Optional.empty(); }
    return t.match(gx->Optional.empty(),
      it->Optional.of(Fail.invalidMdf(t)));
  }
  private Optional<CompileError> noExplicitThis(List<String> xs) {
    return xs.stream().anyMatch(x->x.equals("this")) ? Optional.of(Fail.explicitThis()) : Optional.empty();
  }
  private Optional<CompileError> noShadowingVar(List<String> xs) {
    return xs.stream().filter(x->this.env.has(x)).findFirst().map(Fail::shadowingX);
  }
  private Optional<CompileError> noShadowingGX(List<Id.GX<T>> xs) {
    return xs.stream().filter(x->this.env.has(x)).findFirst().map(x->Fail.shadowingGX(x.name()));
  }
  private Optional<CompileError> hasNonDisjoingMs(E.Lambda e) {
    return hasNonDisjointAux(e.meths(),e,
      m->m.name().map(Object::toString).orElse("<unnamed>/"+m.xs().size()));
  }
  private Optional<CompileError> hasNonDisjointXs(List<String> xs, E.Meth e) {
    return hasNonDisjointAux(xs,e,x->x);
  }
  private <TT> Optional<CompileError> hasNonDisjointAux(List<TT> xs, Object e, Function<TT,String> toS) {
    var all = new ArrayList<>(xs);
    xs.stream().distinct().forEach(all::remove);
    if (all.isEmpty()) {
      return Optional.empty();
    }
    var conflicts = all.stream()
      .collect(Collectors.groupingBy(x->toS.apply(x)))
      .keySet().stream()
      .toList();
    return Optional.of(Fail.conflictingMethArgs(conflicts).pos(PosMap.getOrUnknown(e)));
  }
  private Optional<CompileError> noCyclicImplRelations(Program p) {
    for(var key:p.ds().keySet()){
      var ks = p.superDecIds(key);
      if(ks.contains(key)){ return Optional.of(Fail.cyclicImplRelation(key)); }
    }
    return Optional.empty();
  }
}

/*
//TODO: move it in a better place
For the standard library
we need an input format

precompiled Opt
package base
Opt[X]{
  .match(..):T->this
  }

HasId:{  .same[X](mut X x, read X y):Bool Native["java{ return x==y; }"]

Native:{ .get[T](name: Str): T }
 */