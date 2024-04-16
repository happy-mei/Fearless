package program.typesystem;

import ast.E;
import ast.T;
import id.Id;
import id.Mdf;
import program.Program;
import program.TypeRename;
import visitors.Visitor;

import java.util.Set;
import java.util.stream.Collectors;

public record GuessITX(Program p, Gamma g, XBs xbs, int depth) implements Visitor<Set<Id.RT<T>>> {
  @Override public Set<Id.RT<T>> visitX(E.X e) {
    return Set.of(g().get(e).rt());
  }
  @Override public Set<Id.RT<T>> visitLambda(E.Lambda e) {
    return Set.copyOf(e.its());
  }
  @Override public Set<Id.RT<T>> visitMCall(E.MCall e) {
    var renamer = TypeRename.core(p());
    return e.receiver().accept(this).stream()
      .flatMap(recv->p().meths(xbs(), Mdf.recMdf, (Id.IT<T>) recv, depth()).stream())
      .filter(cm->cm.name().nameArityEq(e.name()))
      .map(cm->renamer.renameSigOnMCall(cm.sig(), xbs().addBounds(cm.sig().gens(), cm.bounds()), renamer.renameFun(e.ts(), cm.sig().gens())))
      .map(sig->sig.ret().rt())
      .collect(Collectors.toSet());
  }
}
