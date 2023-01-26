package magic;

import astFull.E;
import astFull.T;
import id.Id;

import java.util.List;
import java.util.Optional;

public class Magic {
  public static final String noMutHygName = "base.NoMutHyg";
  public static final Id.DecId noMutHyg = new Id.DecId(noMutHygName, 1);
  public static Optional<Id.IT<T>> resolve(String name) {
    var isAlphaNumeric  = !name.isEmpty() &&
      (Character.isDigit(name.charAt(0)) || name.startsWith("\""));
    if(isAlphaNumeric){ return Optional.of(new Id.IT<>(name, List.of())); }
    return switch(name){
      case noMutHygName -> Optional.of(new Id.IT<>(new Id.DecId(noMutHygName, 0), List.of()));
      default -> Optional.empty();
    };
  }
  public static T.Dec getDec(Id.DecId id) {
    if(id.equals(noMutHyg)){
      var gens = List.of(new Id.GX<T>());
      return new T.Dec(noMutHyg, gens, new E.Lambda(
        Optional.empty(),
        List.of(),
        null,
        List.of(),
        Optional.empty(),
        Optional.empty()
      ), Optional.empty());
    }
    return null;
  }
}
