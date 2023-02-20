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
  public static final Id.DecId UInt = new Id.DecId("base.UInt", 0);
  public static final Id.DecId Int = new Id.DecId("base.Int", 0);
  public static final Id.DecId Float = new Id.DecId("base.Float", 0);
  public static final Id.DecId Str = new Id.DecId("base.Str", 0);
  public static final Id.DecId RefK = new Id.DecId("base.Ref", 0);
  public static final Id.DecId Ref = new Id.DecId("base.Ref", 1);

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
      T.Dec base;
      if (id.name().chars().anyMatch(c->c=='.')) {
        base = resolve.apply(new Id.DecId("base._FloatInstance", 0));
      } else if (id.name().endsWith("u")) {
        base = resolve.apply(new Id.DecId("base._UIntInstance", 0));
      } else {
        base = resolve.apply(new Id.DecId("base._IntInstance", 0));
      }

      return base.withName(id);
    }
    return null;
  }

  public static ast.T.Dec getDec(Function<Id.DecId, ast.T.Dec> resolve, Id.DecId id) {
    // TODO: strings and unsigned
    if (Character.isDigit(id.name().charAt(0)) && id.gen() == 0) {
      ast.T.Dec baseDec;
      if (id.name().chars().anyMatch(c->c=='.')) {
        baseDec = resolve.apply(new Id.DecId("base._FloatInstance", 0));
      } else if (id.name().endsWith("u")) {
        baseDec = resolve.apply(new Id.DecId("base._UIntInstance", 0));
      } else {
        baseDec = resolve.apply(new Id.DecId("base._IntInstance", 0));
      }
      assert baseDec.lambda().its().size() == 2; // instance, kind
      return baseDec.withName(id).withLambda(baseDec.lambda().withITs(List.of(
        new Id.IT<>(id, List.of()),
        baseDec.lambda().its().get(1)
      )));
    }
    return null;
  }
}
