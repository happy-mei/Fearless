package program.typesystem;

import ast.E;
import ast.T;
import ast.T.Dec;
import failure.CompileError;
import failure.Fail;
import failure.Res;
import id.Id;
import id.Mdf;
import program.CM;
import program.Program;
import utils.Streams;

import java.util.*;

interface ELambdaTypeSystem extends ETypeSystem{
  default Res visitLambda(E.Lambda b){
    Mdf mdf=b.mdf();
//    var parent = b.its().get(0);
//    var parentGxs = p().gxsOf(parent).stream().toList(); // TODO: why parentGXs here?
//    Id.DecId fresh = new Id.DecId(Id.GX.fresh().name(), parentGxs.size());
//    Dec d=new Dec(fresh,parentGxs,b,b.pos());
//    var gxs = b.its().stream().flatMap(it->it.ts().stream().flatMap(T::deepGXs)).distinct().toList();
    Id.DecId fresh = new Id.DecId(Id.GX.fresh().name(), 0);
    Dec d=new Dec(fresh,List.of(),b,b.pos());
    Program p0=p().withDec(d);
    var filtered=p0.meths(d.toIT(), depth()+1).stream()
      .filter(cmi->filterByMdf(mdf,cmi))
      .toList();
    var sadlyAbs=filtered.stream()
      .filter(CM::isAbs)
      .toList();
    if (!sadlyAbs.isEmpty()) {
      return Fail.unimplementedInLambda(sadlyAbs);
    }
    var sadlyExtra=b.meths().stream()
      .filter(m->filtered.stream().anyMatch(cm->cm.name().equals(m.name())))
      .toList();
//    assert sadlyExtra.isEmpty();//TODO: can we break this assertion? Yes. If we override a method.
      // TODO: figure out a better error for the case where a non-overriden method is added (or can we just allow it?)
    return withProgram(p0).bothT(d);
  }

  default Res bothT(Dec d) {
    var b = d.lambda();
    if (expectedT().map(t->t.rt() instanceof Id.GX<T>).orElse(false)) {
      throw Fail.bothTExpectedGens(expectedT().orElseThrow(), d.name()).pos(b.pos());
    }
    //var errMdf = expectedT.map(ti->!p().isSubType(ti.mdf(),b.mdf())).orElse(false);
    //after discussion, line above not needed
    var its = p().itsOf(d.toIT());
    var expectedT=expectedT().stream()
      .filter(ti->ti.match(gx->false, its::contains))
      .findFirst();
    T retT = expectedT //TOP LEVEL = declared type
      .map(t->t.withMdf(b.mdf()))
      .orElseGet(()->new T(b.mdf(), b.its().get(0)));
    T selfT = new T(b.mdf(),d.toIT());
    var selfName=b.selfName();
    var mRes=b.meths().stream().flatMap(mi->mOk(selfName, selfT, mi).stream()).toList();
    if(mRes.isEmpty()){ return retT; }
    return mRes.get(0);
  }
  default Optional<CompileError> mOk(String selfName, T selfT, E.Meth m){
    if(m.isAbs()){ return Optional.empty(); }
    var e   = m.body().orElseThrow();
    var mMdf = m.sig().mdf();

//    var selfTi = fancyRename(selfT, mMdf, Map.of());
//    var args = m.sig().ts().stream().map(ti->fancyRename(ti, mMdf, Map.of())).toList();
    var args = m.sig().ts();
//    var ret = fancyRename(m.sig().ret(), mMdf, Map.of());
    var ret = m.sig().ret();

    // todo: assert empty gamma for MDF mdf
    var g0  = g().capture(p(), selfName, selfT, mMdf);
    var gg  = Streams.zip(m.xs(), args).fold(Gamma::add, g0);

    return flex(gg, m, e, ret);
  }

  default Optional<CompileError> flex(Gamma g, E.Meth m, E e, T ret) {
    var baseCase=okWithSubType(g, m, e, ret);
    var baseDestiny=baseCase.isEmpty() || !ret.mdf().is(Mdf.iso, Mdf.imm);
    if(baseDestiny){ return baseCase; }
    //res is iso or imm, thus is promotable

    var criticalFailure = okWithSubType(g, m, e, ret.withMdf(Mdf.read));
    if (criticalFailure.isPresent()) { return criticalFailure; }

    var isoPromotion = okWithSubType(x->g.getO(x)
      .map(t->{
        if (!t.mdf().isMut()) { return t; }
        return t.withMdf(Mdf.lent); }),
      m, e, ret.withMdf(Mdf.mut));
    if(ret.mdf().isIso() || isoPromotion.isEmpty()){ return isoPromotion; }

    return okWithSubType(x->g
      .getO(x)
      .flatMap(t->{
        if (t.mdf().isLikeMut()) { return Optional.empty(); }
        return Optional.of(t); }),
      m, e, ret.withMdf(Mdf.read))
      .flatMap(ignored->baseCase);
    //TODO: merge errors? this may say error lambda can not capture mut instead of mut is not imm
  }

  default Optional<CompileError> okWithSubType(Gamma g, E.Meth m, E e, T expected) {
    var res = e.accept(ETypeSystem.of(p(), g, Optional.of(expected), depth()+1));
    var subOk = res.t()
      .flatMap(ti->p().isSubType(ti, expected)
        ? Optional.empty()
        : Optional.of(Fail.methTypeError(expected, ti, m.name()).pos(m.pos()))
      );
    return res.err().or(()->subOk);
  }

  default boolean filterByMdf(Mdf mdf, CM m) {
    if (mdf.is(Mdf.iso, Mdf.mut, Mdf.lent)) { return true; }
    return mdf.is(Mdf.imm, Mdf.read) && m.mdf().is(Mdf.imm, Mdf.read);
  }
}
