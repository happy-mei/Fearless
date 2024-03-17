package codegen.java;

import codegen.MIR;
import codegen.MethExprKind;
import codegen.ParentWalker;
import codegen.optimisations.OptimisationBuilder;
import id.Id;
import id.Mdf;
import magic.Magic;
import utils.Box;
import utils.Bug;
import utils.Streams;
import visitors.MIRVisitor;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static magic.MagicImpls.getLiteral;

public class JavaCodegen implements MIRVisitor<String> {
  protected final MIR.Program p;
  protected final Map<MIR.FName, MIR.Fun> funMap;
  private MagicImpls magic;
  private HashMap<Id.DecId, String> freshRecords;
  private MIR.Package pkg;

  public JavaCodegen(MIR.Program p) {
    this.magic = new MagicImpls(this, p.p());
    this.p = new OptimisationBuilder(this.magic)
      .withBoolIfOptimisation()
      .withBlockOptimisation()
      .run(p);
    this.funMap = p.pkgs().stream().flatMap(pkg->pkg.funs().stream()).collect(Collectors.toMap(MIR.Fun::name, f->f));
  }

  protected static String argsToLList(Mdf addMdf) {
    return """
      FAux.LAUNCH_ARGS = base.LList_1._$self;
      for (String arg : args) { FAux.LAUNCH_ARGS = FAux.LAUNCH_ARGS.$43$%s$(arg); }
      """.formatted(addMdf);
  }

  public String visitProgram(Id.DecId entry) {
    var entryName = getName(entry);
    var systemImpl = getName(Magic.SystemImpl);
    var init = """
      static void main(String[] args){
        %s
        base.Main_0 entry = %s._$self;
        try {
          entry.$35$imm$(%s._$self);
        } catch (StackOverflowError e) {
          System.err.println("Program crashed with: Stack overflowed");
          System.exit(1);
        } catch (Throwable t) {
          System.err.println("Program crashed with: "+t.getLocalizedMessage());
          System.exit(1);
        }
      }
    """.formatted(
      argsToLList(Mdf.mut),
      entryName,
      systemImpl
    );

    final String fearlessError = """
      package userCode;
      class FearlessError extends RuntimeException {
        public FProgram.base.Info_0 info;
        public FearlessError(FProgram.base.Info_0 info) {
          super();
          this.info = info;
        }
        public String getMessage() { return this.info.str$imm$(); }
      }
      class FAux { static FProgram.base.LList_1 LAUNCH_ARGS; }
      """;

    return fearlessError+"\npublic interface FProgram{\n" +p.pkgs().stream()
      .map(this::visitPackage)
      .collect(Collectors.joining("\n"))+init+"}";
  }

  public String visitPackage(MIR.Package pkg) {
    this.pkg = pkg;
    this.freshRecords = new HashMap<>();
    Map<Id.DecId, List<MIR.Fun>> funs = pkg.funs().stream().collect(Collectors.groupingBy(f->f.name().d()));
    var typeDefs = pkg.defs().values().stream()
      .map(def->visitTypeDef(pkg.name(), def, funs.getOrDefault(def.name(), List.of())))
      .collect(Collectors.joining("\n"));

    var freshRecords = String.join("\n", this.freshRecords.values());
    return "interface "+getPkgName(pkg.name())+"{"+typeDefs+"\n"+freshRecords+"\n}";
  }

  public String visitTypeDef(String pkg, MIR.TypeDef def, List<MIR.Fun> funs) {
    if (pkg.equals("base") && def.name().name().endsWith("Instance")) {
      return "";
    }
    if (getLiteral(p.p(), def.name()).isPresent()) {
      return "";
    }

    var longName = getName(def.name());
    var shortName = longName;
    if (def.name().pkg().equals(pkg)) { shortName = getBase(def.name().shortName())+"_"+def.name().gen(); }
    final var selfTypeName = shortName;

    var its = def.impls().stream()
      .map(MIR.MT.Plain::id)
      .filter(dec->getLiteral(p.p(), dec).isEmpty())
      .map(JavaCodegen::getName)
      .filter(tr->!tr.equals(longName))
      .distinct()
      .collect(Collectors.joining(","));
    var impls = its.isEmpty() ? "" : " extends "+its;
    var start = "interface "+shortName+impls+"{\n";
    var singletonGet = def.singletonInstance()
      .map(objK->{
        var instance = visitCreateObjNoSingleton(objK, true);
        return """
          %s _$self = %s;
          """.formatted(selfTypeName, instance);
      })
      .orElse("");

    var leastSpecific = ParentWalker.leastSpecificSigs(p, def);

    var sigs = def.sigs().stream()
      .map(sig->visitSig(sig, leastSpecific))
      .collect(Collectors.joining("\n"));

    var staticFuns = funs.stream()
      .map(this::visitFun)
      .collect(Collectors.joining("\n"));

    return start + singletonGet + sigs + staticFuns + "}";
  }

  public String visitSig(MIR.Sig sig, Map<ParentWalker.FullMethId, MIR.Sig> leastSpecific) {
    // If params are different in my parent, we need to objectify
    var overriddenSig = this.overriddenSig(sig, leastSpecific);
    if (overriddenSig.isPresent()) {
      return visitSig(overriddenSig.get(), Map.of());
    }

    var args = sig.xs().stream()
//      .map(x->new MIR.X(x.name(), new MIR.MT.Any(x.t().mdf()))) // required for overriding meths with generic args
      .map(this::typePair)
      .collect(Collectors.joining(","));

    return getRetName(sig.rt())+" "+name(getName(sig.mdf(), sig.name()))+"("+args+");";
  }


  public String visitMeth(MIR.Meth meth, MethExprKind kind, Map<ParentWalker.FullMethId, MIR.Sig> leastSpecific) {
    var overriddenSig = this.overriddenSig(meth.sig(), leastSpecific);
    if (overriddenSig.isPresent()) {
      if (kind.kind() == MethExprKind.Kind.Unreachable) {
        return visitMeth(meth.withSig(overriddenSig.get()), MethExprKind.Kind.Unreachable, Map.of());
      }
      var delegator = visitMeth(meth.withSig(overriddenSig.get()), new MethExprKind.Delegator(meth.sig(), overriddenSig.get()), Map.of());
      var delegate = visitMeth(meth, MethExprKind.Kind.Delegate, Map.of());
      return delegator+"\n"+delegate;
    }

    var methName = switch (kind.kind()) {
      case Delegate -> name(getName(meth.sig().mdf(), meth.sig().name()))+"$Delegate";
      default -> name(getName(meth.sig().mdf(), meth.sig().name()));
    };

    var args = meth.sig().xs().stream()
      .map(this::typePair)
      .collect(Collectors.joining(","));
    var selfArg = meth.capturesSelf() ? Stream.of("this") : Stream.<String>of();
    var funArgs = Streams.of(meth.sig().xs().stream().map(MIR.X::name).map(this::name), selfArg, meth.captures().stream().map(this::name).map(x->"this."+x))
      .collect(Collectors.joining(","));

    var mustCast = meth.fName().isPresent() && this.funMap.get(meth.fName().get()).ret() instanceof MIR.MT.Any && !(meth.sig().rt() instanceof MIR.MT.Any);
    var cast = mustCast ? "(%s)".formatted(getName(meth.sig().rt()) ): "";

    var realExpr = switch (kind) {
      case MethExprKind.Kind k -> switch (k.kind()) {
        case RealExpr, Delegate -> "return %s %s.%s(%s);".formatted(cast, getName(meth.origin()), getName(meth.fName().orElseThrow()), funArgs);
        case Unreachable -> "throw new Error(\"Unreachable code\");";
        case Delegator -> throw Bug.unreachable();
      };
      case MethExprKind.Delegator k -> "return this.%s(%s);".formatted(
        methName+"$Delegate",
        k.xs()
          .map(x->"("+getName(x.t())+") "+name(x.name()))
          .collect(Collectors.joining(", "))
        );
    };

    return """
      public %s %s(%s) {
        %s
      }
      """.formatted(
        getRetName(meth.sig().rt()),
        methName,
        args,
        realExpr);
  }

  public String visitFun(MIR.Fun fun) {
    var name = getName(fun.name());
    var args = fun.args().stream()
      .map(this::typePair)
      .collect(Collectors.joining(", "));
    var body = fun.body().accept(this, true);

    var ret = fun.body() instanceof MIR.Block ? "" : "return ";

    return """
      static %s %s(%s) {
        %s%s;
      }
      """.formatted(getRetName(fun.ret()), name, args, ret, body);
  }
  @Override public String visitBlockExpr(MIR.Block expr, boolean checkMagic) {
    var res = new StringBuilder();
    var stmts = new ArrayDeque<>(expr.stmts());
    var doIdx = new Box<>(0);
    while (!stmts.isEmpty()) {
      res.append(this.visitBlockStmt(expr, stmts, doIdx));
    }
    return res.toString();
  }

  private String visitBlockStmt(MIR.Block expr, ArrayDeque<MIR.Block.BlockStmt> stmts, Box<Integer> doIdx) {
    var stmt = stmts.poll();
    assert stmt != null;
    return switch (stmt) {
      case MIR.Block.BlockStmt.Return ret -> "return %s".formatted(ret.e().accept(this, true));
      case MIR.Block.BlockStmt.Do do_ -> "var doRes%s = %s;\n".formatted(doIdx.update(n->n + 1), do_.e().accept(this, true));
      case MIR.Block.BlockStmt.Loop loop -> """
          while (true) {
            var res = %s;
            if (res == base.ControlFlowContinue_0._$self || res == base.ControlFlowContinue_1._$self) { continue; }
            if (res == base.ControlFlowBreak_0._$self || res == base.ControlFlowBreak_1._$self) { break; }
            if (res instanceof base.ControlFlowReturn_1 rv) { return (%s) rv.value$mut$(); }
          }
          """.formatted(loop.e().accept(this, true), getName(expr.expectedT()));
      case MIR.Block.BlockStmt.If if_ -> {
        var body = this.visitBlockStmt(expr, stmts, doIdx);
        if (body.startsWith("return")) { body += ";"; }
        yield """
          if (%s == base.True_0._$self) { %s }
          """.formatted(if_.pred().accept(this, true), body);
      }
      case MIR.Block.BlockStmt.Var var -> "var %s = %s;\n".formatted(name(var.name()), var.value().accept(this, true));
    };
  }

  @Override public String visitCreateObj(MIR.CreateObj createObj, boolean checkMagic) {
    var magicImpl = magic.get(createObj);
    if (checkMagic && magicImpl.isPresent()) {
      var res = magicImpl.get().instantiate();
      if (res.isPresent()) { return res.get(); }
    }

    var id = createObj.concreteT().id();
    if (p.of(id).singletonInstance().isPresent()) {
      return getName(id)+"._$self";
    }

    return visitCreateObjNoSingleton(createObj, checkMagic);
  }
  public String visitCreateObjNoSingleton(MIR.CreateObj createObj, boolean checkMagic) {
    var name = createObj.concreteT().id();
    var recordName = getSafeName(name)+"Impl"; // todo: should this include a pkg. in front?
    if (!this.freshRecords.containsKey(name)) {
      var leastSpecific = ParentWalker.leastSpecificSigs(p, p.of(name));
      var args = createObj.captures().stream().map(this::typePair).collect(Collectors.joining(", "));
      var ms = createObj.meths().stream()
        .map(m->this.visitMeth(m, MethExprKind.Kind.RealExpr, leastSpecific))
        .collect(Collectors.joining("\n"));
      var unreachableMs = createObj.unreachableMs().stream()
        .map(m->this.visitMeth(m, MethExprKind.Kind.Unreachable, leastSpecific))
        .collect(Collectors.joining("\n"));
      this.freshRecords.put(name, """
        record %s(%s) implements %s {
          %s
          %s
        }
        """.formatted(recordName, args, getName(name), ms, unreachableMs));
    }

    var captures = createObj.captures().stream().map(x->visitX(x, checkMagic)).collect(Collectors.joining(", "));
    return "new "+recordName+"("+captures+")";
  }

  @Override public String visitX(MIR.X x, boolean checkMagic) {
//    return switch (x.t()) {
//      case MIR.MT.Any ignored -> "(("+getName(x.t())+")("+name(x.name())+"))";
//      case MIR.MT.Plain ignored -> name(x.name());
//      case MIR.MT.Usual ignored -> name(x.name());
//    };
//    return "(("+getName(x.t())+")("+name(x.name())+"))";
    return name(x.name());
  }

  @Override public String visitMCall(MIR.MCall call, boolean checkMagic) {
    if (checkMagic && !call.variant().contains(MIR.MCall.CallVariant.Standard)) {
      var impl = magic.variantCall(call).call(call.name(), call.args(), call.variant(), call.t());
      if (impl.isPresent()) { return "(("+getName(call.t())+")"+impl.get()+")"; }
    }

    var magicImpl = magic.get(call.recv());
    if (checkMagic && magicImpl.isPresent()) {
      var impl = magicImpl.get().call(call.name(), call.args(), call.variant(), call.t());
      if (impl.isPresent()) { return "(("+getName(call.t())+")"+impl.get()+")"; }
    }

    var mustCast = !call.t().equals(call.originalRet());
    var cast = mustCast ? "(("+getName(call.t())+")" : "";

    //    var magicRecv = !(call.recv() instanceof MIR.CreateObj) || magicImpl.isPresent();


    var start = cast+call.recv().accept(this, checkMagic)+"."+name(getName(call.mdf(), call.name()))+"(";
    var args = call.args().stream()
      .map(a->a.accept(this, checkMagic))
      .collect(Collectors.joining(","));
    return start+args+")"+(mustCast ? ")" : "");
  }

  @Override public String visitBoolExpr(MIR.BoolExpr expr, boolean checkMagic) {
    var recv = expr.condition().accept(this, checkMagic);
    var mustCast = !this.funMap.get(expr.then()).ret().equals(this.funMap.get(expr.else_()).ret());
    var cast = mustCast ? "(%s)".formatted(getRetName(expr.t())) : "";

    return "(%s(%s == base.True_0._$self ? %s : %s))".formatted(cast, recv, this.funMap.get(expr.then()).body().accept(this, true), this.funMap.get(expr.else_()).body().accept(this, true));
  }

  private Optional<MIR.Sig> overriddenSig(MIR.Sig sig, Map<ParentWalker.FullMethId, MIR.Sig> leastSpecific) {
    var leastSpecificSig = leastSpecific.get(ParentWalker.FullMethId.of(sig));
    if (leastSpecificSig != null && Streams.zip(sig.xs(),leastSpecificSig.xs()).anyMatch((a,b)->!a.t().equals(b.t()))) {
      return Optional.of(leastSpecificSig.withRT(sig.rt()));
    }
    return Optional.empty();
  }

  private String typePair(MIR.X x) {
    return getName(x.t())+" "+name(x.name());
  }
  private String name(String x) {
    return x.equals("this") ? "f$thiz" : x.replace("'", "$"+(int)'\'')+"$";
  }
  private List<String> getImplsNames(List<MIR.MT.Plain> its) {
    return its.stream()
      .map(this::getName)
      .toList();
  }
  public String getName(MIR.FName name) {
    var capturesSelf = name.capturesSelf() ? "selfCap" : "noSelfCap";
    return getSafeName(name.d())+"$"+name(getName(name.mdf(), name.m()))+"$"+capturesSelf;
  }
  public String getName(MIR.MT t) {
    return switch (t) {
      case MIR.MT.Any ignored -> "Object";
      case MIR.MT.Plain plain -> getName(plain.id(), false);
      case MIR.MT.Usual usual -> getName(usual.it().name(), false);
    };
  }
  public String getRetName(MIR.MT t) {
    return switch (t) {
      case MIR.MT.Any ignored -> "Object";
      case MIR.MT.Plain plain -> getName(plain.id(), true);
      case MIR.MT.Usual usual -> getName(usual.it().name(), true);
    };
  }
  public String getName(Id.DecId name, boolean isRet) {
    return switch (name.name()) {
      case "base.Int", "base.UInt" -> isRet ? "Long" : "long";
      case "base.Float" -> isRet ? "Double" : "double";
      case "base.Str" -> "String";
      default -> {
        if (magic.isMagic(Magic.Int, name)) { yield isRet ? "Long" : "long"; }
        if (magic.isMagic(Magic.UInt, name)) { yield isRet ? "Long" : "long"; }
        if (magic.isMagic(Magic.Float, name)) { yield isRet ? "Double" : "double"; }
        if (magic.isMagic(Magic.Float, name)) { yield isRet ? "Double" : "double"; }
        if (magic.isMagic(Magic.Str, name)) { yield "String"; }
        yield getName(name);
      }
    };
  }
  private static String getPkgName(String pkg) {
    return pkg.replace(".", "$"+(int)'.');
  }
  protected static String getName(Id.DecId d) {
    var pkg = getPkgName(d.pkg());
    return pkg+"."+getBase(d.shortName())+"_"+d.gen();
  }
  protected String getRelativeName(Id.DecId d) {
    if (d.pkg().equals(this.pkg)) {
      return getBase(d.shortName());
    }
    var pkg = getPkgName(d.pkg());
    return pkg+"."+getBase(d.shortName())+"_"+d.gen();
  }
  protected static String getSafeName(Id.DecId d) {
    var pkg = getPkgName(d.pkg());
    return pkg+"$"+getBase(d.shortName())+"_"+d.gen();
  }
  private static String getName(Mdf mdf, Id.MethName m) { return getBase(m.name())+"$"+mdf; }
  private static String getBase(String name) {
    if (name.startsWith(".")) { name = name.substring(1); }
    return name.chars().mapToObj(c->{
      if (c != '\'' && (c == '.' || Character.isAlphabetic(c) || Character.isDigit(c))) {
        return Character.toString(c);
      }
      return "$"+c;
    }).collect(Collectors.joining());
  }
}
