package codegen;

import ast.T;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import id.Id;
import id.Mdf;
import visitors.MIRVisitor;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "op")
@JsonSerialize
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public interface MIR {
  <R> R accept(MIRVisitor<R> v);
  T t();

  record Program(Map<String, List<Trait>> pkgs) {}
  record Trait(Id.DecId name, List<Id.GX<T>> gens, List<Id.IT<T>> its, List<Meth> meths) {
    public boolean canSingleton() {
      return false; // TODO
//      return meths().values().stream().noneMatch(Meth::isAbs);
    }
  }
  record Meth(Id.MethName name, Mdf mdf, List<Id.GX<T>> gens, List<X> xs, T rt, Optional<MIR> body) {
    public boolean isAbs() { return body.isEmpty(); }
  }
  record X(String name, T t) implements MIR  {
    public <R> R accept(MIRVisitor<R> v) {
      return v.visitX(this);
    }
  }
  record MCall(MIR recv, Id.MethName name, List<MIR> args, T t) implements MIR {
    public <R> R accept(MIRVisitor<R> v) {
      return v.visitMCall(this);
    }
  }
  record Lambda(Mdf mdf, Id.DecId freshName, String selfName, List<Id.IT<T>> its, List<X> captures, List<Meth> meths) implements MIR {
    public <R> R accept(MIRVisitor<R> v) {
      return v.visitLambda(this);
    }
    public T t() {
      return new T(mdf, new Id.IT<>(freshName, List.of()));
    }
    public Lambda withITs(List<Id.IT<T>> its) {
      return new Lambda(mdf, freshName, selfName, its, captures, meths);
    }
  }
//  record Share(MIR e) implements MIR {
//    public <R> R accept(MIRVisitor<R> v) {
//      return v.visitShare(this);
//    }
//  }
//  }
//  record RefK(L out, L v) implements MIR {}
//  record DeRef(L out, L ref) implements MIR {}
//  record RefSwap(L out, L ref, L v) implements MIR {}

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
