package ast;

import id.Id;
import utils.Bug;

import java.util.List;

public class Program implements program.Program  {
  @Override
  public List<Id.IT<T>> itsOf(Id.IT<T> t) {
    throw Bug.todo();
  }
}
