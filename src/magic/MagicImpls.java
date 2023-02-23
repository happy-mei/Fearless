package magic;

import ast.Program;
import ast.T;
import codegen.MIR;
import codegen.MIRInjectionVisitor;
import id.Id;
import utils.Bug;
import visitors.MIRVisitor;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface MagicImpls<R> {
  static boolean isLiteral(String name) {
    return Character.isDigit(name.charAt(0)) || name.startsWith("\"") || name.startsWith("-");
  }

  default Optional<MagicTrait<R>> get(MIR e) {
    return e.accept(new LambdaVisitor(p())).flatMap(l->{
      // TODO: this may be incorrect, what if we have .x(n: Num), the n implements Num but not 5 or whatever?
      if (isMagic(Magic.Int, l, e)) { return Optional.of(int_(l, e)); }
      if (isMagic(Magic.UInt, l, e)) { return Optional.of(uint(l, e)); }
      if (isMagic(Magic.Float, l, e)) { return Optional.of(float_(l, e)); }
      if (isMagic(Magic.Str, l, e)) { return Optional.of(str(l, e)); }
//      if (isMagic(Magic.RefK, l, e)) { return Optional.of(refK(l, e)); }
      if (isMagic(Magic.Assert, l, e)) { return Optional.of(assert_(l, e)); }
      return Optional.empty();
    });
  }

  default boolean isMagic(Id.DecId magicDec, MIR.Lambda l, MIR e) {
    var name = l.freshName().name();
    if (l.freshName().gen() != 0) { return false; }
    if (!name.startsWith("base.") && Character.isJavaIdentifierStart(name.charAt(0))) {
      return false;
    }
    if (name.startsWith("base._")) { return false; } // Ignore all base helpers
    return p().isSubType(new T(l.mdf(), new Id.IT<>(l.freshName(), List.of())), new T(l.mdf(), new Id.IT<>(magicDec, List.of())));
  }

  MagicTrait<R> int_(MIR.Lambda l, MIR e);
  MagicTrait<R> uint(MIR.Lambda l, MIR e);
  MagicTrait<R> float_(MIR.Lambda l, MIR e);
  MagicTrait<R> str(MIR.Lambda l, MIR e);
  MagicTrait<R> refK(MIR.Lambda l, MIR e);
  MagicTrait<R> assert_(MIR.Lambda l, MIR e);
  ast.Program p();

  record LambdaVisitor(Program p) implements MIRVisitor<Optional<MIR.Lambda>> {
    public Optional<MIR.Lambda> visitProgram(Map<String, List<MIR.Trait>> pkgs, Id.DecId entry) { throw Bug.unreachable(); }
    public Optional<MIR.Lambda> visitTrait(String pkg, MIR.Trait trait) { throw Bug.unreachable(); }
    public Optional<MIR.Lambda> visitMeth(MIR.Meth meth, String selfName, boolean concrete) { throw Bug.unreachable(); }
    public Optional<MIR.Lambda> visitX(MIR.X x, boolean _ignored) {
      var ret = p().of(x.t().itOrThrow().name());
      try {
        return Optional.of(new MIRInjectionVisitor(p()).visitLambda("base", ret.lambda(), Map.of()));
      } catch (MIRInjectionVisitor.NotInGammaException e) {
        return Optional.empty();
      }
    }
    public Optional<MIR.Lambda> visitMCall(MIR.MCall mCall, boolean _ignored) {
      var ret = p().of(mCall.t().itOrThrow().name());
      try {
        return Optional.of(new MIRInjectionVisitor(p()).visitLambda("base", ret.lambda(), Map.of()));
      } catch (MIRInjectionVisitor.NotInGammaException e) {
        return Optional.empty();
      }
    }
    public Optional<MIR.Lambda> visitLambda(MIR.Lambda newL, boolean _ignored) { return Optional.of(newL); }
  }
}
