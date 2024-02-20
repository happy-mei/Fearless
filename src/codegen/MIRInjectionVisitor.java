package codegen;

import ast.E;
import ast.Program;
import ast.T;
import id.Id;
import id.Mdf;
import magic.Magic;
import program.CM;
import program.typesystem.EMethTypeSystem;
import program.typesystem.XBs;
import utils.Mapper;
import utils.Push;
import utils.Streams;
import visitors.CollectorVisitor;
import visitors.CtxVisitor;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static program.Program.filterByMdf;

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
    var pkgs = p.ds().values().stream()
      .collect(Collectors.groupingBy(t->t.name().pkg()))
      .entrySet().stream()
      .map(kv->visitPackage(kv.getKey(), kv.getValue()))
      .toList();

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
    return new MIR.Package(pkg, Mapper.of(defs->allTDefs.forEach(def->defs.put(def.name(), def))), Collections.unmodifiableList(allFuns));
  }

  public TopLevelRes visitTopDec(String pkg, T.Dec dec) {
    var res = this.visitLambda(pkg, dec.lambda(), Ctx.EMPTY);
    return new TopLevelRes(res.defs(), res.funs());
  }

  /*
  #Define constr[R L]^G = MK
  L = D[Xs]:D1[_]...Dn[_]{'x M1...Mk}
  Mmeths = {method[DM] |  DM in meths(D[Xs]) so that callable(R,DM.M) }
  MK = new {x:R D [captures(L, G)] Mmeths} //note: FV(L) does not include the x
   */
//  public MIR.CreateObj constr(String pkg, E.Lambda e, Ctx ctx) {
//    var ms =  p.meths(XBs.empty(), Mdf.recMdf, e, 0).stream()
//      .filter(cm->filterByMdf(e.mdf(), cm.mdf()))
//      .map(cm->visitMeth(cm, cm.sig(), ))
//      .toList();
//  }

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
    var captures = captures(cm.m(), ctx);

    var mCtx = new Ctx(Mapper.of(xXs->{
      xXs.putAll(ctx.xXs());
      sig.xs().forEach(x->xXs.put(x.name(), x));
      captures.forEach(x->xXs.put(x.name(), x));
    }));

    var rawBody = cm.m().body().orElseThrow();
    var bodyRes = rawBody.accept(this, pkg, mCtx);
    var fun = new MIR.Fun(new MIR.FName(cm), Stream.concat(sig.xs().stream(), captures.stream()).toList(), sig.rt(), bodyRes.e());
    return new TopLevelRes(bodyRes.defs(), Push.of(bodyRes.funs(), fun));
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

    var allCaptures = captures(e, ctx);
    allCaptures.forEach(x->xXs.put(x.name(), x));

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

        var captures = captures(((CM.CoreCM)cm).m(), relativeCtx);
        return this.visitMeth((CM.CoreCM)cm, sig, captures);
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

  private SortedSet<MIR.X> captures(E.Lambda e, Ctx ctx) {
    var fv = new FreeVariables();
    fv.visitLambda(e);
    return Collections.unmodifiableSortedSet(fv.res().stream().
      map(x->visitX(x, ctx))
      .collect(Collectors.toCollection(MIR::createCapturesSet)
      ));
  }
  private SortedSet<MIR.X> captures(E.Meth m, Ctx ctx) {
    var fv = new FreeVariables();
    fv.visitMeth(m);
    return Collections.unmodifiableSortedSet(fv.res().stream().
      map(x->visitX(x, ctx))
      .collect(Collectors.toCollection(MIR::createCapturesSet)
      ));
  }

  private static class FreeVariables implements CollectorVisitor<Set<String>> {
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
