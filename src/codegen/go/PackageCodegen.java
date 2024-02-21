package codegen.go;

import codegen.MIR;
import utils.Bug;
import visitors.MIRVisitor;

public class PackageCodegen implements MIRVisitor<String> {
  public GoPackage visitPackage() {
    throw Bug.todo();
  }
  @Override public String visitCreateObj(MIR.CreateObj createObj, boolean checkMagic) {
    throw Bug.todo();
  }
  @Override public String visitX(MIR.X x, boolean checkMagic) {
    throw Bug.todo();
  }
  @Override public String visitMCall(MIR.MCall call, boolean checkMagic) {
    throw Bug.todo();
  }
  public record GoPackage(String name, String src) {}
    public PackageCodegen(MIR.Program p, MIR.Package pkg) {}
}
//import ast.T;
//import codegen.MIR;
//import id.Id;
//import id.Mdf;
//import magic.Magic;
//import utils.Bug;
//import visitors.MIRVisitor;
//
//import java.util.ArrayList;
//import java.util.HashSet;
//import java.util.Objects;
//import java.util.Optional;
//import java.util.stream.Collectors;
//
//import static codegen.go.GoCodegen.getPkgName;
//
//public class PackageCodegen implements MIRVisitor<String> {
//  public record GoPackage(String name, String src) {}
//
//  protected final MIR.Program p;
//  private final MIR.Package pkg;
//  private final MagicImpls magic;
//
//  private record ObjLitK(MIR.ObjLit lit, boolean checkMagic) {}
//  private HashSet<ObjLitK> objLits = new HashSet<>();
//  private HashSet<String> codeGenedObjLits = new HashSet<>();
//  private final HashSet<String> imports = new HashSet<>();
//
//  public PackageCodegen(MIR.Program p, MIR.Package pkg) {
//    this.p = p;
//    this.pkg = pkg;
//    this.magic = new MagicImpls(this, p.p());
//  }
//
//  public GoPackage visitPackage() {
//    this.objLits = new HashSet<>();
//    this.codeGenedObjLits = new HashSet<>();
//    var typeDefs = pkg.defs().values().stream()
//      .map(def->visitTypeDef(pkg.name(), def))
//      .collect(Collectors.joining("\n"));
//    // TODO: move obj lits to the package level (package of the caller, not of type def).
////    var objLits = p.literals().values().stream()
////      .filter(lit->lit.)
////      .map(def->visitTypeDef(pkg.name(), def))
////      .collect(Collectors.joining("\n"));
//
//    var imports = ""; // TODO
//
//    var src = """
//      package %s
//
//      %s
//
//      %s
//      """.formatted(pkg.name(), imports, typeDefs);
//
//    return new GoPackage(pkg.name(), src);
//  }
//
//  public String visitTypeDef(String pkg, MIR.TypeDef def) {
//    var ms = def.meths().stream()
//      .map(m->visitMeth(m, "this", Optional.empty(), true))
//      .collect(Collectors.joining("\n"));
//
//    var singletonStruct = def.singletonInstance().map(objK->{
//      var constructor = visitCreateObjNoSingleton(objK, true);
//      return null; // TODO
//    });
//
//    var lits = new StringBuilder();
//    while (true) {
//      var litsInPkg = new ArrayList<>(objLits);
//      if (objLits.stream().map(litK->litK.lit.uniqueName()).allMatch(codeGenedObjLits::contains)) {
//        break;
//      }
//      litsInPkg.stream().filter(litK->!codeGenedObjLits.contains(litK.lit.uniqueName())).forEach(litK->{
//        codeGenedObjLits.add(litK.lit.uniqueName());
//        visitObjLit(litK.lit, false, litK.checkMagic).ifPresent(lits::append);
//      });
//    }
//
//    var iface = """
//      type %s interface {
//        %s
//      }
//      %s
//      """.formatted(getShortName(def.name()), ms, lits.toString());
//    return iface;
//  }
//
//  public String visitMeth(MIR.Meth meth, String selfName, Optional<String> implName, boolean checkMagic) {
//    var args = meth.xs().stream()
//      .map(x->new MIR.X(x.name(), new T(x.t().mdf(), new Id.GX<>("interface{}")))) // required for overriding meths with generic args
//      .map(this::typePair)
//      .collect(Collectors.joining(","));
//
//    var methodHeader = name(getName(meth.mdf(), meth.name()))+"("+args+") "+getRetName(meth.rt());
//    var signatureOnly = implName.isEmpty();
//    if (signatureOnly) {
//      return methodHeader;
//    }
//    assert !meth.isAbs();
//
//    return """
//      func (%s %s) %s {
//        return %s.(%s)
//      }
//      """.formatted(
//        selfName,
//      implName.get(),
//      methodHeader,
//      meth.body().orElseThrow().accept(this, checkMagic)
//    );
//  }
//
//  public Optional<String> visitObjLit(MIR.ObjLit lit, boolean asSingleton, boolean checkMagic) {
//    System.out.println(lit);
//    return Optional.empty();
////    throw Bug.todo();
//  }
//
//  @Override public String visitCreateObj(MIR.CreateObj createObj, boolean checkMagic) {
//    var magicImpl = magic.get(createObj);
//    if (checkMagic && magicImpl.isPresent()) {
//      return magicImpl.get().instantiate();
//    }
//
//    var lit = p.literals().get(createObj);
//    assert Objects.nonNull(lit);
//
//    if (createObj.canSingleton() && p.of(createObj.def()).singletonInstance().isPresent()) {
//      return getName(createObj.def())+"Impl{}";
//    }
//    return visitCreateObjNoSingleton(createObj, checkMagic);
//  }
//  public String visitCreateObjNoSingleton(MIR.CreateObj createObj, boolean checkMagic) {
//    var lit = p.literals().get(createObj);
//    assert Objects.nonNull(lit);
//    this.objLits.add(new ObjLitK(lit, checkMagic));
//
//    var captures = createObj.captures().stream()
//      .map(x->name(x.name())+": "+visitX(x, checkMagic))
//      .collect(Collectors.joining(", "));
//
//    return "%s{%s}".formatted(name(lit.uniqueName()), captures);
//  }
//
//  @Override public String visitX(MIR.X x, boolean checkMagic) {
//    return "%s.(%s)".formatted(name(x.name()), getName(x.t()));
//  }
//
//  @Override public String visitMCall(MIR.MCall call, boolean checkMagic) {
//    throw Bug.todo();
//  }
//
//  @Override public String visitUnreachable(MIR.Unreachable unreachable) {
//    throw Bug.todo();
//  }
//
//  private String typePair(MIR.X x) {
//    return name(x.name())+" "+getName(x.t());
//  }
//  private String name(String x) {
//    return x.equals("this")
//      ? "this"
//      : x.replace("'", "φ"+(int)'\'').replace("$", "φ"+(int)'$')+"φ";
//  }
//  private String getName(T t) { return t.match(this::getName, this::getName); }
//  private String getRetName(T t) { return t.match(this::getName, this::getName); }
//  private String getName(Id.GX<T> gx) { return "interface{}"; }
//  private String getName(Id.IT<T> it) {
//    return switch (it.name().name()) {
//      case "base.Int" -> "int64";
//      case "base.UInt" -> "uint64";
//      case "base.Float" -> "float64";
//      case "base.Str" -> "string";
//      default -> {
//        if (magic.isMagic(Magic.Int, it.name())) { yield "int64"; }
//        if (magic.isMagic(Magic.UInt, it.name())) { yield "uint64"; }
//        if (magic.isMagic(Magic.Float, it.name())) { yield "float64"; }
//        if (magic.isMagic(Magic.Str, it.name())) { yield "string"; }
//        yield getName(it.name());
//      }
//    };
//  }
//  protected String getName(Id.DecId d) {
//    if (d.pkg().equals(this.pkg.name())) {
//      return getShortName(d);
//    }
//    return getPkgName(d.pkg())+"."+getBase(d.shortName())+"_"+d.gen();
//  }
//  protected String getShortName(Id.DecId d) {
//    return getBase(d.shortName())+"_"+d.gen();
//  }
//  private String getName(Mdf mdf, Id.MethName m) { return getBase(m.name())+"_"+m.num()+"_"+mdf; }
//  private static String getBase(String name) {
//    if (name.startsWith(".")) { name = "Φ"+name.substring(1); }
//    return name.chars().mapToObj(c->{
//      if (c != '\'' && (c == '.' || Character.isAlphabetic(c) || Character.isDigit(c))) {
//        return Character.toString(c);
//      }
//      // We have to start with a capital to export
//      return "Φ"+c;
//    }).collect(Collectors.joining());
//  }
////  protected enum NameKind {
////    LIT, DEF;
////    public String suffix() {
////      return switch (this) {
////        case LIT -> "Impl";
////        case DEF -> "";
////      };
////    }
////  }
//}
