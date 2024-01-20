package magic;

import ast.E;
import id.Id;
import program.typesystem.ETypeSystem;
import program.typesystem.GuessIT;
import visitors.CollectorVisitor;

import java.util.*;

public class VariantCallCollector implements CollectorVisitor<List<E.MCall>> {
  private final List<E.MCall> res = new ArrayList<>();
  private final Goal goal;
  private final GuessIT guessIT;
  private final Set<Goal> visited = new HashSet<>();
  public VariantCallCollector(ETypeSystem ts, Goal goal) {
    this.goal = goal;
    this.guessIT = new GuessIT(ts.p(), ts.g(), ts.xbs(), ts.depth());
  }

  @Override public List<E.MCall> res() {
    return Collections.unmodifiableList(res);
  }

  @Override public Void visitMCall(E.MCall e) {
    if (!e.name().equals(goal.expectedName)) { return CollectorVisitor.super.visitMCall(e); }

    var recvITs = e.receiver().accept(guessIT);
    recvITs.stream().filter(it->it.name().equals(goal.expectedRecv)).findFirst().ifPresent(recvIT->{
//      if (e.)
    });

    return CollectorVisitor.super.visitMCall(e);
  }

  public record Goal(Id.DecId expectedRecv, Id.MethName expectedName) {}
}
