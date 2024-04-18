package program.typesystem;

import ast.E;
import ast.T;
import id.Id;
import id.Mdf;
import program.Program;
import program.TypeRename;
import utils.OneOr;
import visitors.Visitor;

import java.util.Set;
import java.util.stream.Collectors;

public record GuessITX(Program p, Gamma g, XBs xbs, int depth) implements Visitor<Id.RT<T>> {
  @Override public Id.RT<T> visitX(E.X e) {
    return g().get(e).rt();
  }
  @Override public Id.RT<T> visitLambda(E.Lambda e) {
    return e.name().toIT();
  }
  @Override public Id.RT<T> visitMCall(E.MCall e) {
    var renamer = TypeRename.core(p());
    var recv = e.receiver().accept(this);
    return OneOr.of("", this.p().meths(this.xbs(), Mdf.recMdf, (Id.IT<T>) recv, this.depth()).stream()
      .filter(cm->cm.name().nameArityEq(e.name()))
      .map(cm->renamer.renameSigOnMCall(cm.sig(), this.xbs().addBounds(cm.sig().gens(), cm.bounds()), renamer.renameFun(e.ts(), cm.sig().gens())))
      .filter(sig->sig.ret().isIt())
      .map(sig->sig.ret().itOrThrow()));
  }
}
