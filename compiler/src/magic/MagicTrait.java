package magic;

import codegen.MIR;
import id.Id;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

public interface MagicTrait<E,R> extends MagicCallable<E,R> {
  Optional<R> instantiate();

  @Override default Optional<R> call(Id.MethName m, List<? extends E> args, EnumSet<MIR.MCall.CallVariant> variants, MIR.MT expectedT) {
    return Optional.empty();
  }
}
