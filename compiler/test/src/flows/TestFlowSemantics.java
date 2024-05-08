package flows;

import org.junit.jupiter.api.Test;
import utils.Base;
import utils.RunOutput;

import static codegen.java.RunJavaProgramTests.ok;

public class TestFlowSemantics {
  @Test void flowSum() {ok(new RunOutput.Res("60", "", 0), """
    package test
    Test: Main{sys -> Block#
      .let[mut Flow[Nat]] x = {Flow#[Nat](1,2,3).map{x->x * 10}}
      .let[Nat] sum = {x#(Flow.uSum)}
      .let[mut IO] io = {FIO#sys}
      .do {io.println(sum.str)}
      .return {{}}
      }
    """, Base.mutBaseAliases);}

  @Test void flowReuse() {ok(new RunOutput.Res("60", "Program crashed with: \"This flow cannot be reused.\"", 1), """
    package test
    Test: Main{sys -> Block#
      .let[mut Flow[Nat]] x = {Flow#[Nat](1,2,3).map{x->x * 10}}
      .let[Nat] sum = {x#(Flow.uSum)}
      .let[mut Flow[Nat]] bigSum = {x.map{y->y * 10}}
      .let[mut IO] io = {FIO#sys}
      .do {io.println(sum.str)}
      .do {io.println(bigSum#(Flow.uSum).str)}
      .return {{}}
      }
    """, Base.mutBaseAliases);}

  /*
   * When a flow has an uncaught error, it goes "stuck". The error is not accessible by the outside world and that
   * step will not process any new elements. This observable behaviour is the same in parallel and sequential flows.
   * It is quite sad to have flows not reveal any exceptions or even if one occurred. But I think it's the most sound
   * approach for now.
   *
   * Effectively, any error in a flow can be seen as non-termination. (TODO: is this true? I think I can collect it to a list still and it'll work because I just skip all elements after the failure instead of truly going stuck)
   */
  @Test void throwInAFlowBeforeStopPar() {ok(new RunOutput.Res("10", "", 0), """
    package test
    Test: Main{sys -> Block#
      .let x = {Flow#[Nat](1, 2, 3)
        .map{x->Block#
          .if {x.nat == 2} .do {Error.msg (x.str)}
          .return {x.nat * 10}
          }
        .limit(1)
        }
      .let[Nat] sum = {x#(Flow.uSum)}
      .let[mut IO] io = {FIO#sys}
      .do {io.println(sum.str)}
      .return {{}}
      }
    """, Base.mutBaseAliases);}
  @Test void throwInAFlowAfterStopPar() {ok(new RunOutput.Res("10", "", 0), """
    package test
    Test: Main{sys -> Block#
      .let x = {Flow#[Nat](1, 2, 3)
        .map{x->Block#
          .if {x.nat == 2} .do {Error.msg (x.str)}
          .return {x.nat * 10}
          }
        .limit(1)
        }
      .let[Nat] sum = {x#(Flow.uSum)}
      .let[mut IO] io = {FIO#sys}
      .do {io.println(sum.str)}
      .return {{}}
      }
    """, Base.mutBaseAliases);}

  @Test void throwInAFlowBeforeStopSeq() {ok(new RunOutput.Res("10", "", 0), """
    package test
    Test: Main{sys -> Block#
      .let x = {Flow#[mut Nat](mut 1, mut 2, mut 3)
        .map{x->Block#
          .if {x.nat == 2} .do {Error.msg (x.str)}
          .return {x.nat * 10}
          }
        .limit(1)
        }
      .let[Nat] sum = {x#(Flow.uSum)}
      .let[mut IO] io = {FIO#sys}
      .do {io.println(sum.str)}
      .return {{}}
      }
    """, Base.mutBaseAliases);}
  @Test void throwInAFlowAfterStopSeq() {ok(new RunOutput.Res("10", "", 0), """
    package test
    Test: Main{sys -> Block#
      .let x = {Flow#[mut Nat](mut 1, mut 2, mut 3)
        .map{x->Block#
          .if {x.nat == 2} .do {Error.msg (x.str)}
          .return {x.nat * 10}
          }
        .limit(2)
        }
      .let[Nat] sum = {x#(Flow.uSum)}
      .let[mut IO] io = {FIO#sys}
      .do {io.println(sum.str)}
      .return {{}}
      }
    """, Base.mutBaseAliases);}
}
