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
  record Trait(Id.DecId name, List<Id.GX<T>> gens, List<Id.IT<T>> its, List<Meth> meths, boolean canSingleton) {}

  record Meth(Id.MethName name, Mdf mdf, List<Id.GX<T>> gens, List<X> xs, T rt, Optional<MIR> body) {
    @JsonIgnore
    public boolean isAbs() { return body.isEmpty(); }
  }
  record X(String name, T t) implements MIR  {
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
  record Lambda(Mdf mdf, Id.DecId freshName, String selfName, List<Id.IT<T>> its, Set<X> captures, List<Meth> meths, boolean canSingleton) implements MIR {
    public Lambda(Mdf mdf, Id.DecId impls) {
      this(
        mdf,
//        new Id.DecId(impls.pkg()+"."+Id.GX.fresh().name(), 0),
        impls,
        astFull.E.X.freshName(),
//        List.of(new Id.IT<>(impls, List.of())),
        List.of(),
        Set.of(),
        List.of(),
        true
      );
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
    public Lambda withITs(List<Id.IT<T>> its) {
      return new Lambda(mdf, freshName, selfName, its, captures, meths, canSingleton);
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
