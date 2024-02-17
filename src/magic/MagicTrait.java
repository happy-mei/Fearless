package magic;

import codegen.MIR;

public interface MagicTrait<E,R> extends MagicCallable<E,R> {
  MIR.MT.Usual name();
  R instantiate();
}
