package magic;

import astFull.T;
import id.Id;

import java.util.List;
import java.util.Optional;

public class Magic {
  public static final Id.DecId NoMutHyg = new Id.DecId("base.NoMutHyg", 1);
  public static final Id.DecId UNum = new Id.DecId("base.UNum", 0);
  public static final Id.DecId Num = new Id.DecId("base.Num", 0);

  public static Optional<Id.IT<T>> resolve(String name) {
    var isAlphaNumeric  = !name.isEmpty() && (Character.isDigit(name.charAt(0)) || name.startsWith("\""));
    if(isAlphaNumeric){ return Optional.of(new Id.IT<>(name, List.of())); }
    return switch(name){
//      case noMutHygName -> Optional.of(new Id.IT<>(new Id.DecId(noMutHygName, 0), List.of()));
      default -> Optional.empty();
    };
  }
  public static T.Dec getDec(Id.DecId id) {
    // TODO: nums
//    if (Character.isDigit(id.name().charAt(0))) {
//      return new T.Dec(
//
//      );
//    }
    return null;
  }
}
