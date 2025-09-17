package codegen.js;

import main.CompilerFrontEnd;
import main.InputOutput;
import main.Main;
import main.js.LogicMainJs;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import utils.Err;
import java.util.Arrays;
import java.util.List;

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
        static $hash$imm$fun(fear31$_m$, $this) {
          return base$$Void_0.$self;
        }
      }
      
      export class test$$Test_0Impl {
        $hash$imm(fear31$_m$) { return test$$Test_0.$hash$imm$fun(fear31$_m$, this); }
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
     static $self = new test$$Test_0Impl();
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
    """, """
    import { base$$Bool_0, base$$False_0 } from "../base/index.js";
    import { rt$$Str } from "../rt-js/Str.js";
    
    export class base$$True_0 {
     static $self = new base$$True_0Impl();
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
    """),
    List.of("test/Test_0.js", "base/True_0.js"),
    """
    package test
    alias base.Bool as Bool, alias base.True as True, alias base.False as False,
    Test:{ .m:Bool-> True, .b:Bool-> False }
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
     static $self = new test$$Test_0Impl();
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

  @Test void numOps() {
    ok("""
    import { rt$$Numbers } from "../rt-js/Numbers.js";
    
    export class test$$Test_0 {
     static $self = new test$$Test_0Impl();
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
     static $self = new test$$Test_0Impl();
      static $hash$imm$fun(fear[###]$_m$, $this) {
        return base$$_NatAssertionHelper_0.assertEq$imm$fun(5n, 5n, null);
      }
    }
    
    export class test$$Test_0Impl {
      $hash$imm(fear[###]$_m$) { return test$$Test_0.$hash$imm$fun(fear[###]$_m$, this); }
    }
    """, """
    import { base$$Assert_0, base$$Fear2252$_0, base$$Fear2257$_0 } from "../base/index.js";
    import { rt$$Numbers } from "../rt-js/Numbers.js";
    import { rt$$Str } from "../rt-js/Str.js";
    
    export class base$$_NatAssertionHelper_0 {
     static $self = new base$$_NatAssertionHelper_0Impl();
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

  @Test void numOpsCompBit() {
    ok("""
    import { rt$$Numbers } from "../rt-js/Numbers.js";
    
    export class test$$Test_0 {
     static $self = new test$$Test_0Impl();
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
      reverse$imm() { throw new Error('Abstract method'); }
      turn$imm() { throw new Error('Abstract method'); }
      static reverse$imm$fun($this) {
        return $this.turn$imm().turn$imm();
      }
    }
    """, """
    import { test$$Direction_0 } from "../test/Direction_0.js";
    import { test$$East_0 } from "../test/East_0.js";
    
    export class test$$North_0 {
      reverse$imm() { throw new Error('Abstract method'); }
      turn$imm() { throw new Error('Abstract method'); }
      static turn$imm$fun($this) {
        return test$$East_0.$self;
      }
    }
    
    export class test$$North_0Impl extends test$$North_0 {
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
      of$imm(heading_m$, aiming_m$) { throw new Error('Abstract method'); }
      static of$imm$fun(heading_m$, aiming_m$, $this) {
        return new test$$Tank_0Impl(aiming_m$, heading_m$);
      }
    }
    
    export class test$$Tanks_0Impl extends test$$Tanks_0 {
      of$imm(heading_m$, aiming_m$) { return test$$Tanks_0.of$imm$fun(heading_m$, aiming_m$, this); }
    }
    
    test$$Tanks_0.$self = new test$$Tanks_0Impl();
    """, """
    export class test$$Tank_0 {
      aiming$imm() { throw new Error('Abstract method'); }
      heading$imm() { throw new Error('Abstract method'); }
      static heading$imm$fun(fear[###]$_m$, heading_m$) {
        return heading_m$;
      }
      static aiming$imm$fun(fear[###]$_m$, aiming_m$) {
        return aiming_m$;
      }
    }
    
    export class test$$Tank_0Impl extends test$$Tank_0 {
      constructor(aiming_m$, heading_m$) {
        this.aiming_m$ = aiming_m$;
        this.heading_m$ = heading_m$;
      }
      aiming$imm() { return test$$Tank_0.aiming$imm$fun(this, this.aiming_m$); }
      heading$imm() { return test$$Tank_0.heading$imm$fun(this, this.heading_m$); }
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

  @Test void tanksWithSugar() {
    okList(List.of("""
    import { test$$Direction_0 } from "../test/Direction_0.js";
    import { test$$East_0 } from "../test/East_0.js";
    
    export class test$$North_0 {
      reverse$imm() { throw new Error('Abstract method'); }
      turn$imm() { throw new Error('Abstract method'); }
      static turn$imm$fun($this) {
        return test$$East_0.$self;
      }
    }
    
    export class test$$North_0Impl extends test$$North_0 {
      reverse$imm() { return test$$Direction_0.reverse$imm$fun(this); }
      turn$imm() { return test$$North_0.turn$imm$fun(this); }
    }
    
    test$$North_0.$self = new test$$North_0Impl();
    """, """
    import { test$$Fear[###]$_0Impl } from "../test/Fear[###]$_0.js";
    
    export class test$$Tanks_0 {
      $hash$imm(heading_m$, aiming_m$) { throw new Error('Abstract method'); }
      static $hash$imm$fun(heading_m$, aiming_m$, $this) {
        return new test$$Fear[###]$_0Impl(aiming_m$, heading_m$);
      }
    }

    export class test$$Tanks_0Impl extends test$$Tanks_0 {
      $hash$imm(heading_m$, aiming_m$) { return test$$Tanks_0.$hash$imm$fun(heading_m$, aiming_m$, this); }
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
      $hash$imm(d_m$) { throw new Error('Abstract method'); }
    }
    """, """
    export class test$$Turn270_0 {
      $hash$imm(fear[###]$_m$) { throw new Error('Abstract method'); }
      static $hash$imm$fun(fear[###]$_m$, $this) {
        return fear[###]$_m$.turn$imm().turn$imm().turn$imm();
      }
    }
    
    export class test$$Turn270_0Impl extends test$$Turn270_0 {
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
      $plus$imm(r_m$) { throw new Error('Abstract method'); }
      $hash$imm(d_m$) { throw new Error('Abstract method'); }
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
      $hash$imm(heading_m$, aiming_m$) { throw new Error('Abstract method'); }
      static $hash$imm$fun(heading_m$, aiming_m$, $this) {
        return new test$$Fear[###]$_0Impl(aiming_m$, heading_m$);
      }
    }
    
    export class test$$Tanks_0Impl extends test$$Tanks_0 {
      $hash$imm(heading_m$, aiming_m$) { return test$$Tanks_0.$hash$imm$fun(heading_m$, aiming_m$, this); }
    }
    
    test$$Tanks_0.$self = new test$$Tanks_0Impl();
    """, """
    import { test$$Tanks_0 } from "../test/Tanks_0.js";
    
    export class test$$Tank_0 {
      turnTurret$imm(r_m$) { throw new Error('Abstract method'); }
      aiming$imm() { throw new Error('Abstract method'); }
      heading$imm() { throw new Error('Abstract method'); }
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
      point$imm() { throw new Error('Abstract method'); }
      turn$imm() { throw new Error('Abstract method'); }
      static turn$imm$fun($this) {
        return test$$East_0.$self;
      }
      static point$imm$fun($this) {
        return test$$Points_0.$self.$hash$imm(-1n,0n);
      }
    }
  
    export class test$$North_0Impl extends test$$North_0 {
      point$imm() { return test$$North_0.point$imm$fun(this); }
      turn$imm() { return test$$North_0.turn$imm$fun(this); }
    }
  
    test$$North_0.$self = new test$$North_0Impl();
    """, """
    import { test$$Fear[###]$_0Impl } from "../test/Fear[###]$_0.js";
    
    export class test$$Points_0 {
      $hash$imm(x_m$, y_m$) { throw new Error('Abstract method'); }
      static $hash$imm$fun(x_m$, y_m$, $this) {
        return new test$$Fear[###]$_0Impl(x_m$, y_m$);
      }
    }
    
    export class test$$Points_0Impl extends test$$Points_0 {
      $hash$imm(x_m$, y_m$) { return test$$Points_0.$hash$imm$fun(x_m$, y_m$, this); }
    }
    
    test$$Points_0.$self = new test$$Points_0Impl();
    """, """
    import { test$$Points_0 } from "../test/Points_0.js";
    
    export class test$$Point_0 {
      move$imm(d_m$) { throw new Error('Abstract method'); }
      y$imm() { throw new Error('Abstract method'); }
      $plus$imm(other_m$) { throw new Error('Abstract method'); }
      x$imm() { throw new Error('Abstract method'); }
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
      choose$imm(l_m$, r_m$) { throw new Error('Abstract method'); }
      static choose$imm$fun(l_m$, r_m$, $this) {
        return l_m$;
      }
    }
    
    export class test$$Left_0Impl extends test$$Left_0 {
      choose$imm(l_m$, r_m$) { return test$$Left_0.choose$imm$fun(l_m$, r_m$, this); }
    }
    
    test$$Left_0.$self = new test$$Left_0Impl();
    """, """
    export class test$$Fork_0 {
      choose$imm(leftVal_m$, rightVal_m$) { throw new Error('Abstract method'); }
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
      choose$imm(fear[###]$_m$) { throw new Error('Abstract method'); }
      static choose$imm$fun(fear[###]$_m$, $this) {
        return fear[###]$_m$.right$imm();
      }
    }
    
    export class test$$Right_0Impl extends test$$Right_0 {
      choose$imm(fear[###]$_m$) { return test$$Right_0.choose$imm$fun(fear[###]$_m$, this); }
    }
    
    test$$Right_0.$self = new test$$Right_0Impl();
    """, """
    export class test$$Fork_0 {
      choose$imm(leftRight_m$) { throw new Error('Abstract method'); }
    }
    """, """
    export class test$$LeftRight_1 {
      right$imm() { throw new Error('Abstract method'); }
      left$imm() { throw new Error('Abstract method'); }
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
      message$imm(s_m$) { throw new Error('Abstract method'); }
      static message$imm$fun(s_m$, $this) {
        return (s_m$.$equals$equals$imm(rt$$Str.fromJavaStr("hello")) == base$$True_0.$self ? rt$$Str.fromJavaStr("Hi, I'm Bot; how can I help you?") : (s_m$.$equals$equals$imm(rt$$Str.fromJavaStr("bye")) == base$$True_0.$self ? rt$$Str.fromJavaStr("goodbye!") : rt$$Str.fromJavaStr("I don't understand")));
      }
    }
    
    export class test$$Bot_0Impl extends test$$Bot_0 {
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
      m$imm(b_m$) { throw new Error('Abstract method'); }
      static m$imm$fun(b_m$, $this) {
        return (b_m$ == base$$True_0.$self ? $this : $this.m$imm(b_m$));
      }
    }
    
    export class test$$Test_0Impl extends test$$Test_0 {
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
        m$imm() { throw new Error('Abstract method'); }
        static m$imm$fun($this) {
          return (rt$$Numbers.toBool((5n) > (2n)) == base$$True_0.$self ? rt$$Str.fromJavaStr("True") : rt$$Str.fromJavaStr("False"));
        }
      }
      
      export class test$$Test_0Impl extends test$$Test_0 {
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
      getAge$imm(p_m$) { throw new Error('Abstract method'); }
      static getAge$imm$fun(p_m$, $this) {
        return p_m$.match$imm(test$$Fear[###]$_0.$self);
      }
    }
    
    export class test$$Test_0Impl extends test$$Test_0 {
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
      $hash$imm(fear[###]$_m$) { throw new Error('Abstract method'); }
      static $hash$imm$fun(fear[###]$_m$, $this) {
        let n_m$ = 5n;
    var doRes1 = test$$ForceGen_0.$self.$hash$imm();
    return base$$Void_0.$self;
      }
    }
    
    export class test$$Test_0Impl extends test$$Test_0 {
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
      $hash$imm(fear[###]$_m$) { throw new Error('Abstract method'); }
      static $hash$imm$fun(fear[###]$_m$, $this) {
        var n_m$ = base$$Vars_0.$self.$hash$imm(rt$$Str.fromJavaStr("Hi"));
    var doRes1 = test$$ForceGen_0.$self.$hash$imm();
    return base$$Void_0.$self;
      }
    }
    
    export class test$$Test_0Impl extends test$$Test_0 {
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
      $hash$imm(fear[###]$_m$) { throw new Error('Abstract method'); }
      static $hash$imm$fun(fear[###]$_m$, $this) {
        let n_m$ = 5n;
    let n2_m$ = 10n;
    var n3_m$ = base$$Vars_0.$self.$hash$imm(15n);
    if (rt$$Numbers.toBool((BigInt(n3_m$.get$mut())) === ((BigInt(BigInt(n_m$)) + n2_m$))) == base$$True_0.$self) { throw base$$Infos_0.$self.msg$imm(rt$$Str.fromJavaStr("oh no"));
     }
    var doRes1 = test$$ForceGen_0.$self.$hash$imm();
    return base$$Void_0.$self;
      }
    }
    
    export class test$$Test_0Impl extends test$$Test_0 {
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
      $hash$imm(fear[###]$_m$) { throw new Error('Abstract method'); }
      static $hash$imm$fun(fear[###]$_m$, $this) {
        return (base$$True_0.$self == base$$True_0.$self ? (() => {
    return base$$Void_0.$self})() : (() => {
    return base$$Void_0.$self})());
      }
    }
    
    export class test$$Test_0Impl extends test$$Test_0 {
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
        $hash$imm(sys_m$) { throw new Error('Abstract method'); }
        static $hash$imm$fun(sys_m$, $this) {
          return sys_m$.io$mut().println$mut(rt$$Str.fromJavaStr("Hello World"));
        }
      }
      
      export class test$$Test_0Impl extends test$$Test_0 {
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
     static $self = new test$$Names_0Impl();
      static $hash$imm$fun(ps_m$, $this) {
        return rt$$flows.FlowCreator.fromFlow(base$$flows$$_DataParallelFlow_0.$self, ps_m$.flow$imm()).map$mut(test$$Fear[###]$_0.$self).list$mut();
      }
    }
    
    export class test$$Names_0Impl {
      $hash$imm(ps_m$) { return test$$Names_0.$hash$imm$fun(ps_m$, this); }
    }
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
    import { base$$flows$$_DataParallelFlow_0 } from "../base/index.js";
    import { test$$Fear[###]$_0 } from "../test/Fear[###]$_0.js";
    
    export class test$$Doctors_0 {
      $hash$imm(ps_m$) { throw new Error('Abstract method'); }
      static $hash$imm$fun(ps_m$, $this) {
        return rt.flows.FlowCreator.fromFlow(base$$flows$$_DataParallelFlow_0.$self, ps_m$.flow$imm()).filter$mut(test$$Fear[###]$_0.$self).list$mut();
      }
    }
    
    export class test$$Doctors_0Impl extends test$$Doctors_0 {
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
    import { base$$flows$$_DataParallelFlow_0 } from "../base/index.js";
    import { test$$Fear[###]$_0 } from "../test/Fear[###]$_0.js";
    
    export class test$$AllCats_0 {
      $hash$imm(ps_m$) { throw new Error('Abstract method'); }
      static $hash$imm$fun(ps_m$, $this) {
        return rt.flows.FlowCreator.fromFlow(base$$flows$$_DataParallelFlow_0.$self, ps_m$.flow$imm()).flatMap$mut(test$$Fear[###]$_0.$self).list$mut();
      }
    }
    
    export class test$$AllCats_0Impl extends test$$AllCats_0 {
      $hash$imm(ps_m$) { return test$$AllCats_0.$hash$imm$fun(ps_m$, this); }
    }
    
    test$$AllCats_0.$self = new test$$AllCats_0Impl();
    """, """
    import { base$$flows$$_DataParallelFlow_0 } from "../base/index.js";
    import { test$$Fear[###]$_0 } from "../test/Fear[###]$_0.js";
    
    export class test$$Sad_0 {
      $hash$imm(ps_m$) { throw new Error('Abstract method'); }
      static $hash$imm$fun(ps_m$, $this) {
        return rt.flows.FlowCreator.fromFlow(base$$flows$$_DataParallelFlow_0.$self, ps_m$.flow$imm()).any$mut(test$$Fear[###]$_0.$self);
      }
    }
    
    export class test$$Sad_0Impl extends test$$Sad_0 {
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

  @Disabled void listBlock() {
    ok("""
    import { base$$List_0 } from "../base/List_0.js";
    import { base$$Void_0 } from "../base/Void_0.js";
    
    export class test$$Test_0 {
      $hash$imm(s_m$) { throw new Error('Abstract method'); }
      static $hash$imm$fun(s_m$, $this) {
        let l = base$$List_0.$self.$hash$imm(12n,13n,14n);
    return base$$Void_0.$self;
      }
    }
    
    export class test$$Test_0Impl extends test$$Test_0 {
      $hash$imm(s_m$) { return test$$Test_0.$hash$imm$fun(s_m$, this); }
    }
    
    test$$Test_0.$self = new test$$Test_0Impl();
    """,
    "test/Test_0.js",
    """
    package test
    alias base.List as List, alias base.Int as Int, alias base.Block as Block, alias base.Main as Main, alias Str as Str,
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
      $hash$read() { throw new Error('Abstract method'); }
      static $hash$read$fun($this) {
        return base$$List_0.$self.$hash$imm().$plus$mut(1n).$plus$mut(2n).$plus$mut(3n).$plus$mut(4n);
      }
    }
    
    export class test$$GetList_0Impl extends test$$GetList_0 {
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

  @Test void listBlockAddJoin() {
    ok("""
    import { base$$List_0, base$$Void_0, base$$flows$$_DataParallelFlow_0 } from "../base/index.js";
    import { rt$$Str } from "../rt-js/Str.js";
    import { test$$Fear[###]$_0 } from "../test/Fear[###]$_0.js";
    
    export class test$$Test_0 {
      $hash$imm(sys_m$) { throw new Error('Abstract method'); }
      static $hash$imm$fun(sys_m$, $this) {
        let l_m$ = base$$List_0.$self.$hash$imm(1n,2n,3n);
    let l2_m$ = l_m$.as$read(test$$Fear[###]$_0.$self);
    var doRes1 = l_m$.add$mut(4n);
    var doRes2 = sys_m$.io$mut().println$mut(rt.flows.FlowCreator.fromFlow(base$$flows$$_DataParallelFlow_0.$self, l2_m$.flow$imm()).join$mut(rt$$Str.fromJavaStr(",")));
    return base$$Void_0.$self;
      }
    }
    
    export class test$$Test_0Impl extends test$$Test_0 {
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
      $hash$imm() { throw new Error('Abstract method'); }
      static $hash$imm$fun($this) {
        let l_m$ = base$$List_0.$self.$hash$imm();
    var doRes1 = l_m$.add$mut(test$$Fear[###]$_0.$self);
    return l_m$.flow$mut().fold$mut(test$$Fear[###]$_0.$self,test$$Fear[###]$_0.$self);
      }
    }

    export class test$$BadMutation_0Impl extends test$$BadMutation_0 {
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
    import { base$$List_0, base$$caps$$UnrestrictedIO_0 } from "../base/index.js";
    import { rt$$Str } from "../rt-js/Str.js";
    import { test$$Fear[###]$_0 } from "../test/Fear[###]$_0.js";
    import { test$$Fear[###]$_0 } from "../test/Fear[###]$_0.js";
    import { test$$Fear[###]$_0 } from "../test/Fear[###]$_0.js";
    import { test$$Fear[###]$_0 } from "../test/Fear[###]$_0.js";
    
    export class test$$Test_0 {
      $hash$imm(sys_m$) { throw new Error('Abstract method'); }
      static $hash$imm$fun(sys_m$, $this) {
        let l1_m$ = base$$List_0.$self.$hash$imm(35n,52n,84n,14n);
    let msg_m$ = l1_m$.iter$mut().filter$mut(test$$Fear[###]$_0.$self).flatMap$mut(test$$Fear[###]$_0.$self).map$mut(test$$Fear[###]$_0.$self).str$mut(test$$Fear[###]$_0.$self,rt$$Str.fromJavaStr(","));
    let io_m$ = base$$caps$$UnrestrictedIO_0.$self.$hash$read(sys_m$);
    return io_m$.println$mut(msg_m$);
      }
    }
    
    export class test$$Test_0Impl extends test$$Test_0 {
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
      $hash$imm(sys_m$) { throw new Error('Abstract method'); }
      static $hash$imm$fun(sys_m$, $this) {
        return base$$Block_0.$self.$hash$imm().let$mut(test$$Fear[###]$_0.$self,test$$Fear[###]$_0.$self);
      }
    }
    
    export class test$$Test_0Impl extends test$$Test_0 {
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
        static $hash$imm$fun(fear0$_m$, $this) {
          return base$$Assert_0.$self.$exclamation$imm(base$$True_0.$self,test$$Fear[###]$_0.$self);
        }
      }
      
      export class test$$Test_0Impl {
        $hash$imm(fear0$_m$) { return test$$Test_0.$hash$imm$fun(fear0$_m$, this); }
      }
      
      test$$Test_0.$self = new test$$Test_0Impl();
      """, """
      import { base$$Fear[###]$_0, base$$True_0 } from "../base/index.js";
      
      export class base$$Assert_0 {
        $exclamation$imm(assertion_m$) { throw new Error('Abstract method'); }
        $exclamation$imm(assertion_m$, cont_m$) { throw new Error('Abstract method'); }
        $exclamation$imm(assertion_m$, msg_m$, cont_m$) { throw new Error('Abstract method'); }
        _fail$imm() { throw new Error('Abstract method'); }
        _fail$imm(msg_m$) { throw new Error('Abstract method'); }
        static $exclamation$imm$fun(assertion_m$, $this) {
          return $this.$exclamation$imm(assertion_m$,base$$Fear[###]$_0.$self);
        }
        static $exclamation$imm$fun(assertion_m$, cont_m$, $this) {
          return (assertion_m$ == base$$True_0.$self ? cont_m$.$hash$mut() :   (() => {
          console.error("Assertion failed :(");
          if (typeof process !== "undefined") process.exit(1);
          else throw new Error("Assertion failed :(");
        })()
      );
        }
        static $exclamation$imm$fun(assertion_m$, msg_m$, cont_m$, $this) {
          return (assertion_m$ == base$$True_0.$self ? cont_m$.$hash$mut() :   (() => {
          console.error(msg_m$);
          if (typeof process !== "undefined") process.exit(1);
          else throw new Error(msg_m$);
        })()
      );
        }
        static _fail$imm$fun($this) {
          return (function() {
        console.error("No magic code was found at:\\n" + new Error().stack);
        if (typeof process !== "undefined") process.exit(1);
        else throw new Error("No magic code was found");
      })()
      ;
        }
        static _fail$imm$fun(msg_m$, $this) {
          return (function() {
        console.error("No magic code was found at:\\n" + new Error().stack);
        if (typeof process !== "undefined") process.exit(1);
        else throw new Error("No magic code was found");
      })()
      ;
        }
      }

      export class base$$Assert_0Impl extends base$$Assert_0 {
        $exclamation$imm(assertion_m$) { return base$$Assert_0.$exclamation$imm$fun(assertion_m$, this); }
        $exclamation$imm(assertion_m$, cont_m$) { return base$$Assert_0.$exclamation$imm$fun(assertion_m$, cont_m$, this); }
        $exclamation$imm(assertion_m$, msg_m$, cont_m$) { return base$$Assert_0.$exclamation$imm$fun(assertion_m$, msg_m$, cont_m$, this); }
        _fail$imm() { return base$$Assert_0._fail$imm$fun(this); }
        _fail$imm(msg_m$) { return base$$Assert_0._fail$imm$fun(msg_m$, this); }
      }

      base$$Assert_0.$self = new base$$Assert_0Impl();
      """),
    List.of("test/Test_0.js", "base/Assert_0.js"),
      """
      package test
      alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
      alias base.Void as Void,
      Test:Main{ _ -> Assert!(True, { Void }) }
      """);
  }

  @Test void assertTrueAbort() {
    okList(List.of("""
      import { base$$Assert_0, base$$True_0 } from "../base/index.js";
      import { test$$Fear[###]$_0 } from "../test/Fear[###]$_0.js";
      
      export class test$$Test_0 {
        static $hash$imm$fun(fear0$_m$, $this) {
          return base$$Assert_0.$self.$exclamation$imm(base$$True_0.$self,test$$Fear[###]$_0.$self);
        }
      }
      
      export class test$$Test_0Impl {
        $hash$imm(fear0$_m$) { return test$$Test_0.$hash$imm$fun(fear0$_m$, this); }
      }
      
      test$$Test_0.$self = new test$$Test_0Impl();
      """, """
      export class base$$Assert_0 {
        static $exclamation$imm$fun(assertion_m$, $this) {
          return (function() {
        console.error("Program aborted at:\\n" + new Error().stack);
        if (typeof process !== "undefined") process.exit(1);
        else throw new Error("Program aborted");
      })()
      ;
        }
        static $exclamation$imm$fun(assertion_m$, cont_m$, $this) {
          return (function() {
        console.error("Program aborted at:\\n" + new Error().stack);
        if (typeof process !== "undefined") process.exit(1);
        else throw new Error("Program aborted");
      })()
      ;
        }
        static $exclamation$imm$fun(assertion_m$, msg_m$, cont_m$, $this) {
          return (function() {
        console.error("Program aborted at:\\n" + new Error().stack);
        if (typeof process !== "undefined") process.exit(1);
        else throw new Error("Program aborted");
      })()
      ;
        }
        static _fail$imm$fun($this) {
          return (function() {
        console.error("Program aborted at:\\n" + new Error().stack);
        if (typeof process !== "undefined") process.exit(1);
        else throw new Error("Program aborted");
      })()
      ;
        }
        static _fail$imm$fun(msg_m$, $this) {
          return (function() {
        console.error("Program aborted at:\\n" + new Error().stack);
        if (typeof process !== "undefined") process.exit(1);
        else throw new Error("Program aborted");
      })()
      ;
        }
      }

      export class base$$Assert_0Impl {
        $exclamation$imm(assertion_m$) { return base$$Assert_0.$exclamation$imm$fun(assertion_m$, this); }
        $exclamation$imm(assertion_m$, cont_m$) { return base$$Assert_0.$exclamation$imm$fun(assertion_m$, cont_m$, this); }
        $exclamation$imm(assertion_m$, msg_m$, cont_m$) { return base$$Assert_0.$exclamation$imm$fun(assertion_m$, msg_m$, cont_m$, this); }
        _fail$imm() { return base$$Assert_0._fail$imm$fun(this); }
        _fail$imm(msg_m$) { return base$$Assert_0._fail$imm$fun(msg_m$, this); }
      }

      base$$Assert_0.$self = new base$$Assert_0Impl();
      """),
      List.of("test/Test_0.js", "base/Assert_0.js"),
      """
      package test
      alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
      alias base.Void as Void,
      Test:Main{ _ -> Assert!(True, { Void }) }
      """);
  }
}