package wellFormedness;

import ast.Mdf;
import astFull.E;
import astFull.T;
import main.CompileError;
import main.Fail;
import utils.Bug;
import visitors.FullCollectorVisitorWithEnv;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
      .or(()->super.visitMCall(e));
  }

  @Override
  public Optional<CompileError> visitAlias(T.Alias a) {
    return hasIsoParams(a.from().ts())
      .or(()->super.visitAlias(a));
  }

  @Override
  public Optional<CompileError> visitLambda(E.Lambda e) {
    var genArgs = e.its().stream()
      .flatMap(it ->it.ts().stream())
      .map(t->new T(t.mdf(), t.rt()))
      .toList();

    return hasIsoParams(genArgs)
      .or(()->noExplicitThis(List.of(e.selfName())))
      .or(()->super.visitLambda(e));
  }

  @Override public Optional<CompileError> visitMeth(E.Meth e){
    return hasDisjXs(e.xs(),e)
      .or(()->noExplicitThis(e.xs()))
      .or(()->super.visitMeth(e));
  }

  private Optional<CompileError> hasIsoParams(List<T> genArgs) {
    return genArgs.stream()
      .dropWhile(t->t.mdf()!=Mdf.iso)
      .map(Fail::isoInTypeArgs)
      .findFirst();
  }

  private Optional<CompileError> noExplicitThis(List<String> xs) {
    return xs.stream().anyMatch(x->x.equals("this")) ? Optional.of(Fail.explicitThis()) : Optional.empty();
  }

  private Optional<CompileError> hasDisjXs(List<String> xs, E.Meth e){
    var all=new ArrayList<>(xs);
    xs.stream().distinct().forEach(ei->all.remove(ei));
    if(all.isEmpty()){ return Optional.empty(); }
    throw Bug.todo();
      //Optional.of(Fail.conflictingXs(e.name().))
      //TODO: what if the name is not there? what is a good error
  }
}