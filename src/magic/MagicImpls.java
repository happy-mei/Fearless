package magic;

import codegen.MIR;

import java.util.Optional;

public interface MagicImpls<R> {
  default Optional<MagicTrait<R>> get(MIR.Lambda e) {
    // TODO: this may be incorrect, what if we have .x(n: Num), the n implements Num but not 5 or whatever?
    if (e.its().stream().anyMatch(it->it.name().equals(Magic.Int))) { return Optional.of(int_(e)); }
    if (e.its().stream().anyMatch(it->it.name().equals(Magic.UInt))) { return Optional.of(uint(e)); }
    if (e.its().stream().anyMatch(it->it.name().equals(Magic.Float))) { return Optional.of(uint(e)); }
    if (e.its().stream().anyMatch(it->it.name().equals(Magic.Str))) { return Optional.of(str(e)); }
    if (e.its().stream().anyMatch(it->it.name().equals(Magic.RefK))) { return Optional.of(refK(e)); }
    return Optional.empty();
  }
  MagicTrait<R> int_(MIR.Lambda e);
  MagicTrait<R> uint(MIR.Lambda e);
  MagicTrait<R> float_(MIR.Lambda e);
  MagicTrait<R> str(MIR.Lambda e);
  MagicTrait<R> refK(MIR.Lambda e);
}
