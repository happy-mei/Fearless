package program.typesystem;

import ast.E;
import failure.Res;
import utils.Bug;

interface ELambdaTypeSystem extends ETypeSystem{
  default Res visitLambda(E.Lambda e){ throw Bug.todo(); }
}
