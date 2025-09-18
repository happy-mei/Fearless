package codegen.js;
import codegen.java.RunJavaProgramTests;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import utils.Base;

import static codegen.js.RunJsProgramTests.ok;
import static codegen.js.RunJsProgramTests.fail;
import static utils.RunOutput.Res;

public class TestJsProgram {
  @Test void emptyProgram() { ok(new Res("", "", 0), """
    package test
    alias base.Void as Void,
    Test:base.Main{ _ -> {} }
    """);}
  @Test void captureTest() { ok(new Res("", "", 0), """
    package test
    alias base.Main as Main, alias base.Void as Void,
    Test:Main{ _ -> {} }
    A:{ #: A -> A{ # -> A { # -> this } }# }
    """);}

  /** Assert! **/
  @Test void assertTrue() { ok(new Res("", "", 0), """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert!(True, { Void }) }
    """);}
  @Test void assertFalse() { ok(new Res("", "Assertion failed :(", 1), """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert!(False, { Void }) }
    """);}
  @Test void assertFalseMsg() { ok(new Res("", "power level less than 9000", 1), """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert!(False, "power level less than 9000", { Void }) }
    """);}

  /** Boolean **/
  @Test void falseToStr() { ok(new Res("", "False", 1), """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert!(False, Foo.bs(False), { Void }) }
    Foo:{ .bs(b: base.Bool): base.Str -> b.str }
    """);}
  @Test void trueToStr() { ok(new Res("", "True", 1), """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert!(False, Foo.bs(True), { Void }) }
    Foo:{ .bs(s: base.Stringable): base.Str -> s.str }
    """);}
  @Test void binaryAnd1() { ok(new Res("", "True", 1), """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert!(False, (True & True) .str, { Void }) }
    """);}
  @Test void binaryAnd2() { ok(new Res("", "False", 1), """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert!(False, (True & False) .str, { Void }) }
    """);}
  @Test void binaryAnd3() { ok(new Res("", "False", 1), """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert!(False, (False & False) .str, { Void }) }
    """);}
  @Test void binaryOr1() { ok(new Res("", "True", 1), """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert!(False, (True | True) .str, { Void }) }
    """);}
  @Test void binaryOr2() { ok(new Res("", "True", 1), """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert!(False, (True | False) .str, { Void }) }
    """);}
  @Test void binaryOr3() { ok(new Res("", "True", 1), """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert!(False, (False | True) .str, { Void }) }
    """);}
  @Test void binaryOr4() { ok(new Res("", "False", 1), """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert!(False, (False | False) .str, { Void }) }
    """);}
  @Test void compare1() { ok(new Res("", "Assertion failed :(", 1), """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert!(420 > 9000, { Void }) }
    """);}
  @Test void compare2() { ok(new Res("", "", 0), """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert!("hi".size == 2, { Void }) }
    """);}
  @Test void conditional() { ok(new Res("1", "", 0), """
    package test
    alias base.Main as Main, alias base.True as True, alias base.Nat as Nat,
    Test:Main {sys -> sys.io.println(True ?[Nat] {.then -> 1, .else -> 3}.str)}
    """); }
  @Test void nestedConditional() { ok(new Res("2", "", 0), """
    package test
    alias base.Main as Main, alias base.True as True, alias base.False as False, alias base.Nat as Nat,
    Test:Main {sys -> sys.io.println(True ?[Nat] {.then -> False ?[Nat] {.then -> 1, .else -> Block#[Foo,Nat](Foo, 2)}, .else -> 3}.str)}
    Foo: {}
    Block:{#[A:imm,R:imm](_: A, r: R): R -> r}
    """); }


  /** String **/
  // .str
  @Test void longToStr() { ok(new Res("", "123456789", 1), """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert!(False, 123456789 .str, { Void }) }
    """);}
  @Test void longLongToStr() { ok(new Res("", "9223372036854775807", 1), """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert!(False, 9223372036854775807 .str, { Void }) }
    """);}
  @Test void veryLongLongToStr() { ok(new Res("", "9223372036854775808", 1), """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert!(False, 9223372036854775808 .str, { Void }) }
    """);}
  @Test void veryLongLongIntFail() { fail("""
    [E31 invalidNum]
    The number +9223372036854775808 is not a valid Int
    """, """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert!(False, +9223372036854775808 .str, { Void }) }
    """);}
  @Test void veryLongLongNatFail() { fail("""
    [E31 invalidNum]
    The number 10000000000000000000000 is not a valid Nat
    """, """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert!(False, 10000000000000000000000 .str, { Void }) }
    """);}
  @Test void negativeToStr() { ok(new Res("", "-123456789", 1), """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert!(False, -123456789 .str, { Void }) }
    """);}

  /** utf8 **/
  @Disabled void strToBytes() {
    ok(new Res("72,101,108,108,111,33", "", 0), """
    package test
    Test: Main{sys -> sys.io.println("Hello!".utf8.flow.map{b -> b.str}.join ",")}
    """, Base.mutBaseAliases);}
  @Test void byteEq() {
    ok(new Res(), """
    package test
    Test: Main{_ -> "Hello!".utf8.get(0).assertEq(72 .byte)}
    """, Base.mutBaseAliases);}
  @Disabled void bytesToStr() {
    ok(new Res("Hello!", "", 0), """
    package test
    alias base.UTF8 as UTF8,
    Test: Main{sys -> sys.io.println(UTF8.fromBytes("Hello!".utf8)!)}
    """, Base.mutBaseAliases);}
  @Disabled void bytesToStrManual() {
    ok(new Res("AB", "", 0), """
    package test
    alias base.UTF8 as UTF8,
    Test: Main{sys -> sys.io.println(UTF8.fromBytes(List#(65 .byte, 66 .byte))!)}
    """, Base.mutBaseAliases);}

  /** Number **/
  @Test void addition() { ok(new Res("", "7", 1), """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert!(False, (5 + 2) .str, { Void }) }
    """);}
  @Test void addWithUnderscoreInt() { ok(new Res("", "500002", 1), """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert!(False, (5_00_000 + 2) .str, { Void }) }
    """);}
  @Test void addWithUnderscoreNat() { ok(new Res("", "500002", 1), """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert!(False, (5_00_000 + 2) .str, { Void }) }
    """);}
  @Test void addWithUnderscoreFloat() { ok(new Res("", "500002.6", 1), """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert!(False, (5_00_000.5 + 2.1) .str, { Void }) }
    """);}
  @Test void intDivByZero() { ok(new Res("", """
    Program crashed with: RangeError: Division by zero
        at test$$Test_0.$hash$imm$fun ([###])
        [###]
    """, 1), """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void, alias base.Block as Do,
    Test:Main{ _ -> Do#(5 / 0) }
    """);}
  @Test void subtraction() { ok(new Res("", "3", 1), """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert!(False, (5 - 2) .str, { Void }) }
    """);}
  @Test void subtractionNeg() { ok(new Res("", "-2", 1), """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert!(False, (-0 - +2) .str, { Void }) }
    """);}
  @Test void subtractionUnderflow() { ok(new Res("", "9223372036854775807", 1), """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert!(False, ((0 - 2) - 9223372036854775807) .str, { Void }) }
    """);}
  @Test void modulo() {
    ok(new Res("1", "", 0), """
      package test
      alias base.Main as Main,
      Test:Main{sys -> sys.io.println((5 % 2) .str) }
      """);
  }
  @Test void exponent() {
    ok(new Res("8", "", 0), """
      package test
      alias base.Main as Main,
      Test:Main{sys -> sys.io.println((2 ** 3) .str) }
      """);
  }
  @Test void absoluteValue() {
    ok(new Res("5", "", 0), """
      package test
      alias base.Main as Main,
      Test:Main{sys -> sys.io.println(-5.abs.str) }
      """);
  }
  @Test void sqrt() {
    ok(new Res("3", "", 0), """
      package test
      alias base.Main as Main,
      Test:Main{sys -> sys.io.println((9).sqrt.str) }
      """);
  }
  @Test void numOpsOrder() {
    ok(new Res("", "", 0), """
      package test
      Test:Main{ _ -> Block#
        .do{ Assert!(1 + 2 * 3 == 9, "order of ops left-associative", {{}}) }
        .do{ Assert!(1 + (2 * 3) == 7, "order of ops", {{}}) }
        .do{ Assert!(10 - 3 * 2 == 14, "subtraction left-associative", {{}}) }
        .do{ Assert!(8 / 2 + 1 == 5, "division and addition", {{}}) }
      .return{{}} }
      """, Base.mutBaseAliases); }
  @Test void comparisonOperators() {
    ok(new Res("", "", 0), """
    package test
    Test:Main{ _ -> Block#
      .do{ Assert!((5 > 2), "greater than", {{}}) }
      .do{ Assert!((-5000 < -1000), "less than", {{}}) }
      .do{ Assert!((0 == 0), "equality", {{}}) }
      .do{ Assert!((5.1 != 5.0), "inequality", {{}}) }
      .return{{}}
    }
    """, Base.mutBaseAliases);
  }
  @Test void bitwiseOperations() {
    ok(new Res("", "", 0), """
    package test
    Test:Main{ _ -> Block#
      .do{ Assert!((5.bitwiseAnd 2) == 0, "bitwise AND", {{}}) }
      .do{ Assert!((5.bitwiseOr 2) == 7, "bitwise OR", {{}}) }
      .do{ Assert!((5.xor 2) == 7, "bitwise XOR", {{}}) }
      .do{ Assert!((5.shiftLeft 2) == 20, "shift left", {{}}) }
      .do{ Assert!((5.shiftRight 2) == 1, "shift right", {{}}) }
      .return{{}}
    }
    """, Base.mutBaseAliases);
  }
  @Test void negativeNums() { ok(new Res("", "", 0), """
    package test
    Test:Main{ _ -> Block#
      .do{ Assert!(-5 == -5, "id", {{}}) }
      .do{ Assert!((-5 - -5) == +0, "subtraction 1", {{}}) }
      .do{ Assert!((-5 - +5) == -10, "subtraction 2", {{}}) }
      .do{ Assert!((-5 + +3) == -2, "addition 1", {{}}) }
      .do{ Assert!((-5 + +7) == +2, "addition 2", {{}}) }
      .do{ Assert!((+5 + -7) == -2, "addition 3", {{}}) }
      .return{{}}
      }
    """, Base.mutBaseAliases); }
  @Test void assertEq() { ok(new Res("", "", 0), """
    package test
    Test: Main{_ -> 5.assertEq(5)}
    """, Base.mutBaseAliases); }
  @Test void floats() { ok(new Res("", "", 0), """
    package test
    Test:Main{ _ -> Block#
      .do{ Assert!(-5.0 == -5.0, "id (neg)", {{}}) }
      .do{ Assert!((-5.0 - -5.0) == 0.0, "subtraction 1", {{}}) }
      .do{ Assert!((-5.0 - 5.0) == -10.0, "subtraction 2", {{}}) }
      .do{ Assert!((-5.0 + 3.0) == -2.0, "addition 1", {{}}) }
      .do{ Assert!((-5.0 + 7.0) == 2.0, "addition 2", {{}}) }
      .do{ Assert!((5.0 + -7.0) == -2.0, "addition 3", {{}}) }
      // pos
      .do{ Assert!(1.0 == 1.0, "id", {{}}) }
      .do{ Assert!(1.0 + 3.5 == 4.5, "addition 1 (pos)", {{}}) }
      .do{ Assert!((1.0).str == "1", "str pt 0", {{}}) }
      .do{ Assert!((1.5).str == "1.5", "str pt 5", {{}}) }
      .do{ Assert!((5.0 / 2.0) == 2.5, (5.0 / 2.0).str, {{}}) }
      .return{{}}
      }
    """, Base.mutBaseAliases); }

  /** Block **/
  @Test void lazyCall() { ok(new Res("hey", "", 0), """
    package test
    Test: Main{sys -> Block#
      .let[Void] x = {UnrestrictedIO#sys.println("hey")}
      .return {Void}
      }
    """, Base.mutBaseAliases); }

  /** List **/
  @Test void LListItersIterImm() { ok(new Res("", "", 0), """
    package test
    Test:Main{ _ -> Block#
      .let[LList[Int]] l1 = { LList[Int] + +35 + +52 + +84 + +14 }
      .do{ Assert!(l1.head! == (l1.iter.next!), "sanity", {{}}) }
      .do{ Assert!((l1.iter.find{n -> n > +60})! == +84, "find some", {{}}) }
      .do{ Assert!((l1.iter.find{n -> n > +100}).isEmpty, "find empty", {{}}) }
      .do{ Assert!((l1.iter
                      .map{n -> n * +10}
                      .find{n -> n == +140})
                      .isSome,
        "map", {{}})}
      .return{{}}
      }
    """, Base.mutBaseAliases); }
  @Test void LListItersIterMut() { ok(new Res("", "", 0), """
    package test
    Test:Main{ _ -> Block#
      .let[mut LList[Int]] l1 = { mut LList[Int] +[] +35 +[] +52 +[] +84 +[] +14 }
      .do{ Assert!(l1.head! == (l1.iter.next!), "sanity", {{}}) }
      .do{ Assert!((l1.iter.find{n -> n > +60})! == +84, "find some", {{}}) }
      .do{ Assert!((l1.iter.find{n -> n > +100}).isEmpty, "find empty", {{}}) }
      .do{ Assert!(l1.iter
                      .map{n -> n * +10}
                      .find{n -> n == +140}
                      .isSome,
        "map", {{}})}
      .return{{}}
      }
    """, Base.mutBaseAliases); }
  @Test void paperExamplePrintIter() { ok(new Res("350,350,350,140,140,140", "", 0), """
    package test
    alias base.Nat as Nat, alias base.Str as Str,
    alias base.List as List, alias base.Block as Block,
    alias base.caps.UnrestrictedIO as UnrestrictedIO, alias base.caps.IO as IO,

    Test :base.Main{ sys -> Block#
        .let l1 = { List#[Nat](35, 52, 84, 14) }
        .assert{l1.iter
          .map{n -> n * 10}
          .find{n -> n == 140}
          .isSome}
        .let[Str] msg = {l1.iter
          .filter{n -> n < 40}
          .flatMap{n -> List#(n, n, n).iter}
          .map{n -> n * 10}
          .str({n -> n.str}, ",")}
        .let io = {UnrestrictedIO#sys}
        .return {io.println(msg)}
        // prints 350,350,350,140,140,140
    }
    """);}
  // as
  @Disabled void soundAsIdFnUList() { ok(new Res("1,2,3", "", 0), """
    package test
    Test: Main{sys -> Block#
      .let[mut List[Nat]] l = {List#(1, 2, 3)}
      .let[List[Nat]] l2 = {l.as{::}}
      .do {l.add(4)}
      .do {sys.io.println(l2.flow.map{::str}.join ",")}
      .return {{}}
      }
    """, Base.mutBaseAliases);}
  @Disabled void soundAsUList() { ok(new Res("1,2,3", "", 0), """
    package test
    Test: Main{sys -> Block#
      .let[mut List[Nat]] l = {List#(1, 2, 3)}
      .let[List[Str]] l2 = {l.as{::str}}
      .do {l.add(4)}
      .do {sys.io.println(l2.flow.join ",")}
      .return {{}}
      }
    """, Base.mutBaseAliases);}

  /** Flow **/
  @Disabled void flow() {
    ok(new Res("Transformed List: 6, 7, 12, 13, 18", "", 0), """
    package test
    
    Test: Main{
      #(sys) -> Block#
        .let[mut List[Int]] numbers = {List#(+1, +2, +3, +4, +5, +6, +7, +8, +9, +10)}

        // Create a flow from the list
        .let[mut Flow[Int]] flow = {numbers.flow}

        // Apply various flow operations
        .let[mut Flow[Int]] transformedFlow = {flow
          .map{n -> n * +2} // Multiply each number by 2
//          .filter{n -> n % +3 == +0} // Keep only numbers divisible by 3
//          .flatMap{n -> Flow#(n, n + +1)} // For each number, create a flow of the number and the number + 1
//          .limit(5) // Limit the flow to the first 5 elements
        }

        // Collect the results into a list
        .let[mut List[Int]] resultList = {transformedFlow.list}

        // Print the results
        .do {sys.io.println("Transformed List: " + (resultList.flow.map{num -> num.str}.join ", "))}

        .return {Void}
    }
    """, Base.mutBaseAliases);}

  /** sys **/
  @Test void sysPrint() { ok(new Res("Hello, World!", "", 0), """
    package test
    alias base.Main as Main, alias base.True as True, alias base.Nat as Nat,
    Test:Main {sys -> sys.io.println("Hello, World!")}
    """); }
  @Test void println() { ok(new Res("Hello, World!", "", 0), """
    package test
    alias base.Main as Main, alias base.Void as Void, alias base.Block as Block,
    Test:Main{ s -> Block#
      .let({ base.caps.UnrestrictedIO#s }, { io, s' -> s'.return{ io.println "Hello, World!" } })
      }
    """);}
  @Test void printlnSugar() { ok(new Res("Hello, World!", "", 0), """
    package test
    alias base.Main as Main, alias base.Void as Void, alias base.Block as Block,
    alias base.caps.IO as IO, alias base.caps.UnrestrictedIO as UnrestrictedIO,
    Test:Main{ s -> Block#
      .let[mut IO] io = { UnrestrictedIO#s }
      .return{ io.println("Hello, World!") }
      }
    """); }
  @Test void printlnDeeper() { ok(new Res("IO begets IO", "", 0), """
    package test
    alias base.Main as Main, alias base.Void as Void, alias base.Block as Block,
    alias base.caps.IO as IO, alias base.caps.UnrestrictedIO as UnrestrictedIO,
    Test:Main{ s -> Block#
      .let[mut IO] io = { UnrestrictedIO#s }
      .return{ Block#
        .let io2 = { base.caps.UnrestrictedIO#s }
        .return{ io2.println("IO begets IO") }
        }
      }
    """); }
  @Test void print() { ok(new Res("Hello, World!", "", 0), """
    package test
    alias base.Main as Main, alias base.Void as Void, alias base.Block as Block,
    alias base.caps.IO as IO, alias base.caps.UnrestrictedIO as UnrestrictedIO,
    Test:Main{ s -> Block#
      .let[mut IO] io = { UnrestrictedIO#s }
      .do{ io.print("Hello") }
      .return{ io.print(", World!") }
      }
    """); }
  @Test void printlnErr() { ok(new Res("", "Hello, World!", 0), """
    package test
    alias base.Main as Main, alias base.Void as Void, alias base.Block as Block,
    alias base.caps.IO as IO, alias base.caps.UnrestrictedIO as UnrestrictedIO,
    Test:Main{ s -> Block#
      .let[mut IO] io = { UnrestrictedIO#s }
      .return{ io.printlnErr("Hello, World!") }
      }
    """); }
  @Test void printErr() { ok(new Res("", "Hello, World!", 0), """
    package test
    alias base.Main as Main, alias base.Void as Void, alias base.Block as Block,
    alias base.caps.IO as IO, alias base.caps.UnrestrictedIO as UnrestrictedIO,
    Test:Main{ s -> Block#
      .let[mut IO] io = { UnrestrictedIO#s }
      .do{ io.printErr("Hello") }
      .return{ io.printErr(", World!") }
      }
    """); }
  @Test void printlnShareLent() { ok(new Res("Hello, World!", "", 0), """
    package test
    alias base.Main as Main, alias base.Void as Void, alias base.Block as Block,
    alias base.caps.IO as IO, alias base.caps.UnrestrictedIO as UnrestrictedIO,
    Test:Main{ s -> Block#
      .let[mut IO] io = { UnrestrictedIO#s }
      .return{ Usage#io }
      }
    Usage:{
      #(io: mutH IO): Void -> io.println("Hello, World!"),
      }
    """); }

  /** IsoPod **/
  @Disabled void shouldReadFullIsoPod() { ok(new Res("hi", "", 0), """
    package test
    Test:Main{ s ->
      Try#[Str]{ Block#
        .let[mut IsoPod[Str]] pod = { IsoPod#[Str] iso "hi" }
        .return{pod!}
        }.run{
          .ok(msg) -> UnrestrictedIO#s.println(msg),
          .info(info) -> UnrestrictedIO#s.printlnErr(info.str),
        }
      }
    """, Base.mutBaseAliases); }
}