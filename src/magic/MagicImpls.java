package magic;

import ast.E;
import ast.T;
import id.Id;
import id.Mdf;
import program.Program;
import utils.Bug;

public interface MagicImpls<R> {
  default MagicTrait<R> get(E.Lambda e) {
    // TODO: this may be incorrect, what if we have .x(n: Num), the n implements Num but not 5 or whatever?
    if (e.its().stream().anyMatch(it->it.name().equals(Magic.Num))) { return num(e); }
    if (e.its().stream().anyMatch(it->it.name().equals(Magic.UInt))) { return unum(e); }
    if (e.its().stream().anyMatch(it->it.name().equals(Magic.Str))) { return str(e); }
    if (e.its().stream().anyMatch(it->it.name().equals(Magic.RefK))) { return refK(e); }
    throw Bug.unreachable();
  }
  MagicTrait<R> num(E.Lambda e);
  MagicTrait<R> unum(E.Lambda e);
  MagicTrait<R> str(E.Lambda e);
  MagicTrait<R> refK(E.Lambda e);
}
