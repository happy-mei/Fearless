package codegen.mir2;

import ast.E;
import ast.Program;
import ast.T;
import id.Id;
import id.Mdf;
import program.CM;
import program.typesystem.EMethTypeSystem;
import program.typesystem.XBs;
import utils.Bug;
import utils.Mapper;
import utils.Streams;
import visitors.CollectorVisitor;
import visitors.CtxVisitor;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MIRInjectionVisitor implements CtxVisitor<MIRInjectionVisitor.Ctx, MIR.E> {
  private Program p;
  private final IdentityHashMap<E.MCall, EMethTypeSystem.TsT> resolvedCalls;
//  private final List<MIR.TypeDef> inlineDefs = new ArrayList<>();
  private final List<MIR.CreateObj> objKs = new ArrayList<>();

  public MIRInjectionVisitor(Program p, IdentityHashMap<E.MCall, EMethTypeSystem.TsT> resolvedCalls) {
    this.p = p;
    this.resolvedCalls = resolvedCalls;
  }

  public MIR.Program visitProgram() {
    Map<Id.DecId, MIR.TypeDef> defs = Mapper.of(res->Stream.concat(p.ds().values().stream(), p.inlineDs().values().stream())
      .map(d->visitTopDec(d.name().pkg(), d))
      .forEach(typeDef->res.put(typeDef.name(), typeDef)));

    var literals = new IdentityHashMap<MIR.CreateObj, MIR.ObjLit>();
    for (var objK : objKs) {
      var def = defs.get(objK.def());
      var allMeths = new HashMap<Id.MethName, MIR.Meth>(def.meths().size() + objK.localMeths().size());
      def.meths().forEach(m->allMeths.put(m.name(), m));
      objK.localMeths().forEach(m->allMeths.put(m.name(), m));
      var ms = allMeths.values().stream()
        .map(m->{
          if (!m.isAbs()) { return m; }
          return m.withUnreachable();
        })
        .toList();

      var uniqueName = Id.GX.fresh().name()+"$Impl$"+def.name().shortName()+"$"+def.name().gen()+"$"+objK.mdf();

      // TODO: canSingleton
      var lit = new MIR.ObjLit(uniqueName, objK.selfName(), def, ms, objK.methCaptures(), false);
      literals.put(objK, lit);
    }

    var pkgs = defs.values().stream()
      .collect(Collectors.groupingBy(t->t.name().pkg()))
      .entrySet().stream()
      .map(kv->new MIR.Package(kv.getKey(), Mapper.of(res->kv.getValue().forEach(def->res.put(def.name(), def)))))
      .toList();

    return new MIR.Program(p.shallowClone(), pkgs, literals);
  }

  public MIR.TypeDef visitTopDec(String pkg, T.Dec dec) {
    var rawMs = p.meths(XBs.empty().addBounds(dec.gxs(), dec.bounds()), Mdf.recMdf, dec.toIT(), 0);

    var canSingleton = rawMs.stream().noneMatch(CM::isAbs);
    if (canSingleton) {
      var capturesVisitor = new CaptureCollector();
      capturesVisitor.visitLambda(dec.lambda());
      canSingleton = capturesVisitor.res().isEmpty();
    }

    var selfX = new MIR.X(dec.lambda().selfName(), new T(Mdf.mdf, dec.toIT()), Optional.empty());
    var xXs = new HashMap<String, MIR.X>();
    xXs.put(dec.lambda().selfName(), selfX);
    var selfCtx = new Ctx(xXs);

    var ms = rawMs.stream()
        .map(cm->{
          var isLocal = cm.c().equals(dec.toIT());
          var ctx = selfCtx;
          if (!isLocal) {
            // if this method is inherited, the self-name will always be "this", so we need to map that here.
            var remoteXXs = new HashMap<>(xXs);
            remoteXXs.put("this", selfX);
            ctx = new Ctx(remoteXXs);
          }
          final var finalCtx = ctx;

          var m = ((CM.CoreCM)cm).m();
          try {
            return visitMeth(pkg, m, finalCtx);
          } catch (codegen.MIRInjectionVisitor.NotInGammaException e) {
            // if a capture failed, this method is not relevant at the top level anyway, skip it
            return visitMeth(pkg, m.withBody(Optional.empty()), finalCtx).withUnreachable();
          }
        })
      .toList();

    var singletonInstance = visitLambda(pkg, dec.lambda(), new Ctx());

    return new MIR.TypeDef(
      dec.name(),
      dec.gxs(),
      dec.lambda().its(),
      ms,
      canSingleton ? Optional.of(singletonInstance) : Optional.empty()
    );
  }

  public MIR.Meth visitMeth(String pkg, E.Meth m, Ctx ctx) {
    var g = new HashMap<>(ctx.xXs);
    List<MIR.X> xs = Streams.zip(m.xs(), m.sig().ts())
      .map((x,t)->{
        if (x.equals("_")) { x = astFull.E.X.freshName(); }
        var fullX = new MIR.X(x, t, Optional.empty());
        g.put(x, fullX);
        return fullX;
      })
      .toList();

    return new MIR.Meth(
      m.name(),
      m.sig().mdf(),
      m.sig().gens(),
      xs,
      m.sig().ret(),
      m.body().map(e->e.accept(this, pkg, new Ctx(g)))
    );
  }

  @Override public MIR.MCall visitMCall(String pkg, E.MCall e, Ctx ctx) {
    var recv = e.receiver().accept(this, pkg, ctx);
    var tst = this.resolvedCalls.get(e);

    return new MIR.MCall(
      recv,
      e.name().withMdf(Optional.of(tst.original().mdf())),
      e.es().stream().map(ei->ei.accept(this, pkg, ctx)).toList(),
      tst.t(),
      tst.original().mdf(),
      EnumSet.of(MIR.MCall.CallVariant.Standard) // TODO
    );
  }

  @Override public MIR.X visitX(E.X e, Ctx ctx) {
    return visitX(e.name(), ctx);
  }
  public MIR.X visitX(String x, Ctx ctx) {
    var fullX = ctx.xXs.get(x);
    if (fullX == null) { throw new codegen.MIRInjectionVisitor.NotInGammaException(x); }
    return fullX;
  }

  @Override public MIR.CreateObj visitLambda(String pkg, E.Lambda e, Ctx ctx) {
    var selfX = new MIR.X(e.selfName(), new T(e.mdf(), e.name().toIT()), Optional.empty());
    var xXs = new HashMap<>(ctx.xXs);
    xXs.put(e.selfName(), selfX);
    var selfCtx = new Ctx(xXs);

    var allCaptures = new HashMap<MIR.Capturer, List<MIR.X>>(e.meths().size());
    var localMs = e.meths().stream()
      .filter(m->!m.isAbs())
      .map(m->{
        var captureVisitor = new CaptureCollector();
        captureVisitor.visitMeth(m);
        var res = captureVisitor.res();
        var mXXs = new HashMap<>(xXs);
        var capturer = new MIR.Capturer(e.name().id(), m.sig().mdf(), m.name());
        try {
          var captures = res.stream()
            .filter(x->!x.equals(e.selfName()))
            .map(x->visitX(x, selfCtx))
            .map(x->x.withCapturer(Optional.of(capturer)))
            .peek(x->mXXs.put(x.name(), x))
            .toList();
          allCaptures.put(capturer, captures);
        } catch (codegen.MIRInjectionVisitor.NotInGammaException err) {
          return visitMeth(pkg, m.withBody(Optional.empty()), new Ctx(mXXs)).withUnreachable();
        }

        return visitMeth(pkg, m, new Ctx(mXXs));
      })
      .toList();

    var canSingleton = localMs.isEmpty();
    var res = new MIR.CreateObj(e.mdf(), e.selfName(), e.name().id(), localMs, allCaptures, canSingleton);
    objKs.add(res);
    return res;
  }

  public record Ctx(Map<String, MIR.X> xXs) {
    public Ctx() { this(Map.of()); }
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
}
