package magic;

import ast.Program;
import ast.T;
import codegen.MIR;
import codegen.MIRInjectionVisitor;
import failure.CompileError;
import id.Id;
import id.Mdf;
import utils.Bug;
import visitors.MIRVisitor;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface MagicImpls<R> extends MIRVisitor<Optional<MIR.Lambda>> {
  default Optional<MIR.Lambda> visitProgram(Map<String, List<MIR.Trait>> pkgs, Id.DecId entry) { throw Bug.unreachable(); }
  default Optional<MIR.Lambda> visitTrait(String pkg, MIR.Trait trait) { throw Bug.unreachable(); }
  default Optional<MIR.Lambda> visitMeth(MIR.Meth meth, String selfName, boolean concrete) { throw Bug.unreachable(); }
  default Optional<MIR.Lambda> visitX(MIR.X x) {
    var ret = p().of(x.t().itOrThrow().name());
    try {
      return Optional.of(new MIRInjectionVisitor(p()).visitLambda("base", ret.lambda(), Map.of()));
    } catch (MIRInjectionVisitor.NotInGammaException e) {
      return Optional.empty();
    }
  }
  default Optional<MIR.Lambda> visitMCall(MIR.MCall mCall) {
    var ret = p().of(mCall.t().itOrThrow().name());
    try {
      return Optional.of(new MIRInjectionVisitor(p()).visitLambda("base", ret.lambda(), Map.of()));
    } catch (MIRInjectionVisitor.NotInGammaException e) {
      return Optional.empty();
    }
  }
  default Optional<MIR.Lambda> visitLambda(MIR.Lambda newL) { return Optional.of(newL); }

  default Optional<MagicTrait<R>> get(MIR e) {
    return e.accept(this).flatMap(l->{
      // TODO: this may be incorrect, what if we have .x(n: Num), the n implements Num but not 5 or whatever?
      if (isMagic(Magic.Int, l)) { return Optional.of(int_(l)); }
//      if (isMagic(Magic.UInt, l)) { return Optional.of(uint(l)); }
//      if (isMagic(Magic.Float, l)) { return Optional.of(float_(l)); }
//      if (isMagic(Magic.Str, l)) { return Optional.of(str(l)); }
//      if (isMagic(Magic.RefK, l)) { return Optional.of(refK(l)); }
      return Optional.empty();
    });
  }

  default boolean isMagic(Id.DecId magicDec, MIR.Lambda l) {
    var name = l.freshName().name();
    if (l.freshName().gen() != 0) { return false; }
    if (!name.startsWith("base.") && Character.isJavaIdentifierStart(name.charAt(0))) {
      return false;
    }
    if (name.startsWith("base._")) { return false; } // Ignore all base helpers
    return p().isSubType(new T(l.mdf(), new Id.IT<>(l.freshName(), List.of())), new T(l.mdf(), new Id.IT<>(magicDec, List.of())));
  }

  MagicTrait<R> int_(MIR.Lambda e);
  MagicTrait<R> uint(MIR.Lambda e);
  MagicTrait<R> float_(MIR.Lambda e);
  MagicTrait<R> str(MIR.Lambda e);
  MagicTrait<R> refK(MIR.Lambda e);
  ast.Program p();
}
