package program.typesystem;

import ast.E;
import failure.Res;
import utils.Bug;

interface EMethTypeSystem extends ETypeSystem{
  default Res visitMCall(E.MCall e){throw Bug.todo();}
}
