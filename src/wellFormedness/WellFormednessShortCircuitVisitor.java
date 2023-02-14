package wellFormedness;

import ast.E;
import ast.T;
import astFull.Program;
import failure.CompileError;
import failure.Fail;
import files.HasPos;
import id.Id;
import id.Mdf;
import magic.Magic;
import visitors.FullShortCircuitVisitorWithEnv;
import visitors.ShortCircuitVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// TODO: Sealed and _C/_m restrictions
public class WellFormednessShortCircuitVisitor implements ShortCircuitVisitor<CompileError> {
  @Override
  public Optional<CompileError> visitLambda(E.Lambda e) {
    return ShortCircuitVisitor.visitAll(e.its(), it->noRecMdfInImpls(it).map(err->err.pos(e.pos())))
      .or(()->ShortCircuitVisitor.super.visitLambda(e));
  }

  @Override
  public Optional<CompileError> visitMeth(E.Meth e) {
    return noRecMdfInNonHyg(e)
      .or(()->ShortCircuitVisitor.super.visitMeth(e));
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
}
