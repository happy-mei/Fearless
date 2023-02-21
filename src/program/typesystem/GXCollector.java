package program.typesystem;

import ast.T;
import id.Id;
import visitors.CollectorVisitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Collects top level generic params */
public class GXCollector implements CollectorVisitor<List<Id.GX<T>>> {
  private final List<Id.GX<T>> res = new ArrayList<>();
  public List<Id.GX<T>> res() { return Collections.unmodifiableList(res); }
  public Void visitGX(Id.GX<T> gx) {
    res.add(gx);
    return CollectorVisitor.super.visitGX(gx);
  }
}
