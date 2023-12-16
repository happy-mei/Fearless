package codegen.java.tour;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import utils.Base;

import static utils.RunJava.Res;
import static codegen.java.RunJavaProgramTests.*;

@Disabled
public class Ex09FlowsTest {
  @Test void flowSumStr() { ok(new Res("30", "", 0), "test.Test", """
    package test
    Test:Main {sys -> FIO#sys.println(
      Flow#[Int](5, 10, 15)
        #(Flow.sum)
        .str
      )}
    """, Base.mutBaseAliases);}
  @Disabled
  @Test void flowSumAssert() { ok(new Res(), "test.Test", """
    package test
    // We cannot have Assert.eq without a magic equality (HasEq)
    // and magic toString (which would help us provide a better error message)
    Test:Main {sys -> Assert.eq({n1, n2 -> n1 == n2},
      Flow#[Int](5, 10, 15)#(Flow.sum),
      30
      )}
    """, Base.mutBaseAliases);}
  @Test void flowSumAssertNoEq() { ok(new Res(), "test.Test", """
    package test
    // We cannot have Assert.eq without a magic equality and magic toString (which would help us provide a better
    // error message)
    Test:Main {sys -> Assert!(
      Flow#[Int](5, 10, 15)#(Flow.sum)
      == 30
      )}
    """, Base.mutBaseAliases);}
  // Attempting to do a terminal operation on an infinite flow is always an exception.
  // The flow must be bounded by an intermediate operation before a terminal operation can be performed.
  @Test void flowFilter() { ok(new Res(), "test.Test", """
    package test
    Test:Main {sys -> Assert!(
      Flow#[Int](5, 10, 15).filter{n -> n > 5}.size
      == 2u
      )}
    """, Base.mutBaseAliases);}
  @Test void flowFilterMap() { ok(new Res(), "test.Test", """
    package test
    Test:Main {sys -> Assert!(
      Flow#[Int](5, 10, 15)
        .filter{n -> n > 5}
        .map{n -> n * 10}
        #(Flow.max)
      == 150
      )}
    """, Base.mutBaseAliases);}
  @Test void flowFilterMapIntEq1() { ok(new Res(), "test.Test", """
    package test
    Test:Main {sys -> 150.assertEq("max assert failed",
      Flow#[Int](5, 10, 15)
        .filter{n -> n > 5}
        .map{n -> n * 10}
        #(Flow.max)
      )}
    """, Base.mutBaseAliases);}
  // We prefer flowFilterMapIntEq1 because it is more clear that this test is of an assertion rather than of a flow.
  @Test void flowFilterMapIntEq2() { ok(new Res(), "test.Test", """
    package test
    Test:Main {sys -> Flow#[Int](5, 10, 15)
      .filter{n -> n > 5}
      .map{n -> n * 10}
      #(Flow.max)
      .assertEq(150)
      }
    """, Base.mutBaseAliases);}

  // We may not need this
  @Test void flowScan() { ok(new Res(), "test.Test", """
    package test
    Test:Main {sys -> "!5 !510 !51015".assertEq(
      Flow#[Int](5, 10, 15)
        .scan("!", {acc, n -> acc + (n.str)})
        .map{n -> n.str}
        #(Flow.str " ")
      )}
    """, Base.mutBaseAliases);}

  // TODO: Next topic is what parallelisation is possible on .map

  @Test void flowMutScan() { ok(new Res(), "test.Test", """
    package test
    Test:Main {sys -> "5 10 500".assertEq(
      Flow#[Int](5, 10, 15)
        // .actor requires an iso S for its initial value
        // This lambda has the type read ActorImpl[mut IsoPod[S], ... E, R]
        .actor(Ref#1, mut Consume[mut Ref[Int]]{state->someRandom.set(state.get)}, {state, n -> Block#
          .do {state.set(someMutList.get(0u)!)}
          .if {state.get > 10} .return {500}
          .return {n})
          
        // Actors on:
        // - mut flow of imm values with an imm lambda
        //    + the lambda can take mut state
              - the lambda can only capture imm
        // - mut flow of mut values with an imm lambda
        //    - we cannot take mut state
        //    - the lambda can only capture imm
        // - mut flow of imm values with an read lambda
        //    - the lambda cannot take mut state
        //    + the lambda can capture mut state as read
        // - mut flow of mut values with an read lambda
        //    - the lambda cannot take mut state
        //    - unsound to parallelise
        // - mut flow of imm values with an readH lambda
        // - mut flow of mut values with an readH lambda
          
        .actor(Ref#1, {state, n -> Block#
          .if {state.get > 10} .return {500}
          .do {state.put(n + state.get)}
          .return {n})
        .map{n -> n.str}
        #(Flow.str " ")
      )}
    """, Base.mutBaseAliases);}

  // If we do not offer any mapMut/mut lambdas, we can have parallelised read lambdas
  @Test void mapAndMapMut() { ok(new Res(), "test.Test", """
    package test
    Test:Main {sys -> "5 10 500".assertEq(
      Flow#[Int](5, 10, 15)
        .map{n -> n * 10}
        .mapMut{n -> Block#(counter++, n)}
        .mapMut{n -> Block#(counter++, n)}
        .map{n -> n + 10}
        #(Flow.str " ")
      )}
    """, Base.mutBaseAliases);}

  @Test void flowScan2() { ok(new Res(), "test.Test", """
    package test
    Test:Main {sys -> "5 10 500".assertEq(
      Flow#[Int](5, 10, 15)
        .actor(v, {acc, n -> ...1})
        .actor(v, {acc, n -> ...2})
        .map{n -> n.str}
        #(Flow.str " ")
      )}
    """, Base.mutBaseAliases);}
}
