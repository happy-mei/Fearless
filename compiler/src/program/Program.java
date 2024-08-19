package program;

import program.typesystem.SubTyping;

public interface Program extends TypeTable, SubTyping, MethLookup {
  default void reset() {
    MethLookup.methsCache.clear();
    SubTyping.subTypeCache.clear();
  }
}