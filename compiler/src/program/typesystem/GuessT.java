package program.typesystem;

import ast.E;
import ast.T;
import id.Mdf;
import program.Program;
import program.TypeRename;
import utils.OneOr;
import visitors.Visitor;

public record GuessT(Program p, Gamma g, XBs xbs, int depth) implements Visitor<T> {
  @Override public T visitX(E.X e) {
    return g().get(e);
  }
  @Override public T visitLambda(E.Lambda e) {
    return new T(e.mdf(), e.id().toIT());
  }
  @Override public T visitMCall(E.MCall e) {
    var renamer = TypeRename.core();
    var recv = e.receiver().accept(this);
    return OneOr.of(
      "More than one matching method for GuessT",
      p().meths(xbs(), Mdf.recMdf, recv.itOrThrow(), depth()).stream()
      .filter(cm->cm.name().nameArityEq(e.name()))
      .map(cm->renamer.renameSigOnMCall(cm.sig(), xbs().addBounds(cm.sig().gens(), cm.bounds()), renamer.renameFun(e.ts(), cm.sig().gens())))
      .map(E.Sig::ret));
  }
}
