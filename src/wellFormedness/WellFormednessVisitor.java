package wellFormedness;

import ast.Mdf;
import astFull.E;
import astFull.PosMap;
import astFull.T;
import main.CompileError;
import main.Fail;
import visitors.FullCollectorVisitorWithEnv;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/*
  ✅Actual generic parameters can not be iso //checked in Alias, Lambda and mCall
  ✅No explicitly declared this
  Arguments list disjoints
  No shadowing
  ✅(done in package) Disjoint class names //overloading on generic arity
  Disjoint method names //overloading on parameter arity
  'mdf' only in front of X, recMdf only for read/lent methods
  X in base.NoMutHyg[X] is a generic parameter on the interface implementing it
  ✅(done in parser) If the lambda type is inferred then at least "{}" must be present
  ✅(done in parser) In a B a SM of form ()->e is ill formed (but ok as an SM)
  Implements relation is acyclic //ok to implement same trait with different generics
    that is, forall n,C C[mdf X1..mdf Xn] notin supertypes(Ds, C[mdf X1..mdf Xn]) where X1..Xn are fresh.
 */
public class WellFormednessVisitor extends FullCollectorVisitorWithEnv<CompileError> {
  @Override
  public Optional<CompileError> visitMCall(E.MCall e) {
    return e.ts().flatMap(this::hasIsoParams)
      .or(()->super.visitMCall(e))
      .map(err->err.pos(PosMap.getOrUnknown(e)));
  }

  @Override
  public Optional<CompileError> visitAlias(T.Alias a) {
    return hasIsoParams(a.from().ts())
      .or(()->super.visitAlias(a))
      .map(err->err.pos(PosMap.getOrUnknown(a)));
  }

  @Override
  public Optional<CompileError> visitLambda(E.Lambda e) {
    var genArgs = e.its().stream()
      .flatMap(it ->it.ts().stream())
      .map(t->new T(t.mdf(), t.rt()))
      .toList();

    return hasIsoParams(genArgs)
      .or(()->Optional.ofNullable(e.selfName()).flatMap(x->noExplicitThis(List.of(x))))
      .or(()->super.visitLambda(e))
      .map(err->err.pos(PosMap.getOrUnknown(e)));
  }

  @Override public Optional<CompileError> visitMeth(E.Meth e){
    return hasNonDisjointXs(e.xs(),e)
      .or(()->noExplicitThis(e.xs()))
      .or(()->super.visitMeth(e))
      .map(err->err.pos(PosMap.getOrUnknown(e)));
  }

  private Optional<CompileError> hasIsoParams(List<T> genArgs) {
    return genArgs.stream()
      .flatMap(T::flatten)
      .dropWhile(t->t.mdf()!=Mdf.iso)
      .map(Fail::isoInTypeArgs)
      .findFirst();
  }

  private Optional<CompileError> noExplicitThis(List<String> xs) {
    return xs.stream().anyMatch(x->x.equals("this")) ? Optional.of(Fail.explicitThis()) : Optional.empty();
  }

  private Optional<CompileError> hasNonDisjointXs(List<String> xs, E.Meth e){
    var all=new ArrayList<>(xs);
    xs.stream().distinct().forEach(all::remove);
    if(all.isEmpty()){ return Optional.empty(); }
    var conflicts = all.stream()
      .collect(Collectors.groupingBy(x->x))
      .keySet().stream()
      .toList();
    return Optional.of(Fail.conflictingMethArgs(conflicts).pos(PosMap.getOrUnknown(e)));
      //Optional.of(Fail.conflictingXs(e.name().))
      //TODO: what if the name is not there? what is a good error
  }
}