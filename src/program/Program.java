package program;

import astFull.T;
import id.Id;
import magic.Magic;
import utils.Bug;
import visitors.FullCloneVisitor;
import visitors.FullCollectorVisitor;

import java.util.*;

public record Program(Map<Id.DecId, T.Dec> ds) {
  T.Dec of(Id.DecId d) {
    var res = ds.get(d);
    if (res == null) { res = Magic.getDec(d); }
    assert res != null;
    return res;
  }

  T.Dec of(Id.IT<T> t) {
    throw Bug.todo();
  }

  public Set<Id.DecId> superDecIds(Id.DecId start) {
    HashSet<Id.DecId> visited = new HashSet<>();
    superDecIds(visited, start);
    return Collections.unmodifiableSet(visited);
  }

  public void superDecIds(HashSet<Id.DecId> visited, Id.DecId current) {
    var currentDec = of(current);
    for(var it : currentDec.lambda().its()) {
      var novel=visited.add(it.name());
      if(novel){ superDecIds(visited, it.name()); }
    }
  }

  @Override public String toString() { return this.ds().toString(); }
}
