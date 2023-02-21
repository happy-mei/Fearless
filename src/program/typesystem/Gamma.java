package program.typesystem;

import failure.CompileError;
import failure.Fail;
import program.Program;
import ast.T;
import id.Mdf;

import java.util.Optional;

public interface Gamma {
  default T get(ast.E.X x) {
    try {
      return getO(x).orElseThrow(()->Fail.undefinedName(x.name()).pos(x.pos()));
    } catch (CompileError e) {
      throw e.pos(x.pos());
    }
  }
  default T get(String s) {
    return getO(s).orElseThrow(()->Fail.undefinedName(s));
  }
  default Optional<T> getO(ast.E.X x){ return getO(x.name()); }
  Optional<T> getO(String s);
  static Gamma empty(){ return x->Optional.empty(); }
  default Gamma add(String s, T t) {
    return x->x.equals(s)?Optional.of(t):this.getO(x);
  }
  default Gamma capture(Program p, String x, T t, Mdf mMdf) {
    Gamma g = xi->this.getO(xi).map(ti->xT(xi,t,ti,mMdf));
    return g.add(x,t.withMdf(mMdf.adapt(t)));
  }
  static T xT(String x, T t, T ti, Mdf mMdf){
    var self = t.mdf();
    var captured = ti.mdf();
    if (t.mdf().isIso()) { return xT(x, t.withMdf(Mdf.mut), ti, mMdf); }
    //TODO: ignoring the NoMutHyg thing for now
    if (self.isMut() && captured.isHyg()
        || self.isMut() && captured.isIso() && !mMdf.is(Mdf.read, Mdf.imm)
        || self.isImm() && captured.isLikeMut()) {
          throw Fail.badCapture(x, ti, t, mMdf);
        }
    return self.restrict(mMdf).map(mdfi->mdfi.adapt(captured))
      .map(ti::withMdf)
      .orElseThrow(()->Fail.badCapture(x, ti, t, mMdf));
  }
}
