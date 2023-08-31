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
  default Gamma captureSelf(Program p, String x, T t, Mdf mMdf) {
    Gamma g = xi->this.getO(xi).map(ti->xT(xi,t,ti,mMdf,p));
    Mdf selfMdf = t.mdf().restrict(mMdf).orElseThrow();
    return g.add(x,t.withMdf(selfMdf));
  }
  static T xT(String x, T t, T ti, Mdf mMdf, Program p){
    var self = t.mdf();
    var captured = ti.mdf();
    if (t.mdf().isIso()) { return xT(x, t.withMdf(Mdf.mut), ti, mMdf, p); }
    var isoToImm = captured.isIso();
    if (isoToImm){ return xT(x, t, ti.withMdf(Mdf.imm), mMdf, p); }

//    var isMdfInMutHyg = captured.is(Mdf.mdf, Mdf.recMdf) && self.isMut() && mMdf.isHyg() && ti.isGX();
//    var isMdfInMutHyg = captured.is(Mdf.mdf, Mdf.recMdf) && self.isMut() && ti.isGX();
//    var isNoMutHygCapture = isMdfInMutHyg && t.match(gx->false, it->p.getNoMutHygs(it).anyMatch(t_->t_.equals(ti)));
//    if (isNoMutHygCapture) {
//      return xT(x, t.withMdf(Mdf.lent), ti, mMdf, p);
//    }

    // TODO: X cases with bounds
    var mutCapturesHyg = self.isMut() && captured.is(Mdf.read, Mdf.lent, Mdf.recMdf);

    var immCapturesMuty = self.isImm() && captured.is(Mdf.read, Mdf.lent, Mdf.mut, Mdf.recMdf);

    // TODO: recmdf cases
    var recMdfCapturesMuty = self.isRecMdf() && captured.isLikeMut();

    if (mutCapturesHyg || immCapturesMuty || recMdfCapturesMuty) {
          throw Fail.badCapture(x, ti, t, mMdf);
    }
    return self.restrict(mMdf).map(mdfi->{
      assert !(mdfi.isRecMdf() && captured.isMdf() && !ti.isGX());
      return mdfi.adapt(captured);
      })
      .map(ti::withMdf)
      .orElseThrow(()->Fail.badCapture(x, ti, t, mMdf));
  }
}
