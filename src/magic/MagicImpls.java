package magic;

import ast.T;
import codegen.MIR;
import id.Id;
import id.Mdf;
import program.typesystem.XBs;

import java.util.List;
import java.util.Optional;

public interface MagicImpls<R> {
  static boolean isLiteral(String name) {
    return Character.isDigit(name.charAt(0)) || name.startsWith("\"") || name.startsWith("-");
  }

  default Optional<MagicTrait<MIR,R>> get(MIR e) {
    if (isMagic(Magic.Int, e)) { return Optional.ofNullable(int_(e)); }
    if (isMagic(Magic.UInt, e)) { return Optional.ofNullable(uint(e)); }
    if (isMagic(Magic.Float, e)) { return Optional.ofNullable(float_(e)); }
    if (isMagic(Magic.Str, e)) { return Optional.ofNullable(str(e)); }
    if (isMagic(Magic.Debug, e)) { return Optional.ofNullable(debug(e)); }
    if (isMagic(Magic.RefK, e)) { return Optional.ofNullable(refK(e)); }
    if (isMagic(Magic.IsoPodK, e)) { return Optional.ofNullable(isoPodK(e)); }
    if (isMagic(Magic.Assert, e)) { return Optional.ofNullable(assert_(e)); }
    if (isMagic(Magic.Abort, e)) { return Optional.ofNullable(abort(e)); }
    if (isMagic(Magic.MagicAbort, e)) { return Optional.ofNullable(magicAbort(e)); }
    if (isMagic(Magic.ErrorK, e)) { return Optional.ofNullable(errorK(e)); }
    if (isMagic(Magic.Try, e)) { return Optional.ofNullable(tryCatch(e)); }
    if (isMagic(Magic.PipelineParallelSinkK, e)) { return Optional.ofNullable(pipelineParallelSinkK(e)); }
    return Magic.ObjectCaps.stream()
      .filter(target->isMagic(target, e))
      .map(target->Optional.ofNullable(objCap(target, e)))
      .findAny()
      .flatMap(o->o);
  }

  default boolean isMagic(Id.DecId magicDec, MIR e) {
    var it = e.t().itOrThrow();
    return isMagic(magicDec, it.name());
  }
  default boolean isMagic(Id.DecId magicDec, Id.DecId freshName) {
    var name = freshName.name();
    if (freshName.gen() != 0) { return false; }
//    if (!name.startsWith("base.") && Character.isJavaIdentifierStart(name.charAt(0))) {
//      return false;
//    }
    return p().isSubType(XBs.empty(), new T(Mdf.mdf, new Id.IT<>(freshName, List.of())), new T(Mdf.mdf, new Id.IT<>(magicDec, List.of())));
  }

  MagicTrait<MIR,R> int_(MIR e);
  MagicTrait<MIR,R> uint(MIR e);
  MagicTrait<MIR,R> float_(MIR e);
  MagicTrait<MIR,R> str(MIR e);
  MagicTrait<MIR,R> debug(MIR e);
  MagicTrait<MIR,R> refK(MIR e);
  MagicTrait<MIR,R> isoPodK(MIR e);
  MagicTrait<MIR,R> assert_(MIR e);
  default MagicTrait<MIR,R> abort(MIR e) { return null; }
  default MagicTrait<MIR,R> magicAbort(MIR e) { return null; }
  default MagicTrait<MIR,R> errorK(MIR e) { return null; }
  default MagicTrait<MIR,R> tryCatch(MIR e) { return null; }
  default MagicTrait<MIR,R> pipelineParallelSinkK(MIR e) { return null; }
  default MagicTrait<MIR,R> objCap(Id.DecId magicTrait, MIR e) { return null; }
  default MagicTrait<MIR,R> variantCall(MIR e) { return null; }
  ast.Program p();

}
