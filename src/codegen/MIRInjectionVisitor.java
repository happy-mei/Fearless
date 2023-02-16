package codegen;

import ast.E;
import ast.Program;
import ast.T;
import id.Id;
import id.Mdf;
import magic.Magic;
import utils.Bug;
import utils.Streams;
import visitors.CollectorVisitor;
import visitors.GammaVisitor;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

// TODO: Change this to keep names as-is, leave that all to the JavaCodegen visitor (etc.)

public class MIRInjectionVisitor implements GammaVisitor<MIR> {
  private final List<MIR.Trait> freshTraits = new ArrayList<>();
  private final Program p;
  public MIRInjectionVisitor(Program p) { this.p = p; }

  public MIR.Program visitProgram() {
    var traits = p.ds().values().stream().map(d->visitDec(d.name().pkg(), d)).toList();
    Map<String, List<MIR.Trait>> ds = Stream.concat(
        traits.stream(),
        freshTraits.stream()
      ).collect(Collectors.groupingBy(t->t.name().split("\\..+$")[0]));
    return new MIR.Program(ds);
  }
  public MIR.Trait visitDec(String pkg, T.Dec dec) {
    List<String> gens = dec.gxs().stream().map(MIRInjectionVisitor::getName).toList();
    var ms = p.meths(dec.toIT(), 0).stream()
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
    return new MIR.Trait(
      getName(dec.name()),
      gens,
      getImplsNames(impls),
      ms
    );
  }

  public MIR.MCall visitMCall(String pkg, E.MCall e, Map<String, T> gamma) {
    return new MIR.MCall(
      e.receiver().accept(this, pkg, gamma),
      getName(e.name()),
      e.es().stream().map(ei->ei.accept(this, pkg, gamma)).toList()
    );
  }

  public MIR.X visitX(E.X e, Map<String, T> gamma) { return visitX(e.name(), gamma); }
  public MIR.X visitX(String x, Map<String, T> gamma) {
    var type = requireNonNull(gamma.get(x));
    return new MIR.X(type.mdf(), x, getName(type));
  }

  public MIR visitLambda(String pkg, E.Lambda e, Map<String, T> gamma) {
    if (Magic.resolve(e.its().get(0).name().name()).isPresent()) {
      return visitMagic(pkg, e, gamma);
    }

    var captureCollector = new CaptureCollector();
    captureCollector.visitLambda(e);
    List<MIR.X> captures = captureCollector.res().stream().map(x->visitX(x, gamma)).toList();

    var impls = simplifyImpls(e.its());
    if (impls.size() == 1) {
      var it = impls.get(0);
      var g = new HashMap<>(gamma);
      g.put(e.selfName(), new T(e.mdf(), it));
      List<MIR.Meth> ms = e.meths().stream().map(m->visitMeth(pkg, m, g)).toList();
      return new MIR.Lambda(
        e.mdf(),
        getName(it.name()),
        e.selfName(),
        List.of(),
        captures,
        ms
      );
    }

    var fresh = new Id.DecId(Id.GX.fresh().name(), 0);
    var freshName = pkg+"."+getName(fresh);
    var implNames = getImplsNames(impls.stream().filter(it->!it.name().equals(fresh)).toList());
    MIR.Trait freshTrait = new MIR.Trait(freshName, List.of(), implNames, List.of());
    freshTraits.add(freshTrait);

    var g = new HashMap<>(gamma);
    g.put(e.selfName(), new T(e.mdf(), new Id.IT<>(fresh, List.of())));
    List<MIR.Meth> ms = e.meths().stream().map(m->visitMeth(pkg, m, g)).toList();
    return new MIR.Lambda(
      e.mdf(),
      freshName,
      e.selfName(),
      implNames,
      captures,
      ms
    );
  }
  public MIR visitMagic(String pkg, E.Lambda e, Map<String, T> gamma) {
    var id = e.its().get(0).name();
    var name = id.name();

    if (Character.isDigit(name.charAt(0))) {
      return new MIR.Num(e.mdf(), Integer.parseInt(name, 10));
    }
    if (name.charAt(name.length()-1) == 'u' && Character.isDigit(name.charAt(0))) {
      return new MIR.UInt(e.mdf(), Integer.parseInt(name, 10));
    }

    throw Bug.unreachable();
  }

  public MIR.Meth visitMeth(String pkg, E.Meth m, Map<String, T> gamma) {
    var g = new HashMap<>(gamma);
    List<MIR.X> xs = Streams.zip(m.xs(), m.sig().ts())
      .map((x,t)->{
        g.put(x, t);
        return new MIR.X(t.mdf(), x, getName(t));
      })
      .toList();
    List<String> gens = m.sig().gens().stream().map(MIRInjectionVisitor::getName).toList();

    return new MIR.Meth(
      getName(m.name()),
      m.sig().mdf(),
      gens,
      xs,
      getName(m.sig().ret()),
      m.body().map(e->e.accept(this, pkg, g))
    );
  }

  private static String getName(T t) { return t.match(MIRInjectionVisitor::getName, MIRInjectionVisitor::getName); }
//  private static String getNameGens(T t) {
//    var base = getName(t);
//    return base + t.match(
//      gx->"",
//      it->{
//        if (it.ts().isEmpty()) { return ""; }
//        return "<"+it.ts().stream().map(MIRInjectionVisitor::getNameGens).collect(Collectors.joining(","))+">";
//      }
//    );
//  }
//

  /** Removes any redundant ITs from the list of impls for a lambda. */
  private List<Id.IT<T>> simplifyImpls(List<Id.IT<T>> its) {
    return its.stream()
      .filter(it->its.stream()
        .noneMatch(it1->it != it1 && p.isSubType(new T(Mdf.mdf, it1), new T(Mdf.mdf, it))))
      .toList();
  }

  private List<String> getImplsNames(List<Id.IT<T>> its) {
    return its.stream()
      .map(MIRInjectionVisitor::getName)
      .toList();
  }
//  private static String getNameGens(Id.IT<T> it) {
//    return getNameGens(new T(Mdf.mdf, it));
//  }
  private static String getName(Id.GX<T> gx) { return "Object"; }
  private static String getName(Id.IT<T> it) { return getName(it.name()); }
  private static String getName(Id.DecId d) { return getBase(d.name())+"_"+d.gen(); }
  private static String getName(Id.MethName m) { return getBase(m.name()); }
  private static String getBase(String name) {
    if (name.startsWith(".")) { name = name.substring(1); }
    return name.chars().mapToObj(c->{
      if (c == '.' || Character.isAlphabetic(c) || Character.isDigit(c)) { return Character.toString(c); }
      return "$"+c;
    }).collect(Collectors.joining());
  }

  private static class CaptureCollector implements CollectorVisitor<List<String>> {
    private final List<String> res = new ArrayList<>();
    private Set<String> fresh = new HashSet<>();
    public List<String> res() { return this.res; }

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
