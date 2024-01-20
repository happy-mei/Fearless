package magic;

import ast.E;
import ast.T;
import id.Id;
import program.typesystem.ETypeSystem;
import program.typesystem.GuessIT;
import visitors.CollectorVisitor;
import visitors.ShortCircuitVisitor;
import visitors.Visitor;

import java.util.*;


/*
  The goal here is that I want to give a leaf method (i.e. a flow constructor call) and build up a graph
  of every method that calls it until I can get to the root (i.e. user code calling something that calls it).
  Probably BFS of some kind is what I want?

  I can start with my target, then I need to find the method body that has the method call to that target.
  Then I need to ...

  After I have my root node, I go in reverse.

  ALTERNATIVELY

  If I am in a method that calls Flow.fromOp, dynamically make calls to that method magic?
 */

public interface FindMagicMCallLeaves {
//  static List<E.MCall> of(ETypeSystem ts, Goal goal) {
//    var containsGenericsVisitor = new ContainsGenericsVisitor();
//    var guessIT = new GuessIT(ts.p(), ts.g(), ts.xbs(), ts.depth());
//    var leafCalls = new ArrayList<E.MCall>();
//    var q = new ArrayDeque<Goal>();
//    var visited = new HashSet<>(q);
//    q.offer(goal);
//    while (!q.isEmpty()) {
//      var v = q.poll();
//      var visitor = new VariantCallCollector(guessIT, v);
//      ts.p().lambdas().forEach(visitor::visitLambda);
//      var res = visitor.res();
//      for (var call : res) {
//        if (containsGenericsVisitor.visitMCall(call).isEmpty()) {
//          leafCalls.add(call);
//          continue;
//        }
//        // TODO
////        var next = new Goal(guessIT.)
//      }
//
//    }
//
//    return Collections.unmodifiableList(leafCalls);
//  }
//
//  record Goal(Id.DecId expectedRecv, Id.MethName expectedName) {}
//  class VariantCallCollector implements CollectorVisitor<List<E.MCall>> {
//    private final List<E.MCall> res = new ArrayList<>();
//    private final Goal goal;
//    private final GuessIT guessIT;
//    private Goal location;
//    //  private final Set<Goal> visited = new HashSet<>();
//    public VariantCallCollector(GuessIT guessIT, Goal goal) {
//      this.goal = goal;
//      this.guessIT = guessIT;
//    }
//
//    @Override public List<E.MCall> res() {
//      return Collections.unmodifiableList(res);
//    }
//
//    @Override public Void visitLambda(E.Lambda e) {
//      var oldLoc = location;
//      location = new Goal(e., location.expectedName);
//      CollectorVisitor.super.visitLambda(e);
//      location = oldLoc;
//      return null;
//    }
//
//    @Override public Void visitMeth(E.Meth m) {
//      var oldLoc = location;
//      location = new Goal(location.expectedRecv, m.name());
//      CollectorVisitor.super.visitMeth(m);
//      location = oldLoc;
//      return null;
//    }
//
//    @Override public Void visitMCall(E.MCall e) {
//      if (!e.name().equals(goal.expectedName)) { return CollectorVisitor.super.visitMCall(e); }
//
//      var recvITs = e.receiver().accept(guessIT);
//      recvITs.stream().filter(it->it.name().equals(goal.expectedRecv)).findFirst()
//        .ifPresent(recvIT->res.add(e));
//
//      return CollectorVisitor.super.visitMCall(e);
//    }
//  }
//  class ContainsGenericsVisitor implements ShortCircuitVisitor<Boolean> {
//    @Override public Optional<Boolean> visitGX(Id.GX<T> t) {
//      return Optional.of(true);
//    }
//    @Override public Optional<Boolean> visitLambda(E.Lambda e) {
//      return Optional.empty();
//    }
//  }
}
