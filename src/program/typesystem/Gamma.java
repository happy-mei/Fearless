package program.typesystem;

import failure.CompileError;
import failure.Fail;
import program.Program;
import ast.T;
import id.Mdf;

import java.util.List;
import java.util.Map;
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
    var capturedMdf = t.mdf().isIso() ? Mdf.imm : t.mdf();
    return g.add(x,t.withMdf(mMdf.adapt(capturedMdf)));
  }
  static T xT(String x, T t, T ti, Mdf mMdf){
    var self = t.mdf();
    var captured = ti.mdf();
    if (t.mdf().isIso()) { return xT(x, t.withMdf(Mdf.mut), ti, mMdf); }
    var isoToImm = captured.isIso();
    if (isoToImm){ return xT(x, t, ti.withMdf(Mdf.imm), mMdf); }
    //TODO: ignoring the NoMutHyg thing for now
    // TODO: maybe explain _why_ the capture cannot be allowed in the error message
    var mutCapturesHyg = self.isMut() && captured.is(Mdf.read, Mdf.lent, Mdf.mdf, Mdf.recMdf);
    var immCapturesMuty = self.isImm() && (captured.isLikeMut() || captured.isRecMdf());
    var recMdfCapturesMuty = self.isRecMdf() && captured.isLikeMut();
    if (mutCapturesHyg || immCapturesMuty || recMdfCapturesMuty) {
          throw Fail.badCapture(x, ti, t, mMdf);
    }
    var fixedCaptured = self.isRecMdf() || !captured.isRecMdf()  ? captured : Mdf.read;
    return self.restrict(mMdf).map(mdfi->mdfi.adapt(fixedCaptured))
      .map(ti::withMdf)
      .orElseThrow(()->Fail.badCapture(x, ti, t, mMdf));
  }
}
