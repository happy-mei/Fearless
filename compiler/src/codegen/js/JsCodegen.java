package codegen.js;

import codegen.MIR;
import id.Id;
import id.Mdf;
import utils.Box;
import utils.Bug;
import visitors.MIRVisitor;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import magic.FearlessStringHandler;
import magic.Magic;
import static magic.Magic.getLiteral;
public class JsCodegen implements MIRVisitor<String> {
  protected final MIR.Program p;
  private final JsMagicImpls magic;
  public final StringIds id = new StringIds();
//  public final HashMap<Id.DecId, String> freshClasses= new HashMap<>();
//  private final Map<String, StringBuilder> packageFiles = new HashMap<>();
//  private String currentPackage;

  /*
   * To add as runtime code files:
   * `class Unreachable extends Error { constructor() { super("Unreachable code"); } }`
   */

  public JsCodegen(MIR.Program p) {
    this.p = p;
    this.magic = new JsMagicImpls(this, p.p());
  }

  public String visitProgram(Id.DecId entry){ throw Bug.unreachable(); }
  public String visitPackage(MIR.Package pkg){ throw Bug.unreachable(); }

  // Generate a JS function for the type with all its methods
  public String visitTypeDef(String pkg, MIR.TypeDef def, List<MIR.Fun> funs) {
    if (def.singletonInstance().isEmpty()) {
      return "";
    }

    String name = id.getSimpleName(def.name());
    String parent = extendsStr(def, id.getFullName(def.name()));
    String methods = funs.stream()
      .map(this::visitFun)
      .collect(Collectors.joining("\n    "));

    return """
      export class %s%s {
        static $self = new %s();
        %s
      }
      """.formatted(
      name,
      parent.isEmpty() ? "" : " extends " + parent,
      name,
      methods);
  }
  public boolean isLiteral(Id.DecId d) {
    return id.getLiteral(p.p(), d).isPresent();
  }
  public String extendsStr(MIR.TypeDef def, String fullName) {
    return def.impls().stream()
      .map(MIR.MT.Plain::id) // DecId
      .filter(e -> !isLiteral(e))
      .filter(e -> !id.getFullName(e).equals(fullName))
      .findFirst() // JS only allows one parent
      .map(id::getSimpleName) // now you still have DecId
      .orElse("");
  }

  private String createImport(Id.DecId typeId) {
    String typeName = id.getSimpleName(typeId);
    String pkgPath = typeId.pkg().replace(".", "/");
    if (pkgPath.equals("rt")) {
      return "import { " + typeName + " } from '../rt/main.js';";
    }
    return "import { " + typeName + " } from '../" + pkgPath + "/" + typeName + ".js';";
  }

  public String visitMeth(MIR.Meth meth) {
    var methName = id.getMName(meth.sig().mdf(), meth.sig().name());
    var args = meth.sig().xs().stream()
      .map(x -> this.visitX(x, true))
      .collect(Collectors.joining(", "));

    var selfArg = meth.capturesSelf() ? Stream.of("this") : Stream.<String>of();
    var funArgs = Stream.concat(
      meth.sig().xs().stream().map(x -> this.visitX(x, true)),
      Stream.concat(
        selfArg,
        meth.captures().stream().map(id::varName).map(x -> "this." + x)
      )
    ).collect(Collectors.joining(", "));

    return """
      %s(%s) {
        return %s(%s);
      }""".formatted(methName, args, id.getFunName(meth.fName().orElseThrow()), funArgs);
  }

  public <T> String seq(Collection<T> es, Function<T,String> f, String join){
    return seq(es.stream(),f,join);
  }
  public <T> String seq(Stream<T> s, Function<T,String> f, String join){
    return s.map(f).collect(Collectors.joining(join));
  }
  public String visitFun(MIR.Fun fun) {
    var funName = getFName(fun.name());
    // JS arguments: just variable names, no types
    var args = seq(fun.args(), x -> id.varName(x.name()), ", ");
    var bodyExpr = fun.body();
    var bodyStr = bodyExpr.accept(this, true);

    // Static method (the actual implementation, no forwarding)
    String staticFun;
    if (bodyExpr instanceof MIR.Block) {
      staticFun = """
        static %s(%s) {
          %s;
        }""".formatted(funName, args, bodyStr);
    } else {
      staticFun = """
        static %s(%s) {
          return %s;
        }""".formatted(funName, args, bodyStr);
    }

    return staticFun;
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


  @Override
  public String visitCreateObj(MIR.CreateObj createObj, boolean checkMagic) {
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
    return id.getMName(name.mdf(), name.m())+"$fun";
  }
}