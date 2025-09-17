package codegen.js;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import utils.Base;

import static codegen.js.RunJsProgramTests.ok;
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
  @Test void conditional() { ok(new Res("1", "", 0), """
    package test
    alias base.Main as Main, alias base.True as True, alias base.Nat as Nat,
    Test:Main {sys -> sys.io.println(True ?[Nat] {.then -> 1, .else -> 3}.str)}
    """); }
  @Test void addition() { ok(new Res("7", "", 0), """
    package test
    alias base.Main as Main,
    Test:Main{sys -> sys.io.println((5 + 2) .str) }
    """);}
  @Test void subtraction() {
    ok(new Res("-2", "", 0), """
      package test
      alias base.Main as Main,
      Test:Main{sys -> sys.io.println(1 - (5 - 2) .str) }
      """);
  }
  @Test void multiplication() {
    ok(new Res("15", "", 0), """
      package test
      alias base.Main as Main,
      Test:Main{sys -> sys.io.println(1 + 2 * 5 .str) }
      """);
  }
  @Test void division() {
    ok(new Res("2.5", "", 0), """
      package test
      alias base.Main as Main,
      Test:Main{sys -> sys.io.println((5.0 / 2.0) .str) }
      """);
  }
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
  // Comparison operators
  @Test void greaterThan() {
    ok(new Res("True", "", 0), """
      package test
      alias base.Main as Main, alias base.Str as Str,
      Test:Main{sys -> sys.io.println((5 > 2) ?[Str] {.then -> "True", .else -> "False"}) }
      """);
  }
  @Test void lessThan() {
    ok(new Res("False", "", 0), """
      package test
      alias base.Main as Main, alias base.Str as Str,
      Test:Main{sys -> sys.io.println((5 < 2) ?[Str] {.then -> "True", .else -> "False"}) }
      """);
  }
  @Test void equality() {
    ok(new Res("True", "", 0), """
      package test
      alias base.Main as Main, alias base.Str as Str,
      Test:Main{sys -> sys.io.println((-1000 == -1000) ?[Str] {.then -> "True", .else -> "False"}) }
      """);
  }
  @Test void inequality() {
    ok(new Res("True", "", 0), """
      package test
      alias base.Main as Main, alias base.Str as Str,
      Test:Main{sys -> sys.io.println((5.1 != 5.0) ?[Str] {.then -> "True", .else -> "False"}) }
      """);
  }
  // Bitwise operations
  @Test void bitwiseAnd() {
    ok(new Res("0", "", 0), """
      package test
      alias base.Main as Main,
      Test:Main{sys -> sys.io.println((5 .bitwiseAnd 2) .str) }
      """);
  }
  @Test void bitwiseOr() {
    ok(new Res("7", "", 0), """
      package test
      alias base.Main as Main,
      Test:Main{sys -> sys.io.println((5 .bitwiseOr 2) .str) }
      """);
  }
  @Test void bitwiseXor() {
    ok(new Res("7", "", 0), """
      package test
      alias base.Main as Main,
      Test:Main{sys -> sys.io.println((5 .xor 2) .str) }
      """);
  }
  @Test void shiftLeft() {
    ok(new Res("20", "", 0), """
      package test
      alias base.Main as Main,
      Test:Main{sys -> sys.io.println((5 .shiftLeft 2) .str) }
      """);
  }
  @Test void shiftRight() {
    ok(new Res("1", "", 0), """
      package test
      alias base.Main as Main,
      Test:Main{sys -> sys.io.println((5 .shiftRight 2) .str) }
      """);
  }

  @Disabled void binaryAnd1() { ok(new Res("", "True", 1), """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert!(False, (True & True) .str, { Void }) }
    """);}

  @Disabled void numAssert() {
    ok(new Res("True", "", 0), """
      package test
      alias base.Main as Main, alias base.Int as Int, alias base.Nat as Nat, alias base.Byte as Byte,
      Test: Main{_ -> 5.assertEq(5)}
      """);
  }
  @Disabled void byteEq() {
    ok(new Res("True", "", 0), """
      package test
      alias base.Main as Main,
      Test: Main{_ -> "Hello!".utf8.get(0).assertEq(72 .byte)}
      """);
  }

  @Disabled void arithmeticOrderOfOperations() {
    ok(new Res("", "", 0), """
    package test
    Test:Main{ _ -> Block#
      .do{ Assert!(1 + 2 * 3 == 9, "order of ops left-associative", {{}}) }
      .do{ Assert!(1 + (2 * 3) == 7, "order of ops", {{}}) }
      .do{ Assert!(10 - 3 * 2 == 14, "subtraction left-associative", {{}}) }
      .do{ Assert!(8 / 2 + 1 == 5, "division and addition", {{}}) }
      .return{{}}
    }
  """, Base.mutBaseAliases);
  }
}
