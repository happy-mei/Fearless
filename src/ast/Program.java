package ast;

import id.Id;
import utils.Bug;

import java.util.List;
import java.util.Set;

public class Program implements program.Program  {
  @Override public List<Id.IT<T>> itsOf(Id.IT<T> t) {
    throw Bug.todo();
  }

  @Override
  public List<CM> cMsOf(Id.IT<T> t) {
    throw Bug.todo();
  }

  @Override
  public Set<Id.GX<T>> gxsOf(Id.IT<T> t) {
    throw Bug.todo();
  }
}
