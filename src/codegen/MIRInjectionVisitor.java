package codegen;

import ast.E;
import ast.Program;
import ast.T;
import id.Id;
import id.Mdf;
import program.CM;
import program.typesystem.EMethTypeSystem;
import program.typesystem.XBs;
import utils.Streams;
import visitors.CollectorVisitor;
import visitors.GammaVisitor;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static program.Program.filterByMdf;

public class MIRInjectionVisitor implements GammaVisitor<MIR> {
  private final List<MIR.Trait> freshTraits = new ArrayList<>();
  private Program p;
  private final IdentityHashMap<E.MCall, EMethTypeSystem.TsT> resolvedCalls;
  public MIRInjectionVisitor(Program p, IdentityHashMap<E.MCall, EMethTypeSystem.TsT> resolvedCalls) {
    this.p = p;
    this.resolvedCalls = resolvedCalls;
  }

  public Program getProgram() {
    return this.p.shallowClone();
  }

  public MIR.Program visitProgram() {
    var traits = p.ds().values().stream().map(d->visitDec(d.name().pkg(), d)).toList();

    Map<String, List<MIR.Trait>> ds = Streams.of(
      traits.stream(),
      freshTraits.stream(),
      this.collectInlineTopDecs(p)
    ).collect(Collectors.groupingBy(t->t.name().pkg()));
    return new MIR.Program(ds);
  }

  public MIR.Trait visitDec(String pkg, T.Dec dec) {
    var ms = p.meths(XBs.empty(), Mdf.recMdf, dec.toIT(), 0).stream()
      .map(cm->{
        var m = p.ds().get(cm.c().name())
          .lambda()
          .meths()
          .stream()
          .filter(mi->mi.name().equals(cm.name()))
          .findAny()
          .orElseThrow();
        return visitMeth(pkg, m, Map.of(dec.lambda().selfName(), new T(cm.mdf(), dec.toIT())));
      })
      .toList();
    var impls = simplifyImpls(dec.lambda().its().stream().filter(it->!it.name().equals(dec.name())).toList());
    var canSingleton = p.meths(XBs.empty().addBounds(dec.gxs(), dec.bounds()), Mdf.recMdf, dec.toIT(), 0).stream().noneMatch(CM::isAbs);
    return new MIR.Trait(
      dec.name(),
      dec.gxs(),
      impls,
      ms,
      canSingleton
    );
  }

  public MIR.MCall visitMCall(String pkg, E.MCall e, Map<String, T> gamma) {
    var recv = e.receiver().accept(this, pkg, gamma);
    var tst = this.resolvedCalls.get(e);

    return new MIR.MCall(
      recv,
      e.name().withMdf(Optional.of(tst.original().mdf())),
      e.es().stream().map(ei->ei.accept(this, pkg, gamma)).toList(),
      tst.t(),
      tst.original().mdf(),
      getVariants(recv, e)
    );
  }

  public MIR.X visitX(E.X e, Map<String, T> gamma) { return visitX(e.name(), gamma); }
  public MIR.X visitX(String x, Map<String, T> gamma) {
    var type = gamma.get(x);
    if (type == null) { throw new NotInGammaException(x); }
    return new MIR.X(x, type);
  }
  public static class NotInGammaException extends RuntimeException {
    public NotInGammaException(String x) { super(x); }
  }

  public MIR.Lambda visitLambda(String pkg, E.Lambda e, Map<String, T> gamma) {
    var captureCollector = new CaptureCollector();
    captureCollector.visitLambda(e);
    Set<MIR.X> captures = captureCollector.res().stream().map(x->visitX(x, gamma)).collect(Collectors.toSet());

    var impls = simplifyImpls(e.its());
    var fresh = Id.GX.fresh().name();
    var freshName = new Id.DecId(pkg+"."+fresh, 0);
    var freshDec = new T.Dec(freshName, List.of(), Map.of(), e, e.pos());
    var freshDecImplsOnly = new T.Dec(freshName, List.of(), Map.of(), new E.Lambda(
      new E.Lambda.LambdaId(freshName, List.of(), Map.of()),
      Mdf.recMdf,
      e.its(),
      e.selfName(),
      List.of(),
      e.pos()
    ), e.pos());
    var nonSelfImpls = impls.stream().filter(it->!it.name().equals(freshName)).toList();
//    var recvMdf = e.mdf().isMdf() ? Mdf.recMdf : e.mdf();
    var declP = this.p.withDec(freshDecImplsOnly);
    var declaredMeths = declP.meths(XBs.empty(), Mdf.recMdf, freshDec.toIT(), 0).stream()
      .map(CM::name)
      .collect(Collectors.toSet());

    // This check should not be needed after I refactor to take into account the mdf of the lambda as part of this optimisation.
    var recvMdf = e.mdf().isMdf() ? Mdf.recMdf : e.mdf();
    var uncallableMeths = declP.meths(XBs.empty(), Mdf.recMdf, freshDec.toIT(), 0).stream()
      .filter(cm->!filterByMdf(recvMdf, cm.mdf()))
      .map(cm->visitMeth(pkg, ((CM.CoreCM)cm).m().withBody(Optional.empty()), new HashMap<>(gamma)))
      .map(m->new MIR.Meth(m.name(), m.mdf(), m.gens(), m.xs(), m.rt(), Optional.of(new MIR.Unreachable(m.rt()))));

    var noExtraMeths = e.meths().stream().allMatch(m->declaredMeths.contains(m.name()));

    if (impls.size() == 1 && noExtraMeths) {
      var it = impls.getFirst();
      var g = new HashMap<>(gamma);
      g.put(e.selfName(), new T(e.mdf(), it));
      List<MIR.Meth> ms = Stream.concat(e.meths().stream().map(m->visitMeth(pkg, m, g)), uncallableMeths).toList();
      var canSingleton = ms.isEmpty() && p.meths(XBs.empty(), recvMdf, it, 0).stream().noneMatch(CM::isAbs);
      return new MIR.Lambda(
        e.mdf(),
        it.name(),
        e.selfName(),
        List.of(),
        captures,
        ms,
        canSingleton
      );
    }

    this.p = p.withDec(freshDec);
    var noAbsMeths = this.p.meths(XBs.empty(), recvMdf, freshDec.toIT(), 0).stream().noneMatch(CM::isAbs);
    var canSingletonTrait = noAbsMeths && noExtraMeths;

    var g = new HashMap<>(gamma);
    g.put(e.selfName(), new T(e.mdf(), new Id.IT<>(freshName.name(), List.of())));

    List<MIR.Meth> msTrait = e.meths().stream().map(m->visitMeth(pkg, m.withBody(Optional.empty()), g)).toList();
    List<MIR.Meth> ms = Stream.concat(e.meths().stream().map(m->visitMeth(pkg, m, g)), uncallableMeths).toList();
    MIR.Trait freshTrait = new MIR.Trait(freshName, List.of(), nonSelfImpls, msTrait, canSingletonTrait);
    freshTraits.add(freshTrait);

    return new MIR.Lambda(
      e.mdf(),
      freshName,
      e.selfName(),
      nonSelfImpls,
      captures,
      ms,
      canSingletonTrait && ms.isEmpty()
    );
  }

  public MIR.Meth visitMeth(String pkg, E.Meth m, Map<String, T> gamma) {
    var g = new HashMap<>(gamma);
    List<MIR.X> xs = Streams.zip(m.xs(), m.sig().ts())
      .map((x,t)->{
        if (x.equals("_")) { x = astFull.E.X.freshName(); }
        g.put(x, t);
        return new MIR.X(x, t);
      })
      .toList();

    return new MIR.Meth(
      m.name(),
      m.sig().mdf(),
      m.sig().gens(),
      xs,
      m.sig().ret(),
      m.body().map(e->e.accept(this, pkg, g))
    );
  }

  /** Removes any redundant ITs from the list of impls for a lambda. */
  private List<Id.IT<T>> simplifyImpls(List<Id.IT<T>> its) {
    return its.stream()
      .filter(it->its.stream()
        .noneMatch(it1->it != it1 && p.isSubType(XBs.empty(), new T(Mdf.mdf, it1), new T(Mdf.mdf, it))))
      .toList();
  }

  private Stream<MIR.Trait> collectInlineTopDecs(Program p) {
    return p.inlineDs().values().stream()
      .filter(d->!d.name().isFresh())
      .map(dec->new MIR.Trait(
        dec.name(),
        dec.gxs(),
        List.of(),
        dec.lambda().meths().stream().map(m->visitMeth(dec.name().pkg(), m.withBody(Optional.empty()), Map.of())).toList(),
        false
      ));
  }

  private EnumSet<MIR.MCall.CallVariant> getVariants(MIR recv, E.MCall e) {
    // The issue with basing it on the recv here is that the call to the actual flow constructor only knows
    // about "mdf E" in most cases :(
    // We basically need to make any code that calls the flow functions, magic.
    // We could have a collector which walks the AST and recursively builds up a list of all method calls that end up
    // invoking a flow constructor until it gets non-generic. Then we associate each one of those leaf method calls
    // with a call variant. Basically a BFS/DFS for relevant method calls?

//    if (recv.t().equals(new T(Mdf.imm, new Id.IT<>("base.flows.Flow", List.of())))) {
//      if (e.ts().size() == 1 && e.ts().getFirst().isIt()) {
//        System.out.println(e.ts().getFirst()+" at "+e.pos());
//      }
//    }

    // Standard library .flow methods:
    var recvT = recv.t();
    var recvIT = recvT.itOrThrow();
    if (e.name().name().equals(".flow")) {
      if (recvIT.name().equals(new Id.DecId("base.LList", 1))) {
        var flowElem = recvIT.ts().getFirst();
        if (flowElem.mdf().is(Mdf.read, Mdf.imm)) { return EnumSet.of(MIR.MCall.CallVariant.DataParallelFlow); }
        return EnumSet.of(MIR.MCall.CallVariant.Standard);
      }
      if (recvIT.name().equals(new Id.DecId("base.List", 1))) {
        var flowElem = recvIT.ts().getFirst();
        if (recvT.mdf().is(Mdf.read, Mdf.imm)) { return EnumSet.of(MIR.MCall.CallVariant.DataParallelFlow); }
        if (flowElem.mdf().is(Mdf.read, Mdf.imm)) { return EnumSet.of(MIR.MCall.CallVariant.DataParallelFlow, MIR.MCall.CallVariant.SafeMutSourceFlow); }
        return EnumSet.of(MIR.MCall.CallVariant.Standard);
      }
    }
    if (recvIT.name().equals(new Id.DecId("base.flows.Flow", 0))) {
      if (e.name().equals(new Id.MethName(Optional.of(Mdf.imm), ".fromIter", 1))) {
        var flowElem = e.ts().getFirst();
        // Cannot be safe and data-parallel because mut .next/0 on the iterator may not be called in parallel.
        // Pipeline parallelism is okay because we know .next will never be called simultaneously with itself.
        if (flowElem.mdf().is(Mdf.read, Mdf.imm)) { return EnumSet.of(MIR.MCall.CallVariant.SafeMutSourceFlow, MIR.MCall.CallVariant.PipelineParallelFlow); }
        return EnumSet.of(MIR.MCall.CallVariant.Standard);
      }
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
}
