package codegen.java.tour;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import utils.Base;

import static utils.RunJava.Res;
import static codegen.java.RunJavaProgramTests.*;

public class Ex02CollectionsTest {
  @Disabled // TODO: No flows yet
  @Test void listNums() { ok(new Res("YAY!", "", 0), "test.Test", """
    package test
    Test:Main {sys -> FIO#sys.println(
      // List#[Int](5, 10, 15).str, how do we print these elements?
      List#[Int](5, 10, 15).flow
        .map{n -> n.str}
        #(Flow.join ", ")
      )}
    """, Base.mutBaseAliases);}
  @Test void addAndGetFromList() { ok(new Res("YAY!", "", 0), "test.Test", """
    package test
    Test:Main {sys -> Block#
      .var list = {List#[Str]}
      .do {list.add("YAY!")}
      .var yayOpt = {list.get(0u)}
//      .var[Str] yay = {yayOpt!}
      .return {FIO#sys.println(yayOpt!)}
      }
    """, Base.mutBaseAliases);}
  @Test void listSize() { ok(new Res("", "", 0), "test.Test", """
    package test
    Test:Main {sys -> Block#
      .var list = {List#[Int](5, 10, 15)}
      .do {list.add(20)}
      .assert({list.size == 4u})
      .return{Void}
      }
    """, Base.mutBaseAliases);}

  @Test void mapOfNameToAge() { ok(new Res("30", "", 0), "test.Test", """
    package test
    Test:Main {sys -> Block#
      // Map keys must be imm, element can be anything
      .var map = {Map#[Str,Int]({ .eq(k1, k2) -> k1 == k2, .hash(k) -> k.hash },
        "Alice", 24,
        "Bob", 30)}
      // or:
//      .var map = {Map.str(
//        "Alice", 24,
//        "Bob", 30)}
      .do {map.put("Bob", 30)}
      .var bobAgeOpt = {map.get("Bob")}
      .var bobAge = {bobAgeOpt!}
      .return {FIO#sys.println(bobAge.str)}
      }
    """, Base.mutBaseAliases);}

  // Our maps/sets are linked hashmaps/linked hashsets and thus have deterministic iteration order.
  @Test void mapFlow() { ok(new Res("30", "", 0), "test.Test", """
    package test
    Test:Main {sys -> Block#
      .var map = {Map.str("Alice",24, "Bob",30)}
      // if we have destructuring:
      // .var ageSum = {map.flow.map{[key, val] -> val}#(Flow.sum)}
      // .var ageSum = {map.flow.map{kv -> kv.val}#(Flow.sum)}
//      .return {map#(Map.str "\\n") // Map.str requires Map[Stringable, Stringable]
      .return {map.flow
        .map{kv -> kv.key + ": " + (kv.val.str)}
        #(Flow.str "\\n")
        }
      }
    """, Base.mutBaseAliases);}
}
