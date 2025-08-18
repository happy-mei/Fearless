package codegen.js;

import main.CompilerFrontEnd;
import main.InputOutput;
import main.Main;
import main.js.LogicMainJs;
import org.junit.jupiter.api.Test;
import utils.Base;
import utils.Err;
import utils.RunOutput;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static codegen.java.RunJavaProgramTests.ok;

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
  @Test void emptyProgram() {
    ok("""
    import { Main_0 } from '../base/Main_0.js';
    import { Void_0 } from '../base/Void_0.js';
    
    export class Test_0 extends Main_0 {
        static $self = new Test_0();
        $hash$imm(fear$_m$) {
            return Void_0.$self;
        }
    }
     """,
    "test/Test_0.js",
    """
    package test
    alias base.Main as Main,
    alias base.Void as Void,
    Test:Main{ _ -> {} }
    """);
  }

//  @Test
//  void testFunctionGeneration() {
//    ok("""
//       function Test_0() {
//         return {
//          $self: {
//            $hash$imm: function(fear_m$) {
//              let n_m$ = 5;
//              let doRes1 = ForceGen_0.$self.$hash$imm();
//              return base_Void_0.$self;
//            }
//          }
//         }
//        };
//        """,
//      "test/Test_0.js",
//      """
//        package test
//        alias base.Int as Int, alias base.Block as Block, alias base.Void as Void,
//        Test:base.Main {_ -> Block#
//         .let[Int] n = {+5}
//         .do {ForceGen#}
//         .return {Void}
//         }
//        ForceGen: {#: Void -> {}}
//        """);
//  }

//  @Test void asNonIdFn() {ok("""
//    package test;
//    public interface Test_0 extends base.Main_0{
//    Test_0 $self = new Test_0Impl();
//    base.Void_0 $hash$imm(base.caps.System_0 sys_m$);
//    static base.Void_0 $hash$imm$fun(base.caps.System_0 sys_m$, test.Test_0 $this) {
//      return base.Block_0.$self.$hash$imm(((base.List_1)rt.ListK.$self.$hash$imm()).as$read(test.Fear[###]$_0.$self));
//    }
//    }
//    """, "/test_codegen/test_codegen.js", """
//    package test_codegen
//    Test: Main{sys -> Block#(List#[Nat].as{x->x * 2})}
//    """, Base.mutBaseAliases);}
//  }
}