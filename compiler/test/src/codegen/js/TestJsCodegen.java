package codegen.js;

import main.CompilerFrontEnd;
import main.InputOutput;
import main.Main;
import main.js.LogicMainJs;
import org.junit.jupiter.api.Test;
import utils.Base;
import utils.Err;
import java.util.Arrays;
import java.util.List;

public class TestJsCodegen {
  JsProgram getCode(String... content) {
    Main.resetAll();
    var vb = new CompilerFrontEnd.Verbosity(false, true, CompilerFrontEnd.ProgressVerbosity.None);
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
    assert content.length > 0;
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
    assert content.length > 0;
    assert expected.size() == fileName.size();
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
    import { test$$Test_0Impl } from "../test/Test_0Impl.js";
    import { base$$Void_0 } from "../base/Void_0.js";

    export class test$$Test_0 {
      static $self = new test$$Test_0Impl();
      $hash$imm(fear[###]$_m$) { throw new Error('Abstract method'); }
      static $hash$imm$fun(fear[###]$_m$, $this) {
        return base$$Void_0.$self;
      }
    }
    """,
    "test/Test_0.js",
    """
    package test
    alias base.Void as Void,
    Test:base.Main{ _ -> {} }
    """);
  }

  @Test void simpleProgram() {
    ok("""
    import { test$$B_0 } from "../test/B_0.js";
    import { test$$Test_0Impl } from "../test/Test_0Impl.js";
    import { base$$True_0 } from "../base/True_0.js";
    
    export class test$$Test_0 {
      static $self = new test$$Test_0Impl();
      m$imm() { throw new Error('Abstract method'); }
      b$imm() { throw new Error('Abstract method'); }
      static m$imm$fun($this) {
        return base$$True_0.$self;
      }
      static b$imm$fun($this) {
        return test$$B_0.$self;
      }
    }
    """,
    "test/Test_0.js",
    """
    package test
    Test:{ .m:base.Bool-> base.True, .b:B->B }
    B: {}
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
    """,
    """
    import { test$$East_0 } from "../test/East_0.js";
    import { test$$North_0Impl } from "../test/North_0Impl.js";
    
    export class test$$North_0 {
      static $self = new test$$North_0Impl();
      reverse$imm() { throw new Error('Abstract method'); }
      turn$imm() { throw new Error('Abstract method'); }
      static turn$imm$fun($this) {
        return test$$East_0.$self;
      }
    }
    """,
    """
    import { test$$North_0 } from "../test/North_0.js";
    import { test$$Direction_0 } from "../test/Direction_0.js";
    
    export class test$$North_0Impl extends test$$North_0 {
      reverse$imm() {
        return test$$Direction_0.reverse$imm$fun(this);
      }
      turn$imm() {
        return test$$North_0.turn$imm$fun(this);
      }
    }
    """),
    List.of("test/Direction_0.js", "test/North_0.js", "test/North_0Impl.js"),
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
      import { test$$Tanks_0Impl } from "../test/Tanks_0Impl.js";
      import { test$$Tank_0Impl } from "../test/Tank_0Impl.js";
      
      export class test$$Tanks_0 {
        static $self = new test$$Tanks_0Impl();
        of$imm(heading_m$, aiming_m$) { throw new Error('Abstract method'); }
        static of$imm$fun(heading_m$, aiming_m$, $this) {
          return new test$$Tank_0Impl(aiming_m$, heading_m$);
        }
      }
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
      """, """
      import { test$$Tank_0 } from "../test/Tank_0.js";
 
      export class test$$Tank_0Impl extends test$$Tank_0 {
        constructor(aiming_m$, heading_m$) {
          this.aiming_m$ = aiming_m$;
          this.heading_m$ = heading_m$;
        }
        aiming$imm() {
          return test$$Tank_0.aiming$imm$fun(this, this.aiming_m$);
        }
        heading$imm() {
          return test$$Tank_0.heading$imm$fun(this, this.heading_m$);
        }
      }
      """),
      List.of("test/Tanks_0.js", "test/Tank_0.js", "test/Tank_0Impl.js"),
      """
      package test
      Tanks: { .of(heading: Direction, aiming: Direction): Tank ->
        Tank: { .heading: Direction -> heading, .aiming: Direction -> aiming, }
      }
      Direction: {}
      """
    );
  }

  @Test void tanksWithSugar() {
    okList(List.of("""
    import { test$$East_0 } from "../test/East_0.js";
    import { test$$North_0Impl } from "../test/North_0Impl.js";
    
    export class test$$North_0 {
      static $self = new test$$North_0Impl();
      reverse$imm() { throw new Error('Abstract method'); }
      turn$imm() { throw new Error('Abstract method'); }
      static turn$imm$fun($this) {
        return test$$East_0.$self;
      }
    }
    """, """
    import { test$$Tanks_0Impl } from "../test/Tanks_0Impl.js";
    import { test$$Fear93$_0Impl } from "../test/Fear93$_0Impl.js";
    
    export class test$$Tanks_0 {
      static $self = new test$$Tanks_0Impl();
      $hash$imm(heading_m$, aiming_m$) { throw new Error('Abstract method'); }
      static $hash$imm$fun(heading_m$, aiming_m$, $this) {
        return new test$$Fear93$_0Impl(aiming_m$, heading_m$);
      }
    }
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
    import { test$$Turn270_0Impl } from "../test/Turn270_0Impl.js";
    
    export class test$$Turn270_0 {
      static $self = new test$$Turn270_0Impl();
      $hash$imm(fear[###]$_m$) { throw new Error('Abstract method'); }
      static $hash$imm$fun(fear[###]$_m$, $this) {
        return fear[###]$_m$.turn$imm().turn$imm().turn$imm();
      }
    }
    """, """
    import { test$$Turn270_0 } from "../test/Turn270_0.js";
    
    export class test$$Turn270_0Impl extends test$$Turn270_0 {
      $hash$imm(fear[###]$_m$) {
        return test$$Turn270_0.$hash$imm$fun(fear3$_m$, this);
      }
    }
    """),
    List.of("test/Rotation_0.js", "test/Turn270_0.js", "test/Turn270_0Impl.js"),
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
    import { test$$Fear89$_0Impl } from "../test/Fear89$_0Impl.js";
    
    export class test$$Rotation_0 {
      $plus$imm(r_m$) { throw new Error('Abstract method'); }
      $hash$imm(d_m$) { throw new Error('Abstract method'); }
      static $plus$imm$fun(r_m$, $this) {
        return new test$$Fear89$_0Impl(r_m$, $this);
      }
    }
    """, """
    import { test$$Rotation_0 } from "../test/Rotation_0.js";
    import { test$$Fear89$_0 } from "../test/Fear89$_0.js";
    
    export class test$$Fear89$_0Impl extends test$$Fear89$_0 {
      constructor(r_m$, $this) {
        this.r_m$ = r_m$;
        this.$this = $this;
      }
      $plus$imm(r_m$) {
        return test$$Rotation_0.$plus$imm$fun(r_m$, this);
      }
      $hash$imm(d_m$) {
        return test$$Fear89$_0.$hash$imm$fun(d_m$, this, this.r_m$, this.$this);
      }
    }
    """),
    List.of("test/Rotation_0.js", "test/Fear89$_0Impl.js"),
  """
    package test
    Direction: { .turn: Direction }
    Rotation: {
      #(d: Direction):Direction,
      +(r: Rotation): Rotation-> { d -> this#( r#(d) ) }
    }
    """);
  }

  @Test void turnTurret() {
    okList(List.of("""
    import { test$$Tanks_0 } from "../test/Tanks_0.js";
    
    export class test$$Tank_0 {
      turnTurret$imm(r_m$) { throw new Error('Abstract method'); }
      aiming$imm() { throw new Error('Abstract method'); }
      heading$imm() { throw new Error('Abstract method'); }
      static turnTurret$imm$fun(r_m$, $this) {
        return test$$Tanks_0.$self.$hash$imm($this.heading$imm(),r_m$.$hash$imm($this.aiming$imm()));
      }
    }
    """, """
    import { test$$Tanks_0Impl } from "../test/Tanks_0Impl.js";
    import { test$$Fear89$_0Impl } from "../test/Fear89$_0Impl.js";
    
    export class test$$Tanks_0 {
      static $self = new test$$Tanks_0Impl();
      $hash$imm(heading_m$, aiming_m$) { throw new Error('Abstract method'); }
      static $hash$imm$fun(heading_m$, aiming_m$, $this) {
        return new test$$Fear89$_0Impl(aiming_m$, heading_m$);
      }
    }
    """, """
    import { test$$Tank_0 } from "../test/Tank_0.js";
    import { test$$Fear89$_0 } from "../test/Fear89$_0.js";
    
    export class test$$Fear89$_0Impl extends test$$Fear89$_0 {
      constructor(aiming_m$, heading_m$) {
        this.aiming_m$ = aiming_m$;
        this.heading_m$ = heading_m$;
      }
      turnTurret$imm(r_m$) {
        return test$$Tank_0.turnTurret$imm$fun(r_m$, this);
      }
      aiming$imm() {
        return test$$Fear89$_0.aiming$imm$fun(this, this.aiming_m$);
      }
      heading$imm() {
        return test$$Fear89$_0.heading$imm$fun(this, this.heading_m$);
      }
    }
    """),
    List.of("test/Tank_0.js", "test/Tanks_0.js", "test/Fear89$_0Impl.js"),
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
    import { test$$Points_0 } from "../test/Points_0.js";
    import { test$$East_0 } from "../test/East_0.js";
    import { test$$North_0Impl } from "../test/North_0Impl.js";
    
    export class test$$North_0 {
      static $self = new test$$North_0Impl();
      point$imm() { throw new Error('Abstract method'); }
      turn$imm() { throw new Error('Abstract method'); }
      static turn$imm$fun($this) {
        return test$$East_0.$self;
      }
      static point$imm$fun($this) {
        return test$$Points_0.$self.$hash$imm(-1n,0n);
      }
    }
    """, """
    import { test$$Fear90$_0Impl } from "../test/Fear90$_0Impl.js";
    import { test$$Points_0Impl } from "../test/Points_0Impl.js";
    
    export class test$$Points_0 {
      static $self = new test$$Points_0Impl();
      $hash$imm(x_m$, y_m$) { throw new Error('Abstract method'); }
      static $hash$imm$fun(x_m$, y_m$, $this) {
        return new test$$Fear90$_0Impl(x_m$, y_m$);
      }
    }
    """, """
    import { test$$Fear90$_0 } from "../test/Fear90$_0.js";
    import { test$$Point_0 } from "../test/Point_0.js";
    
    export class test$$Fear90$_0Impl extends test$$Fear90$_0 {
      constructor(x_m$, y_m$) {
        this.x_m$ = x_m$;
        this.y_m$ = y_m$;
      }
      move$imm(d_m$) {
        return test$$Point_0.move$imm$fun(d_m$, this);
      }
      y$imm() {
        return test$$Fear90$_0.y$imm$fun(this, this.y_m$);
      }
      $plus$imm(other_m$) {
        return test$$Point_0.$plus$imm$fun(other_m$, this);
      }
      x$imm() {
        return test$$Fear90$_0.x$imm$fun(this, this.x_m$);
      }
    }
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
    List.of("test/North_0.js", "test/Points_0.js", "test/Fear90$_0Impl.js", "test/Point_0.js"),
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
    import { test$$Left_0Impl } from "../test/Left_0Impl.js";
    
    export class test$$Left_0 {
      static $self = new test$$Left_0Impl();
      choose$imm(l_m$, r_m$) { throw new Error('Abstract method'); }
      static choose$imm$fun(l_m$, r_m$, $this) {
        return l_m$;
      }
    }
    """, """
    import { test$$Left_0 } from "../test/Left_0.js";
    
    export class test$$Left_0Impl extends test$$Left_0 {
      choose$imm(l_m$, r_m$) {
        return test$$Left_0.choose$imm$fun(l_m$, r_m$, this);
      }
    }
    """, """
    export class test$$Fork_0 {
      choose$imm(leftVal_m$, rightVal_m$) { throw new Error('Abstract method'); }
    }
    """),
    List.of("test/Left_0.js", "test/Left_0Impl.js", "test/Fork_0.js"),
    """
    package test
    Fork : { .choose[Val](leftVal: Val, rightVal: Val): Val, }
    Left : Fork{ l,r -> l }
    Right: Fork{ l,r -> r }
    """);
  }

  @Test void genericType() {
    okList(List.of("""
    import { test$$Right_0Impl } from "../test/Right_0Impl.js";
    
    export class test$$Right_0 {
      static $self = new test$$Right_0Impl();
      choose$imm(fear[###]$_m$) { throw new Error('Abstract method'); }
      static choose$imm$fun(fear[###]$_m$, $this) {
        return fear[###]$_m$.right$imm();
      }
    }
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
    import { test$$Bot_0Impl } from "../test/Bot_0Impl.js";
    import { base$$True_0 } from "../base/True_0.js";
    
    export class test$$Bot_0 {
      static $self = new test$$Bot_0Impl();
      message$imm(s_m$) { throw new Error('Abstract method'); }
      static message$imm$fun(s_m$, $this) {
        return (s_m$.$equals$equals$imm("hello") == base$$True_0.$self ? "Hi, I'm Bot; how can I help you?" : (s_m$.$equals$equals$imm("bye") == base$$True_0.$self ? "goodbye!" : "I don't understand"));
      }
    }
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
    import { test$$Test_0Impl } from "../test/Test_0Impl.js";
    import { base$$True_0 } from "../base/True_0.js";
    
    export class test$$Test_0 {
      static $self = new test$$Test_0Impl();
      m$imm(b_m$) { throw new Error('Abstract method'); }
      static m$imm$fun(b_m$, $this) {
        return (b_m$ == base$$True_0.$self ? $this : $this.m$imm(b_m$));
      }
    }
    """,
      "test/Test_0.js",
      """
      package test
      Test:{ .m(b: base.Bool):Test-> b ? {.then->this, .else->this.m(b), } }
      """);
  }

  @Test void optional() {
    okList(List.of("""
    import { test$$Fear90$_0 } from "../test/Fear90$_0.js";
    import { test$$Test_0Impl } from "../test/Test_0Impl.js";
    
    export class test$$Test_0 {
      static $self = new test$$Test_0Impl();
      getAge$imm(p_m$) { throw new Error('Abstract method'); }
      static getAge$imm$fun(p_m$, $this) {
        return p_m$.match$imm(test$$Fear90$_0.$self);
      }
    }
    """, """
    import { test$$Fear90$_0Impl } from "../test/Fear90$_0Impl.js";
    
    export class test$$Fear90$_0 {
      static $self = new test$$Fear90$_0Impl();
      some$mut(p$_m$) { throw new Error('Abstract method'); }
      empty$mut() { throw new Error('Abstract method'); }
      static empty$mut$fun(fear[###]$_m$) {
        return 0n;
      }
      static some$mut$fun(p$_m$, fear[###]$_m$) {
        return p$_m$.age$imm();
      }
    }
    """),
    List.of("test/Test_0.js", "test/Fear90$_0.js"),
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

  @Test void stack() {
    okList(List.of("""
    import { test$$Fear89$_1Impl } from "../test/Fear89$_1Impl.js";
    import { test$$Stack_1Impl } from "../test/Stack_1Impl.js";
    
    export class test$$Stack_1 {
      static $self = new test$$Stack_1Impl();
      $plus$imm(e_m$) { throw new Error('Abstract method'); }
      match$imm(m_m$) { throw new Error('Abstract method'); }
      static match$imm$fun(m_m$, $this) {
        return m_m$.empty$imm();
      }
      static $plus$imm$fun(e_m$, $this) {
        return new test$$Fear89$_1Impl(e_m$, $this);
      }
    }
    """, """
    import { test$$Stack_1 } from "../test/Stack_1.js";
    import { test$$Fear89$_1 } from "../test/Fear89$_1.js";
    
    export class test$$Fear89$_1Impl extends test$$Fear89$_1 {
      constructor(e_m$, $this) {
        this.e_m$ = e_m$;
        this.$this = $this;
      }
      $plus$imm(e_m$) {
        return test$$Stack_1.$plus$imm$fun(e_m$, this);
      }
      match$imm(m_m$) {
        return test$$Fear89$_1.match$imm$fun(m_m$, this, this.e_m$, this.$this);
      }
    }
    """, """
    export class test$$Fear89$_1 {
      $plus$imm(e_m$) { throw new Error('Abstract method'); }
      match$imm(m_m$) { throw new Error('Abstract method'); }
      static match$imm$fun(m_m$, fear[###]$_m$, e_m$, $this) {
        return m_m$.elem$imm(e_m$,$this);
      }
    }
    """),
    List.of("test/Stack_1.js", "test/Fear89$_1Impl.js", "test/Fear89$_1.js"),
    """
    package test
    alias base.Nat as Nat,
    Stack[T]: {
      .match[R](m: StackMatch[T,R]): R -> m.empty,
      +(e: T): Stack[T] -> { .match(m) -> m.elem(e, this) },
    }
    StackMatch[T,R]: {
      .empty: R,
      .elem(top:T, tail: Stack[T]): R,
    }
    """);
  }

  @Test void stackMap() {
    okList(List.of(
    """
    import { test$$ExampleSum5_0Impl } from "../test/ExampleSum5_0Impl.js";
    import { test$$Fear92$_0 } from "../test/Fear92$_0.js";
    
    export class test$$ExampleSum5_0 {
      static $self = new test$$ExampleSum5_0Impl();
      $hash$imm(ns_m$) { throw new Error('Abstract method'); }
      static $hash$imm$fun(ns_m$, $this) {
        return ns_m$.map$imm(test$$Fear92$_0.$self);
      }
    }
    """, """
    import { test$$Fear92$_0Impl } from "../test/Fear92$_0Impl.js";
    
    export class test$$Fear92$_0 {
      static $self = new test$$Fear92$_0Impl();
      $hash$read(n_m$) { throw new Error('Abstract method'); }
      static $hash$read$fun(n_m$, fear[###]$_m$) {
        return (BigInt(n_m$) + 5n);
      }
    }
    """, """
    import { test$$Fear90$_1Impl } from "../test/Fear90$_1Impl.js";
    import { test$$Stack_1Impl } from "../test/Stack_1Impl.js";
    
    export class test$$Stack_1 {
      static $self = new test$$Stack_1Impl();
      fold$imm(start_m$, f_m$) { throw new Error('Abstract method'); }
      $plus$imm(e_m$) { throw new Error('Abstract method'); }
      match$imm(m_m$) { throw new Error('Abstract method'); }
      map$imm(f_m$) { throw new Error('Abstract method'); }
      static match$imm$fun(m_m$, $this) {
        return m_m$.empty$imm();
      }
      static fold$imm$fun(start_m$, f_m$, $this) {
        return start_m$;
      }
      static map$imm$fun(f_m$, $this) {
        return test$$Stack_1.$self;
      }
      static $plus$imm$fun(e_m$, $this) {
        return new test$$Fear90$_1Impl(e_m$, $this);
      }
    }
    """
    ),
    List.of("test/ExampleSum5_0.js", "test/Fear92$_0.js", "test/Stack_1.js"),
  """
    package test
    alias base.Nat as Nat,
    alias base.F as F,
    StackMatch[T,R]: { .empty: R, .elem(top:T, tail: Stack[T]): R, }
    Stack[T]: {
      .match[R](m: StackMatch[T,R]): R -> m.empty,
      .fold[R](start:R, f: F[R,T,R]): R -> start,
      .map[R](f: F[T, R]): Stack[R] -> {},
      +(e: T): Stack[T] -> {
        .match(m) -> m.elem(e, this),
        .fold(start, f) -> f#(this.fold(start, f), e),
        .map(f) -> this.map(f) + ( f#(e) ),
        },
      }
    ExampleSum5: {  #(ns: Stack[Nat]): Stack[Nat] -> ns.map { n -> n + 5 }  }
    """);
  }

  @Test void blockLetDoRet() {
    ok("""
    import { test$$ForceGen_0 } from "../test/ForceGen_0.js";
    import { test$$Test_0Impl } from "../test/Test_0Impl.js";
    import { base$$Void_0 } from "../base/Void_0.js";
    
    export class test$$Test_0 {
      static $self = new test$$Test_0Impl();
      $hash$imm(fear0$_m$) { throw new Error('Abstract method'); }
      static $hash$imm$fun(fear0$_m$, $this) {
        let n = 5n;
    var doRes1 = test$$ForceGen_0.$self.$hash$imm();
    return base$$Void_0.$self;
      }
    }
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
    import { test$$ForceGen_0 } from "../test/ForceGen_0.js";
    import { test$$Test_0Impl } from "../test/Test_0Impl.js";
    import { base$$Void_0 } from "../base/Void_0.js";
    import { base$$Vars_0 } from "../base/Vars_0.js";
    
    export class test$$Test_0 {
      static $self = new test$$Test_0Impl();
      $hash$imm(fear0$_m$) { throw new Error('Abstract method'); }
      static $hash$imm$fun(fear0$_m$, $this) {
        var n = base$$Vars_0.$self.$hash$imm("Hi");
    var doRes1 = test$$ForceGen_0.$self.$hash$imm();
    return base$$Void_0.$self;
      }
    }
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
    import { test$$ForceGen_0 } from "../test/ForceGen_0.js";
    import { base$$Infos_0 } from "../base/Infos_0.js";
    import { test$$Test_0Impl } from "../test/Test_0Impl.js";
    import { base$$Void_0 } from "../base/Void_0.js";
    import { base$$Vars_0 } from "../base/Vars_0.js";
    import { base$$True_0 } from "../base/True_0.js";
    
    export class test$$Test_0 {
      static $self = new test$$Test_0Impl();
      $hash$imm(fear0$_m$) { throw new Error('Abstract method'); }
      static $hash$imm$fun(fear0$_m$, $this) {
        let n = 5n;
    let n2 = 10n;
    var n3 = base$$Vars_0.$self.$hash$imm(15n);
    if ((BigInt(n3_m$.get$mut()) === (BigInt(BigInt(n_m$)) + n2_m$)) == base$$True_0.$self) { throw base$$Infos_0.$self.msg$imm("oh no");
     }
    var doRes1 = test$$ForceGen_0.$self.$hash$imm();
    return base$$Void_0.$self;
      }
    }
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
    import { test$$Test_0Impl } from "../test/Test_0Impl.js";
    import { base$$Void_0 } from "../base/Void_0.js";
    import { base$$True_0 } from "../base/True_0.js";
    
    export class test$$Test_0 {
      static $self = new test$$Test_0Impl();
      $hash$imm(fear0$_m$) { throw new Error('Abstract method'); }
      static $hash$imm$fun(fear0$_m$, $this) {
        return (base$$True_0.$self == base$$True_0.$self ? (() => {
    return base$$Void_0.$self})() : (() => {
    return base$$Void_0.$self})());
      }
    }
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

  @Test void sysPrint() {
    ok("""
    import { test$$Test_0Impl } from "../test/Test_0Impl.js";
    
    export class test$$Test_0 {
      static $self = new test$$Test_0Impl();
      $hash$imm(sys_m$) { throw new Error('Abstract method'); }
      static $hash$imm$fun(sys_m$, $this) {
        return sys_m$.io$mut().println$mut("Hello World");
      }
    }
    """,
  "test/Test_0.js",
  """
    package test
    alias base.Main as Main,
    Test: Main{sys -> sys.io.println("Hello World") }
    """);
  }
}