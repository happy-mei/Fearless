package codegen.js;
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
  @Test void conditionals1() { ok(new Res("", "Assertion failed :(", 1), """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert!(420 > 9000, { Void }) }
    """);}
  @Test void conditionals2() { ok(new Res("", "Assertion failed :(", 1), """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert!("hi".size > 9000, { Void }) }
    """);}

  /** Number **/
  @Test void arithmeticOperations() {
    ok(new Res("", "", 0), """
    package test
    Test:Main{ _ -> Block#
      .do{ Assert!((5 + 2) == 7, "addition", {{}}) }
      .do{ Assert!((1 - (5 - 2)) == -2, "subtraction", {{}}) }
      .do{ Assert!((1 + 2 * 5) == 15, "multiplication", {{}}) }
      .do{ Assert!((5.0 / 2.0) == 2.5, "division", {{}}) }
      .do{ Assert!((5 % 2) == 1, "modulo", {{}}) }
      .do{ Assert!((2 ** 3) == 8, "exponent", {{}}) }
      .do{ Assert!((-5).abs == 5, "absolute value", {{}}) }
      .do{ Assert!((9).sqrt == 3, "square root", {{}}) }
      .return{{}}
    }
    """, Base.mutBaseAliases);
  }
  @Test void numberAssertions() {
    ok(new Res("", "", 0), """
    package test
    Test:Main{ _ -> +5.assertEq(+5) }
    """, Base.mutBaseAliases);
  }
  @Test void comparisonOperators() {
    ok(new Res("", "", 0), """
    package test
    Test:Main{ _ -> Block#
      .do{ Assert!((5 > 2), "greater than", {{}}) }
      .do{ Assert!(!(5 < 2), "less than", {{}}) }
      .do{ Assert!((-1000 == -1000), "equality", {{}}) }
      .do{ Assert!((5.1 != 5.0), "inequality", {{}}) }
      .return{{}}
    }
    """, Base.mutBaseAliases);
  }
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

  // Bitwise operations
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

  // print
  @Test void printConditional() { ok(new Res("1", "", 0), """
    package test
    alias base.Main as Main, alias base.True as True, alias base.Nat as Nat,
    Test:Main {sys -> sys.io.println(True ?[Nat] {.then -> 1, .else -> 3}.str)}
    """); }

  // string
  @Test void byteEq() {
    ok(new Res("True", "", 0), """
      package test
      Test: Main{_ -> "Hello!".utf8.get(0).assertEq(72 .byte)}
      """, Base.mutBaseAliases);
  }
}
