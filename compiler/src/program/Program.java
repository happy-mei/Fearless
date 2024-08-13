package program;

import program.typesystem.SubTyping;

public interface Program extends TypeTable, SubTyping, MethLookup {
  default void reset() {
    this.methsCache().clear();
    this.subTypeCache().clear();
  }
}