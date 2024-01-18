package codegen.java.tour;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import utils.Base;

import static utils.RunJava.Res;
import static codegen.java.RunJavaProgramTests.*;

/*
 * 1. Public contract for Flows
 *
 * 2. Internal deterministic specification for Flows
 *
 *
 * (this is important for early exits like .limit or .find)
 */

public class Ex09FlowsTest {
  @Test void flowSumStr() { ok(new Res("30", "", 0), "test.Test", """
    package test
    Test:Main {sys -> FIO#sys.println(
      Flow#[Int](5, 10, 15)
        #(Flow.sum)
        .str
      )}
    """, Base.mutBaseAliases); }
  @Test void flowFilterSumStr() { ok(new Res("25", "", 0), "test.Test", """
    package test
    Test:Main {sys -> FIO#sys.println(
      Flow#[Int](5, 10, 15)
        .filter{n -> n > 5}
        #(Flow.sum)
        .str
      )}
    """, Base.mutBaseAliases); }
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

  @Test void flowMap() { ok(new Res("300", "", 0), "test.Test", """
    package test
    Test:Main {sys -> FIO#sys.println(
      Flow#[Int](5, 10, 15)
        .map{n -> n * 10}
        #(Flow.sum)
        .str
      )}
    """, Base.mutBaseAliases); }

  @Test void flowLimit0() { ok(new Res("0", "", 0), "test.Test", """
    package test
    Test:Main {sys -> FIO#sys.println(
      Flow#[Int](5, 10, 15)
        .limit(0u)
        #(Flow.sum)
        .str
      )}
    """, Base.mutBaseAliases); }
  @Test void flowLimit1() { ok(new Res("5", "", 0), "test.Test", """
    package test
    Test:Main {sys -> FIO#sys.println(
      Flow#[Int](5, 10, 15)
        .limit(1u)
        #(Flow.sum)
        .str
      )}
    """, Base.mutBaseAliases); }
  @Test void flowLimit2() { ok(new Res("15", "", 0), "test.Test", """
    package test
    Test:Main {sys -> FIO#sys.println(
      Flow#[Int](5, 10, 15)
        .limit(2u)
        #(Flow.sum)
        .str
      )}
    """, Base.mutBaseAliases); }
  @Test void flowLimit3() { ok(new Res("30", "", 0), "test.Test", """
    package test
    Test:Main {sys -> FIO#sys.println(
      Flow#[Int](5, 10, 15)
        .limit(3u)
        #(Flow.sum)
        .str
      )}
    """, Base.mutBaseAliases); }
  @Test void flowLimit4() { ok(new Res("30", "", 0), "test.Test", """
    package test
    Test:Main {sys -> FIO#sys.println(
      Flow#[Int](5, 10, 15)
        .limit(4u)
        #(Flow.sum)
        .str
      )}
    """, Base.mutBaseAliases); }

  // Attempting to do a terminal operation on an infinite flow is always an exception.
  // The flow must be bounded by an intermediate operation before a terminal operation can be performed.
  @Test void flowFilter() { ok(new Res(), "test.Test", """
    package test
    Test:Main {sys -> Assert!(
      Flow#[Int](5, 10, 15).filter{n -> n > 5}.size
      == 2u
      )}
    """, Base.mutBaseAliases);}
  @Test void flowFilterPrintSize() { ok(new Res("2", "", 0), "test.Test", """
    package test
    Test:Main {sys -> Block#
      .var size = {Flow#[Int](5, 10, 15).filter{n -> n > 5}.size}
      .return {FIO#sys.println(size.str)}
      }
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

  @Test void flowDuplicate() { ok(new Res("35 40 45", "", 0), "test.Test", """
    package test
    Test:Main {sys -> Flow#[Int](5, 10, 15)
      .duplicate f2 = {f1 -> f1#(Flow.sum)}
      .map{n -> n + f2}
      .map{n -> n.str}
      #(Flow.str " ")
      }
    """, Base.mutBaseAliases);}

  @Test void mutExtensionMethod() { ok(new Res("20 30", "", 0), "test.Test", """
    package test
    Test:Main {sys -> Block#
      .var[List[Int]] list = {List#[Int](1, 2, 3)}
      .var[Str] myFlow = {list.flow
        .filter{n -> n > 1}
        #{f -> f.map{n -> n*10}}
        .map{n -> n.str}
        #(Flow.str " ")
        }
      .return {FIO#sys.println(myFlow)}
      }
    """, Base.mutBaseAliases);}

  @Test void flowActor() { ok(new Res("31", "", 0), "test.Test", """
    package test
    Test:Main {sys -> "42 5 42 10 500".assertEq(
      Flow#[Int](5, 10, 15)
        // .actor requires an iso S for its initial value
        // The 3rd argument is optional
        .actor[mut Ref[Int], Int](Ref#[Int]1, {downstream, state, n -> Block#
          .do {state := (state* + n)}
          .if {state.get > 16} .return{Block#(downstream#500, {})}
          .do{downstream#42}
          .do {downstream#n}
          .return {{}}}, mut Consumer[mut Ref[Int]]{state->FIO#sys.println(state.get.str)})
        .map{n -> n.str}
        #(Flow.str " ")
      )}
    """, Base.mutBaseAliases);}
  @Test void flowActorNoConsumer() { ok(new Res(), "test.Test", """
    package test
    Test:Main {sys -> "42 5 42 10 500".assertEq(
      Flow#[Int](5, 10, 15)
        // .actor requires an iso S for its initial value
        // The 3rd argument is optional
        .actor[mut Ref[Int], Int](Ref#[Int]1, {downstream, state, n -> Block#
          .do {state := (state* + n)}
          .if {state.get > 16} .return{Block#(downstream#500, {})}
          .do{downstream#42}
          .do {downstream#n}
          .return {{}}})
        .map{n -> n.str}
        #(Flow.str " ")
      )}
    """, Base.mutBaseAliases);}

  // We may have this as a specialisation of .actor
  @Test void flowScan() { ok(new Res(), "test.Test", """
    package test
    Test:Main {sys -> "!5 !510 !51015".assertEq(
      Flow#[Int](5, 10, 15)
        .scan("!", {acc, n -> acc + (n.str)})
        .map{n -> n.str}
        #(Flow.str " ")
      )}
    """, Base.mutBaseAliases);}

//  @Test void flowActor() { ok(new Res(), "test.Test", """
//    package test
//    Test:Main {sys -> "5 10 500".assertEq(
//      Flow#[Int](5, 10, 15)
//        // .actor requires an iso S for its initial value
//        // This lambda has the type read ActorImpl[mut IsoPod[S], ... E, R]
//        .actor(Ref#1, mut Consume[mut Ref[Int]]{state->someRandom.set(state.get)}, {state, n -> Block#
//          .do {state.set(someMutList.get(0u)!)}
//          .if {state.get > 10} .return {500}
//          .return {n})
//        .actor(Ref#1, mut Consume[mut Ref[Int]]{state->someRandom.set(state.get)}, {state, n -> Block#
//          .do {state.set(someMutList.get(0u)!)}
//          .if {state.get > 10} .return {500}
//          .return {n})
//        // Actors on:
//        // - mut flow of imm values with an imm lambda
//        //    + the lambda can take mut state
//              - the lambda can only capture imm
//        // - mut flow of mut values with an imm lambda
//        //    - we cannot take mut state
//        //    - the lambda can only capture imm
//        // - mut flow of imm values with an read lambda
//        //    - the lambda cannot take mut state
//        //    + the lambda can capture mut state as read
//        // - mut flow of mut values with an read lambda
//        //    - the lambda cannot take mut state
//        //    - unsound to parallelise
//        // - mut flow of imm values with an readH lambda
//        // - mut flow of mut values with an readH lambda
//
//        .actor(Ref#1, {state, n -> Block#
//          .if {state.get > 10} .return {500}
//          .do {state.put(n + state.get)}
//          .return {n})
//        .map{n -> n.str}
//        #(Flow.str " ")
//      )}
//    """, Base.mutBaseAliases);}

  @Test void flowActorMultiParallel() { ok(new Res(), "test.Test", """
    package test
    Test:Main {sys -> "5 10".assertEq(
      Flow#[Int](5, 10)
        .actor(Ref#1, {state, n -> Block#
          .do {state.set(someMutList.get(0u)!)}
          .if {state.get > 10} .return {500}
          .return {n})
    """, Base.mutBaseAliases); }

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

  @Test void flowSplit() { ok(new Res(), "test.Test", """
    package test
    Test: Main{sys -> "5 10 15".assertEq(
      Flow#[Int](5, 10, 15)
        .split(
          {f -> f.map{n -> n * 5}.filter{n -> n > 10}},
          {f -> f.map{n -> n * 10}},
          {f -> f.map{n -> n * 15}},
          {f1,f2,f3 -> f1#(Flow.str " ")+" "+f2#(Flow.str " ")+" "+f3#(Flow.str " ")},
        )
        //vs
        .split3{
          .f1(f) -> f.map{n -> n * 5},
          .f2(f) -> f.map{n -> n * 10},
          .f3(f) -> f.map{n -> n * 15},
          .merge(f1,f2,f3) -> f1#(Flow.str " ")+" "+f2#(Flow.str " ")+" "+f3#(Flow.str " "),
        }
        // split actors
        // with a capability, this is fine with imm messages. Without a capability we can call actor2 and actor3 from
        // recvActor but actor2 and actor3 cannot call any other actor. Accessing the actor graph needs a capability.
        .spawnSystemOf3(...., {
          .recvActor(state, msg) -> ...,
          .actor2(state, msg, actorRef3) -> ...,
          .actor3(state, msg, actorRef2) -> ...,
          .consume(state1, state2, state3) -> ...,
          }
          // or
        .spawnSystemOf3(...., {
          .recvActor(state, msg, downstream) -> ...,
          .actor2(state, msg, actorRef3, downstream) -> ...,
          .actor3(state, msg, actorRef2, downstream) -> ...,
          } // has to internally represent the result as three downstream lists to have a deterministic ordering
        .map{n -> n + 5}
        .to list..
      )}
    """, Base.mutBaseAliases);}
}
