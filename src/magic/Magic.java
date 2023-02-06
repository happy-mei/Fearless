package magic;

import astFull.E;
import astFull.Program;
import astFull.T;
import id.Id;
import id.Mdf;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class Magic {
  public static final Id.DecId NoMutHyg = new Id.DecId("base.NoMutHyg", 1);
  public static final Id.DecId UNum = new Id.DecId("base.UNum", 0);
  public static final Id.DecId Num = new Id.DecId("base.Num", 0);

  public static Optional<Id.IT<astFull.T>> resolve(String name) {
    var isAlphaNumeric  = !name.isEmpty() && (Character.isDigit(name.charAt(0)) || name.startsWith("\""));
    if(isAlphaNumeric){ return Optional.of(new Id.IT<>(name, List.of())); }
    return switch(name){
//      case noMutHygName -> Optional.of(new Id.IT<>(new Id.DecId(noMutHygName, 0), List.of()));
      default -> Optional.empty();
    };
  }

  public static astFull.T.Dec getFullDec(Function<Id.DecId, astFull.T.Dec> resolve, Id.DecId id) {
    // TODO: strings and unsigned
    if (Character.isDigit(id.name().charAt(0))) {
      var base = resolve.apply(new Id.DecId("base._NumInstance", 0)).toIT();
      return new astFull.T.Dec(id, List.of(), new E.Lambda(
        Optional.of(Mdf.imm),
        List.of(base),
        E.X.freshName(),
        List.of(),
        Optional.of(new Id.IT<>(id.name(), List.of())),
        Optional.empty()
      ), Optional.empty());
    }
    return null;
  }

  public static ast.T.Dec getDec(Function<Id.DecId, ast.T.Dec> resolve, Id.DecId id) {
    // TODO: strings and unsigned
    if (Character.isDigit(id.name().charAt(0)) && id.gen() == 0) {
      var baseDec = resolve.apply(new Id.DecId("base._NumInstance", 0));
      return baseDec.withName(id);
    }
    return null;
  }
}
