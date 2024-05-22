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

public record GuessIT(Program p, Gamma g, XBs xbs, int depth) implements Visitor<Set<Id.IT<T>>> {
  @Override public Set<Id.IT<T>> visitX(E.X e) {
    return g().get(e).match(gx->Set.of(), Set::of);
  }
  @Override public Set<Id.IT<T>> visitLambda(E.Lambda e) {
    return Set.copyOf(e.its());
  }
  @Override public Set<Id.IT<T>> visitMCall(E.MCall e) {
    var renamer = TypeRename.core();
    return e.receiver().accept(this).stream()
      .flatMap(recv->p().meths(xbs(), Mdf.recMdf, recv, depth()).stream())
      .filter(cm->cm.name().nameArityEq(e.name()))
      .map(cm->renamer.renameSigOnMCall(cm.sig(), xbs().addBounds(cm.sig().gens(), cm.bounds()), renamer.renameFun(e.ts(), cm.sig().gens())))
      .filter(sig->sig.ret().isIt())
      .map(sig->sig.ret().itOrThrow())
      .collect(Collectors.toSet());
  }
}
