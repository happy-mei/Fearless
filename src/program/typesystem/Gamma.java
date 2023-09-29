package program.typesystem;

import ast.T;
import failure.CompileError;
import failure.Fail;
import id.Mdf;
import utils.Push;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static id.Mdf.*;
import static java.util.Set.of;

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
  List<String> dom();
  static Gamma empty(){ return new Gamma() {
    @Override public Optional<T> getO(String x) {
      return Optional.empty();
    }
    @Override public List<String> dom() {
      return List.of();
    }
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
    };
  }
  default Gamma captureSelf(XBs xbs, String x, T t, Mdf mMdf) {
    var outer = this;
    var g = new Gamma() {
      @Override public Optional<T> getO(String xi) {
        return outer.getO(xi).map(ti->xT(xi,xbs,t.mdf(),ti,mMdf));
      }
      @Override public List<String> dom() {
        return outer.dom();
      }
    };
    Mdf selfMdf = t.mdf().restrict(mMdf).orElseThrow();
    return g.add(x,t.withMdf(selfMdf));
  }
  static T xT(String x, XBs xbs, Mdf self, T captured, Mdf mMdf){
    var bounds = captured.isMdfX() ? xbs.get(captured.gxOrThrow()) : null;
    assert !captured.isMdfX() || Objects.nonNull(bounds);
    if (captured.isMdfX() && of(imm, iso).containsAll(bounds)) { return captured.withMdf(imm); }
    if (captured.mdf().is(imm, iso)) { return captured.withMdf(imm); }
    if (mMdf.isIso()) { return xT(x, xbs, self, captured, mut); }

    if (self.isMut()) {
      if (captured.isMdfX()) {
        if (mMdf.isMut() && of(imm, mut, read).containsAll(bounds)) { return captured; }
        if (mMdf.isImm() && of(imm, mut, read, iso).containsAll(bounds)) { return captured.withMdf(imm); }
        if (mMdf.isRead() && of(imm, mut, read, iso).containsAll(bounds)) { return captured.withMdf(read); }
        if (mMdf.isLent() && of(imm, mut, read).containsAll(bounds)) { return captured.withMdf(lent); }
        if (mMdf.isLent() && of(imm, mut, read, iso).containsAll(bounds)) { return captured.withMdf(readOnly); }
        if (mMdf.isReadOnly() && of(imm, mut, read, iso).containsAll(bounds)) { return captured.withMdf(readOnly); }
        if (mMdf.isRecMdf() && of(imm, mut, read).containsAll(bounds)) { return captured.withMdf(recMdf); }
      }
      if (mMdf.isMut() && captured.mdf().is(mut, read)) { return captured; }
      if (mMdf.isImm() && captured.mdf().is(mut, read)) { return captured.withMdf(imm); }
      if (mMdf.isRead() && captured.mdf().is(mut, read)) { return captured.withMdf(imm); }
      if (mMdf.isLent() && captured.mdf().is(mut, read)) { return captured.mdf().isMut() ? captured.withMdf(lent) : captured; }
      if (mMdf.isReadOnly() && captured.mdf().isMut()) { return captured.withMdf(readOnly); }
      if (mMdf.isRecMdf() && captured.mdf().isMut()) { return captured.withMdf(recMdf); }
    }

    if (self.isLent()) {
      if (captured.isMdfX()) {
        if (mMdf.isMut()) { return captured.withMdf(readOnly); }
        if (mMdf.isImm()) { return captured.withMdf(imm); }
        if (mMdf.isRead()) { return captured.withMdf(readOnly); }
        if (mMdf.isLent() && of(mut,lent,iso).containsAll(bounds)) { return captured.withMdf(lent); }
        if (mMdf.isLent()) { return captured.withMdf(readOnly); }
        if (mMdf.isReadOnly()) { return captured.withMdf(readOnly); }
        if (mMdf.isRecMdf() && of(mut, imm, lent, readOnly).containsAll(bounds)) { return captured.withMdf(recMdf); }
      }
      if (mMdf.is(mut, lent) && captured.mdf().is(mut, lent)) { return captured.mdf().isMut() ? captured.withMdf(lent) : captured; }
      // TODO: here
      if (mMdf.isMut() && captured.mdf().is()) { return captured.mdf().isMut() ? captured.withMdf(lent) : captured; }
      if (mMdf.isImm() && captured.mdf().is(mut, lent, readOnly, recMdf)) { return captured.withMdf(imm); }
      if (mMdf.isLent() && captured.mdf().isReadOnly()) { return captured; }
      if (mMdf.isReadOnly() && captured.mdf().is(mut, lent, readOnly)) { return captured.withMdf(readOnly); }
      if (mMdf.isRecMdf() && captured.mdf().is(mut, lent)) { return captured.withMdf(recMdf); }
      if (captured.mdf().is(readOnly, recMdf)) { return captured.withMdf(readOnly); }
    }

    if (self.isReadOnly()) {
      if (captured.isMdfX()) {
//        var validReadCaptures = of(mut, imm, iso, lent, read);
        if (mMdf.isImm()) { return captured.withMdf(imm); }
        if (mMdf.isReadOnly()) { return captured.withMdf(readOnly); }
        if (mMdf.isRecMdf()) { return captured.withMdf(recMdf); }
      }
      if (mMdf.isImm() && captured.mdf().is(mut, lent, readOnly, recMdf)) { return captured.withMdf(imm); }
      if (mMdf.isReadOnly() && captured.mdf().is(mut, lent, readOnly, recMdf)) { return captured.withMdf(readOnly); }
      if (mMdf.isRecMdf()) { return captured.withMdf(recMdf); }
    }

    if (self.isRecMdf()) {
      if (captured.isMdfX()) {
        if (mMdf.isMut() && of(imm, mut, iso).containsAll(bounds)) { return captured; }
      }
//      if (mMdf.isImm() && captured.mdf().is(mut, lent, read, recMdf)) { return captured.withMdf(imm); }
      if (mMdf.isMut() && captured.mdf().isMut()) { return captured; }
      if (mMdf.isRecMdf() && captured.mdf().isRecMdf()) { return captured; }
//      if (captured.isMdfX() && of(mut, imm, lent, read).containsAll(bounds)) { return captured.withMdf(recMdf); }
//      if (mMdf.isRecMdf() && captured.mdf().isMdf()) { return captured; }
    }

//    if (self.isIso()) {
//      if (captured.isMdfX()) {
//        if (of(imm, iso).containsAll(bounds)) { return captured.withMdf(imm); }
//      }
//      if (captured.mdf().is(imm, iso)) { return captured.withMdf(imm); }
//    }

    throw Fail.badCapture(x, captured, self, mMdf);
  }
//  static T xT(String x, T t, T ti, Mdf mMdf, Program p){
//    var self = t.mdf();
//    var captured = ti.mdf();
//    if (t.mdf().isIso()) { return xT(x, t.withMdf(mut), ti, mMdf, p); }
//    var isoToImm = captured.isIso();
//    if (isoToImm){ return xT(x, t, ti.withMdf(Mdf.imm), mMdf, p); }
//
////    var isMdfInMutHyg = captured.is(Mdf.mdf, Mdf.recMdf) && self.isMut() && mMdf.isHyg() && ti.isGX();
////    var isMdfInMutHyg = captured.is(Mdf.mdf, Mdf.recMdf) && self.isMut() && ti.isGX();
////    var isNoMutHygCapture = isMdfInMutHyg && t.match(gx->false, it->p.getNoMutHygs(it).anyMatch(t_->t_.equals(ti)));
////    if (isNoMutHygCapture) {
////      return xT(x, t.withMdf(Mdf.lent), ti, mMdf, p);
////    }
//
//    // TODO: X cases with bounds
//    var mutCapturesHyg = self.isMut() && captured.is(Mdf.read, Mdf.lent, Mdf.recMdf);
//
//    var immCapturesMuty = self.isImm() && captured.is(Mdf.read, Mdf.lent, mut, Mdf.recMdf);
//
//    // TODO: recmdf cases
//    var recMdfCapturesMuty = self.isRecMdf() && captured.isLikeMut();
//
//    if (mutCapturesHyg || immCapturesMuty || recMdfCapturesMuty) {
//          throw Fail.badCapture(x, ti, t, mMdf);
//    }
//    return self.restrict(mMdf).map(mdfi->{
//      assert !(mdfi.isRecMdf() && captured.isMdf() && !ti.isGX());
//      return mdfi.adapt(captured);
//      })
//      .map(ti::withMdf)
//      .orElseThrow(()->Fail.badCapture(x, ti, t, mMdf));
//  }
}
