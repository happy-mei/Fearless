package codegen.mir2.java;

import ast.T;
import codegen.mir2.MIR;
import id.Id;
import id.Mdf;
import visitors.MIRVisitor2;

import java.util.*;
import java.util.stream.Collectors;

public class JavaCodegen implements MIRVisitor2<String> {
  private MIR.Program p;

  private record ObjLitK(MIR.ObjLit lit, boolean checkMagic) {}
  private HashSet<ObjLitK> objLitsInPkg = new HashSet<>();
  private HashSet<String> codeGenedObjLits = new HashSet<>();
  private MIR.Package pkg;

  public JavaCodegen(MIR.Program p) {
    this.p = p;
  }

  public String visitProgram(MIR.Program p, Id.DecId entry) {
    assert this.p == p;
    var entryName = getName(entry);
    var init = "\nstatic void main(String[] args){ base.Main_0 entry = new "+entryName+"(){}; entry.$35$imm$(new base$46caps.$95System_0(){});\n";

    return "package userCode;\npublic interface FProgram{\n" +p.pkgs().stream()
      .map(this::visitPackage)
      .collect(Collectors.joining("\n"))+init+"}}";
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

    var longName = getName(def.name());
    var shortName = longName;
    if (def.name().pkg().equals(pkg)) { shortName = getBase(def.name().shortName())+"_"+def.name().gen(); }
    final var selfTypeName = shortName;

    var its = def.its().stream()
      .map(this::getName)
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
      .map(m->visitMeth(m, "this", true))
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
  public String visitMeth(MIR.Meth meth, String selfName, boolean signatureOnly) {
    var selfVar = "var "+name(selfName)+" = this;\n";
    var args = meth.xs().stream()
      .map(x->new MIR.X(x.name(), new T(x.t().mdf(), new Id.GX<>("Object")), Optional.empty())) // required for overriding meths with generic args
      .map(this::typePair)
      .collect(Collectors.joining(","));

    var visibility = signatureOnly ? "" : "public ";
    signatureOnly = signatureOnly || meth.isAbs();
    var start = visibility+getRetName(meth.rt())+" "+name(getName(meth.mdf(), meth.name()))+"("+args+")";
    if (signatureOnly) { return start + ";"; }
    return start + "{\n"+selfVar+"return (("+getName(meth.rt())+")("+meth.body().get().accept(this, true)+"));\n}";
  }
  public Optional<String> visitObjLit(MIR.ObjLit lit, boolean checkMagic) {
    var def = lit.def();
    var shortName = getName(def.name());
    if (def.name().pkg().equals(this.pkg.name())) { shortName = getBase(def.name().shortName())+"_"+def.name().gen(); }

    var ms = lit.allMeths().stream()
      .map(m->visitMeth(m, lit.selfName(), false))
      .collect(Collectors.joining("\n"));

    var capts = lit.captures().stream().map(this::typePair).collect(Collectors.joining(", "));

    // TODO: captures
    var res = """
      record %s(%s) implements %s {
        %s
      }
      """.formatted(name(lit.uniqueName()), capts, shortName, ms);
    return Optional.of(res);
  }

  @Override public String visitCreateObj(MIR.CreateObj createObj, boolean checkMagic) {
    if (createObj.canSingleton() && pkg.defs().get(createObj.def()).singletonInstance().isPresent()) {
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
    var magicRecv = false;
    var start = "(("+getRetName(call.t())+")"+call.recv().accept(this, magicRecv)+"."+name(getName(call.mdf(), call.name()))+"(";
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
  private String getName(MIR.Capturer capturer) {
    return capturer.id().shortName()+"$"+name(getName(capturer.methMdf(), capturer.name()))+"$ImplCapts";
  }
  private String getRetName(T t) { return t.match(this::getName, it->getName(it, true)); }
  private String getName(Id.GX<T> gx) { return "Object"; }
  private String getName(Id.IT<T> it) { return getName(it, false); }
  private String getName(Id.IT<T> it, boolean isRet) {
    return getName(it.name());
//    return switch (it.name().name()) {
//      case "base.Int", "base.UInt" -> isRet ? "Long" : "long";
//      case "base.Float" -> isRet ? "Double" : "double";
//      case "base.Str" -> "String";
//      default -> {
//        if (magic.isMagic(Magic.Int, it.name())) { yield isRet ? "Long" : "long"; }
//        if (magic.isMagic(Magic.UInt, it.name())) { yield isRet ? "Long" : "long"; }
//        if (magic.isMagic(Magic.Float, it.name())) { yield isRet ? "Double" : "double"; }
//        if (magic.isMagic(Magic.Float, it.name())) { yield isRet ? "Double" : "double"; }
//        if (magic.isMagic(Magic.Str, it.name())) { yield "String"; }
//        yield getName(it.name());
//      }
//    };
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
