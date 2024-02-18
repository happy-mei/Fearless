package magic;

import codegen.MIR;

public interface MagicTrait<E,R> extends MagicCallable<E,R> {
  R instantiate();
}
