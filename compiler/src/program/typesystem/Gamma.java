package program.typesystem;

import ast.T;
import failure.CompileError;
import failure.Fail;
import id.Mdf;
import program.Program;
import utils.Bug;
import utils.Push;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static id.Mdf.*;

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
  String toStr();
  Optional<T> getO(String s);
  List<String> dom();
  static Gamma empty(){ return new Gamma() {
    @Override public Optional<T> getO(String x) {
      return Optional.empty();
    }
    @Override public List<String> dom() {
      return List.of();
    }
    @Override public String toString(){ return "Gamma[]"; }
    @Override public String toStr(){ return ""; }
  }; }
  default Gamma add(String s, T t) {
    var outer = this;
    return new Gamma() {
      @Override public Optional<T> getO(String x) {
        return x.equals(s)?Optional.of(t):outer.getO(x);
      }
      @Override public List<String> dom() {
        return Push.of(outer.dom(), s);
      }
      @Override public String toString() { return "Gamma["+toStr()+"]"; }
      @Override public String toStr(){ return s+":"+t+" "+outer.toStr(); }
    };
  }
  default Gamma ctxAwareGamma(Program p, XBs xbs, T t, Mdf mMdf) {
    var outer = this;
    return new Gamma() {
      @Override public Optional<T> getO(String xi) {
        return outer.getO(xi)
          .map(ti->xT(p, xi, xbs, t.mdf(), ti, mMdf));
      }
      @Override public List<String> dom() {
        return outer.dom();
      }
      @Override public String toString() { throw Bug.unreachable(); }
      @Override public String toStr(){ return "; with xbs:"+xbs; }
    };
  }
  static T xT(Program p, String x, XBs xbs, Mdf self, T captured, Mdf mMdf){
    assert !self.isReadImm() && !mMdf.is(mdf,readImm,iso,mutH,readH) && !self.is(mutH,readH,recMdf,mdf);
    // (x : T ) [∆, RC 0, RC] = ∅ where discard(T, ∆, RC 0 )
    if (discard(p, captured, xbs, self)) {
      throw Fail.badCapture(x, captured, self, mMdf);
    }

    // (x : T ) [∆, RC 0, RC] = T [imm] where ∆ ⊢ T : iso, imm
    if (captured.accept(new KindingJudgement(p,"-", xbs, Set.of(iso,imm), true)).isRes()) {
      return captured.withMdf(imm);
    }
    // (x : T ) [∆, MutRead, imm] = T [imm] where ∆ ⊢ T : iso, imm, mut, read
    if (self.is(mut,read) && mMdf.isImm() && captured.accept(new KindingJudgement(p, "-", xbs, Set.of(iso,imm,mut,read), true)).isRes()) {
      return captured.withMdf(imm);
    }

    // (x : T ) [∆, RC 0, RC] = x : (T [∆, RC 0, RC]) otherwise:
    // T [∆, MutRead, read] = T [read] where T of form {mut _, read _}
    if (self.is(mut,read) && mMdf.is(read) && captured.mdf().is(mut, read)) {
      return captured.withMdf(read);
    }
    // T [∆, MutRead, read] = read/imm X where T of form {X, read/imm X }
    if (self.is(mut,read) && mMdf.is(read) && captured.isGX() && captured.mdf().is(mdf, readImm)) {
      return captured.withMdf(readImm);
    }
    // T [∆, mut, mut] = T where ∆ ⊢ T : imm, mut, read
    if (self.isMut() && mMdf.isMut() && captured.accept(new KindingJudgement(p, "-", xbs, Set.of(imm,mut,read), true)).isRes()) {
      return captured;
    }
    // T [∆, mut, mut] = T [read] where not ∆ ⊢ T : imm, mut, read
    if (self.isMut() && mMdf.isMut() && !captured.accept(new KindingJudgement(p, "-", xbs, Set.of(imm,mut,read), true)).isRes()) {
      return captured.withMdf(read);
    }

    throw Fail.badCapture(x, captured, self, mMdf);
  }
  static boolean discard(Program p, T t, XBs xbs, Mdf self) {
    // discard(T, ∆, IsoImm) where not ∆ ⊢ T : iso, imm
    if (self.is(iso,imm)) {
      return !t.accept(new KindingJudgement(p, "-", xbs, Set.of(iso,imm), true)).isRes();
    }
    // discard(T, ∆, _) where not ∆ ⊢ T : iso, imm, mut, read
    return !t.accept(new KindingJudgement(p, "-", xbs, Set.of(iso,imm,mut,read), true)).isRes();
  }
}
