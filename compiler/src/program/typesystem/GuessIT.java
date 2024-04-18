package program.typesystem;

import ast.E;
import ast.T;
import id.Id;
import id.Mdf;
import program.Program;
import program.TypeRename;
import utils.Bug;
import utils.OneOr;
import visitors.Visitor;

import java.util.Set;
import java.util.stream.Collectors;

public record GuessIT(Program p, Gamma g, XBs xbs, int depth) implements Visitor<Id.IT<T>> {
  @Override public Id.IT<T> visitX(E.X e) {
    return g().get(e).itOrThrow();
  }
  @Override public Id.IT<T> visitLambda(E.Lambda e) {
    return e.name().toIT();
  }
  @Override public Id.IT<T> visitMCall(E.MCall e) {
    var renamer = TypeRename.core(p());
    var recv = e.receiver().accept(this);
    return OneOr.of("", this.p().meths(this.xbs(), Mdf.recMdf, recv, this.depth()).stream()
      .filter(cm->cm.name().nameArityEq(e.name()))
      .map(cm->renamer.renameSigOnMCall(cm.sig(), this.xbs().addBounds(cm.sig().gens(), cm.bounds()), renamer.renameFun(e.ts(), cm.sig().gens())))
      .filter(sig->sig.ret().isIt())
      .map(sig->sig.ret().itOrThrow()));
  }
}
