package codegen;

import ast.E;
import ast.Program;
import ast.T;
import id.Id;
import id.Mdf;
import magic.Magic;
import magic.MagicImpls;
import program.CM;
import program.typesystem.EMethTypeSystem;
import program.typesystem.XBs;
import utils.Bug;
import utils.Mapper;
import utils.Push;
import utils.Streams;
import visitors.CollectorVisitor;
import visitors.CtxVisitor;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MIRInjectionVisitor implements CtxVisitor<MIRInjectionVisitor.Ctx, MIRInjectionVisitor.Res<? extends MIR.E>> {
  private Program p;
  private final IdentityHashMap<E.MCall, EMethTypeSystem.TsT> resolvedCalls;
//  private final List<MIR.TypeDef> inlineDefs = new ArrayList<>();
//  private final List<MIR.CreateObj> objKs = new ArrayList<>();

  public record Res<E extends MIR.E>(E e, List<MIR.TypeDef> defs, List<MIR.Fun> funs) {
    public TopLevelRes mergeAsTopLevel(Res<?> other) {
      return new TopLevelRes(Push.of(defs(), other.defs()), Push.of(funs(), other.funs()));
    }
  }
  public record TopLevelRes(List<MIR.TypeDef> defs, List<MIR.Fun> funs) {
    public static TopLevelRes EMPTY = new TopLevelRes(List.of(), List.of());
    public TopLevelRes merge(TopLevelRes other) {
      return new TopLevelRes(Push.of(defs(), other.defs()), Push.of(funs(), other.funs()));
    }
    public TopLevelRes mergeAsTopLevel(Res<?> other) {
      return new TopLevelRes(Push.of(defs(), other.defs()), Push.of(funs(), other.funs()));
    }
  }

  public record Ctx(Map<String, MIR.X> xXs) {
    public static Ctx EMPTY = new Ctx();
    public Ctx {
      xXs = Collections.unmodifiableMap(xXs);
    }
    private Ctx() { this(Map.of()); }
  }

  public MIRInjectionVisitor(Program p, IdentityHashMap<E.MCall, EMethTypeSystem.TsT> resolvedCalls) {
    this.p = p;
    this.resolvedCalls = resolvedCalls;
  }

  public MIR.Program visitProgram() {
    var meaningfulInlineDecs = p.inlineDs().values().stream()
      .filter(d->getTransparentSource(d).isEmpty());
//    Map<Id.DecId, MIR.TypeDef> defs = Mapper.of(res->Stream.concat(p.ds().values().stream(), meaningfulInlineDecs)
//      .map(d->visitTopDec(d.name().pkg(), d))
//      .forEach(typeDef->res.put(typeDef.name(), typeDef)));
//
    var pkgs = p.ds().values().stream()
      .collect(Collectors.groupingBy(t->t.name().pkg()))
      .entrySet().stream()
      .map(kv->visitPackage(kv.getKey(), kv.getValue()))
      .toList();
//      .map(kv->new MIR.Package(kv.getKey(), Mapper.of(res->kv.getValue().forEach(def->res.put(def.name(), def)))))
//      .toList();

    return new MIR.Program(p.shallowClone(), pkgs);
  }

  public MIR.Package visitPackage(String pkg, List<T.Dec> ds) {
    var allTDefs = new ArrayList<MIR.TypeDef>(ds.size());
    var allFuns = new ArrayList<MIR.Fun>(ds.size());
    ds.stream()
      .map(d->visitTopDec(pkg, d))
      .forEach(res->{
        allTDefs.addAll(res.defs());
        allFuns.addAll(res.funs());
      });
//    return new TopLevelRes(Collections.unmodifiableList(allTDefs), Collections.unmodifiableList(allFuns));
    return new MIR.Package(pkg, Mapper.of(defs->allTDefs.forEach(def->defs.put(def.name(), def))), Collections.unmodifiableList(allFuns));
  }

  public TopLevelRes visitTopDec(String pkg, T.Dec dec) {
    var res = this.visitLambda(pkg, dec.lambda(), Ctx.EMPTY);
    return new TopLevelRes(res.defs(), res.funs());

//    var rawMs = p.meths(XBs.empty().addBounds(dec.gxs(), dec.bounds()), Mdf.recMdf, dec.toIT(), 0);
//
//    var canSingleton = rawMs.stream().noneMatch(CM::isAbs);
//    if (canSingleton) {
//      var capturesVisitor = new CaptureCollector();
//      capturesVisitor.visitLambda(dec.lambda());
//      canSingleton = capturesVisitor.res().isEmpty();
//    }
//
//    var selfX = new MIR.X(dec.lambda().selfName(), MIR.MT.of(new T(Mdf.mdf, dec.toIT())));
//    var xXs = new HashMap<String, MIR.X>();
//    xXs.put(dec.lambda().selfName(), selfX);
//    var selfCtx = new Ctx(xXs);
//
//    var ms = rawMs.stream()
//        .map(cm->{
//          var isLocal = cm.c().equals(dec.toIT());
//          var ctx = selfCtx;
//          if (!isLocal) {
//            // if this method is inherited, the self-name will always be "this", so we need to map that here.
//            var remoteXXs = new HashMap<>(xXs);
//            var remoteX = new MIR.X(dec.lambda().selfName(), MIR.MT.of(new T(Mdf.mdf, cm.c())));
//            remoteXXs.put("this", remoteX);
//            ctx = new Ctx(remoteXXs);
//          }
//          final var finalCtx = ctx;
//
//          var m = ((CM.CoreCM)cm).m();
//          try {
//            return visitMeth(pkg, m, finalCtx);
//          } catch (NotInGammaException e) {
//            // if a capture failed, this method is not relevant at the top level anyway, skip it
//            return visitMeth(pkg, m.withBody(Optional.empty()), finalCtx).withUnreachable();
//          }
//        })
//      .toList();
//
//    var singletonInstance = visitLambda(pkg, dec.lambda(), new Ctx());
//
//    return new MIR.TypeDef(
//      dec.name(),
//      dec.gxs(),
//      dec.lambda().its(),
//      ms,
//      canSingleton ? Optional.of(singletonInstance) : Optional.empty()
//    );
  }

  public MIR.Sig visitSig(CM.CoreCM cm) {
    return new MIR.Sig(
      cm.name(),
      cm.mdf(),
      Streams.zip(cm.xs(), cm.sig().ts()).map((x,t)->{
        if (x.equals("_")) { x = astFull.E.X.freshName(); }
        return new MIR.X(x, MIR.MT.of(t));
      }).toList(),
      MIR.MT.of(cm.ret())
    );
  }

  public TopLevelRes visitMeth(String pkg, CM.CoreCM cm, Ctx ctx) {
    var sig = visitSig(cm);
    var cc = new CaptureCollector();
    cc.visitMeth(cm.m());
    var captures = cc.res().stream().map(x->visitX(x, ctx)).collect(Collectors.toCollection(MIR::createCapturesSet));

    var mCtx = new Ctx(Mapper.of(xXs->{
      xXs.putAll(ctx.xXs());
      sig.xs().forEach(x->xXs.put(x.name(), x));
      captures.forEach(x->xXs.put(x.name(), x));
    }));

    var rawBody = cm.m().body().orElseThrow();
    var bodyRes = rawBody.accept(this, pkg, mCtx);
    var fun = new MIR.Fun(new MIR.FName(cm), sig.xs(), captures, sig.rt(), bodyRes.e());
    return new TopLevelRes(bodyRes.defs(), Push.of(bodyRes.funs(), fun));

//    var g = new HashMap<>(ctx.xXs);
//    List<MIR.X> xs = Streams.zip(m.xs(), m.sig().ts())
//      .map((x,t)->{
//        if (x.equals("_")) { x = astFull.E.X.freshName(); }
//        var fullX = new MIR.X(x, MIR.MT.of(t));
//        g.put(x, fullX);
//        return fullX;
//      })
//      .toList();
//
//    return new MIR.Meth(
//      m.name(),
//      m.sig().mdf(),
//      xs,
//      m.sig().ret(),
//      m.body().map(e->e.accept(this, pkg, new Ctx(g)))
//    );
  }
  public MIR.Meth visitMeth(CM.CoreCM cm, MIR.Sig sig, SortedSet<MIR.X> captures) {
    return new MIR.Meth(cm.c().name(), sig, captures, new MIR.FName(cm));
  }

  @Override public Res<MIR.MCall> visitMCall(String pkg, E.MCall e, Ctx ctx) {
    var recvRes = e.receiver().accept(this, pkg, ctx);
    var tst = this.resolvedCalls.get(e);
    var args = e.es().stream().map(ei->ei.accept(this, pkg, ctx)).toList();
    var topLevel = Stream.concat(Stream.of(recvRes), args.stream())
      .reduce(TopLevelRes.EMPTY, TopLevelRes::mergeAsTopLevel, TopLevelRes::merge);

    var call = new MIR.MCall(
      recvRes.e(),
      e.name().withMdf(Optional.of(tst.original().mdf())),
      args.stream().map(Res::e).toList(),
      MIR.MT.of(tst.t()),
      tst.original().mdf(),
      getVariants(recvRes.e(), e)
    );

    return new Res<>(call, topLevel.defs(), topLevel.funs());

//    return new MIR.MCall(
//      recv,
//      e.name().withMdf(Optional.of(tst.original().mdf())),
//      e.es().stream().map(ei->ei.accept(this, pkg, ctx)).toList(),
//      tst.t(),
//      tst.original().mdf(),
//      getVariants(recv, e)
//    );
  }

  @Override public Res<MIR.X> visitX(E.X e, Ctx ctx) {
    return new Res<>(visitX(e.name(), ctx), List.of(), List.of());
  }
  public MIR.X visitX(String x, Ctx ctx) {
    var fullX = ctx.xXs.get(x);
    if (fullX == null) {
      throw new NotInGammaException(x);
    }
    return fullX;
  }

  @Override public Res<MIR.CreateObj> visitLambda(String pkg, E.Lambda e, Ctx ctx) {
    var transparentSource = getTransparentSource(p.of(e.name().id()));
    if (transparentSource.isPresent()) {
      var realDec = transparentSource.get();
      var k = new MIR.CreateObj(MIR.MT.of(new T(e.mdf(), realDec.toIT())), realDec.lambda().selfName(), List.of(), MIR.createCapturesSet(), true);
      return new Res<>(k, List.of(), List.of());
    }

    var selfT = MIR.MT.of(new T(e.mdf(), e.name().toIT()));
    var selfX = new MIR.X(e.selfName(), selfT);
    var xXs = new HashMap<>(ctx.xXs);
    xXs.put(e.selfName(), selfX);
//
    var captureVisitor = new CaptureCollector();
    captureVisitor.visitLambda(e);
    var allCaptures = captureVisitor.res.stream()
      .map(x->visitX(x, ctx))
      .peek(x->xXs.put(x.name(), x))
      .collect(Collectors.toCollection(MIR::createCapturesSet));

    var selfCtx = new Ctx(xXs);

    var dec = p.of(e.name().id());
    var recvMdf = e.mdf().isMdf() ? Mdf.recMdf : e.mdf();
    var bounds = XBs.empty().addBounds(dec.gxs(), dec.bounds());
    var rawMs = p.meths(bounds, recvMdf, e, 0);

    var sigs = rawMs.stream()
      .map(cm->this.visitSig((CM.CoreCM)cm))
      .toList();

    var allMs = Streams.zip(rawMs, sigs)
      .filter((cm,sig)->!cm.isAbs())
      .map((cm,sig)->{
        var isLocal = p.isSubType(bounds, new T(Mdf.mdf, cm.c()), new T(Mdf.mdf, dec.toIT()));
        final Ctx relativeCtx;
        if (!isLocal) {
          var remoteXXs = new HashMap<String, MIR.X>();
          remoteXXs.put(p.of(cm.c().name()).lambda().selfName(), selfCtx.xXs().get(e.selfName()));
          relativeCtx = new Ctx(remoteXXs);
        } else {
          relativeCtx = selfCtx;
        }

        var cc = new CaptureCollector();
        cc.visitMeth(((CM.CoreCM)cm).m());
        var captures = cc.res().stream().map(x->visitX(x, relativeCtx)).collect(Collectors.toCollection(MIR::createCapturesSet));
        return this.visitMeth((CM.CoreCM)cm, sig, Collections.unmodifiableSortedSet(captures));
      })
      .toList();

    var freshRes = e.meths().stream()
      .filter(m->!m.isAbs())
      .map(m->new CM.CoreCM(e.name().toIT(), m, m.sig()))
      .map(cm->this.visitMeth(pkg, cm, selfCtx))
      .reduce(TopLevelRes::merge)
      .orElse(TopLevelRes.EMPTY);

    var canSingleton = allCaptures.isEmpty() && allMs.size() == rawMs.size();
    var k = new MIR.CreateObj(selfT, e.selfName(), allMs, allCaptures, canSingleton);

    var impls = e.its().stream().map(it->new MIR.MT.Plain(Mdf.mdf, it.name())).toList();
    var typeDef = new MIR.TypeDef(e.name().id(), impls, sigs, canSingleton ? Optional.of(k) : Optional.empty());

    return new Res<>(k, Push.of(freshRes.defs(), typeDef), freshRes.funs());
//    var allCaptures = captureVisitor.res.stream()
//      .map(x->{
//        try {
//          return Optional.of(visitX(x, ctx));
//        } catch (NotInGammaException err) {
//          return Optional.<MIR.X>empty();
//        }
//      })
//      .filter(Optional::isPresent)
//      .map(Optional::get)
//      .filter(x->!x.name().equals(e.selfName()))
//      .peek(x->xXs.put(x.name(), x))
//      .collect(Collectors.toCollection(MIR::createCapturesSet));
//    var selfCtx = new Ctx(Collections.unmodifiableMap(xXs));
//
//
//    var localMs = e.meths().stream()
//      .filter(m->!m.isAbs())
//      .map(m->{
//        try {
//          return visitMeth(pkg, m, selfCtx);
//        } catch (NotInGammaException err) {
//          return visitMeth(pkg, m.withBody(Optional.empty()), selfCtx).withUnreachable();
//        }
//      })
//      .toList();
//
//    var canSingleton = localMs.isEmpty();
//    var res = new MIR.CreateObj(new T(e.mdf(), e.name().toIT()), e.selfName(), e.name().id(), localMs, allCaptures, canSingleton);
//    objKs.add(res);
//    return res;
  }

  private Optional<T.Dec> getTransparentSource(T.Dec d) {
    if (d.name().isFresh() && d.lambda().meths().isEmpty()) {
      var nonSelfImpls = d.lambda().its().stream().filter(it->!it.name().equals(d.name())).toList();
      if (nonSelfImpls.size() != 1) { return Optional.empty(); }
      var realIT = nonSelfImpls.getFirst();
      return Optional.of(p.of(realIT.name()));
    }
    return Optional.empty();
  }

  private EnumSet<MIR.MCall.CallVariant> getVariants(MIR.E recv, E.MCall e) {
    // Standard library .flow methods:
    var recvT = (MIR.MT.Usual) recv.t();
    var recvIT = recvT.it();
    if (e.name().name().equals(".flow")) {
      if (recvIT.name().equals(new Id.DecId("base.LList", 1))) {
        var flowElem = recvIT.ts().getFirst();
//        if (flowElem.mdf().is(Mdf.read, Mdf.imm)) { return EnumSet.of(MIR.MCall.CallVariant.DataParallelFlow, MIR.MCall.CallVariant.PipelineParallelFlow); }
        return EnumSet.of(MIR.MCall.CallVariant.Standard);
      }
      if (recvIT.name().equals(Magic.FList)) {
        var flowElem = recvIT.ts().getFirst();
        if (recvT.mdf().is(Mdf.read, Mdf.imm)) { return EnumSet.of(MIR.MCall.CallVariant.DataParallelFlow, MIR.MCall.CallVariant.PipelineParallelFlow); }
        if (flowElem.mdf().is(Mdf.read, Mdf.imm)) { return EnumSet.of(MIR.MCall.CallVariant.DataParallelFlow, MIR.MCall.CallVariant.PipelineParallelFlow, MIR.MCall.CallVariant.SafeMutSourceFlow); }
//        if (flowElem.mdf().is(Mdf.read, Mdf.imm)) { return EnumSet.of(MIR.MCall.CallVariant.SafeMutSourceFlow); }
        return EnumSet.of(MIR.MCall.CallVariant.Standard);
      }
    }
    if (recvIT.name().equals(Magic.FlowK) && e.name().name().equals("#")) {
      var flowElem = e.ts().getFirst();
      if (flowElem.mdf().is(Mdf.read, Mdf.imm)) { return EnumSet.of(MIR.MCall.CallVariant.DataParallelFlow, MIR.MCall.CallVariant.PipelineParallelFlow, MIR.MCall.CallVariant.SafeMutSourceFlow); }
    }

    return EnumSet.of(MIR.MCall.CallVariant.Standard);
  }

  private static class CaptureCollector implements CollectorVisitor<Set<String>> {
    private final Set<String> res = new HashSet<>();
    private Set<String> fresh = new HashSet<>();
    public Set<String> res() { return this.res; }

    public Void visitLambda(E.Lambda e) {
      var old = fresh;
      fresh = new HashSet<>(fresh);
      fresh.add(e.selfName());
      CollectorVisitor.super.visitLambda(e);
      this.fresh = old;
      return null;
    }

    public Void visitMeth(E.Meth m) {
      var old = fresh;
      fresh = new HashSet<>(fresh);
      fresh.addAll(m.xs());
      CollectorVisitor.super.visitMeth(m);
      this.fresh = old;
      return null;
    }

    public Void visitX(E.X e) {
      if (!fresh.contains(e.name())) { res.add(e.name()); }
      return CollectorVisitor.super.visitX(e);
    }
  }

  private static class NotInGammaException extends RuntimeException {
    public NotInGammaException(String x) { super(x); }
  }

}
