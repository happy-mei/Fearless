package visitors;

import astFull.E;
import astFull.T;

import java.util.Optional;

public class FullShortCircuitVisitorWithEnv<R> implements FullShortCircuitVisitor<R> {
  protected Env env = new Env();
  public Optional<R> visitMeth(E.Meth e){
    var oldEnv=env;
    this.env=env.add(e);
    try{ return FullShortCircuitVisitor.super.visitMeth(e); }
    finally{ this.env=oldEnv; }
  }

  public Optional<R> visitDec(T.Dec d){
    var oldEnv=env;
    this.env=env.add(d);
    try{ return FullShortCircuitVisitor.super.visitDec(d); }
    finally{ this.env=oldEnv;}

  }
  public Optional<R> visitLambda(E.Lambda e){
    var oldEnv = env;
    this.env = Optional.ofNullable(e.selfName()).map(n->env.add(n, e.t())).orElse(env);
    try { return FullShortCircuitVisitor.super.visitLambda(e); }
    finally { this.env = oldEnv; }
  }
}
