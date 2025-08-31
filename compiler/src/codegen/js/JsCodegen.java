package codegen.js;

import codegen.MIR;
import id.Id;
import id.Mdf;
import utils.Box;
import utils.Bug;
import visitors.MIRVisitor;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import magic.FearlessStringHandler;
import magic.Magic;
import static magic.Magic.getLiteral;
public class JsCodegen implements MIRVisitor<String> {
  protected final MIR.Program p;
  private final JsMagicImpls magic;
  private final codegen.js.TypeIds typeIds;
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
    this.typeIds = new TypeIds(magic, id);
  }

  public String visitProgram(Id.DecId entry){ throw Bug.unreachable(); }
  public String visitPackage(MIR.Package pkg){ throw Bug.unreachable(); }

  // Generate a JS function for the type with all its methods
  public String visitTypeDef(String pkg, MIR.TypeDef def, List<MIR.Fun> funs) {
    if (def.singletonInstance().isEmpty()) {
      return "";
    }

    String name = id.getSimpleName(def.name());
    String methods = funs.stream()
      .map(this::visitFun)
      .collect(Collectors.joining("\n    "));

    // Only import types that:
    // 1. Are from different packages
    // 2. Aren't string literals
    // 3. Are actually referenced in code
//    Set<String> imports = new LinkedHashSet<>();
//
//    // Check extended interfaces
//    def.impls().stream()
//      .map(impl -> impl.id())
//      .filter(id -> !id.pkg().equals(pkg))
//      .forEach(id -> imports.add(createImport(id)));
//
//    // Check return types
//    funs.stream()
//      .map(fun -> fun.ret())
//      .filter(ret -> ret.name().isPresent())
//      .map(ret -> ret.name().get())
//      .filter(id -> {
////        System.out.println(id.name());
//        return !id.name().contains("uStrLit");
//      })// Bug to fix: utils.OneOr$OneOrException: Malformed package: base.uStrLit."\\\\" at utils.OneOr.lambda$of$1(OneOr.java:12)
//      .filter(id -> !id.pkg().equals(pkg))
//      .forEach(id -> imports.add(createImport(id)));

//    String importSection = String.join("\n", imports);

    return """
        class %s {
            static $self = new %s();
            %s
        }
        """.formatted(
//      importSection.isEmpty() ? "" : importSection + "\n",
      name,
      name,
      methods);
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
        }
        """.formatted(methName, args, id.getFunName(meth.fName().orElseThrow()), funArgs);
  }
  public String visitFun(MIR.Fun fun) {
    var funName = id.getFunName(fun.name());

    var args = fun.args().stream()
      .map(MIR.X::name)
      .collect(Collectors.joining(", "));

    var bodyExpr = fun.body();
    var bodyStr = bodyExpr.accept(this, true);

    String instanceMethod;
    if (bodyExpr instanceof MIR.Block) {
      instanceMethod = "%s(%s) { %s }".formatted(funName, args, bodyStr);
    } else {
      instanceMethod = "%s(%s) { return %s; }".formatted(funName, args, bodyStr);
    }

    // Add the forwarding static method
    String forwarder =
      "static %s$fun(%s, $this) { return $this.%s(%s); }"
        .formatted(funName, args, funName, args);

    return instanceMethod + "\n\n    " + forwarder;
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
        "%s %s;\n".formatted(returnKind, ret.e().accept(this, true));
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

//  public String visitCreateObjNoSingleton(MIR.CreateObj createObj, boolean checkMagic) {
//    var name = createObj.concreteT().id();
//    var className = id.getSimpleName(name)+"Impl";
//    if (!this.freshClasses.containsKey(name)) {
//      var capturesArgs = createObj.captures().stream().map(x->visitX(x, checkMagic)).collect(Collectors.joining(", "));
//      var capturesAsFields = createObj.captures().stream()
//        .map(x->visitX(x, checkMagic))
//        .map(x->"this."+x+" = "+x+";")
//        .collect(Collectors.joining("\n"));
//      var ms = createObj.meths().stream()
//        .map(this::visitMeth)
//        .collect(Collectors.joining("\n"));
//
//      var constructor = createObj.captures().isEmpty() ? "" : """
//        constructor(%s) {
//            %s
//        }
//        """.formatted(capturesArgs, capturesAsFields);
//
//      this.freshClasses.put(name, """
//        class %s {
//          %s
//          %s
//        }
//        """.formatted(className, constructor, ms));
//    }
//    var captures = createObj.captures().stream().map(x->visitX(x, checkMagic)).collect(Collectors.joining(", "));
//    return "new "+className+"("+captures+")";
//  }

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

  private String name(String x) {
    return x.equals("this")
      ? "f$this"
      : x.replace("'", "$"+(int)'\'').replace("$", "$"+(int)'$')+"$";
  }

  public String getName(MIR.FName name) {
    var capturesSelf = name.capturesSelf() ? "selfCap" : "noSelfCap";
    return getName(name.d())+"$"+name(getName(name.mdf(), name.m()))+"$"+capturesSelf;
  }
  public String getName(MIR.MT t) {
    return switch (t) {
      case MIR.MT.Any ignored -> throw Bug.unreachable();
      case MIR.MT.Plain plain -> getName(plain.id());
      case MIR.MT.Usual usual -> getName(usual.it().name());
    };
  }
  public String getName(Mdf mdf, Id.MethName m) { return getBase(m.name())+"_"+m.num()+"_"+mdf; }

  private static String getBase(String name) {
    if (name.startsWith(".")) { name = name.substring(1); }
    return name.chars().mapToObj(c->{
      if (c != '\'' && (c == '.' || Character.isAlphabetic(c) || Character.isDigit(c))) {
        return Character.toString(c);
      }
      return "$"+c;
    }).collect(Collectors.joining());
  }
  public String getTName(MIR.MT t, boolean isRet) {
    return typeIds.getTName(t, isRet);
  }
}