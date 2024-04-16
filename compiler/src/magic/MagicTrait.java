package magic;

import java.util.Optional;

public interface MagicTrait<E,R> extends MagicCallable<E,R> {
  Optional<R> instantiate();
}
