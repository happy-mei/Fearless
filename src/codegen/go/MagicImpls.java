package codegen.go;

import codegen.MIR;
import magic.MagicCallable;
import magic.MagicTrait;
import utils.Bug;

public record MagicImpls(PackageCodegen gen, ast.Program p) implements magic.MagicImpls<String> {
  @Override public MagicTrait<MIR.E, String> int_(MIR.E e) {
    throw Bug.todo();
  }

  @Override public MagicTrait<MIR.E, String> uint(MIR.E e) {
    throw Bug.todo();
  }

  @Override public MagicTrait<MIR.E, String> float_(MIR.E e) {
    throw Bug.todo();
  }

  @Override public MagicTrait<MIR.E, String> str(MIR.E e) {
    throw Bug.todo();
  }

  @Override public MagicTrait<MIR.E, String> debug(MIR.E e) {
    throw Bug.todo();
  }

  @Override public MagicTrait<MIR.E, String> refK(MIR.E e) {
    throw Bug.todo();
  }

  @Override public MagicTrait<MIR.E, String> isoPodK(MIR.E e) {
    throw Bug.todo();
  }

  @Override public MagicTrait<MIR.E, String> assert_(MIR.E e) {
    throw Bug.todo();
  }

  @Override public MagicCallable<MIR.E, String> variantCall(MIR.E e) {
    return magic.MagicImpls.super.variantCall(e);
  }
}
