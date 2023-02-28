package wellFormedness;

import ast.E;
import ast.Program;
import ast.T;
import failure.CompileError;
import failure.Fail;
import id.Id;
import magic.Magic;
import visitors.ShortCircuitVisitor;

import java.util.List;
import java.util.Optional;

// TODO: Sealed and _C/_m restrictions
public class WellFormednessShortCircuitVisitor implements ShortCircuitVisitor<CompileError> {
  private final Program p;
  private Optional<String> pkg = Optional.empty();
  public WellFormednessShortCircuitVisitor(Program p) { this.p = p; }

  @Override
  public Optional<CompileError> visitDec(T.Dec d) {
    pkg = Optional.of(d.name().pkg());
    return ShortCircuitVisitor.super.visitDec(d);
  }

  @Override public Optional<CompileError> visitLambda(E.Lambda e) {
    return ShortCircuitVisitor.visitAll(e.its(), it->noRecMdfInImpls(it).map(err->err.pos(e.pos())))
      .or(()->noSealedOutsidePkg(e))
      .or(()->ShortCircuitVisitor.super.visitLambda(e));
  }

  @Override public Optional<CompileError> visitMeth(E.Meth e) {
    return noRecMdfInNonHyg(e)
      .or(()->ShortCircuitVisitor.super.visitMeth(e));
  }

  @Override public Optional<CompileError> visitT(T t) {
    assert !(t.mdf().isMdf() && t.isIt());
    return ShortCircuitVisitor.super.visitT(t);
  }

  private Optional<CompileError> noRecMdfInImpls(Id.IT<T> it) {
    return new ShortCircuitVisitor<CompileError>(){
      public Optional<CompileError> visitT(T t) {
        if (t.mdf().isRecMdf()) { return Optional.of(Fail.recMdfInImpls(t)); }
        return ShortCircuitVisitor.super.visitT(t);
      }
    }.visitIT(it);
  }
  private Optional<CompileError> noRecMdfInNonHyg(E.Meth m) {
    if (m.sig().mdf().isHyg()) { return Optional.empty(); }
    return new ShortCircuitVisitor<CompileError>(){
      @Override
      public Optional<CompileError> visitLambda(E.Lambda e) {
        if (e.mdf().isRecMdf()) { return Optional.of(Fail.recMdfInNonHyg(m.sig().mdf(), m.name(), e).pos(e.pos())); }
        return ShortCircuitVisitor.super.visitLambda(e);
      }
      public Optional<CompileError> visitT(T t) {
        if (t.mdf().isRecMdf()) { return Optional.of(Fail.recMdfInNonHyg(m.sig().mdf(), m.name(), t).pos(m.pos())); }
        return ShortCircuitVisitor.super.visitT(t);
      }
    }.visitMeth(m);
  }

  private Optional<CompileError> noSealedOutsidePkg(E.Lambda e) {
    var pkg = this.pkg.orElseThrow();
    return getSealedDec(e.its()).filter(dec->!dec.pkg().equals(pkg)).map(dec->Fail.sealedCreation(dec, pkg).pos(e.pos()));
  }

  private Optional<Id.DecId> getSealedDec(List<Id.IT<T>> its) {
    if (its.isEmpty()) { return Optional.empty(); }
    return its.stream()
//      .map(Id.IT::name)
      .filter(it->p.itsOf(it).stream().anyMatch(it1->it1.name().equals(Magic.Sealed)))
      .map(Id.IT::name)
      .findFirst()
      .or(()->getSealedDec(its.stream().flatMap(it->p.itsOf(it).stream()).toList()));
  }
}
