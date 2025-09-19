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

public class TestHtmlDemo {
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
  @Test void todolist() {
    ok("""
    """,
    "test/Test_0.js",
    """
    package test
    
    Todo: Main {
      sys -> Block#
        .let doc = Dom#document
        .let input = doc.getElementById "todoInput"
        .let addBtn = doc.getElementById "addBtn"
        .let list = doc.getElementById "todoList"
    
        // handler for adding a task
        .let addTask = Block#(
          .let text = input.value.trim
          ?[text.isEmpty] { .then -> Block#(.return {Void}), .else -> Block#{} }
          .let li = doc.createElement "li"
    
          .let span = doc.createElement "span"
          span.setText text
          span.addEventListener "click" (Block#(
            li.toggleClass "completed"
          ))
    
          .let delBtn = doc.createElement "button"
          delBtn.setText "✕"
          delBtn.addEventListener "click" (Block#(
            li.remove
          ))
    
          li.appendChild span
          li.appendChild delBtn
          list.appendChild li
    
          input.setValue ""
          input.focus
        )
    
        addBtn.addEventListener "click" addTask
        input.addEventListenerKey "Enter" addTask
    
        .return {Void}
    }
    """, Base.mutBaseAliases);
  }
}