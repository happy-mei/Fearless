package wellFormedness;

import ast.E;
import ast.T;
import id.Id;
import visitors.CollectorVisitor;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class UndefinedGXsVisitor implements CollectorVisitor<Set<Id.GX<T>>> {
  private final HashSet<Id.GX<T>> undefined = new HashSet<>();
  private final HashSet<Id.GX<T>> defined;
  public UndefinedGXsVisitor(Collection<Id.GX<T>> topLevelGXs) {
    defined = new HashSet<>(topLevelGXs);
  }

  public Set<Id.GX<T>> res() {
    var intersection = new HashSet<>(undefined);
    intersection.retainAll(defined);
    assert intersection.isEmpty();
    return undefined;
  }

  public Void visitGX(Id.GX<T> gx) {
    if (!defined.contains(gx)) { undefined.add(gx); }
    return CollectorVisitor.super.visitGX(gx);
  }
  public Void visitSig(E.Sig s) {
    s.gens().forEach(gx->{
      defined.add(gx);
      undefined.remove(gx);
    });
    return CollectorVisitor.super.visitSig(s);
  }
}
