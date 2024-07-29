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
      .let[mut IO] io = {UnrestrictedIO#sys}
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
      .let[mut IO] io = {UnrestrictedIO#sys}
      .do {io.println(sum.str)}
      .do {io.println(bigSum#(Flow.uSum).str)}
      .return {{}}
      }
    """, Base.mutBaseAliases);}

  /*
   * Fearless errors in flow before a stop are propagated. Fearless errors after the "stop" are ignored.
   */
  @Test void throwInAFlowBeforeStopPar() {ok(new RunOutput.Res("", "Program crashed with: \"2\"", 1), """
    package test
    Test: Main{sys -> Block#
      .let x = {Flow#[Nat](1, 2, 3)
        .map{x->Block#
          .if {x.nat == 2} .do {Error.msg (x.str)}
          .return {x.nat * 10}
          }
        }
      .let[Nat] sum = {x#(Flow.uSum)}
      .let[mut IO] io = {UnrestrictedIO#sys}
      .do {io.println(sum.str)}
      .return {{}}
      }
    """, Base.mutBaseAliases);}
  @Test void throwInAFlowBeforeStopDP() {ok(new RunOutput.Res("", "Program crashed with: \"2\"", 1), """
    package test
    Test: Main{sys -> Block#
      .let x = {Flow.range(+0, +50)
        .map{n->n.nat}
        .map{x->Block#
          .if {x.nat == 2} .do {Error.msg (x.str)}
          .return {x.nat * 10}
          }
        }
      .let[Nat] sum = {x#(Flow.uSum)}
      .let[mut IO] io = {UnrestrictedIO#sys}
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
      .let[mut IO] io = {UnrestrictedIO#sys}
      .do {io.println(sum.str)}
      .return {{}}
      }
    """, Base.mutBaseAliases);}
  @Test void throwInAFlowAfterStopDP() {ok(new RunOutput.Res("10", "", 0), """
    package test
    Test: Main{sys -> Block#
      .let x = {Flow.range(+0, +50)
        .map{n->n.nat}
        .map{x->Block#
          .if {x.nat == 2} .do {Error.msg (x.str)}
          .return {x.nat * 10}
          }
        .limit(1)
        }
      .let[Nat] sum = {x#(Flow.uSum)}
      .let[mut IO] io = {UnrestrictedIO#sys}
      .do {io.println(sum.str)}
      .return {{}}
      }
    """, Base.mutBaseAliases);}

  @Test void throwInAFlowBeforeStopSeq() {ok(new RunOutput.Res("", "Program crashed with: \"2\"", 1), """
    package test
    Test: Main{sys -> Block#
      .let x = {Flow#[mut Nat](mut 1, mut 2, mut 3)
        .map{x->Block#
          .if {x.nat == 2} .do {Error.msg (x.str)}
          .return {x.nat * 10}
          }
        }
      .let[Nat] sum = {x#(Flow.uSum)}
      .let[mut IO] io = {UnrestrictedIO#sys}
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
        .limit(1)
        }
      .let[Nat] sum = {x#(Flow.uSum)}
      .let[mut IO] io = {UnrestrictedIO#sys}
      .do {io.println(sum.str)}
      .return {{}}
      }
    """, Base.mutBaseAliases);}

  /*
   * Non-deterministic errors bubble up, even if the flow isn't listening anymore because they're only catchable
   * in Fearless code with a capability anyway.
   */
  @Test void throwInAFlowBeforeStopDP_ND() {ok(new RunOutput.Res("", "Program crashed with: Stack overflowed", 1), """
    package test
    Test: Main{sys -> Block#
      .let x = {Flow#[Nat](1, 2, 3, 4)
        .map{x->Block#
          .if {x.nat == 2} .do {StackOverflow#}
          .return {x.nat * 10}
          }
        }
      .let[Nat] sum = {x#(Flow.uSum)}
      .let[mut IO] io = {UnrestrictedIO#sys}
      .do {io.println(sum.str)}
      .return {{}}
      }
    StackOverflow: {#[R]: R -> this#}
    """, Base.mutBaseAliases);}
  @Test void throwInAFlowBeforeStopParND() {ok(new RunOutput.Res("", "Program crashed with: Stack overflowed", 1), """
    package test
    Test: Main{sys -> Block#
      .let x = {Flow#[Nat](1, 2, 3)
        .map{x->Block#
          .if {x.nat == 2} .do {StackOverflow#}
          .return {x.nat * 10}
          }
        }
      .let[Nat] sum = {x#(Flow.uSum)}
      .let[mut IO] io = {UnrestrictedIO#sys}
      .do {io.println(sum.str)}
      .return {{}}
      }
    StackOverflow: {#[R]: R -> this#}
    """, Base.mutBaseAliases);}
  @Test void throwInAFlowAfterStopParND() {ok(new RunOutput.Res("", "Program crashed with: Stack overflowed", 1), """
    package test
    Test: Main{sys -> Block#
      .let x = {Flow#[Nat](1, 2, 3)
        .map{x->Block#
          .if {x.nat == 2} .do {StackOverflow#}
          .return {x.nat * 10}
          }
        .limit(1)
        }
      .let[Nat] sum = {x#(Flow.uSum)}
      .let[mut IO] io = {UnrestrictedIO#sys}
      .do {io.println(sum.str)}
      .return {{}}
      }
    StackOverflow: {#[R]: R -> this#}
    """, Base.mutBaseAliases);}

  @Test void throwInAFlowBeforeStopSeqND() {ok(new RunOutput.Res("", "Program crashed with: Stack overflowed", 1), """
    package test
    Test: Main{sys -> Block#
      .let x = {Flow#[mut Nat](mut 1, mut 2, mut 3)
        .map{x->Block#
          .if {x.nat == 2} .do {StackOverflow#}
          .return {x.nat * 10}
          }
        }
      .let[Nat] sum = {x#(Flow.uSum)}
      .let[mut IO] io = {UnrestrictedIO#sys}
      .do {io.println(sum.str)}
      .return {{}}
      }
    StackOverflow: {#[R]: R -> this#}
    """, Base.mutBaseAliases);}
  @Test void throwInAFlowAfterStopSeqND() {ok(new RunOutput.Res("10", "", 0), """
    package test
    Test: Main{sys -> Block#
      .let x = {Flow#[mut Nat](mut 1, mut 2, mut 3)
        .map{x->Block#
          .if {x.nat == 2} .do {StackOverflow#}
          .return {x.nat * 10}
          }
        .limit(1)
        }
      .let[Nat] sum = {x#(Flow.uSum)}
      .let[mut IO] io = {UnrestrictedIO#sys}
      .do {io.println(sum.str)}
      .return {{}}
      }
    StackOverflow: {#[R]: R -> this#}
    """, Base.mutBaseAliases);}
}
