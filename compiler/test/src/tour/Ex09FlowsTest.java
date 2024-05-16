package tour;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import utils.Base;

import static utils.RunOutput.Res;
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
  @Test void flowSumStr() { ok(new Res("30", "", 0), """
    package test
    Test:Main {sys -> FIO#sys.println(
      Flow#[Int](+5, +10, +15)
        #(Flow.sum)
        .str
      )}
    """, Base.mutBaseAliases); }
  @Test void immFlowSumStr() { ok(new Res("30", "", 0), """
    package test
    Test:Main {sys -> FIO#sys.println(
      As[imm List[Int]]#(List#[Int](+5, +10, +15)).flow
        #(Flow.sum)
        .str
      )}
    """, Base.mutBaseAliases); }
  @Test void flowFilterSumStr() { ok(new Res("25", "", 0), """
    package test
    Test:Main {sys -> FIO#sys.println(
      Flow#[Int](+5, +10, +15)
        .filter{n -> n > +5}
        #(Flow.sum)
        .str
      )}
    """, Base.mutBaseAliases); }
  @Disabled
  @Test void flowSumAssert() { ok(new Res(), """
    package test
    // We cannot have Assert.eq without a magic equality (HasEq)
    // and magic toString (which would help us provide a better error message)
    Test:Main {sys -> Assert.eq({n1, n2 -> n1 == n2},
      Flow#[Int](+5, +10, +15)#(Flow.sum),
      30
      )}
    """, Base.mutBaseAliases);}
  @Test void flowSumAssertNoEq() { ok(new Res(), """
    package test
    // We cannot have Assert.eq without a magic equality and magic toString (which would help us provide a better
    // error message)
    Test:Main {sys -> Assert!(
      Flow#[Int](+5, +10, +15)#(Flow.sum)
      == +30
      )}
    """, Base.mutBaseAliases);}

  @Test void flowMap() { ok(new Res("300", "", 0), """
    package test
    Test:Main {sys -> FIO#sys.println(
      Flow#[Int](+5, +10, +15)
        .map{n -> n * +10}
        #(Flow.sum)
        .str
      )}
    """, Base.mutBaseAliases); }
  @Test void flowMapMapMap() { ok(new Res("", "", 0), """
    package test
    Test:Main {sys -> FIO#sys.println(
      As[List[Nat]]#(List#(5, 10, 15, 50)).flow
        .map{n -> n * 10}
        .map{n -> n * 10}
        .map{n -> n * 10}
        .fold[Nat](0, {acc, n -> acc + n}, {a, b -> a + b})
//        #(Flow.uSum)
        .str
      )}
    """, Base.mutBaseAliases); }
  @Test void flowMapWithListConstructor() { ok(new Res("300", "", 0), """
    package test
    Test:Main {sys -> FIO#sys.println(
      List#[Int](+5, +10, +15).flow
        .map{n -> n * +10}
        #(Flow.sum)
        .str
      )}
    """, Base.mutBaseAliases); }

  @Test void flowFlatMap() { ok(new Res("50, 50, 100, 100, 150", "", 0), """
    package test
    Test:Main {sys -> FIO#sys.println(
      Flow#[Int](+5, +10, +15)
        .flatMap{n -> Flow#[Int](n, n, n).limit(2).map{n' -> n' * +10}}
        .limit(5)
        .map{n -> n.str}
        #(Flow.str ", ")
      )}
    """, Base.mutBaseAliases); }

  @Test void flowGetFirst() { ok(new Res("100", "", 0), """
    package test
    Test:Main {sys -> FIO#sys.println(
      Flow#[Int](+5, +10, +15)
        .map{n -> n * +10}
        .filter{n -> n > +50}
        .find{n -> True}!
        .str
      )}
    """, Base.mutBaseAliases); }

  @Test void flowGetFirstDifferentApproaches() { ok(new Res("", "", 0), """
    package test
    Test:Main {sys -> Block#
      .assert({Trick.find == (Trick.findAndLimit)}, "find == findAndLimit ("+(Trick.find.str)+" == "+(Trick.findAndLimit.str)+")")
      .assert({Trick.first == (Trick.firstAndLimit)}, "first == firstAndLimit ("+(Trick.first.str)+" == "+(Trick.firstAndLimit.str)+")")
      .assert({Trick.first == (Trick.find)}, "first == find ("+(Trick.first.str)+" == "+(Trick.find.str)+")")
      .return{Void}
      }
    Trick: {
      .find: Nat -> Flow#[Nat](5, 10, 15)
        .map{n -> n * 10}
        .filter{n -> n > 50}
        .find{n -> True}
        .match{.some(n) -> n, .empty -> Error.msg ".find was empty"},
      .findAndLimit: Nat -> Flow#[Nat](5, 10, 15)
        .map{n -> n * 10}
        .filter{n -> n > 50}
        .limit(1)
        .find{n -> True}
        .match{.some(n) -> n, .empty -> Error.msg ".findAndLimit was empty"},
      .first: Nat -> Flow#[Nat](5, 10, 15)
        .map{n -> n * 10}
        .filter{n -> n > 50}
        .first
        .match{.some(n) -> n, .empty -> Error.msg ".first was empty"},
      .firstAndLimit: Nat -> Flow#[Nat](5, 10, 15)
        .map{n -> n * 10}
        .filter{n -> n > 50}
        .limit(1)
        .first
        .match{.some(n) -> n, .empty -> Error.msg ".firstAndLimit was empty"},
      }
    """, Base.mutBaseAliases); }
  @Test void flowGetFirstDifferentApproachesSeq() { ok(new Res("", "", 0), """
    package test
    Test:Main {sys -> Block#
      .assert({Trick.find == (Trick.findAndLimit)}, "find == findAndLimit ("+(Trick.find.str)+" == "+(Trick.findAndLimit.str)+")")
      .assert({Trick.first == (Trick.firstAndLimit)}, "first == firstAndLimit ("+(Trick.first.str)+" == "+(Trick.firstAndLimit.str)+")")
      .assert({Trick.first == (Trick.find)}, "first == find ("+(Trick.first.str)+" == "+(Trick.find.str)+")")
      .return{Void}
      }
    Trick: {
      .find: mut Person -> Flow#[mut Person](FPerson#24, FPerson#60, FPerson#75)
        .map{p -> FPerson#(p.age * 10)}
        .filter{p -> p.age > 50}
        .find{n -> True}
        .match{.some(n) -> n, .empty -> Error.msg ".find was empty"},
      .findAndLimit: mut Person -> Flow#[mut Person](FPerson#24, FPerson#60, FPerson#75)
        .map{p -> FPerson#(p.age * 10)}
        .filter{p -> p.age > 50}
        .limit(1)
        .find{n -> True}
        .match{.some(n) -> n, .empty -> Error.msg ".findAndLimit was empty"},
      .first: mut Person -> Flow#[mut Person](FPerson#24, FPerson#60, FPerson#75)
        .map{p -> FPerson#(p.age * 10)}
        .filter{p -> p.age > 50}
        .first
        .match{.some(n) -> n, .empty -> Error.msg ".first was empty"},
      .firstAndLimit: mut Person -> Flow#[mut Person](FPerson#24, FPerson#60, FPerson#75)
        .map{p -> FPerson#(p.age * 10)}
        .filter{p -> p.age > 50}
        .limit(1)
        .first
        .match{.some(n) -> n, .empty -> Error.msg ".firstAndLimit was empty"},
      }
    """, """
    package test
    FPerson: { #(age: Nat): mut Person -> mut Person: {'self
      read .age: Nat -> age,
      read .str: Str -> "Person that is "+(self.age.str)+" years old",
      read ==(other: read Person): Bool -> self.age == (other.age),
      }}
    """, Base.mutBaseAliases); }

  @Test void optFlow() { ok(new Res(), """
    package test
    Test:Main {sys -> Block#
      .let f1 = {(Opts#[Int]+5).flow
        .map{n -> n * +10}
        .list
        }
      .let f2 = {mut Opt[Int].flow
        .map{n -> n * +10}
        .list
        }
      .assert{f1.get(0) == +50}
      .assert{f2.size == 0}
      .return {{}}
      }
    """, Base.mutBaseAliases); }

  @Test void flowLimit0() { ok(new Res("0", "", 0), """
    package test
    Test:Main {sys -> FIO#sys.println(
      Flow#[Int](+5, +10, +15)
        .limit(0)
        #(Flow.sum)
        .str
      )}
    """, Base.mutBaseAliases); }
  @Test void flowLimit1() { ok(new Res("5", "", 0), """
    package test
    Test:Main {sys -> FIO#sys.println(
      Flow#[Int](+5, +10, +15)
        .limit(1)
        #(Flow.sum)
        .str
      )}
    """, Base.mutBaseAliases); }
  @Test void flowLimit2() { ok(new Res("15", "", 0), """
    package test
    Test:Main {sys -> FIO#sys.println(
      Flow#[Int](+5, +10, +15)
        .limit(2)
        #(Flow.sum)
        .str
      )}
    """, Base.mutBaseAliases); }
  @Test void flowLimit2List() { ok(new Res("15", "", 0), """
    package test
    Test:Main {sys -> FIO#sys.println(
      List#[Int](+5, +10, +15).flow
        .limit(2)
        #(Flow.sum)
        .str
      )}
    """, Base.mutBaseAliases); }
  @Test void flowLimit3() { ok(new Res("30", "", 0), """
    package test
    Test:Main {sys -> FIO#sys.println(
      Flow#[Int](+5, +10, +15)
        .limit(3)
        #(Flow.sum)
        .str
      )}
    """, Base.mutBaseAliases); }
  @Test void flowLimit3List() { ok(new Res("30", "", 0), """
    package test
    Test:Main {sys -> FIO#sys.println(
      List#[Int](+5, +10, +15).flow
        .limit(3)
        #(Flow.sum)
        .str
      )}
    """, Base.mutBaseAliases); }
  @Test void flowLimit4() { ok(new Res("30", "", 0), """
    package test
    Test:Main {sys -> FIO#sys.println(
      Flow#[Int](+5, +10, +15)
        .limit(4)
        #(Flow.sum)
        .str
      )}
    """, Base.mutBaseAliases); }

  // Attempting to do a terminal operation on an infinite flow is always an exception.
  // The flow must be bounded by an intermediate operation before a terminal operation can be performed.
  @Test void flowFilter() { ok(new Res(), """
    package test
    Test:Main {sys -> Assert!(
      Flow#[Int](+5, +10, +15).filter{n -> n > +5}.size
      == 2
      )}
    """, Base.mutBaseAliases);}
  @Test void flowFilterPrintSize() { ok(new Res("2", "", 0), """
    package test
    Test:Main {sys -> Block#
      .let size = {Flow#[Nat](5, 10, 15).filter{n -> n > 5}.size}
      .return {FIO#sys.println(size.str)}
      }
    """, Base.mutBaseAliases);}
  @Test void flowFilterMap() { ok(new Res(), """
    package test
    Test:Main {sys -> Assert!(
      Flow#[Int](+5, +10, +15)
        .filter{n -> n > +5}
        .map{n -> n * +10}
        .max(base.CompareInts)!
      == +150
      )}
    """, Base.mutBaseAliases);}
  @Test void flowFilterMapIntEq1() { ok(new Res(), """
    package test
    Test:Main {sys -> (+150).assertEq("max assert failed",
      Flow#[Int](+5, +10, +15)
        .filter{n -> n > +5}
        .map{n -> n * +10}
        .max(base.CompareInts)!
      )}
    """, Base.mutBaseAliases);}
  // We prefer flowFilterMapIntEq1 because it is more clear that this test is of an assertion rather than of a flow.
  @Test void flowFilterMapIntEq2() { ok(new Res(), """
    package test
    Test:Main {sys -> Flow#[Int](+5, +10, +15)
      .filter{n -> n > +5}
      .map{n -> n * +10}
      .max(base.CompareInts)!
      .assertEq(+150)
      }
    """, Base.mutBaseAliases);}

  @Test void flowLet() { ok(new Res("35 40 45", "", 0), """
    package test
    Test:Main {sys -> FIO#sys.println(
      Flow#[Int](+5, +10, +15)
        .let[Int,Str] f2 = {f1 -> f1# #(Flow.sum)}
        .map{n -> n + f2}
        .map{n -> n.str}
        #(Flow.str " ")
        )
      }
    """, Base.mutBaseAliases);}
  @Test void flowLetMultiple() { ok(new Res("65 70 75", "", 0), """
    package test
    Test:Main {sys -> FIO#sys.println(
      Flow#[Int](+5, +10, +15)
        .let[Int,Str] f2 = {f1 -> (f1# #(Flow.sum)) + (f1# #(Flow.sum))}
        .map{n -> n + f2}
        .map{n -> n.str}
        #(Flow.str " ")
        )
      }
    """, Base.mutBaseAliases);}
  @Test void flowLetNoCollect() { ok(new Res("135 140 145", "", 0), """
    package test
    Test:Main {sys -> FIO#sys.println(
      Flow#[Int](+5, +10, +15)
        .let[Int,Str] f2 = {f -> f# #(Flow.sum)}
        .let[Int,Str] f3 = {_ -> +100}
        .map{n -> n + f2 + f3}
        .map{n -> n.str}
        #(Flow.str " ")
        )
      }
    """, Base.mutBaseAliases);}

  @Test void mutExtensionMethod() { ok(new Res("20 30", "", 0), """
    package test
    Test:Main {sys -> Block#
      .let[List[Int]] list = {List#[Int](+1, +2, +3)}
      .let[Str] myFlow = {list.flow
        .filter{n -> n > +1}
        #{f -> f.map{n -> n * +10}}
        .map{n -> n.str}
        #(Flow.str " ")
        }
      .return {FIO#sys.println(myFlow)}
      }
    """, Base.mutBaseAliases);}

  @Test void flowActor() { ok(new Res("", "", 0), """
    package test
    Test:Main {sys -> "42 5 42 10 500".assertEq(
      Flow#[Int](+5, +10, +15)
        // .actor requires an iso S for its initial value
        // The 3rd argument is optional
        .actor[mut Var[Int], Int](Var#[Int]+1, {downstream, state, n -> Block#
          .do {state := (state* + n)}
          .if {state.get > +16} .return{Block#(downstream#(+500), {})}
          .do {downstream#(+42)}
          .do {downstream#n}
          .return {{}}})
        .map{n -> n.str}
        #(Flow.str " ")
      )}
    """, Base.mutBaseAliases);}
  @Disabled @Test void flowActorMutRet() { ok(new Res("", "", 0), """
    package test
    Test:Main {sys -> "42 5 42 10 500".assertEq(
      Flow#[Int](5, 10, 15)
        // .actor requires an iso S for its initial value
        // The 3rd argument is optional
        .actorMut[mut Var[Int], Int](Var#[Int]1, {downstream, state, n -> Block#
          .do {state := (state* + n)}
          .if {state.get > 16} .return{Block#(downstream#500, {})}
          .do {downstream#42}
          .do {downstream#n}
          .return {{}}})
        .map{n -> n.str}
        #(Flow.str " ")
      )}
    """, Base.mutBaseAliases);}

  @Test void limitedFlowActorAfter() { ok(new Res("", "", 0), """
    package test
    Test:Main {sys -> "42 5".assertEq(
      Flow#[Nat](5, 10, 15)
        .actor[mut Var[Nat], Nat](Var#[Nat]1, {downstream, state, n -> Block#
          .do {state := (state* + n)}
          .if {state.get > 16} .return{Block#(downstream#500, {})}
          .do {downstream#42}
          .do {downstream#n}
          .return {{}}})
        .limit(2)
        .map{n -> n.str}
        #(Flow.str " ")
      )}
    """, Base.mutBaseAliases);}
  @Test void limitedFlowActorBefore() { ok(new Res("", "", 0), """
    package test
    Test:Main {sys -> "42 5 42 10".assertEq(
      Flow#[Int](+5, +10, +15)
        .limit(2)
        .actor[mut Var[Int], Int](Var#[Int]+1, {downstream, state, n -> Block#
          .do {state := (state* + n)}
          .if {state.get > +16} .return{Block#(downstream#(+500), {})}
          .do {downstream#(+42)}
          .do {downstream#n}
          .return {{}}})
        .map{n -> n.str}
        #(Flow.str " ")
      )}
    """, Base.mutBaseAliases);}
  @Test void flowActorNoConsumer() { ok(new Res(), """
    package test
    Test:Main {sys -> "42 5 42 10 500".assertEq(
      Flow#[Int](+5, +10, +15)
        .actor[mut Var[Int], Int](Var#[Int]+1, {downstream, state, n -> Block#
          .do {state := (state* + n)}
          .if {state.get > +16} .return{Block#(downstream#(+500), {})}
          .do {downstream#(+42)}
          .do {downstream#n}
          .return {{}}})
        .map{n -> n.str}
        #(Flow.str " ")
      )}
    """, Base.mutBaseAliases);}

  // We have this as a specialisation of .actor
  @Test void flowScan() { ok(new Res(), """
    package test
    Test:Main {sys -> "!5 !510 !51015".assertEq(
      Flow#[Nat](5, 10, 15)
        .scan[Str]("!", {acc, n -> acc + (n.str)})
        .map{n -> n.str}
        #(Flow.str " ")
      )}
    """, Base.mutBaseAliases);}
  @Test void flowScan2() { ok(new Res(), """
    package test
    Test:Main {sys -> "5 20 50".assertEq(
      Flow#[Int](+5, +10, +15)
        .scan[Int](+0, {acc, n -> acc + n})
        .scan[Int](+0, {acc, n -> acc + n})
        .map{n -> n.str}
        #(Flow.str " ")
      )}
    """, Base.mutBaseAliases);}

  // TODO: fix top level dec issue when wanting a mut instance of a top level lambda
  @Test void flowSimpleActorMutRet() { ok(new Res(), """
    package test
    Test:Main {sys -> "!5 !510 !51015".assertEq(
      Flow#[Nat](5, 10, 15)
        .scan[Str]("!", {acc, n -> acc + (n.str)})
        .map{n -> n.str}
        #(Flow.str " ")
      )}
    """, """
    package test
    FPerson:{
      #(age: Nat): mut Person -> Block#
        .let[mut Var[Nat]] age' = {Var#age}
        .return mut base.ReturnStmt[mut Person]{mut Person: Person{
          read .age: Nat -> age'.get,
          mut .age(n: Nat): Void -> age' := n,
          }}
      }
    """, Base.mutBaseAliases);}

//  @Test void flowActor() { ok(new Res(), "test.Test", """
//    package test
//    Test:Main {sys -> "5 10 500".assertEq(
//      Flow#[Int](5, 10, 15)
//        // .actor requires an iso S for its initial value
//        // This lambda has the type read ActorImpl[mut IsoPod[S], ... E, R]
//        .actor(Var#1, mut Consume[mut Var[Int]]{state->someRandom.set(state.get)}, {state, n -> Block#
//          .do {state.set(someMutList.get(0)!)}
//          .if {state.get > 10} .return {500}
//          .return {n})
//        .actor(Var#1, mut Consume[mut Var[Int]]{state->someRandom.set(state.get)}, {state, n -> Block#
//          .do {state.set(someMutList.get(0)!)}
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
//        .actor(Var#1, {state, n -> Block#
//          .if {state.get > 10} .return {500}
//          .do {state.put(n + state.get)}
//          .return {n})
//        .map{n -> n.str}
//        #(Flow.str " ")
//      )}
//    """, Base.mutBaseAliases);}

  @Test void flowActorMultiParallel() { ok(new Res(), """
    package test
    Test:Main {sys -> Block#
      .let[mut List[Int]] someMutList = {List#[Int](+30)}
      .return {"500 5 500 10".assertEq(
        Flow#[Int](+5, +10)
          .actor[mut Var[Int],Int](Var#[Int]+1, {next, state, n -> Block#
            .do {state.set(someMutList.get(0))}
            .if {state.get > +10} .do {next#(+500)}
            .do {next#n}
            .return {{}}
            })
          .map{n -> n.str}
          #(Flow.str " ")
        )}
      }
    """, Base.mutBaseAliases); }

  // If we do not offer any mapMut/mut lambdas, we can have parallelised read lambdas
  @Disabled @Test void mapAndMapMut() { ok(new Res(), """
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

  // TODO: Do we want .split?
  @Disabled @Test void flowSplit() { ok(new Res(), """
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
          .actor2(state, msg, actorVa3, downstream) -> ...,
          .actor3(state, msg, actorRef2, downstream) -> ...,
          } // has to internally represent the result as three downstream lists to have a deterministic ordering
        .map{n -> n + 5}
        .to list..
      )}
    """, Base.mutBaseAliases);}

  @Test void cannotUnwrapFlow() {fail("""
    In position [###]/Dummy0.fear:2:56
    [E48 privateTraitImplementation]
    The private trait base.flows._UnwrapFlowToken/0 cannot be implemented outside of its package.
    """, """
    package test
    Test:Main {sys -> Block#(Flow#[Int](5, 10, 15).unwrapOp({}))}
    """, Base.mutBaseAliases);}
}
