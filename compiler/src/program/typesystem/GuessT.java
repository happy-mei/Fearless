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

public record GuessT(Program p, Gamma g, XBs xbs, int depth) implements Visitor<Set<T>> {
  @Override public Set<T> visitX(E.X e) {
    return Set.of(g().get(e));
  }
  @Override public Set<T> visitLambda(E.Lambda e) {
    return e.its().stream().map(it->new T(e.mdf(), it)).collect(Collectors.toSet());
  }
  @Override public Set<T> visitMCall(E.MCall e) {
    var renamer = TypeRename.core(p());
    return e.receiver().accept(this).stream()
      .flatMap(recv->p().meths(xbs(), Mdf.recMdf, recv.itOrThrow(), depth()).stream())
      .filter(cm->cm.name().nameArityEq(e.name()))
      .map(cm->renamer.renameSigOnMCall(cm.sig(), xbs().addBounds(cm.sig().gens(), cm.bounds()), renamer.renameFun(e.ts(), cm.sig().gens())))
      .map(E.Sig::ret)
      .collect(Collectors.toSet());
  }
}
