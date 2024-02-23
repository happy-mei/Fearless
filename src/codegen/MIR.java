package codegen;

import ast.T;
import id.Id;
import id.Mdf;
import program.CM;
import utils.Bug;
import visitors.MIRVisitor;

import java.util.*;

public sealed interface MIR {
  sealed interface E extends MIR {
    MT t();
    <R> R accept(MIRVisitor<R> v, boolean checkMagic);
  }

  static TreeSet<X> createCapturesSet() {
    return new TreeSet<>(Comparator.comparing(X::name));
  }

  record Program(ast.Program p, List<Package> pkgs) implements MIR {
    public TypeDef of(Id.DecId id) {
      return pkgs.stream()
        .filter(pkg->pkg.defs.containsKey(id))
        .map(pkg->pkg.defs.get(id))
        .findFirst()
        .orElseThrow();
    }
  }
  record Package(String name, Map<Id.DecId, TypeDef> defs, List<Fun> funs) implements MIR {}
  record TypeDef(Id.DecId name, List<MT.Plain> impls, List<Sig> sigs, Optional<CreateObj> singletonInstance) implements MIR {}
  record Fun(FName name, List<X> args, MT ret, E body) implements MIR {}
  record CreateObj(MT t, String selfName, List<Meth> meths, List<Meth> unreachableMs, SortedSet<X> captures) implements E {
    public CreateObj {
      captures = Collections.unmodifiableSortedSet(captures);
    }

    public MT.Plain concreteT() {
      return switch (t) {
        case MT.Any ignored -> throw Bug.unreachable();
        case MT.Plain plain -> plain;
        case MT.Usual usual -> new MT.Plain(usual.mdf(), usual.it().name());
      };
    }

    public CreateObj(Mdf mdf, Id.DecId def) {
      this(
        new MT.Usual(new T(mdf, new Id.IT<>(def, Id.GX.standardNames(def.gen()).stream().map(gx->new T(Mdf.mdf, gx)).toList()))),
        astFull.E.X.freshName(),
        List.of(),
        List.of(),
        Collections.unmodifiableSortedSet(createCapturesSet())
      );
    }

    @Override public <R> R accept(MIRVisitor<R> v, boolean checkMagic) {
      return v.visitCreateObj(this, checkMagic);
    }
  }
  record Meth(Id.DecId origin, Sig sig, boolean capturesSelf, SortedSet<String> captures, FName fName) implements MIR {
    public Meth withSig(Sig sig) {
      return new Meth(origin, sig, capturesSelf, captures, fName);
    }
    public Meth withName(Id.MethName name) {
      return this.withSig(this.sig.withName(name));
    }
  }
  record Sig(Id.MethName name, Mdf mdf, List<X> xs, MT rt) implements MIR {
    public Sig withName(Id.MethName name) {
      return new Sig(name, mdf, xs, rt);
    }
    public Sig withRT(MT rt) {
      return new Sig(name, mdf, xs, rt);
    }
  }
  record X(String name, MT t) implements E {
//    public X withCapturer(Optional<Capturer> capturer) {
//      return new X(name, t, capturer);
//    }

    @Override public <R> R accept(MIRVisitor<R> v, boolean checkMagic) {
      return v.visitX(this, checkMagic);
    }
  }
  record MCall(E recv, Id.MethName name, List<? extends E> args, MT t, Mdf mdf, EnumSet<CallVariant> variant) implements E {
    @Override public <R> R accept(MIRVisitor<R> v, boolean checkMagic) {
      return v.visitMCall(this, checkMagic);
    }

    public enum CallVariant {
      Standard,
      PipelineParallelFlow,
      DataParallelFlow,
      SafeMutSourceFlow;

      public boolean isStandard() { return this == Standard; }
      public boolean canParallelise() { return this == PipelineParallelFlow || this == DataParallelFlow; }
    }
  }

  record FName(Id.DecId d, Id.MethName m, boolean capturesSelf, Mdf mdf) {
    public FName(CM cm, boolean capturesSelf) {
      this(cm.c().name(), cm.name(), capturesSelf, cm.mdf());
    }
  }

  sealed interface MT {
    Mdf mdf();
    Optional<Id.DecId> name();
    static MT of(T t) {
      return t.match(gx->new Any(t.mdf()), it->new Usual(t));
    }
    record Usual(T t) implements MT {
      public Usual {
        assert t.isIt();
      }
      public Mdf mdf() {
        return t.mdf();
      }
      public Id.IT<T> it() {
        return (Id.IT<T>) t.rt();
      }
      public Optional<Id.DecId> name() { return Optional.of(it().name()); }
    }
    record Plain(Mdf mdf, Id.DecId id) implements MT {
      public Optional<Id.DecId> name() { return Optional.of(id); }
    }
    record Any(Mdf mdf) implements MT {
      @Override public Optional<Id.DecId> name() {
        return Optional.empty();
      }
    }
  }
}
