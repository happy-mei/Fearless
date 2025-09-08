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
     class Test_0 extends Main_0 {
         static $self = new Test_0();
         static $hash$imm$fun(fear[###]$_m$, $this) {
         return base.Void_0.$self;
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

//  @Test void number() {
//    ok("""
//      class Test_0 extends Main_0 {
//          static $self = new Test_0();
//          static $hash$imm$fun(fear[###]$_m$, $this) {
//              let n = 5;
//              let nn = -2;
//              return base.Void_0.$self;
//          }
//      }
//     """,
//    "test/Test_0.js",
//    """
//    package test
//    alias base.Int as Int, alias base.Str as Str, alias base.Block as Block, alias base.Void as Void,
//    Test:base.Main{ _ -> Block#
//       .let[Int] n = {+5}
//       .let[Int] nn = {-2}
//       .return {Void}
//    }
//    """);
//  }

}