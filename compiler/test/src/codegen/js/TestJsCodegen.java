package codegen.js;

import main.CompilerFrontEnd;
import main.InputOutput;
import main.Main;
import main.js.LogicMainJs;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import utils.Base;
import utils.Err;
import utils.RunOutput;

import java.util.Arrays;
import java.util.List;

import static codegen.js.RunJsProgramTests.ok;

public class TestJsCodegen {
  JsProgram getCode(String... content) {
    Main.resetAll();
    var vb = new CompilerFrontEnd.Verbosity(false, false, CompilerFrontEnd.ProgressVerbosity.None);
    var main = LogicMainJs.of(InputOutput.programmaticAuto(Arrays.asList(content)), vb);
    var fullProgram = main.parse(); // builds the AST from source files (base + test)
    main.wellFormednessFull(fullProgram); // checks semantic correctness of the high-level AST
    var program = main.inference(fullProgram); // runs type inference, producing a typed AST
    main.wellFormednessCore(program); // validates the core typed program
    var resolvedCalls = main.typeSystem(program); // resolves method calls, producing a mapping of call sites
    var mir = main.lower(program, resolvedCalls); // AST → MIR
    var code = main.codeGeneration(mir);
    return code;
  }
  void ok(String expected, String fileName, String... content) {
    assert content.length > 0 : "Content must not be empty";
    JsProgram code = getCode(content);
    var fileCode = code.files().stream()
      .filter(f -> f.toUri().toString().endsWith(fileName))
      .map(JsFile::code)
      .findFirst().orElseThrow();
    Err.strCmp(expected, fileCode);
//    Err.strCmp(normalizeWhitespace(expected), normalizeWhitespace(fileCode));
  }
//  String normalizeWhitespace(String str) {
//    // Remove all newlines and any surrounding whitespace, but preserve other spaces
//    return str.replaceAll("\\s*\\n+\\s*", "\n").trim();
//  }
  void okList(List<String> expected, List<String> fileName, String... content) {
    assert content.length > 0 : "Content must not be empty";
    assert expected.size() == fileName.size() : "Expected and fileName lists must have the same size";
    JsProgram code = getCode(content);
    for (int i = 0; i < expected.size(); i++) {
      String exp = expected.get(i);
      String fName = fileName.get(i);
      var fileCode = code.files().stream()
        .filter(f -> f.toUri().toString().endsWith(fName))
        .map(JsFile::code)
        .findFirst().orElseThrow();
      Err.strCmp(exp, fileCode);
    }
  }
  @Test void emptyProgram() {
    ok("""
      import { base$$Void_0 } from "../base/index.js";
      
      export class test$$Test_0 {
        static $hash$imm$fun(fear[###]$_m$, $this) {
          return base$$Void_0.$self;
        }
      }
      
      export class test$$Test_0Impl {
        $hash$imm(fear[###]$_m$) { return test$$Test_0.$hash$imm$fun(fear[###]$_m$, this); }
      }
      
      test$$Test_0.$self = new test$$Test_0Impl();
      """,
      "test/Test_0.js",
      """
      package test
      alias base.Void as Void,
      Test:base.Main{ _ -> {} }
      """
    );
  }

  @Test void extendsClass() {
    okList(List.of("""
    import { rt$$Str } from "../rt-js/Str.js";
    
    export class test$$A_0 {
      static ma$imm$fun($this) {
        return rt$$Str.fromJsStr("A");
      }
    }
    
    export class test$$A_0Impl {
      ma$imm() { return test$$A_0.ma$imm$fun(this); }
    }
    
    test$$A_0.$self = new test$$A_0Impl();
    """, """
    import { rt$$Str } from "../rt-js/Str.js";
    import { test$$A_0 } from "../test/A_0.js";
    
    export class test$$B_0 {
      static mb$imm$fun($this) {
        return rt$$Str.fromJsStr("B").$plus$imm($this.ma$imm());
      }
    }
    
    export class test$$B_0Impl {
      mb$imm() { return test$$B_0.mb$imm$fun(this); }
      ma$imm() { return test$$A_0.ma$imm$fun(this); }
    }
    
    test$$B_0.$self = new test$$B_0Impl();
    """),
      List.of("test/A_0.js", "test/B_0.js"),
      """
      package test
      alias base.Str as Str,
      A:{.ma:Str -> "A" }
      B:A {.mb:Str-> "B" + (this.ma) }
      """);
  }

  @Test void extendsClass2() {
    okList(List.of("""
    import { rt$$Str } from "../rt-js/Str.js";
    
    export class test$$A_0 {
      static ma$imm$fun($this) {
        return rt$$Str.fromJsStr("A");
      }
    }
    """, """
    import { rt$$Str } from "../rt-js/Str.js";
    
    export class test$$B_0 {
      static mb$imm$fun($this) {
        return rt$$Str.fromJsStr("B").$plus$imm($this.ma$imm());
      }
    }
    """, """
    import { rt$$Str } from "../rt-js/Str.js";
    import { test$$A_0 } from "../test/A_0.js";
    import { test$$B_0 } from "../test/B_0.js";
    
    export class test$$C_0 {
      static mc$imm$fun($this) {
        return rt$$Str.fromJsStr("C").$plus$imm($this.mb$imm());
      }
      static mk$imm$fun($this) {
        return $this.mk$imm();
      }
    }
    
    export class test$$C_0Impl {
      mk$imm() { return test$$C_0.mk$imm$fun(this); }
      mb$imm() { return test$$B_0.mb$imm$fun(this); }
      ma$imm() { return test$$A_0.ma$imm$fun(this); }
      mc$imm() { return test$$C_0.mc$imm$fun(this); }
    }
    
    test$$C_0.$self = new test$$C_0Impl();
    """, """
    import { test$$C_0 } from "../test/C_0.js";
    
    export class test$$User_0 {
      static foo$imm$fun($this) {
        return test$$C_0.$self.mc$imm();
      }
    }
    
    export class test$$User_0Impl {
      foo$imm() { return test$$User_0.foo$imm$fun(this); }
    }
    
    test$$User_0.$self = new test$$User_0Impl();
    """),
      List.of("test/A_0.js", "test/B_0.js", "test/C_0.js", "test/User_0.js"),
      """
      package test
      alias base.Str as Str,
      A:{.ma:Str -> "A", .mk:Str }
      B:A {.mb:Str-> "B" + (this.ma) }
      C:B{ .mc:Str-> "C" + (this.mb), .mk->this.mk}
      User: {
        .foo:Str->C.mc()
      }
      """);
  }

  @Test void bool() {
    okList(List.of("""
    import { base$$False_0, base$$True_0 } from "../base/index.js";
    
    export class test$$Test_0 {
      static m$imm$fun($this) {
        return base$$True_0.$self;
      }
      static b$imm$fun($this) {
        return base$$False_0.$self;
      }
    }
    
    export class test$$Test_0Impl {
      m$imm() { return test$$Test_0.m$imm$fun(this); }
      b$imm() { return test$$Test_0.b$imm$fun(this); }
    }
    
    test$$Test_0.$self = new test$$Test_0Impl();
    """, """
    import { base$$Bool_0, base$$False_0 } from "../base/index.js";
    import { rt$$Str } from "../rt-js/Str.js";
    
    export class base$$True_0 {
      static and$imm$fun(b_m$, $this) {
        return b_m$;
      }
      static $ampersand$ampersand$imm$fun(b_m$, $this) {
        return b_m$.$hash$mut();
      }
      static or$imm$fun(b_m$, $this) {
        return $this;
      }
      static $pipe$pipe$imm$fun(b_m$, $this) {
        return $this;
      }
      static not$imm$fun($this) {
        return base$$False_0.$self;
      }
      static if$imm$fun(f_m$, $this) {
        return f_m$.then$mut();
      }
      static str$read$fun($this) {
        return rt$$Str.fromJsStr("True");
      }
      static toImm$read$fun($this) {
        return base$$True_0.$self;
      }
    }
    
    export class base$$True_0Impl {
      str$read() { return base$$True_0.str$read$fun(this); }
      if$imm(f_m$) { return base$$True_0.if$imm$fun(f_m$, this); }
      $ampersand$ampersand$imm(b_m$) { return base$$True_0.$ampersand$ampersand$imm$fun(b_m$, this); }
      $question$imm(f_m$) { return base$$Bool_0.$question$imm$fun(f_m$, this); }
      $pipe$pipe$imm(b_m$) { return base$$True_0.$pipe$pipe$imm$fun(b_m$, this); }
      $pipe$imm(b_m$) { return base$$Bool_0.$pipe$imm$fun(b_m$, this); }
      match$imm(m_m$) { return base$$Bool_0.match$imm$fun(m_m$, this); }
      not$imm() { return base$$True_0.not$imm$fun(this); }
      or$imm(b_m$) { return base$$True_0.or$imm$fun(b_m$, this); }
      $ampersand$imm(b_m$) { return base$$Bool_0.$ampersand$imm$fun(b_m$, this); }
      and$imm(b_m$) { return base$$True_0.and$imm$fun(b_m$, this); }
      toImm$read() { return base$$True_0.toImm$read$fun(this); }
    }
    
    base$$True_0.$self = new base$$True_0Impl();
    """),
      List.of("test/Test_0.js", "base/True_0.js"),
      """
      package test
      alias base.Bool as Bool, alias base.True as True, alias base.False as False,
      Test:{ .m:Bool-> True, .b:Bool-> False }
      """);
  }


  @Test void strUtf8() {
    ok("""
    import { rt$$Str } from "../rt-js/Str.js";
    
    export class test$$Test_0 {
      static $hash$imm$fun(sys_m$, $this) {
        return sys_m$.io$mut().print$mut(rt$$Str.fromJsStr(Number((rt$$Str.fromJsStr("Hello!").utf8$imm().get$imm(0n) & 0xFF) & 0xFFn).toString()));
      }
    }
    
    export class test$$Test_0Impl {
      $hash$imm(sys_m$) { return test$$Test_0.$hash$imm$fun(sys_m$, this); }
    }
    
    test$$Test_0.$self = new test$$Test_0Impl();
    """, "test/Test_0.js", """
    package test
    alias base.Main as Main,
    Test: Main{sys -> sys.io.print("Hello!".utf8())}
    """);
  }

  @Test void numberStr() {
    ok("""
      import { rt$$Str } from "../rt-js/Str.js";
      
      export class test$$A_0 {
        static a$imm$fun($this) {
          return rt$$Str.fromJsStr((-5n).toString());
        }
      }
      
      export class test$$A_0Impl {
        a$imm() { return test$$A_0.a$imm$fun(this); }
      }
      
      test$$A_0.$self = new test$$A_0Impl();
      """, "test/A_0.js",
      """
      package test
      alias base.Main as Main, alias base.Str as Str,
      A:{.a:Str -> -5.str }
      """);
  }
  @Test void veryLongLongToStr() { ok("""
    import { base$$Assert_0, base$$False_0 } from "../base/index.js";
    import { rt$$Str } from "../rt-js/Str.js";
    import { test$$Fear718$_0 } from "../test/Fear718$_0.js";
    
    export class test$$Test_0 {
      static $hash$imm$fun(fear[###]$_m$, $this) {
        return base$$Assert_0.$self.$exclamation$imm(base$$False_0.$self,rt$$Str.fromJsStr((-9223372036854775808n).toString()),test$$Fear718$_0.$self);
      }
    }
    
    export class test$$Test_0Impl {
      $hash$imm(fear[###]$_m$) { return test$$Test_0.$hash$imm$fun(fear[###]$_m$, this); }
    }
    
    test$$Test_0.$self = new test$$Test_0Impl();
    """, "test/Test_0.js", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert!(False, 9223372036854775808 .str, { Void }) }
    """);
  }

  @Test void numberStrAbs() {
    ok("""
    import { rt$$Str } from "../rt-js/Str.js";
    
    export class test$$Test_0 {
      static $hash$imm$fun(sys_m$, $this) {
        return sys_m$.io$mut().println$mut(rt$$Str.fromJsStr((BigInt(-999n < 0n ? -(-999n) : -999n)).toString()));
      }
    }
    
    export class test$$Test_0Impl {
      $hash$imm(sys_m$) { return test$$Test_0.$hash$imm$fun(sys_m$, this); }
    }
    
    test$$Test_0.$self = new test$$Test_0Impl();
    """, "test/Test_0.js",
      """
      package test
      alias base.Main as Main, alias base.Str as Str,
      Test:Main{sys -> sys.io.println(-999 .abs.str) }
      """);
  }

  @Test void numArithmeticOps() {
    ok("""
    export class test$$Test_0 {
      static a$imm$fun($this) {
        return (BigInt((BigInt((BigInt((1n + 2n)) * 3n)) / 1n)) - 4n);
      }
      static b$imm$fun($this) {
        return (1n + (2n * 3n));
      }
      static mod$imm$fun($this) {
        return (10n % 3n);
      }
      static pow$imm$fun($this) {
        return (2n ** 5n);
      }
      static divInt$imm$fun($this) {
        return (-7n / 2n);
      }
      static divFloat$imm$fun($this) {
        return (7.0 / 2.0);
      }
    }
    
    export class test$$Test_0Impl {
      divInt$imm() { return test$$Test_0.divInt$imm$fun(this); }
      mod$imm() { return test$$Test_0.mod$imm$fun(this); }
      divFloat$imm() { return test$$Test_0.divFloat$imm$fun(this); }
      pow$imm() { return test$$Test_0.pow$imm$fun(this); }
      b$imm() { return test$$Test_0.b$imm$fun(this); }
      a$imm() { return test$$Test_0.a$imm$fun(this); }
    }
    
    test$$Test_0.$self = new test$$Test_0Impl();
    """,
      "test/Test_0.js",
      """
      package test
      alias base.Nat as Nat, alias base.Int as Int, alias base.Float as Float, alias base.Bool as Bool,
      Test:{
        .a:Nat-> 1 + 2 * 3 / 1 - 4,
        .b:Nat-> 1 + (2 * 3),
        .mod:Nat -> 10 % 3,
        .pow:Nat -> 2 ** 5,
        .divInt:Int-> -7 / +2,
        .divFloat:Float -> 7.0 / 2.0,
      }
      """);
  }

  @Test void subtractionUnderflow() { ok("""
    import { rt$$Numbers } from "../rt-js/Numbers.js";
    import { rt$$Str } from "../rt-js/Str.js";
    
    export class test$$Test_0 {
      static a$imm$1$fun($this) {
        return rt$$Str.fromJsStr(rt$$Numbers.toNat64(BigInt(rt$$Numbers.toNat64((rt$$Numbers.toNat64(BigInt(rt$$Numbers.toNat64((rt$$Numbers.toNat64(0n) - rt$$Numbers.toNat64(2n))))) - rt$$Numbers.toNat64(9223372036854775807n))))));
      }
    }
    
    export class test$$Test_0Impl {
      a$imm$0() { return test$$Test_0.a$imm$1$fun(this); }
    }
    
    test$$Test_0.$self = new test$$Test_0Impl();
    """, "test/Test_0.js", """
    package test
    alias base.Str as Str,
    Test:{ .a:Str -> ((0 - 2) - 9223372036854775807) .str }
    """);}

  @Test void numOps() {
    ok("""
    import { rt$$Numbers } from "../rt-js/Numbers.js";
    
    export class test$$Test_0 {
      static a$imm$fun($this) {
        return 1n;
      }
      static abs$imm$fun($this) {
        return -5n < 0n ? -(-5n) : -5n;
      }
      static sqrt$imm$fun($this) {
        return Math.sqrt(9.0);
      }
      static isNaN$imm$fun($this) {
        return rt$$Numbers.toBool(isNaN(parseFloat((0.0 / 0.0))));
      }
      static isNegInf$imm$fun($this) {
        return rt$$Numbers.toBool(0.0 === -Infinity);
      }
      static round$imm$fun($this) {
        return Math.round(2.7);
      }
    }
    
    export class test$$Test_0Impl {
      isNegInf$imm() { return test$$Test_0.isNegInf$imm$fun(this); }
      abs$imm() { return test$$Test_0.abs$imm$fun(this); }
      isNaN$imm() { return test$$Test_0.isNaN$imm$fun(this); }
      round$imm() { return test$$Test_0.round$imm$fun(this); }
      sqrt$imm() { return test$$Test_0.sqrt$imm$fun(this); }
      a$imm() { return test$$Test_0.a$imm$fun(this); }
    }
    
    test$$Test_0.$self = new test$$Test_0Impl();
    """,
      "test/Test_0.js",
      """
      package test
      alias base.Nat as Nat, alias base.Int as Int, alias base.Bool as Bool, alias base.Float as Float, alias base.Void as Void,
      Test:{
        .a:Int-> 1 .int,
        .abs:Int -> (-5) .abs,
        .sqrt:Float -> 9.0 .sqrt,
        .isNaN:Bool  -> (0.0 / 0.0) .isNaN,
        .isNegInf:Bool -> 0.0 .isNegInfinity,
        .round:Int -> 2.7 .round,
      }
      """);
  }

  @Test void numAssert() {
    okList(List.of("""
    import { base$$_NatAssertionHelper_0 } from "../base/index.js";
    
    export class test$$Test_0 {
      static $hash$imm$fun(fear[###]$_m$, $this) {
        return base$$_NatAssertionHelper_0.assertEq$imm$fun(5n, 5n, null);
      }
    }
    
    export class test$$Test_0Impl {
      $hash$imm(fear[###]$_m$) { return test$$Test_0.$hash$imm$fun(fear[###]$_m$, this); }
    }
    
    test$$Test_0.$self = new test$$Test_0Impl();
    """, """
    import { base$$Assert_0, base$$Fear2252$_0, base$$Fear2257$_0 } from "../base/index.js";
    import { rt$$Numbers } from "../rt-js/Numbers.js";
    import { rt$$Str } from "../rt-js/Str.js";
    
    export class base$$_NatAssertionHelper_0 {
      static assertEq$imm$fun(expected_m$, actual_m$, $this) {
        return base$$Assert_0.$self.$exclamation$imm(rt$$Numbers.toBool((BigInt(expected_m$)) === (actual_m$)),rt$$Str.fromJsStr("Expected: ").$plus$imm(rt$$Str.fromJsStr((BigInt(expected_m$)).toString())).$plus$imm(rt$$Str.fromJsStr("\\nActual: ")).$plus$imm(rt$$Str.fromJsStr((BigInt(actual_m$)).toString())),base$$Fear2252$_0.$self);
      }
      static assertEq$imm$fun(expected_m$, actual_m$, message_m$, $this) {
        return base$$Assert_0.$self.$exclamation$imm(rt$$Numbers.toBool((BigInt(expected_m$)) === (actual_m$)),message_m$.$plus$imm(rt$$Str.fromJsStr("\\nExpected: ")).$plus$imm(rt$$Str.fromJsStr((BigInt(expected_m$)).toString())).$plus$imm(rt$$Str.fromJsStr("\\nActual: ")).$plus$imm(rt$$Str.fromJsStr((BigInt(actual_m$)).toString())),base$$Fear2257$_0.$self);
      }
    }
    
    export class base$$_NatAssertionHelper_0Impl {
      assertEq$imm(expected_m$, actual_m$) { return base$$_NatAssertionHelper_0.assertEq$imm$fun(expected_m$, actual_m$, this); }
      assertEq$imm(expected_m$, actual_m$, message_m$) { return base$$_NatAssertionHelper_0.assertEq$imm$fun(expected_m$, actual_m$, message_m$, this); }
    }
    
    base$$_NatAssertionHelper_0.$self = new base$$_NatAssertionHelper_0Impl();
    """),
      List.of("test/Test_0.js", "base/_NatAssertionHelper_0.js"),
      """
      package test
      alias base.Main as Main, alias base.Void as Void,
      Test: Main{_ -> 5.assertEq(5)}
      A: {
        .assertOk:Void -> (5 .assertEq 5),
        .assertFail:Void -> (3 .assertEq(4, "should fail")),
      }
      """);
  }
  @Test void byteEq() {
    ok("""
    import { base$$_ByteAssertionHelper_0 } from "../base/index.js";
    import { rt$$Str } from "../rt-js/Str.js";
    
    export class test$$Test_0 {
      static $hash$imm$fun(fear[###]$_m$, $this) {
        return base$$_ByteAssertionHelper_0.assertEq$imm$fun((rt$$Str.fromJsStr("Hello!").utf8$imm().get$imm(0n) & 0xFFn), (72n & 0xFFn), null);
      }
    }
    
    export class test$$Test_0Impl {
      $hash$imm(fear[###]$_m$) { return test$$Test_0.$hash$imm$fun(fear[###]$_m$, this); }
    }
    
    test$$Test_0.$self = new test$$Test_0Impl();
    """, "test/Test_0.js", """
    package test
    alias base.Main as Main,
    Test: Main{_ -> "Hello!".utf8.get(0).assertEq(72 .byte)}
    """);
  }

  @Test void numOpsCompBit() {
    ok("""
    import { rt$$Numbers } from "../rt-js/Numbers.js";
    
    export class test$$Test_0 {
      static gt$imm$fun($this) {
        return rt$$Numbers.toBool((5n) > (3n));
      }
      static lt$imm$fun($this) {
        return rt$$Numbers.toBool((2n) < (7n));
      }
      static and$imm$fun($this) {
        return (6n & 3n);
      }
      static or$imm$fun($this) {
        return 6n | 3n;
      }
      static xor$imm$fun($this) {
        return 6n ^ 3n;
      }
      static shl$imm$fun($this) {
        return ((1n << 3n) & ((1n<<64n)-1n));
      }
      static shr$imm$fun($this) {
        return ((8n & ((1n<<64n)-1n)) >> 2n);
      }
    }
    
    export class test$$Test_0Impl {
      gt$imm() { return test$$Test_0.gt$imm$fun(this); }
      xor$imm() { return test$$Test_0.xor$imm$fun(this); }
      shl$imm() { return test$$Test_0.shl$imm$fun(this); }
      or$imm() { return test$$Test_0.or$imm$fun(this); }
      shr$imm() { return test$$Test_0.shr$imm$fun(this); }
      and$imm() { return test$$Test_0.and$imm$fun(this); }
      lt$imm() { return test$$Test_0.lt$imm$fun(this); }
    }
    
    test$$Test_0.$self = new test$$Test_0Impl();
    """,
      "test/Test_0.js",
      """
      package test
      alias base.Nat as Nat, alias base.Int as Int, alias base.Bool as Bool, alias base.Float as Float, alias base.Void as Void,
      Test:{
        .gt:Bool -> 5 > 3,
        .lt:Bool -> 2 < 7,
        .and:Int -> +6 .bitwiseAnd +3,
        .or:Int -> +6 .bitwiseOr +3,
        .xor:Nat -> 6 .xor 3,
        .shl:Nat -> 1 .shiftLeft 3,
        .shr:Nat -> 8 .shiftRight 2,
      }
      """);
  }

  @Test void direction() {
    okList(List.of("""
    
    export class test$$Direction_0 {
      static reverse$imm$fun($this) {
        return $this.turn$imm().turn$imm();
      }
    }
    """, """
    import { test$$Direction_0 } from "../test/Direction_0.js";
    import { test$$East_0 } from "../test/East_0.js";
    
    export class test$$North_0 {
      static turn$imm$fun($this) {
        return test$$East_0.$self;
      }
    }
    
    export class test$$North_0Impl {
      reverse$imm() { return test$$Direction_0.reverse$imm$fun(this); }
      turn$imm() { return test$$North_0.turn$imm$fun(this); }
    }
    
    test$$North_0.$self = new test$$North_0Impl();
    """),
      List.of("test/Direction_0.js", "test/North_0.js"),
      """
      package test
      Direction: {
        .turn: Direction,
        .reverse: Direction -> this.turn.turn,
      }
      North: Direction {.turn -> East, }
      East : Direction {.turn -> South,}
      South: Direction {.turn -> West, }
      West : Direction {.turn -> North,}
      """);
  }

  @Test void tanks() {
    okList(List.of("""
      import { test$$Tank_0Impl } from "../test/Tank_0.js";
      
      export class test$$Tanks_0 {
        static of$imm$3$fun(heading_m$, aiming_m$, $this) {
          return new test$$Tank_0Impl(aiming_m$, heading_m$);
        }
      }
      
      export class test$$Tanks_0Impl {
        of$imm$2(heading_m$, aiming_m$) { return test$$Tanks_0.of$imm$3$fun(heading_m$, aiming_m$, this); }
      }
      
      test$$Tanks_0.$self = new test$$Tanks_0Impl();
      """, """
      export class test$$Tank_0 {
        static heading$imm$2$fun(fear[###]$_m$, heading_m$) {
          return heading_m$;
        }
        static aiming$imm$2$fun(fear[###]$_m$, aiming_m$) {
          return aiming_m$;
        }
      }
      
      export class test$$Tank_0Impl {
        constructor(aiming_m$, heading_m$) {
          this.aiming_m$ = aiming_m$;
          this.heading_m$ = heading_m$;
        }
        aiming$imm$0() { return test$$Tank_0.aiming$imm$2$fun(this, this.aiming_m$); }
        heading$imm$0() { return test$$Tank_0.heading$imm$2$fun(this, this.heading_m$); }
      }
      """),
      List.of("test/Tanks_0.js", "test/Tank_0.js"),
      """
      package test
      Tanks: { .of(heading: Direction, aiming: Direction): Tank ->
        Tank: { .heading: Direction -> heading, .aiming: Direction -> aiming, }
      }
      Direction: {}
      """);
  }

  @Test void tanksAnonymous() {
    okList(List.of("""
      import { test$$Fear714$_0Impl } from "../test/Fear714$_0.js";
      
      export class test$$Tanks_0 {
        static of$imm$3$fun(heading_m$, aiming_m$, $this) {
          return new test$$Fear714$_0Impl(aiming_m$, heading_m$);
        }
      }
      
      export class test$$Tanks_0Impl {
        of$imm$2(heading_m$, aiming_m$) { return test$$Tanks_0.of$imm$3$fun(heading_m$, aiming_m$, this); }
      }
      
      test$$Tanks_0.$self = new test$$Tanks_0Impl();
      """, """
      export class test$$Fear714$_0 {
        static heading$imm$2$fun(fear[###]$_m$, heading_m$) {
          return heading_m$;
        }
        static aiming$imm$2$fun(fear[###]$_m$, aiming_m$) {
          return aiming_m$;
        }
      }
      
      export class test$$Fear714$_0Impl {
        constructor(aiming_m$, heading_m$) {
          this.aiming_m$ = aiming_m$;
          this.heading_m$ = heading_m$;
        }
        aiming$imm$0() { return test$$Fear714$_0.aiming$imm$2$fun(this, this.aiming_m$); }
        heading$imm$0() { return test$$Fear714$_0.heading$imm$2$fun(this, this.heading_m$); }
      }
      """, """
      export class test$$Tank_0 {
      }
      """),
      List.of("test/Tanks_0.js", "test/Fear714$_0.js", "test/Tank_0.js"),
      """
      package test
      Tank: { .heading: Direction, .aiming: Direction, }
      Tanks: { .of(heading: Direction, aiming: Direction): Tank ->
        { .heading -> heading, .aiming -> aiming, }
        }
      Direction: {}
      """);
  }

  @Test void tanksWithSugar() {
    okList(List.of("""
      import { test$$Direction_0 } from "../test/Direction_0.js";
      import { test$$East_0 } from "../test/East_0.js";
      
      export class test$$North_0 {
        static turn$imm$1$fun($this) {
          return test$$East_0.$self;
        }
      }
      
      export class test$$North_0Impl {
        reverse$imm$0() { return test$$Direction_0.reverse$imm$1$fun(this); }
        turn$imm$0() { return test$$North_0.turn$imm$1$fun(this); }
      }
      
      test$$North_0.$self = new test$$North_0Impl();
      """, """
      import { test$$Fear[###]$_0Impl } from "../test/Fear[###]$_0.js";
      
      export class test$$Tanks_0 {
        static $hash$imm$3$fun(heading_m$, aiming_m$, $this) {
          return new test$$Fear[###]$_0Impl(aiming_m$, heading_m$);
        }
      }
      
      export class test$$Tanks_0Impl {
        $hash$imm$2(heading_m$, aiming_m$) { return test$$Tanks_0.$hash$imm$3$fun(heading_m$, aiming_m$, this); }
      }
      
      test$$Tanks_0.$self = new test$$Tanks_0Impl();
      """),
      List.of("test/North_0.js", "test/Tanks_0.js"),
      """
      package test
      Direction: {
        .turn: Direction,
        .reverse: Direction -> this.turn.turn,
        }
      North: Direction { East  }
      East : Direction { South }
      South: Direction { West  }
      West : Direction { North }
      
      Tank: {.heading: Direction, .aiming: Direction, }
      Tanks: { #(heading: Direction, aiming: Direction): Tank ->
        { .heading -> heading, .aiming -> aiming, }
      }
      """);
  }

  @Test void rotation() {
    okList(List.of("""
    
    export class test$$Rotation_0 {
    }
    """, """
    export class test$$Turn270_0 {
      static $hash$imm$fun(fear[###]$_m$, $this) {
        return fear[###]$_m$.turn$imm().turn$imm().turn$imm();
      }
    }
    
    export class test$$Turn270_0Impl {
      $hash$imm(fear[###]$_m$) { return test$$Turn270_0.$hash$imm$fun(fear[###]$_m$, this); }
    }
    
    test$$Turn270_0.$self = new test$$Turn270_0Impl();
    """),
      List.of("test/Rotation_0.js", "test/Turn270_0.js"),
      """
      package test
      Direction: { .turn: Direction }
      Rotation: { #(d: Direction):Direction }
      Turn0: Rotation{::}
      Turn90: Rotation{::turn}
      Turn180: Rotation{::turn.turn}
      Turn270: Rotation{::turn.turn.turn}
      """);
  }

  @Test void rotationPlus() {
    okList(List.of("""
    import { test$$Fear[###]$_0Impl } from "../test/Fear[###]$_0.js";
    
    export class test$$Rotation_0 {
      static $plus$imm$fun(r_m$, $this) {
        return new test$$Fear[###]$_0Impl(r_m$, $this);
      }
    }
    """),
      List.of("test/Rotation_0.js"),
      """
        package test
        Direction: { .turn: Direction }
        Rotation: {
          #(d: Direction):Direction,
          +(r: Rotation): Rotation-> { d -> this#( r#(d) ) }
        }
        """);
  }

  @Test void tankTurn() {
    okList(List.of("""
    import { test$$Fear[###]$_0Impl } from "../test/Fear[###]$_0.js";
    
    export class test$$Tanks_0 {
      static $hash$imm$fun(heading_m$, aiming_m$, $this) {
        return new test$$Fear[###]$_0Impl(aiming_m$, heading_m$);
      }
    }
    
    export class test$$Tanks_0Impl {
      $hash$imm(heading_m$, aiming_m$) { return test$$Tanks_0.$hash$imm$fun(heading_m$, aiming_m$, this); }
    }
    
    test$$Tanks_0.$self = new test$$Tanks_0Impl();
    """, """
    import { test$$Tanks_0 } from "../test/Tanks_0.js";
    
    export class test$$Tank_0 {
      static turnTurret$imm$fun(r_m$, $this) {
        return test$$Tanks_0.$self.$hash$imm($this.heading$imm(),r_m$.$hash$imm($this.aiming$imm()));
      }
    }
    """),
      List.of("test/Tanks_0.js", "test/Tank_0.js"),
      """
      package test
      Direction: { .turn: Direction }
      Rotation: { #(d: Direction):Direction }
      Tanks: { #(heading: Direction, aiming: Direction): Tank ->
        { .heading -> heading, .aiming -> aiming, }
      }
      Tank: {
        .heading: Direction,
        .aiming: Direction,
        .turnTurret(r: Rotation): Tank ->
          Tanks#(this.heading, r#(this.aiming))
      }
      """);
  }

  @Test void directionWithPoint() {
    okList(List.of("""
    import { test$$East_0 } from "../test/East_0.js";
    import { test$$Points_0 } from "../test/Points_0.js";
    
    export class test$$North_0 {
      static turn$imm$fun($this) {
        return test$$East_0.$self;
      }
      static point$imm$fun($this) {
        return test$$Points_0.$self.$hash$imm(-1n,0n);
      }
    }
    
    export class test$$North_0Impl {
      point$imm() { return test$$North_0.point$imm$fun(this); }
      turn$imm() { return test$$North_0.turn$imm$fun(this); }
    }
    
    test$$North_0.$self = new test$$North_0Impl();
    """, """
    import { test$$Fear[###]$_0Impl } from "../test/Fear[###]$_0.js";
    
    export class test$$Points_0 {
      static $hash$imm$fun(x_m$, y_m$, $this) {
        return new test$$Fear[###]$_0Impl(x_m$, y_m$);
      }
    }
    
    export class test$$Points_0Impl {
      $hash$imm(x_m$, y_m$) { return test$$Points_0.$hash$imm$fun(x_m$, y_m$, this); }
    }
    
    test$$Points_0.$self = new test$$Points_0Impl();
    """, """
    import { test$$Points_0 } from "../test/Points_0.js";
    
    export class test$$Point_0 {
      static $plus$imm$fun(other_m$, $this) {
        return test$$Points_0.$self.$hash$imm((BigInt(other_m$.x$imm()) + $this.x$imm()),(BigInt(other_m$.y$imm()) + $this.y$imm()));
      }
      static move$imm$fun(d_m$, $this) {
        return $this.$plus$imm(d_m$.point$imm());
      }
    }
    """),
      List.of("test/North_0.js", "test/Points_0.js", "test/Point_0.js"),
      """
      package test
      alias base.Int as Int,
      Point: {
        .x: Int, .y: Int,
        +(other: Point): Point ->
          Points#(other.x + (this.x), other.y + (this.y)),
        .move(d: Direction): Point -> this + ( d.point ),
        }
      Points: {#(x: Int, y: Int): Point -> { .x -> x, .y -> y } }
      
      Direction: { .turn: Direction, .point: Point, }
      North: Direction {.turn -> East,  .point -> Points#(-1, +0), }
      East : Direction {.turn -> South, .point -> Points#(+0, +1), }
      South: Direction {.turn -> West,  .point -> Points#(+1, +0), }
      West : Direction {.turn -> North, .point -> Points#(+0, -1), }
      """);
  }

  @Test void genericMethod() {
    okList(List.of("""
    export class test$$Left_0 {
      static choose$imm$fun(l_m$, r_m$, $this) {
        return l_m$;
      }
    }
    
    export class test$$Left_0Impl {
      choose$imm(l_m$, r_m$) { return test$$Left_0.choose$imm$fun(l_m$, r_m$, this); }
    }
    
    test$$Left_0.$self = new test$$Left_0Impl();
    """, """
    export class test$$Fork_0 {
    }
    """),
      List.of("test/Left_0.js", "test/Fork_0.js"),
      """
      package test
      Fork : { .choose[Val](leftVal: Val, rightVal: Val): Val, }
      Left : Fork{ l,r -> l }
      Right: Fork{ l,r -> r }
      """);
  }

  @Test void genericType() {
    okList(List.of("""
    export class test$$Right_0 {
      static choose$imm$fun(fear[###]$_m$, $this) {
        return fear[###]$_m$.right$imm();
      }
    }
    
    export class test$$Right_0Impl {
      choose$imm(fear[###]$_m$) { return test$$Right_0.choose$imm$fun(fear[###]$_m$, this); }
    }
    
    test$$Right_0.$self = new test$$Right_0Impl();
    """, """
    export class test$$Fork_0 {
    }
    """, """
    export class test$$LeftRight_1 {
    }
    """),
      List.of("test/Right_0.js",  "test/Fork_0.js", "test/LeftRight_1.js"),
      """
      package test
      Fork : { .choose[Val](leftRight: LeftRight[Val]): Val, }
      LeftRight[LR]: { .left: LR, .right: LR }
      Left : Fork{::left}
      Right: Fork{::right}
      """);
  }

  @Test void ifThenElse() {
    ok("""
    import { base$$True_0 } from "../base/index.js";
    import { rt$$Str } from "../rt-js/Str.js";
    
    export class test$$Bot_0 {
      static message$imm$fun(s_m$, $this) {
        return (s_m$.$equals$equals$imm(rt$$Str.fromJsStr("hello")) == base$$True_0.$self ? rt$$Str.fromJsStr("Hi, I'm Bot; how can I help you?") : (s_m$.$equals$equals$imm(rt$$Str.fromJsStr("bye")) == base$$True_0.$self ? rt$$Str.fromJsStr("goodbye!") : rt$$Str.fromJsStr("I don't understand")));
      }
    }
    
    export class test$$Bot_0Impl {
      message$imm(s_m$) { return test$$Bot_0.message$imm$fun(s_m$, this); }
    }
    
    test$$Bot_0.$self = new test$$Bot_0Impl();
    """,
      "test/Bot_0.js",
      """
      package test
      alias base.Str as Str,
      Bot: {
        .message(s: Str): Str ->
          // Outer Check: Is the message `hello`?
          (s == `hello`).if {
            .then -> `Hi, I'm Bot; how can I help you?`,
            .else ->
              // Inner Check: Is the message "bye"?
              (s == `bye`).if {
                .then -> `goodbye!`,
                .else -> `I don't understand`
              } //End of inner ThenElse
          } //End of outer ThenElse
      }
      """);
  }

  @Test void questionMark() {
    ok("""
    import { base$$True_0 } from "../base/index.js";
    
    export class test$$Test_0 {
      static m$imm$fun(b_m$, $this) {
        return (b_m$ == base$$True_0.$self ? $this : $this.m$imm(b_m$));
      }
    }
    
    export class test$$Test_0Impl {
      m$imm(b_m$) { return test$$Test_0.m$imm$fun(b_m$, this); }
    }
    
    test$$Test_0.$self = new test$$Test_0Impl();
    """,
      "test/Test_0.js",
      """
      package test
      Test:{ .m(b: base.Bool):Test-> b ? {.then->this, .else->this.m(b), } }
      """);
  }

  @Test void questionMarkStr() {
    ok("""
      import { base$$True_0 } from "../base/index.js";
      import { rt$$Numbers } from "../rt-js/Numbers.js";
      import { rt$$Str } from "../rt-js/Str.js";
      
      export class test$$Test_0 {
        static m$imm$fun($this) {
          return (rt$$Numbers.toBool((5n) > (2n)) == base$$True_0.$self ? rt$$Str.fromJsStr("True") : rt$$Str.fromJsStr("False"));
        }
      }
      
      export class test$$Test_0Impl {
        m$imm() { return test$$Test_0.m$imm$fun(this); }
      }
      
      test$$Test_0.$self = new test$$Test_0Impl();
      """,
      "test/Test_0.js",
      """
      package test
      alias base.Str as Str,
      Test:{ .m:Str-> (5 > 2) ?[Str] {.then -> "True", .else -> "False", } }
      """);
  }

  @Test void optional() {
    okList(List.of("""
    import { test$$Fear[###]$_0 } from "../test/Fear[###]$_0.js";
    
    export class test$$Test_0 {
      static getAge$imm$fun(p_m$, $this) {
        return p_m$.match$imm(test$$Fear[###]$_0.$self);
      }
    }
    
    export class test$$Test_0Impl {
      getAge$imm(p_m$) { return test$$Test_0.getAge$imm$fun(p_m$, this); }
    }
    
    test$$Test_0.$self = new test$$Test_0Impl();
    """),
      List.of("test/Test_0.js"),
      """
      package test
      alias base.Nat as Nat,
      alias base.Opt as Opt,
      Person: { .age: Nat }
      Test:{
        .getAge(p: Opt[Person]): Nat-> p.match{  // age or zero
          .empty    -> 0,
          .some(p') -> p'.age, //Yes, p' is a valid parameter name
        }
      }
      """);
  }

  @Test void blockLetDoRet() {
    ok("""
    import { base$$Void_0 } from "../base/index.js";
    import { test$$ForceGen_0 } from "../test/ForceGen_0.js";
    
    export class test$$Test_0 {
      static $hash$imm$fun(fear[###]$_m$, $this) {
        let n_m$ = 5n;
    var doRes1 = test$$ForceGen_0.$self.$hash$imm();
    return base$$Void_0.$self;
      }
    }
    
    export class test$$Test_0Impl {
      $hash$imm(fear[###]$_m$) { return test$$Test_0.$hash$imm$fun(fear[###]$_m$, this); }
    }
    
    test$$Test_0.$self = new test$$Test_0Impl();
    """,
      "test/Test_0.js",
      """
      package test
      alias base.Int as Int, alias base.Str as Str, alias base.Block as Block, alias base.Void as Void,
      Test:base.Main {_ -> Block#
       .let[Int] n = {+5}
       .do {ForceGen#}
       .return {Void}
       }
      ForceGen: {#: Void -> {}}
      """);
  }

  @Test void blockVarDoRet() {
    ok("""
    import { base$$Vars_0, base$$Void_0 } from "../base/index.js";
    import { rt$$Str } from "../rt-js/Str.js";
    import { test$$ForceGen_0 } from "../test/ForceGen_0.js";
    
    export class test$$Test_0 {
      static $hash$imm$fun(fear[###]$_m$, $this) {
        var n_m$ = base$$Vars_0.$self.$hash$imm(rt$$Str.fromJsStr("Hi"));
    var doRes1 = test$$ForceGen_0.$self.$hash$imm();
    return base$$Void_0.$self;
      }
    }
    
    export class test$$Test_0Impl {
      $hash$imm(fear[###]$_m$) { return test$$Test_0.$hash$imm$fun(fear[###]$_m$, this); }
    }
    
    test$$Test_0.$self = new test$$Test_0Impl();
    """,
      "test/Test_0.js",
      """
      package test
      alias base.Str as Str, alias base.Block as Block, alias base.Void as Void,
      Test:base.Main {_ -> Block#
       .var[Str] n = {"Hi"}
       .do {ForceGen#}
       .return {Void}
      }
      ForceGen: {#: Void -> {}}
      """);
  }

  @Test void blockError() {
    ok("""
    import { base$$Infos_0, base$$True_0, base$$Vars_0, base$$Void_0 } from "../base/index.js";
    import { rt$$Numbers } from "../rt-js/Numbers.js";
    import { rt$$Str } from "../rt-js/Str.js";
    import { test$$ForceGen_0 } from "../test/ForceGen_0.js";

    export class test$$Test_0 {
      static $hash$imm$fun(fear[###]$_m$, $this) {
        let n_m$ = 5n;
    let n2_m$ = 10n;
    var n3_m$ = base$$Vars_0.$self.$hash$imm(15n);
    if (rt$$Numbers.toBool((BigInt(n3_m$.get$mut())) === ((BigInt(BigInt(n_m$)) + n2_m$))) == base$$True_0.$self) { r$$Error.throwFearlessError(base$$Infos_0.$self.msg$imm(rt$$Str.fromJsStr("oh no")));
     }
    var doRes1 = test$$ForceGen_0.$self.$hash$imm();
    return base$$Void_0.$self;
      }
    }

    export class test$$Test_0Impl {
      $hash$imm(fear[###]$_m$) { return test$$Test_0.$hash$imm$fun(fear[###]$_m$, this); }
    }

    test$$Test_0.$self = new test$$Test_0Impl();
    """,
      "test/Test_0.js",
      """
      package test
      alias base.Int as Int, alias base.Str as Str, alias base.Block as Block, alias base.Void as Void,
      Test:base.Main {_ -> Block#
       .openIso[Int] n = (iso +5)
       .let[Int] n2 = {+10}
       .var[Int] n3 = {+15}
       .if {n3.get == (n.int + n2)} .error {base.Infos.msg "oh no"}
       .do {ForceGen#}
       .return {Void}
       }
      ForceGen: {#: Void -> {}}
      """);
  }

  @Test void boolExprBlock() {
    ok("""
    import { base$$True_0, base$$Void_0 } from "../base/index.js";
    
    export class test$$Test_0 {
      static $hash$imm$fun(fear[###]$_m$, $this) {
        return (base$$True_0.$self == base$$True_0.$self ? (() => {
    return base$$Void_0.$self})() : (() => {
    return base$$Void_0.$self})());
      }
    }
    
    export class test$$Test_0Impl {
      $hash$imm(fear[###]$_m$) { return test$$Test_0.$hash$imm$fun(fear[###]$_m$, this); }
    }
    
    test$$Test_0.$self = new test$$Test_0Impl();
    """,
      "test/Test_0.js",
      """
      package test
      alias base.Block as Block, alias base.Void as Void,
      alias base.True as True,
      Test: base.Main{_ -> True ? {
        .then -> Block#.return {Void},
        .else -> Block#.return {Void},
      }}
      """);
  }

  @Test void sysIOPrint() {
    ok("""
      import { rt$$Str } from "../rt-js/Str.js";
      
      export class test$$Test_0 {
        static $hash$imm$fun(sys_m$, $this) {
          return sys_m$.io$mut().println$mut(rt$$Str.fromJsStr("Hello World"));
        }
      }
      
      export class test$$Test_0Impl {
        $hash$imm(sys_m$) { return test$$Test_0.$hash$imm$fun(sys_m$, this); }
      }
      
      test$$Test_0.$self = new test$$Test_0Impl();
      """,
      "test/Test_0.js",
      """
        package test
        alias base.Main as Main,
        Test: Main{sys -> sys.io.println("Hello World") }
        """);
  }

  @Test void listAndFlowMap() {
    okList(List.of("""
    import { base$$flows$$_DataParallelFlow_0 } from "../base/flows/index.js";
    import { rt$$flows } from "../rt-js/flows.js";
    import { test$$Fear[###]$_0 } from "../test/Fear[###]$_0.js";
    
    export class test$$Names_0 {
      static $hash$imm$fun(ps_m$, $this) {
        return rt$$flows.FlowCreator.fromFlow(base$$flows$$_DataParallelFlow_0.$self, ps_m$.flow$imm()).map$mut(test$$Fear[###]$_0.$self).list$mut();
      }
    }
    
    export class test$$Names_0Impl {
      $hash$imm(ps_m$) { return test$$Names_0.$hash$imm$fun(ps_m$, this); }
    }
    
    test$$Names_0.$self = new test$$Names_0Impl();
    """),
      List.of("test/Names_0.js"),
      """
      package test
      alias base.List as List, alias base.Str as Str,
      
      Person:{  .name:Str,  }
      Names:{  #(ps: List[Person]): List[Str] -> ps.flow.map{::name }.list   }
      """);
  }

  @Test void listAndFlowFilter() {
    okList(List.of("""
    import { base$$flows$$_DataParallelFlow_0 } from "../base/flows/index.js";
    import { rt$$flows } from "../rt-js/flows.js";
    import { test$$Fear[###]$_0 } from "../test/Fear[###]$_0.js";
    
    export class test$$Doctors_0 {
      static $hash$imm$fun(ps_m$, $this) {
        return rt$$flows.FlowCreator.fromFlow(base$$flows$$_DataParallelFlow_0.$self, ps_m$.flow$imm()).filter$mut(test$$Fear[###]$_0.$self).list$mut();
      }
    }
    
    export class test$$Doctors_0Impl {
      $hash$imm(ps_m$) { return test$$Doctors_0.$hash$imm$fun(ps_m$, this); }
    }
    
    test$$Doctors_0.$self = new test$$Doctors_0Impl();
    """),
      List.of("test/Doctors_0.js"),
      """
      package test
      alias base.List as List, alias base.Str as Str,
      
      Person:{  .name:Str,  }
      Doctors:{ #(ps: List[Person]): List[Person] ->ps.flow.filter{::name.startsWith `Dr.` }.list }
      """);
  }

  @Test void listAndFlowFlatMapAny() {
    okList(List.of("""
    import { base$$flows$$_DataParallelFlow_0 } from "../base/flows/index.js";
    import { rt$$flows } from "../rt-js/flows.js";
    import { test$$Fear[###]$_0 } from "../test/Fear[###]$_0.js";
    
    export class test$$AllCats_0 {
      static $hash$imm$fun(ps_m$, $this) {
        return rt$$flows.FlowCreator.fromFlow(base$$flows$$_DataParallelFlow_0.$self, ps_m$.flow$imm()).flatMap$mut(test$$Fear[###]$_0.$self).list$mut();
      }
    }
    
    export class test$$AllCats_0Impl {
      $hash$imm(ps_m$) { return test$$AllCats_0.$hash$imm$fun(ps_m$, this); }
    }
    
    test$$AllCats_0.$self = new test$$AllCats_0Impl();
    """, """
    import { base$$flows$$_DataParallelFlow_0 } from "../base/flows/index.js";
    import { rt$$flows } from "../rt-js/flows.js";
    import { test$$Fear[###]$_0 } from "../test/Fear[###]$_0.js";

    export class test$$Sad_0 {
      static $hash$imm$fun(ps_m$, $this) {
        return rt$$flows.FlowCreator.fromFlow(base$$flows$$_DataParallelFlow_0.$self, ps_m$.flow$imm()).any$mut(test$$Fear[###]$_0.$self);
      }
    }

    export class test$$Sad_0Impl {
      $hash$imm(ps_m$) { return test$$Sad_0.$hash$imm$fun(ps_m$, this); }
    }

    test$$Sad_0.$self = new test$$Sad_0Impl();
    """),
      List.of("test/AllCats_0.js", "test/Sad_0.js"),
      """
      package test
      alias base.List as List, alias base.Str as Str, alias base.Bool as Bool,
      
      Person:{  .name:Str, .cats: List[Cat],  }
      Cat:{ .name: Str, }
      AllCats:{  #(ps: List[Person]): List[Cat] -> ps.flow.flatMap{::cats.flow }.list  }
      Sad :{ #(ps: List[Person]): Bool -> ps.flow.any{::cats.isEmpty  } }
      """);
  }

  @Test void listBlock() {
    ok("""
    import { base$$List_0, base$$Void_0 } from "../base/index.js";
    import { rt$$Str } from "../rt-js/Str.js";
    
    export class test$$Test_0 {
      static $hash$imm$fun(s_m$, $this) {
        let l_m$ = base$$List_0.$self.$hash$imm(rt$$Str.fromJsStr("A"),rt$$Str.fromJsStr("B"),rt$$Str.fromJsStr("C"),rt$$Str.fromJsStr("D"),rt$$Str.fromJsStr("E"));
    let l2_m$ = base$$List_0.$self.$hash$imm(12n,13n,14n);
    return base$$Void_0.$self;
      }
    }
    
    export class test$$Test_0Impl {
      $hash$imm(s_m$) { return test$$Test_0.$hash$imm$fun(s_m$, this); }
    }
    
    test$$Test_0.$self = new test$$Test_0Impl();
    """,
      "test/Test_0.js",
      """
      package test
      alias base.List as List, alias base.Int as Int, alias base.Block as Block, alias base.Main as Main, alias base.Str as Str,
      Test:Main{ s -> Block#
        .let[List[Str]] l = {List#("A", "B", "C", "D", "E")}
        .let[List[Int]] l2 = { List#(+12, +13, +14) }
        .return {{}}
      }
      """);
  }

  @Test void getListByF() {
    ok("""
    import { base$$List_0 } from "../base/index.js";
    
    export class test$$GetList_0 {
      static $hash$read$fun($this) {
        return base$$List_0.$self.$hash$imm().$plus$mut(1n).$plus$mut(2n).$plus$mut(3n).$plus$mut(4n);
      }
    }
    
    export class test$$GetList_0Impl {
      $hash$read() { return test$$GetList_0.$hash$read$fun(this); }
    }
    
    test$$GetList_0.$self = new test$$GetList_0Impl();
    """,
      "test/GetList_0.js",
      """
      package test
      alias base.List as List, alias base.F as F, alias base.Nat as Nat,
      
      GetList: F[mut List[Nat]]{List# + 1 + 2 + 3 + 4}
      """);
  }

  @Test void listAs() {
    ok("""
    import { base$$flows$$_DataParallelFlow_0 } from "../base/flows/index.js";
    import { base$$List_0, base$$Void_0 } from "../base/index.js";
    import { rt$$Str } from "../rt-js/Str.js";
    import { rt$$flows } from "../rt-js/flows.js";
    import { test$$Fear[###]$_0 } from "../test/Fear[###]$_0.js";
    
    export class test$$Test_0 {
      static $hash$imm$fun(sys_m$, $this) {
        let l_m$ = base$$List_0.$self.$hash$imm(1n,2n,3n);
    let l2_m$ = l_m$.as$read(test$$Fear[###]$_0.$self);
    var doRes1 = l_m$.add$mut(4n);
    var doRes2 = sys_m$.io$mut().println$mut(rt$$flows.FlowCreator.fromFlow(base$$flows$$_DataParallelFlow_0.$self, l2_m$.flow$imm()).join$mut(rt$$Str.fromJsStr(",")));
    return base$$Void_0.$self;
      }
    }
    
    export class test$$Test_0Impl {
      $hash$imm(sys_m$) { return test$$Test_0.$hash$imm$fun(sys_m$, this); }
    }
    
    test$$Test_0.$self = new test$$Test_0Impl();
    """,
      "test/Test_0.js",
      """
      package test
      alias base.List as List, alias base.Block as Block, alias base.Main as Main, alias base.Str as Str, alias base.Nat as Nat,
      Test: Main{sys -> Block#
        .let[mut List[Nat]] l = {List#(1, 2, 3)}
        .let[List[Str]] l2 = {l.as{::str}}
        .do {l.add(4)}
        .do {sys.io.println(l2.flow.join ",")}
        .return {{}}
      }
      """);
  }

  @Test void listBlockElem() {
    okList(List.of("""
    import { base$$List_0 } from "../base/index.js";
    import { test$$Fear[###]$_0 } from "../test/Fear[###]$_0.js";
    import { test$$Fear[###]$_0 } from "../test/Fear[###]$_0.js";
    import { test$$Fear[###]$_0 } from "../test/Fear[###]$_0.js";

    export class test$$BadMutation_0 {
      static $hash$imm$fun($this) {
        let l_m$ = base$$List_0.$self.$hash$imm();
    var doRes1 = l_m$.add$mut(test$$Fear[###]$_0.$self);
    return l_m$.flow$mut().fold$mut(test$$Fear[###]$_0.$self,test$$Fear[###]$_0.$self);
      }
    }

    export class test$$BadMutation_0Impl {
      $hash$imm() { return test$$BadMutation_0.$hash$imm$fun(this); }
    }

    test$$BadMutation_0.$self = new test$$BadMutation_0Impl();
    """),
      List.of("test/BadMutation_0.js"),
      """
      package test
      alias base.List as List, alias base.Block as Block, alias base.Nat as Nat,
      
      Elem: { read .n: Nat, }
      BadMutation: {#: Nat -> Block#
        .let[mut List[mut Elem]] l = {List#}
        .do {l.add(mut Elem{.n -> 1, })}
        .return {l.flow
          .fold[Nat]({0}, {acc, e -> acc + (e.n)})
        }
      }
      """);
  }

  @Test void listIter() {
    ok("""
    import { base$$caps$$UnrestrictedIO_0 } from "../base/caps/index.js";
    import { base$$List_0 } from "../base/index.js";
    import { rt$$Str } from "../rt-js/Str.js";
    import { test$$Fear[###]$_0 } from "../test/Fear[###]$_0.js";
    import { test$$Fear[###]$_0 } from "../test/Fear[###]$_0.js";
    import { test$$Fear[###]$_0 } from "../test/Fear[###]$_0.js";
    import { test$$Fear[###]$_0 } from "../test/Fear[###]$_0.js";
    
    export class test$$Test_0 {
      static $hash$imm$fun(sys_m$, $this) {
        let l1_m$ = base$$List_0.$self.$hash$imm(35n,52n,84n,14n);
    let msg_m$ = l1_m$.iter$mut().filter$mut(test$$Fear[###]$_0.$self).flatMap$mut(test$$Fear[###]$_0.$self).map$mut(test$$Fear[###]$_0.$self).str$mut(test$$Fear[###]$_0.$self,rt$$Str.fromJsStr(","));
    let io_m$ = base$$caps$$UnrestrictedIO_0.$self.$hash$read(sys_m$);
    return io_m$.println$mut(msg_m$);
      }
    }
    
    export class test$$Test_0Impl {
      $hash$imm(sys_m$) { return test$$Test_0.$hash$imm$fun(sys_m$, this); }
    }
    
    test$$Test_0.$self = new test$$Test_0Impl();
    """,
      "test/Test_0.js",
      """
      package test
      alias base.List as List, alias base.Block as Block, alias base.Main as Main, alias base.Str as Str, alias base.Nat as Nat,
      alias base.caps.UnrestrictedIO as UnrestrictedIO,
  
      Test :base.Main{ sys -> Block#
        .let l1 = { List#[Nat](35, 52, 84, 14) }
        .let[Str] msg = {l1.iter
          .filter{n -> n < 40}
          .flatMap{n -> List#(n, n, n).iter}
          .map{n -> n * 10}
          .str({n -> n.str}, ",")}
        .let io = {UnrestrictedIO#sys}
        .return {io.println(msg)}
      }
      """);
  }

  @Test void listAssert() {
    okList(List.of("""
    import { base$$Block_0 } from "../base/index.js";
    import { test$$Fear[###]$_0 } from "../test/Fear[###]$_0.js";
    import { test$$Fear[###]$_0 } from "../test/Fear[###]$_0.js";
    
    export class test$$Test_0 {
      static $hash$imm$fun(sys_m$, $this) {
        return base$$Block_0.$self.$hash$imm().let$mut(test$$Fear[###]$_0.$self,test$$Fear[###]$_0.$self);
      }
    }
    
    export class test$$Test_0Impl {
      $hash$imm(sys_m$) { return test$$Test_0.$hash$imm$fun(sys_m$, this); }
    }
    
    test$$Test_0.$self = new test$$Test_0Impl();
    """),
      List.of("test/Test_0.js"),
      """
      package test
      alias base.List as List, alias base.Block as Block, alias base.Main as Main, alias base.Str as Str, alias base.Nat as Nat,
  
      Test :base.Main{ sys -> Block#
        .let l1 = { List#[Nat](35, 52, 84, 14) }
        .assert{l1.iter
          .map{n -> n * 10}
          .find{n -> n == 140}
          .isSome}
        .return {{}}
      }
      """);
  }

  @Test void assertTrue() {
    okList(List.of("""
      import { base$$Assert_0, base$$True_0 } from "../base/index.js";
      import { test$$Fear[###]$_0 } from "../test/Fear[###]$_0.js";
      
      export class test$$Test_0 {
        static $hash$imm$2$fun(fear31$_m$, $this) {
          return base$$Assert_0.$self.$exclamation$imm$2(base$$True_0.$self,test$$Fear[###]$_0.$self);
        }
      }
      
      export class test$$Test_0Impl {
        $hash$imm$1(fear31$_m$) { return test$$Test_0.$hash$imm$2$fun(fear31$_m$, this); }
      }
      
      test$$Test_0.$self = new test$$Test_0Impl();
      """, """
      import { base$$Fear[###]$_0, base$$True_0 } from "../base/index.js";
      import { rt$$IO } from "../rt-js/IO.js";
      
      export class base$$Assert_0 {
        static $exclamation$imm$2$fun(assertion_m$, $this) {
          return $this.$exclamation$imm$2(assertion_m$,base$$Fear[###]$_0.$self);
        }
        static $exclamation$imm$3$fun(assertion_m$, cont_m$, $this) {
          return (assertion_m$ == base$$True_0.$self ? cont_m$.$hash$mut$0() :   (() => {
          console.error("Assertion failed :(");
          if (typeof process !== "undefined") process.exit(1);
          else throw new Error("Assertion failed :(");
        })()
      );
        }
        static $exclamation$imm$4$fun(assertion_m$, msg_m$, cont_m$, $this) {
          return (assertion_m$ == base$$True_0.$self ? cont_m$.$hash$mut$0() :   (() => {
          rt$$IO.$self.printlnErr$mut(msg_m$);
          if (typeof process !== "undefined") process.exit(1);
          else throw new Error(msg_m$);
        })()
      );
        }
        static _fail$imm$1$fun($this) {
          return (function() {
          console.error("No magic code was found at:\\n" + new Error().stack);
          if (typeof process !== "undefined") process.exit(1);
          else throw new Error("No magic code was found");
        })()
      ;
        }
        static _fail$imm$2$fun(msg_m$, $this) {
          return (function() {
          console.error("No magic code was found at:\\n" + new Error().stack);
          if (typeof process !== "undefined") process.exit(1);
          else throw new Error("No magic code was found");
        })()
      ;
        }
      }
      
      export class base$$Assert_0Impl {
        $exclamation$imm$1(assertion_m$) { return base$$Assert_0.$exclamation$imm$2$fun(assertion_m$, this); }
        $exclamation$imm$2(assertion_m$, cont_m$) { return base$$Assert_0.$exclamation$imm$3$fun(assertion_m$, cont_m$, this); }
        $exclamation$imm$3(assertion_m$, msg_m$, cont_m$) { return base$$Assert_0.$exclamation$imm$4$fun(assertion_m$, msg_m$, cont_m$, this); }
        _fail$imm$0() { return base$$Assert_0._fail$imm$1$fun(this); }
        _fail$imm$1(msg_m$) { return base$$Assert_0._fail$imm$2$fun(msg_m$, this); }
      }
      
      base$$Assert_0.$self = new base$$Assert_0Impl();
      """),
      List.of("test/Test_0.js", "base/Assert_0.js"),
      """
      package test
      alias base.Assert as Assert, alias base.True as True, alias base.Void as Void, alias base.Main as Main,
      Test:Main{ _ -> Assert!(True, { Void }) }
      """);
  }


  @Test void bytesToStr() {
    ok("""
      import { rt$$Str } from "../rt-js/Str.js";
      import { rt$$UTF8 } from "../rt-js/UTF8.js";

      export class test$$Test_0 {
        static $hash$imm$2$fun(sys_m$, $this) {
          return sys_m$.io$mut$0().println$mut$1(rt$$UTF8.$self.fromBytes$imm$1(rt$$Str.fromJsStr("Hello!").utf8$imm$0()).$exclamation$mut$0());
        }
      }

      export class test$$Test_0Impl {
        $hash$imm$1(sys_m$) { return test$$Test_0.$hash$imm$2$fun(sys_m$, this); }
      }

      test$$Test_0.$self = new test$$Test_0Impl();
      """, "test/Test_0.js", """
      package test
      alias base.UTF8 as UTF8, alias base.Main as Main,
      Test: Main{sys -> sys.io.println(UTF8.fromBytes("Hello!".utf8)!)}
      """);
  }
  @Test void strToBytes() {
    okList(List.of("""
      import { rt$$Str } from "../rt-js/Str.js";
      import { rt$$flows } from "../rt-js/flows.js";
      import { test$$Fear[###]$_0 } from "../test/Fear[###]$_0.js";
      
      export class test$$Test_0 {
        static $hash$imm$fun(sys_m$, $this) {
          return sys_m$.io$mut().println$mut(rt$$flows.FlowCreator.fromFlow(rt$$flows.dataParallel.DataParallelFlowK.$self, rt$$Str.fromJsStr("Hello!").utf8$imm().flow$imm()).map$mut(test$$Fear[###]$_0.$self).join$mut(rt$$Str.fromJsStr(",")));
        }
      }
      
      export class test$$Test_0Impl {
        $hash$imm(sys_m$) { return test$$Test_0.$hash$imm$fun(sys_m$, this); }
      }
      
      test$$Test_0.$self = new test$$Test_0Impl();
      """, """
      import { base$$flows$$Flow_1, base$$flows$$_CheckFlowReuse_0, base$$flows$$_NonTerminalOps_1, base$$flows$$_TerminalOps_1 } from "../../base/flows/index.js";
      import { base$$Block_0, base$$Extensible_1 } from "../../base/index.js";
      
      export class base$$flows$$Fear894$_1 {
        static actor$mut$fun(state_m$, f_m$, fear[###]$_m$, flow_m$, isTail_m$) {
          return base$$Block_0.$self.$hash$imm(base$$flows$$_CheckFlowReuse_0.$self.$hash$imm(isTail_m$),flow_m$.actor$mut(state_m$,f_m$));
        }
        static first$mut$fun(fear[###]$_m$, flow_m$, isTail_m$) {
          return base$$Block_0.$self.$hash$imm(base$$flows$$_CheckFlowReuse_0.$self.$hash$imm(isTail_m$),flow_m$.first$mut());
        }
        static count$mut$fun(fear[###]$_m$, flow_m$, isTail_m$) {
          return base$$Block_0.$self.$hash$imm(base$$flows$$_CheckFlowReuse_0.$self.$hash$imm(isTail_m$),flow_m$.count$mut());
        }
        static actorMut$mut$fun(state_m$, f_m$, fear[###]$_m$, flow_m$, isTail_m$) {
          return base$$Block_0.$self.$hash$imm(base$$flows$$_CheckFlowReuse_0.$self.$hash$imm(isTail_m$),flow_m$.actorMut$mut(state_m$,f_m$));
        }
        static all$mut$fun(p_m$, fear[###]$_m$, flow_m$, isTail_m$) {
          return base$$Block_0.$self.$hash$imm(base$$flows$$_CheckFlowReuse_0.$self.$hash$imm(isTail_m$),flow_m$.all$mut(p_m$));
        }
        static limit$mut$fun(n_m$, fear[###]$_m$, flow_m$, isTail_m$) {
          return base$$Block_0.$self.$hash$imm(base$$flows$$_CheckFlowReuse_0.$self.$hash$imm(isTail_m$),flow_m$.limit$mut(n_m$));
        }
        static map$mut$fun(...args) {
          switch(args.length) {
            case 4: {
              let f_m$ = args[0];
              let fear[###]$_m$ = args[1];
              let flow_m$ = args[2];
              let isTail_m$ = args[3];
              return base$$Block_0.$self.$hash$imm(base$$flows$$_CheckFlowReuse_0.$self.$hash$imm(isTail_m$),flow_m$.map$mut(f_m$));
        }
            case 5: {
              let ctx_m$ = args[0];
              let f_m$ = args[1];
              let fear[###]$_m$ = args[2];
              let flow_m$ = args[3];
              let isTail_m$ = args[4];
              return base$$Block_0.$self.$hash$imm(base$$flows$$_CheckFlowReuse_0.$self.$hash$imm(isTail_m$),flow_m$.map$mut(ctx_m$,f_m$));
        }
            default: throw new Error('No overload for map$mut$fun with ' + args.length + ' arguments');
          }
        }
      
        static unwrapOp$mut$fun(token_m$, fear[###]$_m$, flow_m$) {
          return flow_m$.unwrapOp$mut(token_m$);
        }
        static fold$mut$fun(acc_m$, f_m$, fear[###]$_m$, flow_m$, isTail_m$) {
          return base$$Block_0.$self.$hash$imm(base$$flows$$_CheckFlowReuse_0.$self.$hash$imm(isTail_m$),flow_m$.fold$mut(acc_m$,f_m$));
        }
        static assumeFinite$mut$fun(fear[###]$_m$, flow_m$, isTail_m$) {
          return base$$Block_0.$self.$hash$imm(base$$flows$$_CheckFlowReuse_0.$self.$hash$imm(isTail_m$),flow_m$.assumeFinite$mut());
        }
        static any$mut$fun(p_m$, fear[###]$_m$, flow_m$, isTail_m$) {
          return base$$Block_0.$self.$hash$imm(base$$flows$$_CheckFlowReuse_0.$self.$hash$imm(isTail_m$),flow_m$.any$mut(p_m$));
        }
        static filter$mut$fun(predicate_m$, fear[###]$_m$, flow_m$, isTail_m$) {
          return base$$Block_0.$self.$hash$imm(base$$flows$$_CheckFlowReuse_0.$self.$hash$imm(isTail_m$),flow_m$.filter$mut(predicate_m$));
        }
        static findMap$mut$fun(f_m$, fear[###]$_m$, flow_m$, isTail_m$) {
          return base$$Block_0.$self.$hash$imm(base$$flows$$_CheckFlowReuse_0.$self.$hash$imm(isTail_m$),flow_m$.findMap$mut(f_m$));
        }
        static size$read$fun(fear[###]$_m$, flow_m$) {
          return flow_m$.size$read();
        }
        static flatMap$mut$fun(f_m$, fear[###]$_m$, flow_m$, isTail_m$) {
          return base$$Block_0.$self.$hash$imm(base$$flows$$_CheckFlowReuse_0.$self.$hash$imm(isTail_m$),flow_m$.flatMap$mut(f_m$));
        }
      }
      
      export class base$$flows$$Fear894$_1Impl {
        constructor(flow_m$, isTail_m$) {
          this.flow_m$ = flow_m$;
          this.isTail_m$ = isTail_m$;
        }
        $hash$read(ext_m$) { return base$$Extensible_1.$hash$read$fun(ext_m$, this); }
        limit$mut(n_m$) { return base$$flows$$Fear894$_1.limit$mut$fun(n_m$, this, this.flow_m$, this.isTail_m$); }
        size$read() { return base$$flows$$Fear894$_1.size$read$fun(this, this.flow_m$); }
        only$mut() { return base$$flows$$Flow_1.only$mut$fun(this); }
        all$mut(p_m$) { return base$$flows$$Fear894$_1.all$mut$fun(p_m$, this, this.flow_m$, this.isTail_m$); }
        fold$mut(acc_m$, f_m$) { return base$$flows$$Fear894$_1.fold$mut$fun(acc_m$, f_m$, this, this.flow_m$, this.isTail_m$); }
        assumeFinite$mut() { return base$$flows$$Fear894$_1.assumeFinite$mut$fun(this, this.flow_m$, this.isTail_m$); }
        self$mut() { return base$$flows$$Flow_1.self$mut$fun(this); }
        $hash$mut(ext_m$) { return base$$Extensible_1.$hash$mut$fun(ext_m$, this); }
        self$imm() { return base$$flows$$Flow_1.self$imm$fun(this); }
        $hash$imm(ext_m$) { return base$$Extensible_1.$hash$imm$fun(ext_m$, this); }
        actor$mut(state_m$, f_m$) { return base$$flows$$Fear894$_1.actor$mut$fun(state_m$, f_m$, this, this.flow_m$, this.isTail_m$); }
        count$mut() { return base$$flows$$Fear894$_1.count$mut$fun(this, this.flow_m$, this.isTail_m$); }
        none$mut(predicate_m$) { return base$$flows$$_TerminalOps_1.none$mut$fun(predicate_m$, this); }
        map$mut(...args) { return base$$flows$$Fear894$_1.map$mut$fun(...args, this, this.flow_m$, this.isTail_m$); }
        mapFilter$mut(f_m$) { return base$$flows$$_NonTerminalOps_1.mapFilter$mut$fun(f_m$, this); }
        for$mut(f_m$) { return base$$flows$$_TerminalOps_1.for$mut$fun(f_m$, this); }
        find$mut(predicate_m$) { return base$$flows$$_TerminalOps_1.find$mut$fun(predicate_m$, this); }
        join$mut(j_m$) { return base$$flows$$Flow_1.join$mut$fun(j_m$, this); }
        first$mut(...args) { return base$$flows$$Fear894$_1.first$mut$fun(...args, this, this.flow_m$, this.isTail_m$); }
        get$mut() { return base$$flows$$Flow_1.get$mut$fun(this); }
        last$mut() { return base$$flows$$_TerminalOps_1.last$mut$fun(this); }
        findMap$mut(f_m$) { return base$$flows$$Fear894$_1.findMap$mut$fun(f_m$, this, this.flow_m$, this.isTail_m$); }
        flatMap$mut(f_m$) { return base$$flows$$Fear894$_1.flatMap$mut$fun(f_m$, this, this.flow_m$, this.isTail_m$); }
        list$mut() { return base$$flows$$_TerminalOps_1.list$mut$fun(this); }
        self$read() { return base$$flows$$Flow_1.self$read$fun(this); }
        unwrapOp$mut(token_m$) { return base$$flows$$Fear894$_1.unwrapOp$mut$fun(token_m$, this, this.flow_m$); }
        actorMut$mut(state_m$, f_m$) { return base$$flows$$Fear894$_1.actorMut$mut$fun(state_m$, f_m$, this, this.flow_m$, this.isTail_m$); }
        peek$mut(...args) { return base$$flows$$_NonTerminalOps_1.peek$mut$fun(...args, this); }
        let$mut(x_m$, cont_m$) { return base$$flows$$Flow_1.let$mut$fun(x_m$, cont_m$, this); }
        scan$mut(acc_m$, f_m$) { return base$$flows$$_NonTerminalOps_1.scan$mut$fun(acc_m$, f_m$, this); }
        max$mut(compare_m$) { return base$$flows$$_TerminalOps_1.max$mut$fun(compare_m$, this); }
        forEffect$mut(f_m$) { return base$$flows$$_TerminalOps_1.forEffect$mut$fun(f_m$, this); }
        any$mut(p_m$) { return base$$flows$$Fear894$_1.any$mut$fun(p_m$, this, this.flow_m$, this.isTail_m$); }
        filter$mut(predicate_m$) { return base$$flows$$Fear894$_1.filter$mut$fun(predicate_m$, this, this.flow_m$, this.isTail_m$); }
        opt$mut() { return base$$flows$$Flow_1.opt$mut$fun(this); }
      }
      """), List.of("test/Test_0.js", "base/flows/Fear894$_1.js"),
      """
      package test
      Test: Main{sys -> sys.io.println("Hello!".utf8.flow.map{b -> b.str}.join ",")}
      """, Base.mutBaseAliases);
  }

  @Test void animalTypes() {
    okList(List.of("""
      import { rt$$Str } from "../rt-js/Str.js";
      
      export class test$$Animal_0 {
        static name$imm$1$fun($this) {
          return rt$$Str.fromJsStr("animal");
        }
        static name$imm$2$fun(a_m$, $this) {
          return a_m$.$plus$imm$1(rt$$Str.fromJsStr("animal"));
        }
      }
      """, """
      import { rt$$Str } from "../rt-js/Str.js";
      
      export class test$$Bear_0 {
        static name$imm$1$fun($this) {
          return rt$$Str.fromJsStr("bear");
        }
      }
      """, """
      import { rt$$Str } from "../rt-js/Str.js";
      import { test$$Animal_0 } from "../test/Animal_0.js";
      import { test$$Bear_0 } from "../test/Bear_0.js";
      
      export class test$$BrownBear_0 {
        static run$imm$1$fun($this) {
          return rt$$Str.fromJsStr("BrownBear runs fast");
        }
      }
      
      export class test$$BrownBear_0Impl {
        name$imm$0() { return test$$Bear_0.name$imm$1$fun(this); }
        name$imm$1(a_m$) { return test$$Animal_0.name$imm$2$fun(a_m$, this); }
        run$imm$0() { return test$$BrownBear_0.run$imm$1$fun(this); }
      }
      
      test$$BrownBear_0.$self = new test$$BrownBear_0Impl();
      """), List.of("test/Animal_0.js", "test/Bear_0.js", "test/BrownBear_0.js"),
      """
      package test
      alias base.Str as Str,
      Animal: {
        .name: Str -> "animal",
        .name(a:Str): Str -> a+"animal",
        .run: Str  // abstract, not implemented
      }
      Bear: Animal {
        .name: Str -> "bear"
      }
      BrownBear: Bear {
        .run: Str -> "BrownBear runs fast"
      }
      """);
  }
  @Test void animalTypes2() { okList(List.of("""
    export class test$$Foo_0 {
      constructor() {throw new Error("from constructor");}
    
    }
    """, """
    export class test$$Beer_0 {
      constructor() {throw new Error("from constructor");}
    
      static beer$imm$2$fun(f_m$, $this) {
        return f_m$.bar$imm$0();
      }
    }
    
    export class test$$Beer_0Impl {
      beer$imm$1(f_m$) { return test$$Beer_0.beer$imm$2$fun(f_m$, this); }
    }
    
    test$$Beer_0.$self = new test$$Beer_0Impl();
    """, """
    import { test$$Beer_0 } from "../test/Beer_0.js";
    import { test$$Fear716$_0 } from "../test/Fear716$_0.js";
    
    export class test$$User_0 {
      constructor() {throw new Error("from constructor");}
    
      static go$imm$1$fun($this) {
        return test$$Beer_0.$self.beer$imm$1(test$$Fear716$_0.$self);
      }
    }
    
    export class test$$User_0Impl {
      go$imm$0() { return test$$User_0.go$imm$1$fun(this); }
    }
    
    test$$User_0.$self = new test$$User_0Impl();
    """), List.of("test/Foo_0.js", "test/Beer_0.js", "test/User_0.js"), """
    package test
    Foo:{.bar:Beer}
    Beer:{.beer(f:Foo):Beer->f.bar}
    User:{.go:Beer->Beer.beer{Beer} }
    """);
  }

  @Test void listFlowLimitJoin() {
    ok("""
      import { base$$flows$$_SafeSource_0 } from "../base/flows/index.js";
      import { rt$$ListK } from "../rt-js/ListK.js";
      import { rt$$Numbers } from "../rt-js/Numbers.js";
      import { rt$$Str } from "../rt-js/Str.js";
      import { rt$$flows } from "../rt-js/flows.js";
      
      export class test$$Test_0 {
        static $hash$imm$2$fun(s_m$, $this) {
          return s_m$.io$mut$0().println$mut$1(rt$$flows.FlowCreator.fromFlow(rt$$flows.dataParallel.DataParallelFlowK.$self, base$$flows$$_SafeSource_0.$self.fromList$imm$1(rt$$ListK.$self.$hash$imm$5(rt$$Str.fromJsStr("A"),rt$$Str.fromJsStr("B"),rt$$Str.fromJsStr("C"),rt$$Str.fromJsStr("D"),rt$$Str.fromJsStr("E")))).limit$mut$1(rt$$Numbers.toNat64(2n)).join$mut$1(rt$$Str.fromJsStr(",")));
        }
      }
      
      export class test$$Test_0Impl {
        $hash$imm$1(s_m$) { return test$$Test_0.$hash$imm$2$fun(s_m$, this); }
      }
      
      test$$Test_0.$self = new test$$Test_0Impl();
      """, "test/Test_0.js",
      """
      package test
      alias base.List as List, alias base.Int as Int, alias base.Block as Block, alias base.Main as Main, alias base.Str as Str,
      Test:Main{ s -> s.io.println(List#("A", "B", "C", "D", "E").flow.limit(2).join ",")}
      """);
  }
}