package magic;

import astFull.E;
import astFull.T;
import id.Id;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

public class Magic {
  // NoMutHyg is a string because it exists with 1...∞ generics
  public static final String noMutHyg = "base.NoMutHyg";
  public static Optional<Id.IT<T>> resolve(String name) {
    var isAlphaNumeric  = !name.isEmpty() &&
      (Character.isDigit(name.charAt(0)) || name.startsWith("\""));
    if(isAlphaNumeric){ return Optional.of(new Id.IT<>(name, List.of())); }
    return switch(name){
      case noMutHyg -> Optional.of(new Id.IT<>(new Id.DecId(noMutHyg, 0), List.of()));
      default -> Optional.empty();
    };
  }
  public static T.Dec getDec(Id.DecId id) {
    if(id.name().equals(noMutHyg)){
      if(id.gen() == 0){ return null; } // TODO: make only 1 instead of >1
      var gens = IntStream.range(0, id.gen()).mapToObj(i->new Id.GX<T>()).toList();
      return new T.Dec(new Id.DecId(noMutHyg, gens.size()), gens, new E.Lambda(
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
