package codegen.js;

import main.CompilerFrontEnd;
import main.InputOutput;
import main.Main;
import main.js.LogicMainJs;
import org.junit.jupiter.api.Test;
import utils.Base;
import utils.Err;
import java.util.Arrays;

public class TestJsCodegen {
  void ok(String expected, String fileName, String... content) {
    assert content.length > 0;
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
    var fileCode = code.files().stream()
      .filter(f -> f.toUri().toString().endsWith(fileName))
      .map(JsFile::code)
      .findFirst().orElseThrow();
    Err.strCmp(expected, fileCode);
  }
//  @Test void emptyProgramAlias() {
//    ok("""
//    import * as base from "../base/index.js";
//
//    export class Test_0 extends base.Main_0 {
//      static $self = new Test_0();
//      $hash$imm(fear0$_m$) {
//      return base.Void_0.$self;
//    }
//    static $hash$imm$fun(fear0$_m$, $this) {
//      return $this.$hash$imm(fear0$_m$, $this);
//    }
//    }
//    """,
//    "test/Test_0.js",
//    """
//    package test
//    alias base.Void as Void,
//    Test:base.Main{ _ -> {} }
//    """);
//  }

  @Test void blockLetDoRet() {
    ok("""
    import { base__Void_0 } from "../base/Void_0.js";
    import { base__Main_0 } from "../base/Main_0.js";
    import { test__ForceGen_0 } from "../test/ForceGen_0.js";
        
    export class test__Test_0 extends base__Main_0 {
      static $self = new test__Test_0();
      $hash$imm(fear0$_m$) {
      let n = 5;
    let nn = -2;
    var doRes1 = test__ForceGen_0.$self.$hash$imm();
    return base__Void_0.$self;
    }
    static $hash$imm$fun(fear0$_m$, $this) {
      return $this.$hash$imm(fear0$_m$, $this);
    }
    }
    """,
    "test/Test_0.js",
    """
    package test
    alias base.Int as Int, alias base.Str as Str, alias base.Block as Block, alias base.Void as Void,
    Test:base.Main{ _ -> Block#
     .let[Int] n = {+5}
     .let[Int] nn = {-2}
     .do {ForceGen#}
     .return {Void}
    }
    ForceGen: {#: Void -> {}}
    """);
  }

//  @Test void number() {
//    ok("""
//     class Test_0 extends Main_0 {
//       static $self = new Test_0();
//       static $hash$imm$fun(fear31$_m$, $this) {
//       let n = 5;
//     let nn = -2;
//     return base.Void_0.$self;
//     }
//     }
//     """,
//      "test/Test_0.js",
//      """
//      package test
//      alias base.Int as Int, alias base.Str as Str, alias base.Block as Block, alias base.Void as Void,
//      Test:base.Main{ _ -> Block#
//        .let[Int] nInt = {+5}
//        .let[Int] nIntM = {-2}
//        .return {Void}
//      }
//      """);
//  }

//  @Test void number() {
//    ok("""
//     class Test_0 extends Main_0 {
//       static $self = new Test_0();
//       static $hash$imm$fun(fear31$_m$, $this) {
//       let n = 5;
//     let nn = -2;
//     return base.Void_0.$self;
//     }
//     }
//     """,
//      "test/Test_0.js",
//      """
//      package test
//      alias base.Int as Int, alias base.Str as Str, alias base.Block as Block, alias base.Void as Void,
//      Test:base.Main{ _ -> Block#
//        .let[Int] nInt = {+5}
//        .let[Int] nIntM = {-2}
//        .return {Void}
//      }
//      """);
//  }

//  @Test void asNonIdFn() {
//    ok("""
//    export class Test_0 extends Main_0 {
//     static $self = new Test_0();
//     static $hash$imm$fun(sys_m$, $this) {
//     return base.Block_0.$self.$hash$imm(base.List_0.$self.$hash$imm().as$read(test.Fear[###]$_0.$self));
//   }
//   }
//   """,
//    "test/Test_0.js",
//    """
//    package test
//    Test:base.Main{sys -> Block#(List#[Nat].as{x->x * 2})}
//    """, Base.mutBaseAliases);
//  }

}