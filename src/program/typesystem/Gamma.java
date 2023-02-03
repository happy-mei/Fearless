package program.typesystem;

import program.Program;
import ast.T;
import id.Mdf;

import java.util.Optional;

public interface Gamma{
  default T get(ast.E.X x){ return getO(x).orElseThrow(); }
  default T get(String s){ return getO(s).orElseThrow(); }
  default Optional<T> getO(ast.E.X x){ return getO(x.name()); }
  Optional<T> getO(String s);
  static Gamma empty(){ return x->Optional.empty(); }
  default Gamma add(String s, T t){ return x->x.equals(s)?Optional.of(t):this.getO(x); }
  default Gamma capture(Program p, String x, T t, Mdf mMdf) {
    Gamma g = xi->this.getO(xi).flatMap(ti->xT(p,t,ti,mMdf));
    return g.add(x,t.withMdf(mMdf.adapt(t)));
  }
  static Optional<T> xT(Program p, T t, T ti, Mdf mMdf){
    var self = t.mdf();
    var captured = ti.mdf();
    if (t.mdf().isIso()) { return xT(p, t.withMdf(Mdf.mut), ti, mMdf); }
    //TODO: ignoring the NoMutHyg thing for now
    if (self.isMut() && captured.isHyg()) { return Optional.empty(); }
    if (self.isMut() && captured.isIso() && !mMdf.is(Mdf.read, Mdf.imm)) { return Optional.empty(); }
    if (self.isImm() && captured.isLikeMut()) { return Optional.empty(); }
    return captured.restrict(mMdf).map(mdfi->mdfi.adapt(self)).map(t::withMdf);
  }
}
