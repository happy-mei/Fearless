package tour;

import org.junit.jupiter.api.Test;
import utils.Base;

import static codegen.java.RunJavaProgramTests.ok;
import static utils.RunOutput.Res;

public class Ex20ImperativeTest {
  @Test void ifTerminatesTrue() { ok(new Res("hi\nyay", "", 0), """
    package test
    Test:Main {sys -> Block#[Void]
      .if {True} .do {UnrestrictedIO#sys.println("hi")}
      .do {UnrestrictedIO#sys.println("yay")}
      .done
      }
    """, Base.mutBaseAliases); }
  @Test void ifTerminatesFalse() { ok(new Res("yay", "", 0), """
    package test
    Test:Main {sys -> Block#
      .if {False} .do {UnrestrictedIO#sys.println("hi")}
      .do {UnrestrictedIO#sys.println("yay")}
      .return {Void}
      }
    """, Base.mutBaseAliases); }
  @Test void refIsMutable() { ok(new Res("", "", 0), """
    package test
    Test:Main {sys -> Block#
      .let[mut Var[Int]] n = {Var#[Int] +5}
      .assert {n.get == +5}
      .do {n := +10}
      .assert {n.get == +10}
      .return {Void}
      }
    """, Base.mutBaseAliases); }
  @Test void incrementLoop() { ok(new Res("", "", 0), """
    package test
    Test:Main {sys -> Block#
      .let n = {Count.int(+0)}
      .loop {Block#
        .if {n.get == +10} .return {ControlFlow.break}
        .do {Block#(n++)}
        .return {ControlFlow.continue}
        }
      .assert {n.get == +10}
      .return {Void}
      }
    """, Base.mutBaseAliases); }
  @Test void incrementLoopNoVar() { ok(new Res("", "", 0), """
    package test
    Test:Main {sys -> Foo#(Count.int(+0)) }
    Foo: {#(n: mut Count[Int]): Void -> Block#
      .loop {Block#
        .if {n.get == +10} .return {ControlFlow.break}
        .do {Block#(n++)}
        .return {ControlFlow.continue}
        }
      .return {Void}
      }
    """, Base.mutBaseAliases); }
  @Test void earlyReturnLoop() { ok(new Res("10", "", 0), """
    package test
    Test:Main {sys -> (UnrestrictedIO#sys).println(Foo#)}
    Foo: {#: Str -> Block#
      .let n = {Count.int(+0)}
      .loop {Block#
        .if {n.get == +10} .return {ControlFlow.return[Str](n.get.str)}
        .do {Block#(n++)}
        .return {ControlFlow.continueWith[Str]}
        }
      .return {"Boo :("}
      }
    """, Base.mutBaseAliases); }
  @Test void earlyReturnLoopEarlyExit() { ok(new Res("Boo :(", "", 0), """
    package test
    Test:Main {sys -> (UnrestrictedIO#sys).println(Foo#)}
    Foo: {#: Str -> Block#
      .let n = {Count.int(+0)}
      .loop {Block#
        .if {n.get == +10} .return {ControlFlow.return[Str](n.get.str)}
        .do {Block#(n++)}
        .return {ControlFlow.breakWith[Str]}
        }
      .return {"Boo :("}
      }
    """, Base.mutBaseAliases); }
}
