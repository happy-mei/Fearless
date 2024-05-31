package program.typesystem;

import ast.E;
import ast.E.Sig;
import ast.T;
import ast.T.Dec;
import failure.CompileError;
import failure.Fail;
import failure.FailOr;
import files.Pos;
import id.Id;
import id.Id.GX;
import id.Mdf;
import utils.Box;
import utils.Bug;
import utils.Streams;
import visitors.ShortCircuitVisitor;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static program.Program.filterByMdf;

interface ELambdaTypeSystem extends ETypeSystem{
  default FailOr<T> visitLambda(E.Lambda b){
    Dec d= new Dec(b);
    var self= (ELambdaTypeSystem)withProgram(p().withDec(d));
    var err1= self.implMethErrors(b);
    return err1.flatMap(_ ->self.bothT(d));
  }
  private List<E.Meth> filteredByMdf(E.Lambda b){
    return b.meths().stream()
    .filter(m->filterByMdf(b.mdf(), m.mdf()))
    .toList();
  }
  private List<E.Meth> excludedByMdf(E.Lambda b){
    return b.meths().stream()
    .filter(m->!filterByMdf(b.mdf(), m.mdf()))
    .toList();
  }
  private FailOr<Void> implMethErrors(E.Lambda b){
    Mdf mdf= b.mdf();
    var validMethods = filteredByMdf(b);
    var validMethNames = validMethods.stream()
      .map(E.Meth::name).collect(Collectors.toSet());
    var hasUncallable= b.meths().stream()
      .anyMatch(m -> !validMethNames.contains(m.name()));
    if (hasUncallable) { return FailOr.err(()->
      Fail.uncallableMeths(mdf,excludedByMdf(b)).pos(b.pos()));
    }
    var sadlyAbs= validMethods.stream().filter(E.Meth::isAbs).toList();
    if (!sadlyAbs.isEmpty()) { return FailOr.err(()->
      Fail.unimplementedInLambda(sadlyAbs).pos(b.pos()));
    }
    return FailOr.ok();
  }
  private XBs addBounds(List<GX<T>> gxs,Map<Id.GX<T>, Set<Mdf>> bounds) {
    var xbs = xbs();
    for (var gx : gxs) {
      var boundsi = bounds.get(gx);
      assert boundsi!=null;
      if (boundsi.isEmpty()) { continue; }
      xbs = xbs.add(gx.name(), boundsi);
    }
    return xbs;
  }
  default FailOr<T> bothT(Dec d){
    var b = d.lambda();
    var xbs = addBounds(d.gxs(),d.bounds());
    var invalidGens = GenericBounds.validGenericLambda(p(), xbs, b);
    if (invalidGens.isPresent()) {
      return FailOr.err(()->invalidGens.get().get().pos(b.pos()));
    }
    T selfT= new T(b.mdf(), d.toIT());
    var selfName=b.selfName();
    var mRes= FailOr.fold(b.meths(),
      mi->methOkCath(xbs, selfT, selfName, mi));
    return mRes.map(_ ->selfT);
  }
  private FailOr<Void> sigOk(Sig sig,Optional<Pos> p){
    var ts= Stream.concat(sig.ts().stream(),Stream.of(sig.ret()));
    var bad= ts.map(t->GenericBounds.validGenericT(p(), xbs(), t))
      .flatMap(Optional::stream)
      .findFirst();
    return FailOr.opt(bad.map(s->()->s.get().pos(p)));
  }
  private FailOr<Void> mOk(String selfName, T selfT, E.Meth m){
    var xbs = addBounds(m.sig().gens(),m.sig().bounds());
    var withXBs = (ELambdaTypeSystem) withXBs(xbs);
    withXBs.sigOk(m.sig(),m.pos());
    if(m.isAbs()){ return FailOr.ok(); }
    return withXBs.mOkEntry(selfName, selfT, m, m.sig());
  }
  private FailOr<Void> methOkCath(
      XBs xbs, T selfT, String selfName, E.Meth mi) {
    var boundedTypeSys =(ELambdaTypeSystem) ETypeSystem.of(
        p().shallowClone(), g(), xbs,
        expectedT(), resolvedCalls(), depth());
    try { return boundedTypeSys.mOk(selfName, selfT, mi); }
    catch (CompileError err) { return err.fail(); }
    //TODO: will die soon since we avoid all the throws?
  }
  default FailOr<Void> mOkEntry(String selfName, T selfT, E.Meth m, E.Sig sig) {
    var e   = m.body().orElseThrow();
    var mMdf = m.mdf();

    var args = sig.ts();
    var ret = sig.ret();
    assert !selfT.mdf().isMdf() || g().dom().isEmpty();
    var g0  = g().captureSelf(xbs(), selfName, selfT, mMdf);
    Mdf selfTMdf = g0.get(selfName).mdf();
    var gg  = Streams.zip(m.xs(), args).fold(Gamma::add, g0);

    var baseCase = topLevelIso(gg, m, e, ret);
    var baseDestiny = baseCase.isEmpty() || ret.mdf().is(Mdf.mut, Mdf.read);
    if (baseDestiny) { return FailOr.opt(baseCase); }
    //res is iso or imm, thus is promotable

    var criticalFailure = topLevelIso(gg, m, e, ret.withMdf(Mdf.readH));
    if (criticalFailure.isPresent()) { return FailOr.opt(baseCase); }

    var readPromotion = mOkReadPromotion(selfName, selfT, m, sig);
    if (readPromotion.isEmpty() || !ret.mdf().is(Mdf.imm, Mdf.iso)) {
      return FailOr.opt(readPromotion.flatMap(ignored->baseCase));
    }

    var isoPromotion = mOkIsoPromotion(selfName, selfT, m, sig);
    if(ret.mdf().isIso() || isoPromotion.isEmpty()){ return FailOr.opt(isoPromotion); }

    return FailOr.opt(mOkImmPromotion(selfName, selfT, m, sig, selfTMdf)
      .flatMap(ignored->baseCase));
  }

  default Optional<Supplier<CompileError>> mOkReadPromotion(String selfName, T selfT, E.Meth m, E.Sig sig) {
    var readOnlyAsReadG = new Gamma() {
      @Override public Optional<T> getO(String x) {
        return g().getO(x).map(t->{
          if (t.mdf().isMdf()) { return t.withMdf(Mdf.iso); }
          return t.mdf().isReadOnly() ? t.withMdf(Mdf.read) : t;
        });
      }
      @Override public List<String> dom() { return g().dom(); }
      @Override public String toString() { return "readOnlyAsReadG"+g().toString(); }
      @Override public String toStr(){ throw Bug.unreachable(); }

    };
    var mMdf = m.mdf();
    var g0 = readOnlyAsReadG.captureSelf(xbs(), selfName, selfT, mMdf.isReadOnly() ? Mdf.read : mMdf);
    var gg  = Streams.zip(
      m.xs(),
      sig.ts().stream().map(t->t.mdf().isReadOnly() ? t.withMdf(Mdf.read) : t).toList()
    ).fold(Gamma::add, g0);
    return topLevelIso(gg, m, m.body().orElseThrow(), sig.ret());
  }

  default Optional<Supplier<CompileError>> mOkIsoPromotion(String selfName, T selfT, E.Meth m, E.Sig sig) {
    Function<T, T> mdfTransform = t->{
      if (t.mdf().isMut()) { return t.withMdf(Mdf.mutH); }
      if (t.mdf().is(Mdf.read, Mdf.readImm)) { return t.withMdf(Mdf.readH); }
      return t;
    };
    var mutAsLentG = new Gamma() {
      @Override public Optional<T> getO(String x) {
        return g().getO(x).filter(t->!t.mdf().isMdf()).map(mdfTransform);
      }
      @Override public List<String> dom() { return g().dom(); }
      @Override public String toString() { return "readOnlyAsReadG"+g().toString(); }
      @Override public String toStr(){ throw Bug.unreachable(); }
    };
    var mMdf = mdfTransform.apply(selfT.withMdf(m.mdf())).mdf();
    var g0 = mutAsLentG.captureSelf(xbs(), selfName, selfT, mMdf.isMut() ? Mdf.mutH : mMdf);
    var gg  = Streams.zip(
      m.xs(),
      sig.ts().stream().map(mdfTransform).toList()
    ).filter((x,t)->!t.mdf().isMdf()).fold(Gamma::add, g0);
    return topLevelIso(gg, m, m.body().orElseThrow(), sig.ret().withMdf(Mdf.mut));
  }

  default Optional<Supplier<CompileError>> mOkImmPromotion(String selfName, T selfT, E.Meth m, E.Sig sig, Mdf selfTMdf) {
    var noMutyG = new Gamma() {
      @Override public Optional<T> getO(String x) {
        return g().getO(x).filter(t->!(t.mdf().isLikeMut() || t.mdf().isRecMdf() || t.mdf().isMdf()));
      }
      @Override public List<String> dom() { return g().dom(); }
      @Override public String toString() { return "noMutyG"+g().toString(); }
      @Override public String toStr(){ throw Bug.unreachable(); }
    };
    var mMdf = m.mdf();
    var g0 = selfTMdf.isLikeMut() || selfTMdf.isRecMdf() ? Gamma.empty() : noMutyG.captureSelf(xbs(), selfName, selfT, mMdf);
    var gg = Streams.zip(m.xs(), sig.ts()).filter((x,t)->!t.mdf().isLikeMut() && !t.mdf().isMdf() && !t.mdf().isRecMdf()).fold(Gamma::add, g0);
    return topLevelIso(gg, m, m.body().orElseThrow(), sig.ret().withMdf(Mdf.readH));
  }

  /**
   * G1,x:iso ITX,G2;XBs |= e : T               (TopLevel-iso)
   *   where
   *   G1,x:mut ITX,G2;XBs |= e : T
   */
  default Optional<Supplier<CompileError>> topLevelIso(Gamma g, E.Meth m, E e, T expected) {
    var res = isoAwareJudgment(g, m, e, expected);
    if (res.isEmpty()) { return res; }
    var isoNames = g.dom().stream().filter(x->{
      try {
        return g.get(x).mdf().isIso();
      } catch (CompileError err) {
        // we cannot capture something it's not in our domain, so skip it
        return false;
      }
    }).toList();

    for (var name : isoNames) {
      var g_ = g.add(name, g.get(name).withMdf(Mdf.mut));
      if (isoAwareJudgment(g_, m, e, expected).isEmpty()) { return Optional.empty(); }
    }
    return res;
  }

  /** G;XBs |= e : T */
  default Optional<Supplier<CompileError>> isoAwareJudgment(Gamma g, E.Meth m, E e, T expected) {//TODO: below is terrible and needs refactoring
    var res1= okWithSubType(g, m, e, expected).asOpt();
    return res1.or(()->g.dom().stream()
      .filter(x->{
        try {
          var xT = g.get(x);
          return xT.mdf().isIso() || (xT.isMdfX() && xbs().get(xT.gxOrThrow()).contains(Mdf.iso));
        } catch (CompileError err) {
          // we cannot capture something it's not in our domain, so skip it
          return false;
        }
      })
      .map(x -> countIsoUsages(e,x))
      .filter(Optional::isPresent)
      .findFirst()
      .flatMap(opt->opt)
    );
  }

  private static Optional<Supplier<CompileError>> countIsoUsages(E e, String x) {
    var nUsages = new Box<>(0);
    var hasCapturedX = new Box<>(false);
    return e.accept(new ShortCircuitVisitor<>() {
      @Override public Optional<Supplier<CompileError>> visitLambda(E.Lambda e) {
        if (hasCapturedX.get()) {return Optional.empty();}
        return new ShortCircuitVisitor<Supplier<CompileError>>() {
          @Override public Optional<Supplier<CompileError>> visitX(E.X e) {
            if (!e.name().equals(x)) {return ShortCircuitVisitor.super.visitX(e);}
            hasCapturedX.set(true);
            if (nUsages.get() > 0) {return Optional.of(() -> Fail.multipleIsoUsage(e).pos(e.pos()));}
            return Optional.empty();
          }
        }.visitLambda(e);
      }

      @Override public Optional<Supplier<CompileError>> visitX(E.X e) {
        if (!e.name().equals(x)) {return ShortCircuitVisitor.super.visitX(e);}
        if (hasCapturedX.get()) {
          return Optional.of(() -> Fail.multipleIsoUsage(e).pos(e.pos()));
        }
        int n = nUsages.update(usages -> usages + 1);
        if (n > 1) {return Optional.of(() -> Fail.multipleIsoUsage(e).pos(e.pos()));}
        return Optional.empty();
      }
    });
  }

  private FailOr<Void> okWithSubType(Gamma g, E.Meth m, E e, T expected) {
    var methodBodyTypeSystem = ETypeSystem.of(p(), g, xbs(), List.of(expected), resolvedCalls(), depth()+1);
    FailOr<T> res = e.accept(methodBodyTypeSystem);
    return res.flatMap(t->methSubType(t,expected)).mapErr(err->()->err.get().parentPos(e.pos()));
    // We pass the expected type of the expression down because different method body promotions
    // have different expected types. We could create a new type system visitor with the updated
    // expected instead, but that feels like overkill.
  }
  private FailOr<Void> methSubType(T t, T expected){
    var resOk= p().isSubType(xbs(), t, expected);
    if (resOk){ return FailOr.ok(); }
    return FailOr.err(()->Fail.noSubTypingRelationship(t, expected));
  }
}
