package codegen.mir2.java;

import ast.T;
import codegen.mir2.MIR;
import id.Id;
import id.Mdf;
import magic.Magic;
import visitors.MIRVisitor2;

import java.util.*;
import java.util.stream.Collectors;

public class JavaCodegen implements MIRVisitor2<String> {
  protected MIR.Program p;
  private MagicImpls magic;

  private record ObjLitK(MIR.ObjLit lit, boolean checkMagic) {}
  private HashSet<ObjLitK> objLitsInPkg = new HashSet<>();
  private HashSet<String> codeGenedObjLits = new HashSet<>();
  private MIR.Package pkg;

  public JavaCodegen(MIR.Program p) {
    this.p = p;
    this.magic = new MagicImpls(this, p.p());
  }

  protected static String argsToLList(Mdf addMdf) {
    return """
      FAux.LAUNCH_ARGS = base.LList_1._$self;
      for (String arg : args) { FAux.LAUNCH_ARGS = FAux.LAUNCH_ARGS.$43$%s$(arg); }
      """.formatted(addMdf);
  }

  public String visitProgram(MIR.Program p, Id.DecId entry) {
    assert this.p == p;
    var entryName = getName(entry);
    var init = """
      static void main(String[] args){
        %s base.Main_0 entry = %s._$self;
        try {
          entry.$35$imm$(FAux.LAUNCH_ARGS);
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
      entryName
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
      """;

    return fearlessError+"\nclass FAux { static FProgram.base.LList_1 LAUNCH_ARGS; }\npublic interface FProgram{\n" +p.pkgs().stream()
      .map(this::visitPackage)
      .collect(Collectors.joining("\n"))+init+"}";
  }
  public String visitPackage(MIR.Package pkg) {
    this.pkg = pkg;
    this.objLitsInPkg = new HashSet<>();
    this.codeGenedObjLits = new HashSet<>();
    var typeDefs = pkg.defs().values().stream()
      .map(def->visitTypeDef(pkg.name(), def))
      .collect(Collectors.joining("\n"));
    // TODO: move obj lits to the package level (package of the caller, not of type def).
//    var objLits = p.literals().values().stream()
//      .filter(lit->lit.)
//      .map(def->visitTypeDef(pkg.name(), def))
//      .collect(Collectors.joining("\n"));
    return "interface "+getPkgName(pkg.name())+"{" + typeDefs + "\n}";
  }
  public String visitTypeDef(String pkg, MIR.TypeDef def) {
    if (pkg.equals("base") && def.name().name().endsWith("Instance")) {
      return "";
    }
    if (MagicImpls.getLiteral(p.p(), def.name()).isPresent()) {
      return "";
    }

    var longName = getName(def.name());
    var shortName = longName;
    if (def.name().pkg().equals(pkg)) { shortName = getBase(def.name().shortName())+"_"+def.name().gen(); }
    final var selfTypeName = shortName;

    var its = def.its().stream()
      .map(Id.IT::name)
      .filter(dec->MagicImpls.getLiteral(p.p(), dec).isEmpty())
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

    var res = new StringBuilder(start + singletonGet + def.meths().stream()
      .map(m->visitMeth(m, "this", true, true))
      .collect(Collectors.joining("\n")) + "}");

    while (!objLitsInPkg.isEmpty()) {
      var litsInPkg = new ArrayList<>(objLitsInPkg);
      if (objLitsInPkg.stream().map(litK->litK.lit.uniqueName()).allMatch(codeGenedObjLits::contains)) {
        break;
      }
//      objLitsInPkg.clear();
      litsInPkg.stream().filter(litK->!codeGenedObjLits.contains(litK.lit.uniqueName())).forEach(litK->{
        codeGenedObjLits.add(litK.lit.uniqueName());
        visitObjLit(litK.lit, litK.checkMagic).ifPresent(res::append);
      });
    }
    return res.toString();
  }
  public String visitMeth(MIR.Meth meth, String selfName, boolean signatureOnly, boolean checkMagic) {
    var selfVar = "var "+name(selfName)+" = this;\n";
    var args = meth.xs().stream()
      .map(x->new MIR.X(x.name(), new T(x.t().mdf(), new Id.GX<>("Object")))) // required for overriding meths with generic args
      .map(this::typePair)
      .collect(Collectors.joining(","));

    var visibility = signatureOnly ? "" : "public ";
    signatureOnly = signatureOnly || meth.isAbs();
    var start = visibility+getRetName(meth.rt())+" "+name(getName(meth.mdf(), meth.name()))+"("+args+")";
    if (signatureOnly) { return start + ";"; }
    return start + "{\n"+selfVar+"return (("+getName(meth.rt())+")("+meth.body().get().accept(this, checkMagic)+"));\n}";
  }
  public Optional<String> visitObjLit(MIR.ObjLit lit, boolean checkMagic) {
    var def = lit.def();
    var shortName = getName(def.name());
    if (def.name().pkg().equals(this.pkg.name())) { shortName = getBase(def.name().shortName())+"_"+def.name().gen(); }

    var ms = lit.allMeths().stream()
      .map(m->visitMeth(m, lit.selfName(), false, checkMagic))
      .collect(Collectors.joining("\n"));

    var capts = lit.captures().stream().map(this::typePair).collect(Collectors.joining(", "));

    var res = """
      record %s(%s) implements %s {
        %s
      }
      """.formatted(name(lit.uniqueName()), capts, shortName, ms);
    return Optional.of(res);
  }

  @Override public String visitCreateObj(MIR.CreateObj createObj, boolean checkMagic) {
    var magicImpl = magic.get(createObj);
    if (checkMagic && magicImpl.isPresent()) {
      return magicImpl.get().instantiate();
    }

    if (createObj.canSingleton() && p.of(createObj.def()).singletonInstance().isPresent()) {
      return getName(createObj.def())+"._$self";
    }
    return visitCreateObjNoSingleton(createObj, checkMagic);
  }
  public String visitCreateObjNoSingleton(MIR.CreateObj createObj, boolean checkMagic) {
    var lit = p.literals().get(createObj);
    assert Objects.nonNull(lit);
    this.objLitsInPkg.add(new ObjLitK(lit, checkMagic));

    var captures = createObj.captures().stream()
      .map(x->visitX(x, checkMagic))
      .collect(Collectors.joining(", "));

    return "new %s(%s)".formatted(name(lit.uniqueName()), captures);
  }

  @Override public String visitX(MIR.X x, boolean checkMagic) {
//    if (x.capturer().isPresent()) {
//      return "(("+getName(x.t())+")(this."+name(x.name())+"))";
//    }
    return "(("+getName(x.t())+")("+name(x.name())+"))";
  }

  @Override public String visitMCall(MIR.MCall call, boolean checkMagic) {
    var variants = EnumSet.of(codegen.MIR.MCall.CallVariant.Standard);
    if (checkMagic && !call.variant().contains(MIR.MCall.CallVariant.Standard)) {
      var impl = magic.variantCall(call).call(call.name(), call.args(), Map.of(), variants);
      if (impl.isPresent()) { return impl.get(); }
    }

    var magicImpl = magic.get(call.recv());
    if (checkMagic && magicImpl.isPresent()) {
      var impl = magicImpl.get().call(call.name(), call.args(), Map.of(), variants);
      if (impl.isPresent()) { return impl.get(); }
    }

//    var magicRecv = !(call.recv() instanceof MIR.CreateObj) || magicImpl.isPresent();
    var start = "(("+getRetName(call.t())+")"+call.recv().accept(this, checkMagic)+"."+name(getName(call.mdf(), call.name()))+"(";
    var args = call.args().stream()
      .map(a->a.accept(this, checkMagic))
      .collect(Collectors.joining(","));
    return start+args+"))";
  }

  @Override public String visitUnreachable(MIR.Unreachable unreachable) {
    return """
      (switch (1) {
        default -> throw new RuntimeException("Unreachable code");
        case 2 -> (Object)null;
        })
      """;
  }

  private String typePair(MIR.X x) {
    return getName(x.t())+" "+name(x.name());
  }
  private String name(String x) {
    return x.equals("this") ? "f$thiz" : x.replace("'", "$"+(int)'\'')+"$";
  }
  private List<String> getImplsNames(List<Id.IT<T>> its) {
    return its.stream()
      .map(this::getName)
      .toList();
  }
  private String getName(T t) { return t.match(this::getName, this::getName); }
  private String getRetName(T t) { return t.match(this::getName, it->getName(it, true)); }
  private String getName(Id.GX<T> gx) { return "Object"; }
  private String getName(Id.IT<T> it) { return getName(it, false); }
  private String getName(Id.IT<T> it, boolean isRet) {
    return switch (it.name().name()) {
      case "base.Int", "base.UInt" -> isRet ? "Long" : "long";
      case "base.Float" -> isRet ? "Double" : "double";
      case "base.Str" -> "String";
      default -> {
        if (magic.isMagic(Magic.Int, it.name())) { yield isRet ? "Long" : "long"; }
        if (magic.isMagic(Magic.UInt, it.name())) { yield isRet ? "Long" : "long"; }
        if (magic.isMagic(Magic.Float, it.name())) { yield isRet ? "Double" : "double"; }
        if (magic.isMagic(Magic.Float, it.name())) { yield isRet ? "Double" : "double"; }
        if (magic.isMagic(Magic.Str, it.name())) { yield "String"; }
        yield getName(it.name());
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
