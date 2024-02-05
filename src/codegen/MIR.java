package codegen;

import ast.T;
import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import id.Id;
import id.Mdf;
import visitors.MIRVisitor;

import java.util.*;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "op")
@JsonSerialize
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public interface MIR {
  <R> R accept(MIRVisitor<R> v);
  <R> R accept(MIRVisitor<R> v, boolean checkMagic);
  T t();

  record Program(Map<String, List<Trait>> pkgs) {}
  record Package(List<Trait> traits, List<Lambda> impls) {}
  record Trait(Id.DecId name, List<Id.GX<T>> gens, List<Id.IT<T>> its, List<Meth> meths, boolean canSingleton) {}

  record Meth(Id.MethName name, Mdf mdf, List<Id.GX<T>> gens, List<X> xs, T rt, Optional<MIR> body) {
    public boolean isAbs() { return body.isEmpty(); }
  }
  record Capturer(Id.DecId id, Id.MethName name) {}
  record X(String name, T t, Optional<Capturer> capturer) implements MIR  {
    public X withCapturer(Optional<Capturer> capturer) {
      return new X(name, t, capturer);
    }
    public <R> R accept(MIRVisitor<R> v) {
      return this.accept(v, true);
    }
    public <R> R accept(MIRVisitor<R> v, boolean checkMagic) {
      return v.visitX(this, checkMagic);
    }
  }
  record MCall(MIR recv, Id.MethName name, List<MIR> args, T t, Mdf mdf, EnumSet<CallVariant> variant) implements MIR {
    public enum CallVariant {
      Standard,
      PipelineParallelFlow,
      DataParallelFlow,
      SafeMutSourceFlow;

      public boolean isStandard() { return this == Standard; }
      public boolean canParallelise() { return this == PipelineParallelFlow || this == DataParallelFlow; }
    }

    public <R> R accept(MIRVisitor<R> v) {
      return this.accept(v, true);
    }
    public <R> R accept(MIRVisitor<R> v, boolean checkMagic) {
      return v.visitMCall(this, checkMagic);
    }
  }
  record Lambda(Mdf mdf, Id.DecId freshName, String selfName, List<Id.IT<T>> its, List<Meth> meths, List<List<X>> methCaptures, boolean canSingleton) implements MIR {
    public Lambda {
      assert meths.size() == methCaptures.size();
    }

    public Lambda(Mdf mdf, Id.DecId impls) {
      this(mdf, impls, astFull.E.X.freshName(), List.of(), List.of(), List.of(), true);
    }
    public <R> R accept(MIRVisitor<R> v) {
      return this.accept(v, true);
    }
    public <R> R accept(MIRVisitor<R> v, boolean checkMagic) {
      return v.visitLambda(this, checkMagic);
    }
    public T t() {
      return new T(mdf, new Id.IT<>(freshName, Collections.nCopies(freshName.gen(), new T(Mdf.mdf, new Id.GX<>("FearIgnored$")))));
    }
  }
  record Unreachable(T t) implements MIR {
    @Override public <R> R accept(MIRVisitor<R> v) {
      return v.visitUnreachable(this);
    }
    @Override public <R> R accept(MIRVisitor<R> v, boolean checkMagic) {
      return v.visitUnreachable(this);
    }
  }

  enum Op {
    X,
    MCall,
    NewLambda,
    NewDynLambda,
    NewStaticLambda,
    NewInlineLambda,
    Share,
    RefK,
    DeRef,
    RefSwap
  }
}
