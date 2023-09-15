package program.typesystem;

import ast.E;
import ast.T;
import failure.CompileError;
import failure.Res;
import id.Mdf;
import program.Program;
import utils.Bug;
import visitors.Visitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface ETypeSystem extends Visitor<Res> {
  Program p();
  Gamma g();
  XBs xbs();
  Optional<T> expectedT();
  int depth();
  Res bothT(T.Dec d);
  default Res visitX(E.X e){
    return g().get(e);
  }

  static ETypeSystem of(Program p, Gamma g, XBs xbs, Optional<T> expectedT,int depth){
    record Ts(Program p, Gamma g, XBs xbs, Optional<T> expectedT, int depth) implements EMethTypeSystem, ELambdaTypeSystem{}
    return new Ts(p,g,xbs,expectedT,depth);
  }
  default ETypeSystem withT(Optional<T> expectedT){ return of(p(), g(), xbs(), expectedT, depth()); }
  default ETypeSystem withGamma(Gamma g){ return of(p(), g, xbs(), expectedT(), depth()); }
  default ETypeSystem withXBs(XBs xbs){ return of(p(), g(), xbs, expectedT(), depth()); }
  default ETypeSystem withProgram(Program p){ return of(p, g(), xbs(), expectedT(), depth()); }
}