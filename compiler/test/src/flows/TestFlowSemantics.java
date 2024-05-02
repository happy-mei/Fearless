package flows;

import org.junit.jupiter.api.Test;
import utils.Base;
import utils.RunOutput;

import static codegen.java.RunJavaProgramTests.ok;

public class TestFlowSemantics {
  @Test void flowSum() {ok(new RunOutput.Res("60", "", 0), """
    package test
    Test: Main{sys -> Block#
      .let[mut Flow[Int]] x = {Flow#[Int](1,2,3).map{x->x * 10}}
      .let[Int] sum = {x#(Flow.sum)}
      .let[mut IO] io = {FIO#sys}
      .do {io.println(sum.str)}
      .return {{}}
      }
    """, Base.mutBaseAliases);}

  @Test void flowReuse() {ok(new RunOutput.Res("60", "Program crashed with: \"This flow cannot be reused.\"", 1), """
    package test
    Test: Main{sys -> Block#
      .let[mut Flow[Int]] x = {Flow#[Int](1,2,3).map{x->x * 10}}
      .let[Int] sum = {x#(Flow.sum)}
      .let[mut Flow[Int]] bigSum = {x.map{y->y * 10}}
      .let[mut IO] io = {FIO#sys}
      .do {io.println(sum.str)}
      .do {io.println(bigSum#(Flow.sum).str)}
      .return {{}}
      }
    """, Base.mutBaseAliases);}

  // TODO: we want to swallow all exceptions after a "stop" was requested.
  @Test void throwInAFlow() {ok(new RunOutput.Res("", "", 0), """
    package test
    Test: Main{sys -> Block#
      .let[mut Flow[Int]] x = {Flow#[Int](1,2,3)
        .map{x->Block#
          .if {x == 2} .do {Error.msg (x.str)}
          .return {x * 10}
          }
        }
      .let[Int] sum = {x#(Flow.sum)}
      .let[mut IO] io = {FIO#sys}
      .do {io.println(sum.str)}
      .return {{}}
      }
    """, Base.mutBaseAliases);}
}
