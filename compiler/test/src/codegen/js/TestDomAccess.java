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

import static codegen.js.RunJsProgramTests.ok;

public class TestDomAccess {
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
  @Test void dom() {ok("""
    import { base$$Void_0 } from "../base/index.js";
    import { rt$$Document } from "../rt-js/Document.js";
    import { rt$$Str } from "../rt-js/Str.js";
    
    export class test$$Test_0 {
      static $hash$imm$2$fun(fear31$_m$, $this) {
        let doc_m$ = rt$$Document.$self;
    let input_m$ = rt$$Document.$self.getElementById(rt$$Str.fromJsStr("todoInput"));
    let btn_m$ = rt$$Document.$self.createElement(rt$$Str.fromJsStr("button"));
    return base$$Void_0.$self;
      }
    }
    
    export class test$$Test_0Impl {
      $hash$imm$1(fear31$_m$) { return test$$Test_0.$hash$imm$2$fun(fear31$_m$, this); }
    }
    
    test$$Test_0.$self = new test$$Test_0Impl();
    """,
    "test/Test_0.js",
    """
    package test
    alias base.Document as Document, alias base.Element as Element, alias base.Main as Main, alias base.Block as Block, alias base.Void as Void,
    Test: Main{_ -> Block#
      .let[Document] doc = { Document }
      .let[Element] input = { doc.getElementById("todoInput") }
      .let[Element] btn = { doc.createElement("button") }
      .return{{}}
    }
    """);
  }
  @Test void TaskManager() {
    ok("""
    """,
    "todo/TaskManager_0.js",
    """
    package todo
    alias base.Document as Document, alias base.Element as Element, alias base.Main as Main, alias base.Block as Block, alias base.Void as Void,
    TaskManager:{
      // captured DOM nodes
      .doc   : Document,
      .input : Element,
      .addBtn: Element,
      .list  : Element,
    
      // a method that builds the addTask block
      .addTask:Void -> Block#
        .if { input.value.trim == "" } .return { Void }
        .let[Str] text = { input.value.trim }
        .let[Element] li   = { doc.createElement("li") }
        .let[Element] span = { doc.createElement("span") }
        .do { span.setText(text) }
        .do { span.addEventListener("click", Block#{
          .do { li.toggleClass("completed") }
        }) }
  
        .let[Element] delBtn = { doc.createElement("button") }
        .do { delBtn.setText("✕") }
        .do { delBtn.addEventListener("click", Block#{
          .do { li.remove }
        }) }
  
        .do { li.appendChild(span) }
        .do { li.appendChild(delBtn) }
        .do { list.appendChild(li) }
  
        .do { input.setValue("") }
        .do { input.focus }
        .return{{}},
    
      // initialize wiring
      .init:Void -> Block#
        .do { addBtn.addEventListener("click", addTask()) }
        .do { input.addEventListenerKey("Enter", addTask()) }
        .return{{}}
    }
    """);
  }
}


// Define addTask block
//        .let[Block[Void]] addTask =
//Block#{
//  .let[Str] text = { input.value.trim }
//  .if { text == "" } .return { Void }
//
//  .let[Element] li   = { doc.createElement("li") }
//  .let[Element] span = { doc.createElement("span") }
//  .do { span.setText(text) }
//  .do { span.addEventListener("click", Block#{
//              .do { li.toggleClass("completed") }
//}) }
//
//  .let[Element] delBtn = { doc.createElement("button") }
//  .do { delBtn.setText("✕") }
//  .do { delBtn.addEventListener("click", Block#{
//              .do { li.remove }
//}) }
//
//  .do { li.appendChild(span) }
//  .do { li.appendChild(delBtn) }
//  .do { list.appendChild(li) }
//
//  .do { input.setValue("") }
//  .do { input.focus }
//  }
