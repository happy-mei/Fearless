package visitors;

import astFull.E;
import astFull.T;

import java.util.Optional;

public class FullCollectorVisitorWithEnv<R> implements FullCollectorVisitor<R> {
  protected Env env = new Env();
  public Optional<R> visitMeth(E.Meth e){
    var oldEnv=env;
    this.env=env.add(e);
    try{ return FullCollectorVisitor.super.visitMeth(e); }
    finally{ this.env=oldEnv; }
  }

  public Optional<R> visitDec(T.Dec d){
    var oldEnv=env;
    this.env=env.add(d.gxs());
    try{ return FullCollectorVisitor.super.visitDec(d); }
    finally{ this.env=oldEnv;}

  }
  public Optional<R> visitLambda(E.Lambda e){
    var oldEnv = env;
    this.env = Optional.ofNullable(e.selfName()).map(n->env.add(n, e.t())).orElse(env);
    try { return FullCollectorVisitor.super.visitLambda(e); }
    finally { this.env = oldEnv; }
  }
}
