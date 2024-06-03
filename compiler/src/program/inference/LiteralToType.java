package program.inference;

import id.Id;
import magic.Magic;

import java.util.List;

public interface LiteralToType {
  static Id.IT<astFull.T> of(Id.IT<astFull.T> literal) {
    return switch (Magic.getLiteralKind(literal.name())) {
      case Str -> new Id.IT<>(Magic.Str, List.of());
      case Int -> new Id.IT<>(Magic.Int, List.of());
      case Nat -> new Id.IT<>(Magic.Nat, List.of());
      case Float -> new Id.IT<>(Magic.Float, List.of());
    };
  }
}
