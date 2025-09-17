package codegen.js;
import codegen.java.RunJavaProgramTests;
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
  @Test void compare2() { ok(new Res("", "Assertion failed :(", 1), """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert!("hi".size > 9000, { Void }) }
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

  /** Number **/
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
  @Test void byteEq() {
    ok(new Res("True", "", 0), """
      package test
      Test: Main{_ -> "Hello!".utf8.get(0).assertEq(72 .byte)}
      """, Base.mutBaseAliases);
  }


  // print
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
}