package magic;

import astFull.E;
import astFull.T;
import id.Id;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

public class Magic {
  // NoMutHyg is a string because it exists with 1...∞ generics
  public static final String NoMutHyg = "base.NoMutHyg";
  public static Optional<Id.IT<T>> resolve(String name) {
    if (!name.isEmpty() && Character.isDigit(name.charAt(0))) { return Optional.of(new Id.IT<>( name, List.of())); }
    return switch (name) {
      case NoMutHyg -> Optional.of(new Id.IT<>(new Id.DecId(NoMutHyg, 0), List.of()));
      default -> Optional.empty();
    };
  }
  public static T.Dec getDec(Id.DecId id) {
    if (id.name().equals(NoMutHyg)) {
      var gens = IntStream.range(0, id.gen()).mapToObj(i->new Id.GX<T>()).toList();
      return new T.Dec(new Id.DecId(NoMutHyg, gens.size()), gens, new E.Lambda(
        Optional.empty(),
        List.of(),
        null,
        List.of(),
        Optional.empty()
      ));
    }
    return null;
  }
}
