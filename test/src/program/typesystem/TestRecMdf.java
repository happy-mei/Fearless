package program.typesystem;

import net.jqwik.api.Example;

import static program.typesystem.RunTypeSystem.fail;
import static program.typesystem.RunTypeSystem.ok;

public class TestRecMdf {
  @Example void shouldCollapseWhenCalled1() { ok("""
    package test
    A:{
      read .m1(_: mut NoPromote): recMdf A,
      mut .m2: mut A -> this.m1{},
      }
    NoPromote:{}
    """); }
  @Example void shouldCollapseWhenCalled1a() { ok("""
    package test
    A:{
      read .m1: recMdf A -> {},
      mut .m2: read A -> this.m1,
      }
    """); }
  @Example void shouldCollapseWhenCalled1b() { fail("""
    In position [###]/Dummy0.fear:4:20
    [E28 undefinedName]
    The identifier "this" is undefined or cannot be captured.
    """, """
    package test
    A:{
      read .m1(_: mut NoPromote): recMdf A,
      mut .m2: imm A -> this.m1{},
      }
    NoPromote:{}
    """); }
  @Example void shouldCollapseWhenCalled1c() { ok("""
    package test
    A:{
      read .m1(_: mut NoPromote): recMdf A,
      imm .m2: imm A -> this.m1{},
      }
    NoPromote:{}
    """); }
  @Example void shouldCollapseWhenCalled1d() { fail("""
    In position [###]/Dummy0.fear:4:24
    [E32 noCandidateMeths]
    When attempting to type check the method call: this .m1/1[]([[-mut-][test.NoPromote[]]{'fear0$ }]), no candidates for .m1/1 returned the expected type mut test.A[]. The candidates were:
    TsT[ts=[read test.A[], mut test.NoPromote[]], t=imm test.A[]]
    TsT[ts=[read test.A[], iso test.NoPromote[]], t=imm test.A[]]
    TsT[ts=[imm test.A[], iso test.NoPromote[]], t=imm test.A[]]
    """, """
    package test
    A:{
      read .m1(_: mut NoPromote): recMdf A,
      imm .m2: mut A -> this.m1{},
      }
    NoPromote:{}
    """); }

  @Example void shouldCollapseWhenCalledGenMut() { ok("""
    package test
    A[X]:{
      read .m1(_: mut NoPromote): recMdf X,
      mut .m2: mdf X -> this.m1{},
      }
    NoPromote:{}
    """); }
  @Example void shouldCollapseWhenCalledGenImm() { ok("""
    package test
    A[X]:{
      read .m1(_: mut NoPromote): recMdf X,
      imm .m2: imm X -> this.m1{},
      }
    NoPromote:{}
    """); }
  @Example void shouldCollapseWhenCalledGenRead1() { ok("""
    package test
    A[X]:{
      read .m1(_: mut NoPromote): recMdf X,
      read .m2: read X -> this.m1{},
      }
    NoPromote:{}
    """); }
  @Example void shouldCollapseWhenCalledGenRead2() { ok("""
    package test
    A[X]:{
      read .m1(_: mut NoPromote): recMdf X,
      read .m2: recMdf X -> this.m1{},
      }
    NoPromote:{}
    """); }
  // TODO: not sure about this one
  @Example void shouldCollapseWhenCalledGenLent() { ok("""
    package test
    A[X]:{
      read .m1(_: mut NoPromote): recMdf X,
      lent .m2: recMdf X -> this.m1{},
      }
    NoPromote:{}
    """); }
  @Example void shouldCollapseWhenCalledGenIso1() { ok("""
    package test
    A[X]:{
      read .m1(_: mut NoPromote): recMdf X,
      iso .m2: imm X -> this.m1{},
      }
    NoPromote:{}
    """); }
  // should fail because iso can capture imm too
  @Example void shouldCollapseWhenCalledGenIso2() { fail("""
    In position [###]/Dummy0.fear:4:24
    [E32 noCandidateMeths]
    When attempting to type check the method call: this .m1/1[]([[-mut-][test.NoPromote[]]{'fear0$ }]), no candidates for .m1/1 returned the expected type mut X. The candidates were:
    TsT[ts=[read test.A[mdf X], mut test.NoPromote[]], t=imm X]
    TsT[ts=[read test.A[mdf X], iso test.NoPromote[]], t=imm X]
    TsT[ts=[imm test.A[mdf X], iso test.NoPromote[]], t=imm X]
    """, """
    package test
    A[X]:{
      read .m1(_: mut NoPromote): recMdf X,
      iso .m2: iso X -> this.m1{},
      }
    NoPromote:{}
    """); }

  @Example void shouldCollapseWhenCalledNestedGenMut1() { ok("""
    package test
    A[X]:{
      read .m1(_: mut NoPromote): Consumer[recMdf X],
      mut .m2: Consumer[mdf X] -> this.m1{},
      }
    Consumer[X]:{ #(x: mdf X): Void }
    NoPromote:{} Void:{}
    """); }
  @Example void shouldCollapseWhenCalledNestedGenMut2() { fail("""
    In position [###]/Dummy0.fear:4:2
    [E23 methTypeError]
    Expected the method .m2/0 to return imm test.Consumer[imm X], got imm test.Consumer[mdf X].
    """, """
    package test
    A[X]:{
      read .m1(_: mut NoPromote): Consumer[recMdf X],
      mut .m2: Consumer[imm X] -> this.m1{},
      }
    Consumer[X]:{ #(x: mdf X): Void }
    NoPromote:{} Void:{}
    """); }
}
