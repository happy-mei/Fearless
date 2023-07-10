package wellFormedness;

import ast.E;
import ast.Program;
import ast.T;
import failure.CompileError;
import failure.Fail;
import id.Id;
import id.Mdf;
import magic.Magic;
import magic.MagicImpls;
import utils.Bug;
import visitors.ShortCircuitVisitor;
import visitors.ShortCircuitVisitorWithEnv;
import visitors.Visitor;

import java.util.List;
import java.util.Optional;

// TODO: Sealed and _C/_m restrictions
public class WellFormednessShortCircuitVisitor extends ShortCircuitVisitorWithEnv<CompileError> {
  private final Program p;
  private Optional<String> pkg = Optional.empty();
  private E.Lambda scope;
  public WellFormednessShortCircuitVisitor(Program p) { this.p = p; }

  @Override public Optional<CompileError> visitDec(T.Dec d) {
    pkg = Optional.of(d.name().pkg());
    return ShortCircuitVisitor.visitAll(
        d.lambda().its(),
        it->noRecMdfInImpls(it).map(err->err.pos(d.lambda().pos()))
        )
      .or(()->super.visitDec(d))
      .map(err->err.parentPos(d.pos()));
  }

  @Override public Optional<CompileError> visitLambda(E.Lambda e) {
    this.scope = e;
    return ShortCircuitVisitor.visitAll(e.its(), it->noPrivateTraitOutsidePkg(it.name()))
      .or(()->noSealedOutsidePkg(e))
      .or(()->super.visitLambda(e))
      .map(err->err.parentPos(e.pos()));
  }

  @Override public Optional<CompileError> visitMCall(E.MCall e) {
    return noIsoParams(e.ts())
      .or(()->noPrivateMethCallOutsideTrait(e, scope))
      .or(()->super.visitMCall(e))
      .map(err->err.parentPos(e.pos()));
  }

  @Override public Optional<CompileError> visitX(E.X e) {
    return super.visitX(e)
      .or(()->noIsoMoreThanOnce(e))
      .map(err->err.parentPos(e.pos()));
  }

  @Override public Optional<CompileError> visitMeth(E.Meth e) {
    return noRecMdfInNonHyg(e.sig(), e.name()).map(err->err.pos(e.pos()))
      .or(()->super.visitMeth(e))
      .map(err->err.parentPos(e.pos()));
  }

  @Override public Optional<CompileError> visitT(T t) {
    assert !(t.mdf().isMdf() && t.isIt());
    return noHygInMut(t).or(()->super.visitT(t));
  }

  @Override public Optional<CompileError> visitIT(Id.IT<T> t) {
    return noIsoParams(t, t.ts())
      .or(()->super.visitIT(t));
  }

  private Optional<CompileError> noIsoMoreThanOnce(E.X x) {
    var t = env.get(x);
    if (!t.mdf().isIso()) { return Optional.empty(); }
    if (env.usages().getOrDefault(x.name(), 0) <= 1) { return Optional.empty(); }
    return Optional.of(Fail.multipleIsoUsage(x).pos(x.pos()));
  }

  private Optional<CompileError> noHygInMut(T t) {
    // TODO: re-evaluate this
//    if (!(t.rt() instanceof Id.IT<T> it)) { return Optional.empty(); }
//    if (t.mdf().isMut() && it.ts().stream().map(T::mdf).anyMatch(Mdf::isHyg)) {
//      return Optional.of(Fail.mutCapturesHyg(t));
//    }
    return Optional.empty();
  }

  private Optional<CompileError> noRecMdfInNonHyg(E.Sig s, Id.MethName name) {
    if (s.mdf().isHyg()) { return Optional.empty(); }
    return new ShortCircuitVisitor<CompileError>(){
      @Override public Optional<CompileError> visitT(T t) {
        if (t.mdf().isRecMdf()) {
          return Optional.of(Fail.recMdfInNonHyg(s.mdf(), name, t).pos(s.pos()));
        }
        return ShortCircuitVisitor.super.visitT(t);
      }
    }.visitSig(s);
  }

  private Optional<CompileError> noRecMdfInImpls(Id.IT<T> it) {
    return new ShortCircuitVisitor<CompileError>(){
      public Optional<CompileError> visitT(T t) {
        if (t.mdf().isRecMdf()) { return Optional.of(Fail.recMdfInImpls(t)); }
        return ShortCircuitVisitor.super.visitT(t);
      }
    }.visitIT(it);
  }

  private Optional<CompileError> noPrivateMethCallOutsideTrait(E.MCall e, E.Lambda callSite) {
    if (!e.name().name().startsWith("._")) { return Optional.empty(); }
    var tmpDec = new T.Dec(new Id.DecId(Id.GX.fresh().name(), 0), List.of(), callSite, callSite.pos());
    var meth = p.withDec(tmpDec).meths(Mdf.mdf, tmpDec.toIT(), e.name(), 0);
    if (meth.isPresent()) { return Optional.empty(); }
    // TODO: use env and handle calls to private methods on a parent scope
    return Optional.empty();
//    return Optional.of(Fail.privateMethCall(e.name()));
  }

  private Optional<CompileError> noPrivateTraitOutsidePkg(Id.DecId dec) {
    if (MagicImpls.isLiteral(dec.name()) || !dec.shortName().startsWith("_")) { return Optional.empty(); }
    var pkg = this.pkg.orElseThrow();
    if (dec.pkg().equals(pkg)) { return Optional.empty(); }
    return Optional.of(Fail.privateTraitImplementation(dec));
  }

  private Optional<CompileError> noSealedOutsidePkg(E.Lambda e) {
    if (e.meths().isEmpty()) { return Optional.empty(); }
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

  private Optional<CompileError> noIsoParams(Id.IT<?> base, List<T> genArgs) {
    return genArgs.stream()
      .flatMap(T::flatten)
      .dropWhile(t->t.mdf() != Mdf.iso)
      .map(t_->base.toString())
      .map(Fail::isoInTypeArgs)
      .findFirst();
  }
  private Optional<CompileError> noIsoParams(List<T> genArgs) {
    return genArgs.stream()
      .flatMap(T::flatten)
      .dropWhile(t->t.mdf() != Mdf.iso)
      .map(T::toString)
      .map(Fail::isoInTypeArgs)
      .findFirst();
  }
}
