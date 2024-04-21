package program.typesystem;

import ast.E;
import ast.T;
import failure.CompileError;
import failure.Fail;
import failure.FailOr;
import program.Program;
import visitors.Visitor;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public interface ETypeSystem extends Visitor<FailOr<T>> {
  Program p();
  Gamma g();
  XBs xbs();
  ConcurrentHashMap<Long, TsT> resolvedCalls();
  List<T> expectedT();
  int depth();//TODO: check if still needed
  default FailOr<T> visitX(E.X e){
    try{ return FailOr.res(g().get(e)); }
    catch (CompileError err){ return err.fail(); }

    //dead error?
    //  return Optional.of(()->Fail.xTypeError(expected, res, e).pos(e.pos()));
  }

  static ETypeSystem of(Program p, Gamma g, XBs xbs, List<T> expectedT, ConcurrentHashMap<Long, TsT> resolvedCalls, int depth){
    record Ts(Program p, Gamma g, XBs xbs, List<T> expectedT, ConcurrentHashMap<Long, TsT> resolvedCalls, int depth) implements EMethTypeSystem, ELambdaTypeSystem{}
    return new Ts(p,g,xbs,expectedT,resolvedCalls,depth);
  }
  default ETypeSystem withExpectedTs(List<T> expectedT){ return of(p(), g(), xbs(), expectedT, resolvedCalls(), depth()); }
  default ETypeSystem withGamma(Gamma g){ return of(p(), g, xbs(), expectedT(), resolvedCalls(), depth()); }
  default ETypeSystem withXBs(XBs xbs){ return of(p(), g(), xbs, expectedT(), resolvedCalls(), depth()); }
  default ETypeSystem withProgram(Program p){ return of(p, g(), xbs(), expectedT(), resolvedCalls(), depth()); }
}