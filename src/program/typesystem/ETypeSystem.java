package program.typesystem;

import ast.E;
import ast.T;
import failure.CompileError;
import failure.Fail;
import program.Program;
import visitors.Visitor;

import java.util.IdentityHashMap;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public interface ETypeSystem extends Visitor<Optional<Supplier<CompileError>>> {
  Program p();
  Gamma g();
  XBs xbs();
  ConcurrentHashMap<Long, EMethTypeSystem.TsT> resolvedCalls();
  Optional<T> expectedT();
  int depth();
  default Optional<Supplier<CompileError>> visitX(E.X e){
    var expected = expectedT().orElseThrow();
    T res; try { res = g().get(e);
    } catch (CompileError err) {
      return Optional.of(()->err.pos(e.pos()));
    }

    var isOk = p().isSubType(xbs(), res, expected);
    if (!isOk) {
      return Optional.of(()->Fail.xTypeError(expected, res, e).pos(e.pos()));
    }
    return Optional.empty();
  }

  static ETypeSystem of(Program p, Gamma g, XBs xbs, Optional<T> expectedT, ConcurrentHashMap<Long, EMethTypeSystem.TsT> resolvedCalls, int depth){
    record Ts(Program p, Gamma g, XBs xbs, Optional<T> expectedT, ConcurrentHashMap<Long, EMethTypeSystem.TsT> resolvedCalls, int depth) implements EMethTypeSystem, ELambdaTypeSystem{}
    return new Ts(p,g,xbs,expectedT,resolvedCalls,depth);
  }
  default ETypeSystem withT(Optional<T> expectedT){ return of(p(), g(), xbs(), expectedT, resolvedCalls(), depth()); }
  default ETypeSystem withGamma(Gamma g){ return of(p(), g, xbs(), expectedT(), resolvedCalls(), depth()); }
  default ETypeSystem withXBs(XBs xbs){ return of(p(), g(), xbs, expectedT(), resolvedCalls(), depth()); }
  default ETypeSystem withProgram(Program p){ return of(p, g(), xbs(), expectedT(), resolvedCalls(), depth()); }
}