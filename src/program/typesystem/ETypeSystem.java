package program.typesystem;

import ast.E;
import ast.T;
import ast.Program;
import utils.Bug;
import visitors.Visitor;
import failure.Res;

public interface ETypeSystem extends Visitor<Res> {
  Program p();
  Gamma g();
  T expectedT();
  default Res visitX(E.X e){ return g().get(e); }

  static ETypeSystem of(Program p, Gamma g, T expectedT){
    record Ts(Program p, Gamma g,T expectedT) implements EMethTypeSystem, ELambdaTypeSystem{}
    return new Ts(p,g,expectedT);
  }
  default ETypeSystem withT(T expectedT){ return of(p(),g(),expectedT); }
}