package codegen;

import ast.E;
import ast.Program;
import ast.T;
import id.Id;
import id.Mdf;
import magic.MagicImpls;
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
    var meaningfulInlineDecs = p.inlineDs().values().stream()
      .filter(d->getTransparentSource(d).isEmpty());
    Map<Id.DecId, MIR.TypeDef> defs = Mapper.of(res->Stream.concat(p.ds().values().stream(), meaningfulInlineDecs)
      .map(d->visitTopDec(d.name().pkg(), d))
      .forEach(typeDef->res.put(typeDef.name(), typeDef)));

    var literals = new IdentityHashMap<MIR.CreateObj, MIR.ObjLit>();
    for (var objK : objKs) {
      var def = defs.get(objK.def());
      if (MagicImpls.isLiteral(objK.def().name())) { continue; }
      var allMeths = new HashMap<Id.MethName, MIR.Meth>(def.meths().size() + objK.localMeths().size());
      def.meths().forEach(m->allMeths.put(m.name(), m));
      objK.localMeths().forEach(m->allMeths.put(m.name(), m));
      var ms = allMeths.values().stream()
        .map(m->{
          if (!m.isAbs()) { return m; }
          return m.withUnreachable();
        })
        .toList();

      var uniqueName = Id.GX.fresh().name()+"$Impl$"+def.name().shortName()+"$"+def.name().gen()+"$"+objK.t().mdf();

      // TODO: canSingleton
      var lit = new MIR.ObjLit(uniqueName, objK.selfName(), def, ms, objK.captures(), false);
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

    var selfX = new MIR.X(dec.lambda().selfName(), new T(Mdf.mdf, dec.toIT()));
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
            var remoteX = new MIR.X(dec.lambda().selfName(), new T(Mdf.mdf, cm.c()));
            remoteXXs.put("this", remoteX);
            ctx = new Ctx(remoteXXs);
          }
          final var finalCtx = ctx;

          var m = ((CM.CoreCM)cm).m();
          try {
            return visitMeth(pkg, m, finalCtx);
          } catch (NotInGammaException e) {
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
        var fullX = new MIR.X(x, t);
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
    if (fullX == null) {
      throw new NotInGammaException(x);
    }
    return fullX;
  }

  @Override public MIR.CreateObj visitLambda(String pkg, E.Lambda e, Ctx ctx) {
//    var literal = MagicImpls.getLiteral(p, e.name().id());
//    if (literal.isPresent()) {
//      var litDecId = new Id.DecId(literal.get(), 0);
//      var it = new Id.IT<T>(litDecId, List.of());
//      return new MIR.CreateObj(new T(e.mdf(), it), "this", litDecId, List.of(), List.of(), true);
//    }
    var transparentSource = getTransparentSource(p.of(e.name().id()));
    if (transparentSource.isPresent()) {
      var realDec = transparentSource.get();
      var res = new MIR.CreateObj(new T(e.mdf(), realDec.toIT()), realDec.lambda().selfName(), realDec.name(), List.of(), List.of(), true);
      objKs.add(res);
      return res;
    }

    var selfX = new MIR.X(e.selfName(), new T(e.mdf(), e.name().toIT()));
    var xXs = new HashMap<>(ctx.xXs);
    xXs.put(e.selfName(), selfX);

    var captureVisitor = new CaptureCollector();
    captureVisitor.visitLambda(e);
    var allCaptures = captureVisitor.res.stream()
      .map(x->{
        try {
          return Optional.of(visitX(x, ctx));
        } catch (NotInGammaException err) {
          return Optional.<MIR.X>empty();
        }
      })
      .filter(Optional::isPresent)
      .map(Optional::get)
      .filter(x->!x.name().equals(e.selfName()))
      .peek(x->xXs.put(x.name(), x))
      .toList();
    var selfCtx = new Ctx(Collections.unmodifiableMap(xXs));


    var localMs = e.meths().stream()
      .filter(m->!m.isAbs())
      .map(m->{
        try {
          return visitMeth(pkg, m, selfCtx);
        } catch (NotInGammaException err) {
          return visitMeth(pkg, m.withBody(Optional.empty()), selfCtx).withUnreachable();
        }
      })
      .toList();

    var canSingleton = localMs.isEmpty();
    var res = new MIR.CreateObj(new T(e.mdf(), e.name().toIT()), e.selfName(), e.name().id(), localMs, allCaptures, canSingleton);
    objKs.add(res);
    return res;
  }

  public record Ctx(Map<String, MIR.X> xXs) {
    public Ctx() { this(Map.of()); }
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
