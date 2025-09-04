package codegen.js;

import codegen.MIR;
import id.Id;
import id.Mdf;
import utils.Box;
import utils.Bug;
import visitors.MIRVisitor;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import magic.FearlessStringHandler;
import magic.Magic;
import static magic.Magic.getLiteral;
public class JsCodegen implements MIRVisitor<String> {
  protected final MIR.Program p;
  private final JsMagicImpls magic;
  protected final Map<MIR.FName, MIR.Fun> funMap;
  public final StringIds id = new StringIds();

  public JsCodegen(MIR.Program p) {
    this.p = p;
    this.magic = new JsMagicImpls(this, p.p());
    this.funMap = p.pkgs().stream()
      .flatMap(pkg->pkg.funs().stream())
      .collect(Collectors.toMap(MIR.Fun::name, f->f));
  }

  public String visitProgram(Id.DecId entry){ throw Bug.unreachable(); }
  public String visitPackage(MIR.Package pkg){ throw Bug.unreachable(); }
  public String visitTypeDef(String pkg, MIR.TypeDef def, List<MIR.Fun> funs) {
    Set<String> deps = new HashSet<>();

    // === Extends clause ===
    String extendsStr = "";
    Id.DecId parentId = getParent(def, id.getFullName(def.name()));
    if (parentId != null) {
      extendsStr = " extends " + id.getFullName(parentId);
      deps.add(id.getFullName(parentId));
    }

    // === Methods ===
    String methods = funs.stream()
      .map(fun -> visitFun(fun))
      .collect(Collectors.joining("\n    "));

    // Collect fully qualified names in methods
    Matcher m = Pattern.compile("[a-z][a-z0-9_]*__[A-Z][A-Za-z0-9_$]*_\\d+").matcher(methods);
    while (m.find()) deps.add(m.group());

    // === Imports ===
    StringBuilder importLines = new StringBuilder();
    for (String dep : deps) {
      String path = getRelativeImportPath(pkg, dep);
      importLines.append("import { ")
        .append(dep)
        .append(" } from \"")
        .append(path)
        .append("\";\n");
    }
    if (!deps.isEmpty()) importLines.append("\n");

    // === Singleton ===
    String className = id.getFullName(def.name());
    String singletonField = def.singletonInstance().isPresent()
      ? "static $self = new " + className + "();"
      : "";

    return importLines + """
    export class %s%s {
      %s
      %s
    }
    """.formatted(className, extendsStr, singletonField, methods);
  }

  private String getRelativeImportPath(String pkg, String encodedDep) {
    // reverse the encoding: "base_$_Main_0" → pkg = "base", file = "Main"
    String[] parts = encodedDep.split("__");
    String depPkg = String.join("/", Arrays.copyOf(parts, parts.length - 1));
    String depFile = parts[parts.length - 1];
    String depPath = depPkg.isEmpty() ? depFile : depPkg + "/" + depFile;

    // compute relative path from currentPkg
    String currentPath = pkg.isEmpty() ? "" : pkg.replace(".", "/");
    int depth = currentPath.isEmpty() ? 0 : currentPath.split("/").length;
    StringBuilder prefix = new StringBuilder();
    if (depth == 0) {
      prefix.append("./");
    } else {
      for (int i = 0; i < depth; i++) prefix.append("../");
    }
    return prefix + depPath + ".js";
  }

  public boolean isLiteral(Id.DecId d) {
    return id.getLiteral(p.p(), d).isPresent();
  }

  // Get the first non-literal parent, or null if none
  private String extendsStr(MIR.TypeDef def, String fullName) {
    String parent = def.impls().stream()
      .map(MIR.MT.Plain::id) // DecId
      .filter(e -> !isLiteral(e))
      .filter(e -> !id.getFullName(e).equals(fullName))
      .findFirst() // JS only allows one parent
      .map(id::getFullName)
      .orElse("");
    return parent.isEmpty() ? "" : " extends " + parent;
  }

  private Id.DecId getParent(MIR.TypeDef def, String fullName) {
    return def.impls().stream()
      .map(MIR.MT.Plain::id) // DecId
      .filter(e -> !isLiteral(e))
      .filter(e -> !id.getFullName(e).equals(fullName))
      .findFirst() // JS only allows one parent
      .orElse(null);
  }


  public <T> String seq(Collection<T> es, Function<T,String> f, String join){
    return seq(es.stream(),f,join);
  }
  public <T> String seq(Stream<T> s, Function<T,String> f, String join){
    return s.map(f).collect(Collectors.joining(join));
  }
  public String visitFun(MIR.Fun fun) {
    var funName = getFName(fun.name()); // e.g. "$hash$imm"
    var args = seq(fun.args(), x -> id.varName(x.name()), ", "); // has $this
    // Drop $this if present (only for instance method)
    var argNames = fun.args().stream()
      .map(x -> id.varName(x.name()))
      .filter(n -> !n.equals("$this"))
      .toList();
    var instArgs = String.join(", ", argNames);
    var bodyExpr = fun.body();
    var bodyStr = bodyExpr.accept(this, true);

    // Instance method
    String instFun;
    if (bodyExpr instanceof MIR.Block) {
      instFun = """
          %s(%s) {
            %s;
          }""".formatted(funName, instArgs, bodyStr);
    } else {
      instFun = """
          %s(%s) {
            return %s;
          }""".formatted(funName, instArgs, bodyStr);
    }

    // Static forwarding method (add `$fun` suffix)
    String staticFun = """
      static %s$fun(%s) {
        return $this.%s(%s);
      }""".formatted(funName, args, funName, args);

    return instFun + "\n" + staticFun;
  }


  @Override
  public String visitBlockExpr(MIR.Block expr, boolean checkMagic) {
    return visitBlockExpr(expr, BlockReturnKind.RETURN);
  }

  private enum BlockReturnKind {
    RETURN {
      @Override public String toString() {
        return "return";
      }
    },
    YIELD {
      @Override public String toString() {
        return "yield";
      }
    }
  }

  private String visitBlockExpr(MIR.Block expr, BlockReturnKind returnKind) {
    var res = new StringBuilder();
    var stmts = new ArrayDeque<>(expr.stmts());
    var doIdx = new Box<>(0); // Mutable int wrapper

    while (!stmts.isEmpty()) {
      res.append(this.visitBlockStmt(expr, stmts, doIdx, returnKind));
    }

    return res.toString();
  }

  private String visitBlockStmt(
    MIR.Block expr,
    ArrayDeque<MIR.Block.BlockStmt> stmts,
    Box<Integer> doIdx,
    BlockReturnKind returnKind
  ) {
    var stmt = stmts.poll();
    assert stmt != null;
    return switch (stmt) {
      case MIR.Block.BlockStmt.Return ret ->
        // JS: return <expr>;
        "%s %s".formatted(returnKind, ret.e().accept(this, true));
      case MIR.Block.BlockStmt.Do do_ ->
        // JS: var doRes0 = <expr>;
        "var doRes%d = %s;\n".formatted(doIdx.update(n -> n + 1), do_.e().accept(this, true));
      case MIR.Block.BlockStmt.Throw throw_ ->
        // JS: throw <expr>;
        "throw %s;\n".formatted(throw_.e().accept(this, true));
      case MIR.Block.BlockStmt.Loop loop ->
        // JS: while(true) { ... }
        """
        while (true) {
          var res = %s.$hash$mut();
          if (res == base.ControlFlowContinue_0.$self || res == base.ControlFlowContinue_1.$self) { continue; }
          if (res == base.ControlFlowBreak_0.$self || res == base.ControlFlowBreak_1.$self) { break; }
          if (res instanceof base.ControlFlowReturn_1 rv) { %s (%s) rv.value$mut(); }
        }
        """.formatted(
          loop.e().accept(this, true),
          returnKind,
          getTName(expr.expectedT(), false)
        );
      case MIR.Block.BlockStmt.If if_ -> {
        // JS: if (<pred> == base.True_0.$self) { ... }
        var body = this.visitBlockStmt(expr, stmts, doIdx, returnKind);
        if (body.startsWith(returnKind.toString())) {
          body += ";";
        }
        yield """
        if (%s == base.True_0.$self) { %s }
        """.formatted(if_.e().accept(this, true), body);
      }
      case MIR.Block.BlockStmt.Let let ->
        // JS: let <name> = <expr>;
        "let %s = %s;\n".formatted(let.name(), let.value().accept(this, true));
      case MIR.Block.BlockStmt.Var var ->
        // JS: var <name> = base.Vars_0.$self.$hash$imm(<expr>);
        "var %s = base.Vars_0.$self.$hash$imm(%s);\n".formatted(var.name(), var.value().accept(this, true));
    };
  }

  // Visit boolean expressions
  @Override public String visitBoolExpr(MIR.BoolExpr expr, boolean checkMagic) {
    String recv = expr.condition().accept(this, checkMagic);

    // Determine if we need a cast (optional in JS, but keep for consistency)
    boolean mustCast = !this.funMap.get(expr.then()).ret().equals(this.funMap.get(expr.else_()).ret());
    String cast = mustCast ? "(" + getTName(expr.t(), true) + ")" : "";

    // Generate then and else bodies
    String thenBody = switch (this.funMap.get(expr.then()).body()) {
      case MIR.Block b -> this.inlineBlock(b);
      case MIR.E e -> e.accept(this, checkMagic);
    };
    String elseBody = switch (this.funMap.get(expr.else_()).body()) {
      case MIR.Block b -> this.inlineBlock(b);
      case MIR.E e -> e.accept(this, checkMagic);
    };

    // Return the wrapped JS boolean expression
    return "(%s(%s == base.True_0.$self ? %s : %s))".formatted(cast, recv, thenBody, elseBody);
  }

  // Wrap a block expression
  private String inlineBlock(MIR.Block block) {
    String blockCode = visitBlockExpr(block, BlockReturnKind.YIELD);
    return """
    (switch (1) { default -> {
      %s;
    }})
    """.formatted(blockCode);
  }


  @Override public String visitCreateObj(MIR.CreateObj createObj, boolean checkMagic) {
    if (magic.isMagic(Magic.Str, createObj.concreteT().id())) {
      return visitStringLiteral(createObj);
    }
    var magicImpl = magic.get(createObj);
    if (checkMagic && magicImpl.isPresent()) {
      var res = magicImpl.get().instantiate();
      if (res.isPresent()) { return res.get(); }
    }
    var id = createObj.concreteT().id();
    var className = getName(id);
    if (createObj.captures().isEmpty()) {
      return className + ".$self";
    }
    // For non-singleton cases with captures, create a new instance
    var captures = createObj.captures().stream()
      .map(x -> visitX(x, checkMagic))
      .collect(Collectors.joining(", "));

    return "new " + className + "(" + captures + ")";
  }

  // Converts string literals to JS strings, handling mutability and escaping
  public String visitStringLiteral(MIR.CreateObj k) {
    var id = k.concreteT().id();
    var fearlessStr = getLiteral(p.p(), id).orElseThrow();
    var jsStr = new FearlessStringHandler(FearlessStringHandler.StringKind.Unicode)
      .toJavaString(fearlessStr)
      .get();
//    System.out.println("visitStringLiteral: "+jsStr);
    // Escape the string safely for JS (you can use a real escape util or JSON-style)
    var escaped = jsonEscape(jsStr); // You can define this or inline escape.

    return switch (k.t().mdf()) {
      case mut, iso -> "new rt.MutStr(" + escaped + ")";
      default -> escaped;
    };
  }
  public static String jsonEscape(String s) {
    return "\"" + s.replace("\\", "\\\\")
      .replace("\"", "\\\"")
      .replace("\n", "\\n")
      .replace("\r", "\\r")
      .replace("\t", "\\t")
      .replace("\b", "\\b")
      .replace("\f", "\\f") + "\"";
  }

  @Override public String visitX(MIR.X x, boolean checkMagic) {
    return id.varName(x.name());
  }

  public String getName(Id.DecId d) {
    return id.getFullName(d);
  }

  @Override public String visitMCall(MIR.MCall call, boolean checkMagic) {
    if (checkMagic && !call.variant().contains(MIR.MCall.CallVariant.Standard)) {
      var impl = magic.variantCall(call).call(call.name(), call.args(), call.variant(), call.t());
      if (impl.isPresent()) return impl.get();
    }

    var magicImpl = magic.get(call.recv());
    if (checkMagic && magicImpl.isPresent()) {
      var impl = magicImpl.get().call(call.name(), call.args(), call.variant(), call.t());
      if (impl.isPresent()) return impl.get();
    }

    var args = call.args().stream()
      .map(a -> a.accept(this, checkMagic))
      .collect(Collectors.joining(","));

    return "%s.%s(%s)".formatted(
      call.recv().accept(this, checkMagic),
      id.getMName(call.mdf(), call.name()),
      args
    );
  }

  public String getTName(MIR.MT t, boolean isRet) {
    return new TypeIds(magic,id).getTName(t, isRet);
  }
  public String getFName(MIR.FName name) {
    return id.getMName(name.mdf(), name.m());
  }
}