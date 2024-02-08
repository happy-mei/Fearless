package codegen.mir2;

import ast.T;
import id.Id;
import id.Mdf;
import utils.Bug;
import visitors.MIRVisitor2;

import java.util.*;

public sealed interface MIR {

  sealed interface E extends MIR {
    T t();
    <R> R accept(MIRVisitor2<R> v, boolean checkMagic);
  }

  record Program(ast.Program p, List<MIR.Package> pkgs, IdentityHashMap<CreateObj, ObjLit> literals) implements MIR {}
  record Package(String name, Map<Id.DecId, TypeDef> defs) implements MIR {}
  record TypeDef(Id.DecId name, List<Id.GX<T>> gens, List<Id.IT<T>> its, List<MIR.Meth> meths, Optional<CreateObj> singletonInstance) implements MIR {}
  record ObjLit(String uniqueName, String selfName, TypeDef def, List<MIR.Meth> allMeths, List<MIR.X> captures, boolean canSingleton) implements MIR {
    public ObjLit {
      assert def.meths().size() == allMeths.size();
      assert allMeths.stream().noneMatch(Meth::isAbs);
    }
  }
  record CreateObj(Mdf mdf, String selfName, Id.DecId def, List<MIR.Meth> localMeths, List<MIR.X> captures, boolean canSingleton) implements E {
    public CreateObj {
      assert localMeths.stream().noneMatch(Meth::isAbs);
    }

    @Override public T t() {
      throw Bug.todo();
    }

    @Override public <R> R accept(MIRVisitor2<R> v, boolean checkMagic) {
      return v.visitCreateObj(this, checkMagic);
    }
  }
  record Meth(Id.MethName name, Mdf mdf, List<Id.GX<T>> gens, List<MIR.X> xs, T rt, Optional<E> body) implements MIR {
    public boolean isAbs() { return body.isEmpty(); }
    public Meth withUnreachable() {
      return new Meth(name, mdf, gens, xs, rt, Optional.of(new MIR.Unreachable(rt)));
    }
  }
  record Capturer(Id.DecId id, Mdf methMdf, Id.MethName name) {}
  record X(String name, T t, Optional<Capturer> capturer) implements E {
    public X withCapturer(Optional<Capturer> capturer) {
      return new X(name, t, capturer);
    }

    @Override public <R> R accept(MIRVisitor2<R> v, boolean checkMagic) {
      return v.visitX(this, checkMagic);
    }
  }
  record MCall(E recv, Id.MethName name, List<E> args, T t, Mdf mdf, EnumSet<MCall.CallVariant> variant) implements E {
    @Override public <R> R accept(MIRVisitor2<R> v, boolean checkMagic) {
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
  record Unreachable(T t) implements E {
    @Override public <R> R accept(MIRVisitor2<R> v, boolean checkMagic) {
      return v.visitUnreachable(this);
    }
  }
}
