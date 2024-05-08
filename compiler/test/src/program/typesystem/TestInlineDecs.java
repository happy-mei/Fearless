package program.typesystem;

import org.junit.jupiter.api.Test;

import static program.typesystem.RunTypeSystem.fail;
import static program.typesystem.RunTypeSystem.ok;

public class TestInlineDecs {
  @Test void personFactory() { ok("""
    package test
    FPerson:{ #(name: Str, age: Nat): Person -> Person:{
      .name: Str -> name,
      .age: Nat -> age,
      }}
    Ex:{
      .create: Person -> FPerson#(Bob, TwentyFour),
      .name(p: Person): Str -> p.name,
      }
    """, """
    package test
    Str:{} Bob:Str{}
    Nat:{} TwentyFour:Nat{}
    """); }
  @Test void genericPerson() { ok("""
    package test
    Person[N]:{ .name: Str, .age: imm N }
    FPerson:{ #[N](name: Str, age: imm N): Person[N] -> {
      .name: Str -> name,
      .age: imm N -> age,
      }}
    Ex:{
      .create: Person[Nat] -> FPerson#[Nat](Bob, TwentyFour),
      .name(p: Person[Nat]): Str -> p.name,
      }
    """, """
    package test
    Str:{} Bob:Str{}
    Nat:{} TwentyFour:Nat{}
    """); }
  @Test void genericPersonInline() { ok("""
    package test
    FPerson:{ #[N](name: Str, age: imm N): Person[N] -> Person[N]:{
      .name: Str -> name,
      .age: imm N -> age,
      }}
    Ex:{
      .create: Person[Nat] -> FPerson#[Nat](Bob, TwentyFour),
      .name(p: Person[Nat]): Str -> p.name,
      }
    """, """
    package test
    Str:{} Bob:Str{}
    Nat:{} TwentyFour:Nat{}
    """); }
  @Test void inlineBoundsForwarding() { ok("""
    package test
    //Person[N: imm]:{ .name: Str, .age: N }
    FPerson:{ #[N: imm](name: Str, age: imm N): Person[N] -> Person[N: imm]:{
      .name: Str -> name,
      .age: imm N -> age,
      }}
    Ex:{
      .create: Person[Nat] -> FPerson#(Bob, TwentyFour),
      .name(p: Person[Nat]): Str -> p.name,
      }
    """, """
    package test
    Str:{} Bob:Str{}
    Nat:{} TwentyFour:Nat{}
    """); }
  @Test void inlineBoundsForwardingMethodSig() { fail("""
    In position [###]/Dummy0.fear:3:67
    [E57 invalidLambdaNameMdfBounds]
    This lambda is missing/has an incompatible set of bounds for its type parameters:
      N: imm
    """, """
    package test
    //Person[N: imm]:{ .name: Str, .age: N }
    FPerson:{ #[N: imm](name: Str, age: imm N): Person[N] -> Person[N]:{
      .name: Str -> name,
      .age: imm N -> age,
      }}
    Ex:{
      .create: Person[Nat] -> FPerson#(Bob, TwentyFour),
      .name(p: Person[Nat]): Str -> p.name,
      }
    """, """
    package test
    Str:{} Bob:Str{}
    Nat:{} TwentyFour:Nat{}
    """); }
  @Test void boundsForwardingImplicit() { ok("""
    package test
    Person[N: imm]:{ .name: Str, .age: N }
    FPerson:{ #[N](name: Str, age: imm N): Person[imm N] -> {
      .name -> name,
      .age -> age,
      }}
    Ex:{
      .create: Person[Nat] -> FPerson#[Nat](Bob, TwentyFour),
      .name(p: Person[Nat]): Str -> p.name,
      }
    """, """
    package test
    Str:{} Bob:Str{}
    Nat:{} TwentyFour:Nat{}
    """); }
  @Test void boundsForwardingImplicitBreak() { fail("""
    In position [###]/Dummy0.fear:3:10
    [E5 invalidMdfBound]
    The type N is not valid because it's modifier is not in the required bounds. The allowed modifiers are: imm.
    """, """
    package test
    Person[N: imm]:{ .name: Str, .age: N }
    FPerson:{ #[N](name: Str, age: imm N): Person[N] -> {
      .name -> name,
      .age -> age,
      }}
    Ex:{
      .create: Person[Nat] -> FPerson#[Nat](Bob, TwentyFour),
      .name(p: Person[Nat]): Str -> p.name,
      }
    """, """
    package test
    Str:{} Bob:Str{}
    Nat:{} TwentyFour:Nat{}
    """); }
  @Test void boundsForwardingImplicit2() { ok("""
    package test
    Person[N: imm]:{ .name: Str, .age: N }
    FPerson:{ #[N](name: Str, age: imm N): Person[imm N] -> Person[imm N]{
      .name -> name,
      .age -> age,
      }}
    Ex:{
      .create: Person[Nat] -> FPerson#[Nat](Bob, TwentyFour),
      .name(p: Person[Nat]): Str -> p.name,
      }
    """, """
    package test
    Str:{} Bob:Str{}
    Nat:{} TwentyFour:Nat{}
    """); }
  @Test void boundsForwardingImplicit3() { ok("""
    package test
    Person[N: imm]:{ .name: Str, .age: N }
    FPerson:{ #[N: imm](name: Str, age: N): Person[N] -> Person[N]{
      .name -> name,
      .age -> age,
      }}
    Ex:{
      .create: Person[Nat] -> FPerson#[Nat](Bob, TwentyFour),
      .name(p: Person[Nat]): Str -> p.name,
      }
    """, """
    package test
    Str:{} Bob:Str{}
    Nat:{} TwentyFour:Nat{}
    """); }
  // TODO: some bounds forwarding logic is broken. I think this should really be failing at like well-formedness, but is not.
  @Test void boundsForwardingExplicit() { fail("""
    In position [###]/Dummy0.fear:3:65
    [E5 invalidMdfBound]
    The type N is not valid because it's modifier is not in the required bounds. The allowed modifiers are: imm.
    """, """
    package test
    Person[N: imm]:{ .name: Str, .age: N }
    FPerson:{ #[N](name: Str, age: imm N): Person[imm N] -> Fresh[N]:Person[imm N]{
      .name -> name,
      .age -> age,
      }}
    Break:{ #: Fresh[mut Nat], }
    Ex:{
      .create: Person[Nat] -> FPerson#[Nat](Bob, TwentyFour),
      .name(p: Person[Nat]): Str -> p.name,
      }
    """, """
    package test
    Str:{} Bob:Str{}
    Nat:{} TwentyFour:Nat{}
    """); }
}
