package codegen.js;

import main.CompilerFrontEnd;
import main.InputOutput;
import main.Main;
import main.js.LogicMainJs;
import org.junit.jupiter.api.Test;
import utils.Base;
import utils.Err;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
  }
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
  String insertMainScript(String html, String mainJsfileName) {
    String mainJsModuleName = mainJsfileName
      .replace("/", "$$")
      .replace(".js", "");  // e.g., "todolist/App_0.js" -> "todolist$$App_0"
    String scriptTag = """
    <script type="module">
      import { rt$$NativeRuntime } from '../rt-js/NativeRuntime.js';
      import { %s } from './%s';
      rt$$NativeRuntime.ensureWasm();
      %s.$self.$hash$imm$1(null);
    </script>
    """.formatted(mainJsModuleName, mainJsfileName, mainJsModuleName);
    // Ensure we insert before the closing </body> tag
    int bodyIndex = html.lastIndexOf("</body>");
    return html.substring(0, bodyIndex) + scriptTag + html.substring(bodyIndex);
  }
  void createApp(String htmlContent, String mainJsfileName, String... content) throws IOException {
    assert content.length > 0 : "Content must not be empty";
    // 1) Compile
    JsProgram code = getCode(content);
    // 2) Write all generated JS files to a real directory (tmp)
    Path tmpOut = java.nio.file.Files.createTempDirectory("fgenjs");
    code.writeJsFiles(tmpOut);
    // 3) Find the directory containing the main JS file within tmpOut
    Path mainJsPath = tmpOut.resolve(mainJsfileName); //    e.g., tmpOut / "todolist/App_0.js"
    Path outputDir  = mainJsPath.getParent();
    java.nio.file.Files.createDirectories(outputDir);
    // 4) Insert a script tag that points to the main file, from the same directory
    String htmlWithScript = insertMainScript(htmlContent, mainJsfileName);
    // 5) Write index.html under tmpOut
    Path htmlPath = tmpOut.resolve("index.html");
    java.nio.file.Files.writeString(htmlPath, htmlWithScript);
    System.out.println("✅ Wrote " + tmpOut.toUri());
  }
  @Test void dom() {ok("""
    import { base$$Void_0 } from "../base/Void_0.js";
    import { rt$$Documents } from "../rt-js/Documents.js";
    import { rt$$Str } from "../rt-js/Str.js";
    
    export class test$$Test_0 {
      static $hash$imm$2$fun(fear31$_m$, $this) {
        let doc_m$ = rt$$Documents.$self.create$read$0();
    let input_m$ = doc_m$.getElementById$read$1(rt$$Str.fromJsStr("todoInput")).$exclamation$mut$0();
    let addBtn_m$ = doc_m$.getElementById$read$1(rt$$Str.fromJsStr("addBtn")).$exclamation$mut$0();
    let list_m$ = doc_m$.getElementById$read$1(rt$$Str.fromJsStr("todoList")).$exclamation$mut$0();
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
    alias base.Documents as Documents, alias base.Document as Document, alias base.Element as Element, alias base.Main as Main, alias base.Block as Block, alias base.Void as Void,
    Test: Main{ _ -> Block#
      .let[mut Document] doc  = { Documents.create() }
      .let[mut Element] input = { doc.getElementById("todoInput")! }
      .let[mut Element] addBtn = { doc.getElementById("addBtn")! }
      .let[mut Element] list = { doc.getElementById("todoList")! }
      .return{{}}
    }
    """);
  }
  @Test void minimalApp() {ok("""
    import { base$$Void_0 } from "../base/Void_0.js";
    import { rt$$Documents } from "../rt-js/Documents.js";
    import { rt$$Str } from "../rt-js/Str.js";
    import { test$$Fear724$_0 } from "../test/Fear724$_0.js";
    
    export class test$$Test_0 {
      static $hash$imm$2$fun(fear31$_m$, $this) {
        let doc_m$ = rt$$Documents.$self.create$read$0();
    let addBtn_m$ = doc_m$.getElementById$read$1(rt$$Str.fromJsStr("addBtn")).$exclamation$mut$0();
    var doRes1 = addBtn_m$.onClick$mut$1(test$$Fear724$_0.$self);
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
    alias base.Documents as Documents, alias base.Document as Document, alias base.Element as Element,
    Test: base.Main{ _ -> base.Block#
      .let[mut Document] doc  = { Documents.create() }
      .let[mut Element] addBtn = { doc.getElementById("addBtn")! }
      .do{
         addBtn.onClick({ev, d -> base.Block#
           .do{ ev.target.setClass("clicked") }
           .return{ base.Void }
         })
      }
      .return{ base.Void }
    }
    """);
  }
  @Test void addTask() {
    ok("""
    import { base$$True_0 } from "../base/True_0.js";
    import { base$$Void_0 } from "../base/Void_0.js";
    import { rt$$Str } from "../rt-js/Str.js";
    import { todo$$Fear20$_0 } from "../todo/Fear20$_0.js";
    import { todo$$Fear30$_0 } from "../todo/Fear30$_0.js";
    
    export class todo$$AddTask_0 {
      static $hash$read$3$fun(e_m$, dom_m$, $this) {
        let input_m$ = dom_m$.getElementById$read$1(rt$$Str.fromJsStr("todoInput")).$exclamation$mut$0();
    let list_m$ = dom_m$.getElementById$read$1(rt$$Str.fromJsStr("todoList")).$exclamation$mut$0();
    let text_m$ = input_m$.value$read$0();
    if (text_m$.$equals$equals$imm$1(rt$$Str.fromJsStr("")) == base$$True_0.$self) { return base$$Void_0.$self; }
    let li_m$ = dom_m$.createElement$mut$1(rt$$Str.fromJsStr("li"));
    let span_m$ = dom_m$.createElement$mut$1(rt$$Str.fromJsStr("span"));
    var doRes1 = span_m$.setText$mut$1(text_m$);
    var doRes2 = span_m$.onClick$mut$1(todo$$Fear20$_0.$self);
    let delBtn_m$ = dom_m$.createElement$mut$1(rt$$Str.fromJsStr("button"));
    var doRes3 = delBtn_m$.setText$mut$1(rt$$Str.fromJsStr("x"));
    var doRes4 = delBtn_m$.onClickWith$mut$2(li_m$,todo$$Fear30$_0.$self);
    var doRes5 = li_m$.appendChild$mut$1(span_m$);
    var doRes6 = li_m$.appendChild$mut$1(delBtn_m$);
    var doRes7 = list_m$.appendChild$mut$1(li_m$);
    var doRes8 = input_m$.setValue$mut$1(rt$$Str.fromJsStr(""));
    var doRes9 = input_m$.focus$mut$0();
    return base$$Void_0.$self;
      }
    }
    
    export class todo$$AddTask_0Impl {
      $hash$read$2(e_m$, dom_m$) { return todo$$AddTask_0.$hash$read$3$fun(e_m$, dom_m$, this); }
    }
    
    todo$$AddTask_0.$self = new todo$$AddTask_0Impl();
    """,
    "todo/AddTask_0.js",
    """
    package todo
    alias base.Document as Document, alias base.Documents as Documents, alias base.Element as Element, alias base.Main as Main, alias base.Block as Block, alias base.Void as Void, alias base.F as F, alias base.Event as Event, alias base.Str as Str,
    AddTask: F[Event, mut Document, Void]{ e, dom -> Block#
      // obtain input and list elements
      .let[mut Element] input = { dom.getElementById("todoInput")! }
      .let[mut Element] list  = { dom.getElementById("todoList")! }
      // read text
      .let[Str] text = { input.value }
      // if empty, do nothing line 9
      .if { text == "" }
      .return { Void }
       // create <li> and <span>
       .let[mut Element] li   = { dom.createElement("li") }
       .let[mut Element] span = { dom.createElement("span") }
       .do{ span.setText(text) }
  
       // toggle completion on span click line17
       .do{
         span.onClick({ev, d -> Block#
           .do{ ev.target.setClass("completed") }
           .return{ Void }
         })
       }
       // create delete button line 24
       .let[mut Element] delBtn = { dom.createElement("button") }
       .do{ delBtn.setText("x") }
       .do{
         delBtn.onClickWith(li, { ev, d, el -> Block#
           .do{ el.remove }
           .return{ Void }
         })
       }
  
       // attach line36
       .do{ li.appendChild(span) }
       .do{ li.appendChild(delBtn) }
       .do{ list.appendChild(li) }
  
       // reset input
       .do{ input.setValue("") }
       .do{ input.focus }
       .return{ Void }
    }
    """);
  }

  @Test void SimpleToDoList() {
    okList(List.of("""
    import { base$$True_0 } from "../base/True_0.js";
    import { base$$Void_0 } from "../base/Void_0.js";
    import { rt$$Str } from "../rt-js/Str.js";
    import { todolist$$Fear857$_0 } from "../todolist/Fear857$_0.js";
    import { todolist$$Fear867$_0 } from "../todolist/Fear867$_0.js";
    
    export class todolist$$AddTask_0 {
      static $hash$read$3$fun(e_m$, dom_m$, $this) {
        let input_m$ = dom_m$.getElementById$read$1(rt$$Str.fromJsStr("todoInput")).$exclamation$mut$0();
    let list_m$ = dom_m$.getElementById$read$1(rt$$Str.fromJsStr("todoList")).$exclamation$mut$0();
    let text_m$ = input_m$.value$read$0();
    if (text_m$.$equals$equals$imm$1(rt$$Str.fromJsStr("")) == base$$True_0.$self) { return base$$Void_0.$self; }
    let li_m$ = dom_m$.createElement$mut$1(rt$$Str.fromJsStr("li"));
    let span_m$ = dom_m$.createElement$mut$1(rt$$Str.fromJsStr("span"));
    var doRes1 = span_m$.setText$mut$1(text_m$);
    var doRes2 = span_m$.onClick$mut$1(todolist$$Fear857$_0.$self);
    let delBtn_m$ = dom_m$.createElement$mut$1(rt$$Str.fromJsStr("button"));
    var doRes3 = delBtn_m$.setText$mut$1(rt$$Str.fromJsStr("x"));
    var doRes4 = delBtn_m$.onClickWith$mut$2(li_m$,todolist$$Fear867$_0.$self);
    var doRes5 = li_m$.appendChild$mut$1(span_m$);
    var doRes6 = li_m$.appendChild$mut$1(delBtn_m$);
    var doRes7 = list_m$.appendChild$mut$1(li_m$);
    var doRes8 = input_m$.setValue$mut$1(rt$$Str.fromJsStr(""));
    var doRes9 = input_m$.focus$mut$0();
    return base$$Void_0.$self;
      }
    }
    
    export class todolist$$AddTask_0Impl {
      $hash$read$2(e_m$, dom_m$) { return todolist$$AddTask_0.$hash$read$3$fun(e_m$, dom_m$, this); }
    }
    
    todolist$$AddTask_0.$self = new todolist$$AddTask_0Impl();
    """, """
    import { rt$$Documents } from "../rt-js/Documents.js";
    import { rt$$Str } from "../rt-js/Str.js";
    import { todolist$$AddTask_0 } from "../todolist/AddTask_0.js";
    import { todolist$$Fear899$_0 } from "../todolist/Fear899$_0.js";
    
    export class todolist$$App_0 {
      static $hash$imm$2$fun(fear49$_m$, $this) {
        let doc_m$ = rt$$Documents.$self.create$read$0();
    let addBtn_m$ = doc_m$.getElementById$read$1(rt$$Str.fromJsStr("addBtn")).$exclamation$mut$0();
    let input_m$ = doc_m$.getElementById$read$1(rt$$Str.fromJsStr("todoInput")).$exclamation$mut$0();
    var doRes1 = addBtn_m$.onClick$mut$1(todolist$$AddTask_0.$self);
    var doRes2 = input_m$.onKeyDown$mut$1(todolist$$Fear899$_0.$self);
    return doc_m$.waitCompletion$mut$0();
      }
    }
    
    export class todolist$$App_0Impl {
      $hash$imm$1(fear49$_m$) { return todolist$$App_0.$hash$imm$2$fun(fear49$_m$, this); }
    }
    
    todolist$$App_0.$self = new todolist$$App_0Impl();
    """),
    List.of("todolist/AddTask_0.js", "todolist/App_0.js"),
    """
    package todolist
    alias base.Document as Document, alias base.Documents as Documents, alias base.Element as Element, alias base.Main as Main, alias base.Block as Block, alias base.Void as Void, alias base.F as F, alias base.Event as Event, alias base.Str as Str,
    AddTask: F[mut Event, mut Document, Void]{ e, dom -> Block#
      // obtain input and list elements
      .let[mut Element] input = { dom.getElementById("todoInput")! }
      .let[mut Element] list  = { dom.getElementById("todoList")! }
      // read text
      .let[Str] text = { input.value }
      // if empty, do nothing line 9
      .if { text == "" }
      .return { Void }
       // create <li> and <span>
       .let[mut Element] li   = { dom.createElement("li") }
       .let[mut Element] span = { dom.createElement("span") }
       .do{ span.setText(text) }
  
       // toggle completion on span click line17
       .do{
         span.onClick({ev, d -> Block#
           .do{ ev.target.setClass("completed") }
           .return{ Void }
         })
       }
       // create delete button line 24
       .let[mut Element] delBtn = { dom.createElement("button") }
       .do{ delBtn.setText("x") }
       .do{
         delBtn.onClickWith(li, { ev, d, el -> Block#
           .do{ el.remove }
           .return{ Void }
         })
       }
  
       // attach line34
       .do{ li.appendChild(span) }
       .do{ li.appendChild(delBtn) }
       .do{ list.appendChild(li) }
  
       // reset input
       .do{ input.setValue("") }
       .do{ input.focus }
       .return{ Void }
    }
    
    App: Main{ _ -> Block#
       // --- create document and key elements --- line46
       .let[mut Document] doc   = { Documents.create() }
       .let[mut Element] addBtn = { doc.getElementById("addBtn")! }
       .let[mut Element] input  = { doc.getElementById("todoInput")! }

       // --- attach button click → AddTask ---
       .do{ addBtn.onClick(AddTask) } // line 52
       // --- attach Enter key → AddTask --- line 53
       .do{
         input.onKeyDown({ e, d -> Block#
           .if { e.key == "Enter" }
           .do{ AddTask#(e, d) }
           .return{ Void }
         })
       }
    
       // --- wait for DOM completion ---
       .return{ doc.waitCompletion }
     }
    """);
  }

  @Test void createSimpleToDoList() throws IOException {
    createApp("""
    <!DOCTYPE html>
    <html lang="en">
    <head>
      <meta charset="UTF-8">
      <title>Todo List</title>
      <style>
        :root {
          --bg: #f6f9fc;
          --card: #ffffff;
          --muted: #6b7280;
          --accent: #3b82f6;
          --accent-2: #9333ea;
          --ok: #10b981;
          --danger: #ef4444;
          --glass: rgba(0, 0, 0, 0.02);
          --radius: 12px;
          --border: rgba(0, 0, 0, 0.08);
          font-family: Inter, system-ui, -apple-system, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
          color-scheme: light;
        }
    
        html, body {
          height: 100%;
          margin: 0;
          background: linear-gradient(180deg, #f2f5fb 0%, #e9eff7 100%);
          color: #111827;
        }
    
        .wrap {
          max-width: 760px;
          margin: 40px auto;
          padding: 28px;
          background: linear-gradient(180deg, var(--card), rgba(255, 255, 255, 0.95));
          border-radius: var(--radius);
          box-shadow: 0 8px 30px rgba(0, 0, 0, 0.05);
          backdrop-filter: blur(8px);
        }
    
        header {
          display: flex;
          align-items: center;
          gap: 16px;
          margin-bottom: 18px;
        }
    
        header h1 {
          font-size: 20px;
          margin: 0;
          color: #111827;
        }
    
        header p {
          margin: 0;
          color: var(--muted);
          font-size: 13px;
        }
    
        .input-row {
          display: flex;
          gap: 8px;
          margin-bottom: 16px;
        }
    
        .input-row input[type="text"] {
          flex: 1;
          padding: 12px 14px;
          border-radius: 10px;
          border: 1px solid var(--border);
          background: var(--glass);
          color: inherit;
          font-size: 15px;
          outline: none;
          transition: border-color 0.15s ease, box-shadow 0.15s ease;
        }
    
        .input-row input[type="text"]:focus {
          border-color: var(--accent);
          box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.15);
        }
    
        .input-row input::placeholder {
          color: var(--muted);
        }
    
        .btn {
          padding: 10px 12px;
          border-radius: 10px;
          border: 0;
          background: linear-gradient(180deg, var(--accent), #2563eb);
          color: white;
          font-weight: 600;
          cursor: pointer;
          box-shadow: 0 4px 10px rgba(59, 130, 246, 0.15);
          transition: background 0.25s ease, transform 0.1s ease;
        }
    
        .btn:hover {
          background: linear-gradient(180deg, #4b8ff8, #1d4ed8);
        }
    
        .btn:active {
          transform: scale(0.97);
        }
    
        .btn.ghost {
          background: transparent;
          border: 1px solid var(--border);
          color: var(--muted);
          box-shadow: none;
        }
    
        .controls {
          display: flex;
          align-items: center;
          justify-content: space-between;
          margin-bottom: 12px;
          gap: 12px;
        }
    
        .list {
          list-style: none;
          padding: 0;
          margin: 0;
          display: grid;
          gap: 8px;
        }
    
        .item {
          display: flex;
          align-items: center;
          gap: 12px;
          padding: 10px 12px;
          border-radius: 10px;
          background: #fafafa;
          border: 1px solid var(--border);
          transition: background 0.2s ease, box-shadow 0.2s ease;
        }
    
        .item:hover {
          background: #f3f4f6;
          box-shadow: 0 2px 6px rgba(0, 0, 0, 0.04);
        }
    
        .item .chk {
          width: 18px;
          height: 18px;
          border-radius: 6px;
          border: 1px solid var(--border);
          display: inline-grid;
          place-items: center;
          cursor: pointer;
          transition: background 0.2s ease, border-color 0.2s ease;
        }
    
        .item.done .chk {
          background: linear-gradient(180deg, var(--ok), #22c55e);
          border: 0;
          color: white;
        }
    
        .item .text {
          flex: 1;
          font-size: 15px;
          word-break: break-word;
          transition: color 0.2s ease;
        }
    
        .item.done .text {
          text-decoration: line-through;
          color: var(--muted);
        }
    
        .item button.icon {
          border: 0;
          background: transparent;
          color: var(--muted);
          cursor: pointer;
          padding: 6px;
          border-radius: 8px;
          transition: background 0.2s ease, color 0.2s ease;
        }
    
        .item button.icon:hover {
          background: rgba(0, 0, 0, 0.04);
          color: #dc2626;
        }
    
        .meta {
          display: flex;
          align-items: center;
          gap: 12px;
          color: var(--muted);
          font-size: 13px;
          margin-top: 12px;
        }
    
        .clear {
          color: var(--danger);
          cursor: pointer;
          border: 0;
          background: transparent;
        }
    
        @media (max-width: 520px) {
          .wrap {
            margin: 24px;
            padding: 18px;
          }
          header h1 {
            font-size: 18px;
          }
        }
    
        :focus {
          outline: 3px solid rgba(59, 130, 246, 0.2);
          outline-offset: 3px;
        }
    
        input:focus,
        button:focus {
          box-shadow: 0 4px 24px rgba(59, 130, 246, 0.06);
        }
      </style>
    </head>
    <body>
      <main class="wrap" role="main" aria-labelledby="title">
        <header>
          <div>
            <h1 id="title">Todo List</h1>
            <p>Create tasks freely.</p>
          </div>
        </header>
    
        <div class="input-row" role="region" aria-label="Add todo">
          <input id="todoInput" type="text" placeholder="What do you want to do?" aria-label="New todo">
          <button id="addBtn" class="btn" title="Add (Enter)">Add</button>
        </div>
    
        <ul id="todoList" class="list" aria-live="polite"></ul>
      </main>
    </body>
    </html>
    """,
    "todolist/App_0.js",
    """
    package todolist
    alias base.Document as Document, alias base.Documents as Documents,
    alias base.Element as Element, alias base.Main as Main,
    alias base.Block as Block, alias base.Void as Void,
    alias base.F as F, alias base.Event as Event, alias base.Str as Str,
    alias base.Bool as Bool, alias base.True as True, alias base.False as False,
    
    ToggleDone: F[mut Event, mut Document, mut Element, Void]{ ev, d, el -> Block#
       .let[Str] done = { el.getData("done") }
       .do{ el.setClass(done == "true" ? {.then -> "item done", .else-> "item" }) }
       .do{ el.setData("done", done == "true" ? {.then -> "false", .else-> "true" }) }
       .return{ Void }
    }
    
    AddTask: F[mut Event, mut Document, Void]{ e, dom -> Block#
      // obtain input and list elements
      .let[mut Element] input = { dom.getElementById("todoInput")! }
      .let[mut Element] list  = { dom.getElementById("todoList")! }
    
      // read text line 13
      .let[Str] text = { input.value }
    
      // if empty, do nothing
      .if { text == "" } .return { Void }
    
      // create <li class="item">
      .let[mut Element] li = { dom.createElement("li") }
      .do{ li.setClass("item") }
      .do{ li.setData("done", "false") }
    
      // create checkbox button line 24
      .let[mut Element] chk = { dom.createElement("button") }
      .do{ chk.setClass("chk") }
      .do{ chk.setText("") }
    
      // create text div line 29
      .let[mut Element] span = { dom.createElement("div") }
      .do{ span.setClass("text") }
      .do{ span.setText(text) }
    
      // create delete button line 34
      .let[mut Element] delBtn = { dom.createElement("button") }
      .do{ delBtn.setText("x") }
      .do{ delBtn.setClass("icon") }
    
      // toggle completion when checkbox clicked line 39
      .do{
       span.onClickWith(li, {ev, d, el -> Block#
         .let[Str] done = { el.getData("done") }
         .do{ el.setClass(done == "true" ? {.then -> "item done", .else-> "item" }) }
         .do{ el.setData("done", done == "true" ? {.then -> "false", .else-> "true" }) }
         .return{ Void }
       })
      }
      .do{
       chk.onClickWith(li, {ev, d, el -> Block#
         .let[Str] done = { el.getData("done") }
         .do{ el.setClass(done == "true" ? {.then -> "item done", .else-> "item" }) }
         .do{ el.setData("done", done == "true" ? {.then -> "false", .else-> "true" }) }
         .return{ Void }
       })
      }
    
      // delete button logic
      .do{
        delBtn.onClickWith(li, { ev, d, el -> Block#
          .do{ el.remove }
          .return{ Void }
        })
      }
    
      // assemble
      .do{ li.appendChild(chk) }
      .do{ li.appendChild(span) }
      .do{ li.appendChild(delBtn) }
      .do{ list.appendChild(li) }
    
      // reset input
      .do{ input.setValue("") }
      .do{ input.focus }
    
      .return{ Void }
    }
    
    App: Main{ _ -> Block#
      .let[mut Document] doc   = { Documents.create() }
      .let[mut Element] addBtn = { doc.getElementById("addBtn")! }
      .let[mut Element] input  = { doc.getElementById("todoInput")! }
    
      // click → AddTask
      .do{ addBtn.onClick(AddTask) }
    
      // Enter key → AddTask
      .do{
        input.onKeyDown({ e, d -> Block#
          .if { e.key == "Enter" } .do{ AddTask#(e, d) }
            .return{ Void }
        })
      }
    
      .return{ doc.waitCompletion }
    }
    
    """);
  }
}