package codegen.go;

import codegen.MIR;
import id.Id;
import id.Mdf;
import magic.Magic;
import utils.Bug;
import utils.Streams;
import visitors.MIRVisitor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static codegen.go.GoCodegen.getPkgName;
import static codegen.go.GoCodegen.pkgPath;
import static magic.MagicImpls.getLiteral;

public class PackageCodegen implements MIRVisitor<String> {
  public record GoPackage(String pkg, String src) implements GoCompiler.Unit {
    public String pkg() {
      return "userCode/"+this.pkg;
    }
    @Override public String name() {
      return pkg+".go";
    }
    @Override public void write(Path workingDir) throws IOException {
      var userCodeDir = workingDir.resolve("userCode");
      if (!Files.exists(userCodeDir)) {
        if (!userCodeDir.toFile().mkdir()) { throw Bug.of("Could not create: "+userCodeDir.toAbsolutePath()); }
      }
      GoCompiler.Unit.super.write(workingDir);
    }
  }

  protected final MIR.Program p;
  private final MIR.Package pkg;
  private final MagicImpls magic;

  private final HashMap<Id.DecId, String> freshStructs = new HashMap<>();
  private final HashSet<String> imports = new HashSet<>();

  public PackageCodegen(MIR.Program p, MIR.Package pkg) {
    this.p = p;
    this.pkg = pkg;
    this.magic = new MagicImpls(this, p.p());
  }

  public GoPackage visitPackage() {
    var typeDefs = pkg.defs().values().stream()
      .map(def->visitTypeDef(pkg.name(), def))
      .collect(Collectors.joining("\n"));
    var funs = pkg.funs().stream().map(this::visitFun).collect(Collectors.joining("\n"));

    var freshStructs = String.join("\n", this.freshStructs.values());

    var imports = this.imports.isEmpty() ? "" : """
      import (
        %s
      )
      """.formatted(this.imports.stream().map(GoCodegen::pkgPath).map("\"%s\""::formatted).collect(Collectors.joining("\n")));

    var src = """
      package %s
      %s
      %s
      %s
      %s
      """.formatted(pkg.name(), imports, typeDefs, freshStructs, funs);

    return new GoPackage(pkg.name(), src);
  }

  public String visitTypeDef(String pkg, MIR.TypeDef def) {
    if (pkg.equals("base") && def.name().name().endsWith("Instance")) {
      return "";
    }
    if (getLiteral(p.p(), def.name()).isPresent()) {
      return "";
    }

    var ms = def.sigs().stream()
      .map(this::visitSig)
      .collect(Collectors.joining("\n"));

    def.singletonInstance().ifPresent(k->visitCreateObjNoSingleton(k, true));

    return """
      type %s interface {
        %s
      }
      """.formatted(getShortName(def.name()), ms);
  }

  public String visitSig(MIR.Sig sig) {
    var args = sig.xs().stream()
      .map(x->new MIR.X(x.name(), new MIR.MT.Any(x.t().mdf()))) // required for overriding meths with generic args
      .map(this::typePair)
      .collect(Collectors.joining(","));

    return name(getName(sig.mdf(), sig.name()))+"("+args+") "+getRetName(sig.rt());
  }

  public String visitMeth(MIR.Meth meth, Id.DecId associated, boolean isReachable) {
    var sigArgs = meth.sig().xs().stream()
      .map(x->new MIR.X(x.name(), new MIR.MT.Any(x.t().mdf()))) // required for overriding meths with generic args
      .toList();
    var args = sigArgs.stream()
      .map(this::typePair)
      .collect(Collectors.joining(","));
    var selfArg = meth.capturesSelf() ? Stream.of("FSpφself") : Stream.<String>of();
    var funArgs = Streams.of(sigArgs.stream().map(MIR.X::name).map(this::name), selfArg, meth.captures().stream().map(this::name).map(x->"FSpφself."+x))
      .collect(Collectors.joining(","));

    var realExpr = isReachable
      ? "return %s(%s)".formatted(getName(meth.fName()), funArgs)
      : "panic(\"Unreachable code\")";

    var methodHeader = name(getName(meth.sig().mdf(), meth.sig().name()))+"("+args+") "+getRetName(meth.sig().rt());

    return """
      func (FSpφself %s) %s {
        %s
      }
      """.formatted(
      getName(associated)+"Impl",
        methodHeader,
        realExpr);
  }

  public String visitFun(MIR.Fun fun) {
    var name = getName(fun.name());
    var args = fun.args().stream().map(x->new MIR.X(x.name(), new MIR.MT.Any(x.t().mdf())))
      .map(this::typePair)
      .collect(Collectors.joining(", "));
    var body = fun.body().accept(this, true);

    return """
      func %s(%s) %s {
        return %s
      }
      """.formatted(name, args, getRetName(fun.ret()), body);
  }

  @Override public String visitCreateObj(MIR.CreateObj createObj, boolean checkMagic) {
    var magicImpl = magic.get(createObj);
    if (checkMagic && magicImpl.isPresent()) {
      return magicImpl.get().instantiate();
    }

    var id = createObj.concreteT().id();
    if (!id.pkg().equals(this.pkg.name())) {
      this.imports.add(id.pkg());
    }

    if (p.of(id).singletonInstance().isPresent()) {
      return getName(id)+"Impl{}";
    }
    return visitCreateObjNoSingleton(createObj, checkMagic);
  }
  public String visitCreateObjNoSingleton(MIR.CreateObj createObj, boolean checkMagic) {
    var name = createObj.concreteT().id();
    if (!name.pkg().equals(this.pkg.name())) {
      this.imports.add(name.pkg());
    }

    var structName = getName(name)+"Impl"; // todo: should this include a pkg. in front?
    if (!this.freshStructs.containsKey(name)) {
      var ms = createObj.meths().stream()
        .map(m->this.visitMeth(m, name, true))
        .collect(Collectors.joining("\n"));
      var unreachableMs = createObj.unreachableMs().stream()
        .map(m->this.visitMeth(m, name, false))
        .collect(Collectors.joining("\n"));

      var captures = createObj.captures().stream().map(this::typePair).collect(Collectors.joining("\n"));
      this.freshStructs.put(name, """
        type %s struct {
          %s
        }
        %s
        %s
        """.formatted(structName, captures, ms, unreachableMs));
    }

    var captures = createObj.captures().stream().map(x->visitX(x, checkMagic)).collect(Collectors.joining(", "));
    return structName+"{"+captures+"}";
  }

  @Override public String visitX(MIR.X x, boolean checkMagic) {
    return "%s.(%s)".formatted(name(x.name()), getName(x.t()));
  }

  @Override public String visitMCall(MIR.MCall call, boolean checkMagic) {
    if (checkMagic && !call.variant().contains(MIR.MCall.CallVariant.Standard)) {
      var impl = magic.variantCall(call).call(call.name(), call.args(), call.variant(), call.t());
      if (impl.isPresent()) { return impl.get(); }
    }

    var magicImpl = magic.get(call.recv());
    if (checkMagic && magicImpl.isPresent()) {
      var impl = magicImpl.get().call(call.name(), call.args(), call.variant(), call.t());
      if (impl.isPresent()) { return impl.get(); }
    }

    var start = "("+call.recv().accept(this, checkMagic)+"."+name(getName(call.mdf(), call.name()))+"(";
    var args = call.args().stream()
      .map(a->a.accept(this, checkMagic))
      .collect(Collectors.joining(","));
    return start+args+")).("+getRetName(call.t())+")";
  }

  private String typePair(MIR.X x) {
    return name(x.name())+" "+getName(x.t());
  }
  private String name(String x) {
    return x.equals("this")
      ? "this"
      : x.replace("'", "φ"+(int)'\'').replace("$", "φ"+(int)'$')+"φ";
  }
public String getName(MIR.FName name) {
  var capturesSelf = name.capturesSelf() ? "selfCap" : "noSelfCap";
  return getSafeName(name.d())+"φ"+name(getName(name.mdf(), name.m()))+"φ"+capturesSelf;
}
  public String getName(MIR.MT t) {
    return switch (t) {
      case MIR.MT.Any ignored -> "interface{}";
      case MIR.MT.Plain plain -> getName(plain.id(), false);
      case MIR.MT.Usual usual -> getName(usual.it().name(), false);
    };
  }
  public String getRetName(MIR.MT t) {
    return switch (t) {
      case MIR.MT.Any ignored -> "interface{}";
      case MIR.MT.Plain plain -> getName(plain.id(), true);
      case MIR.MT.Usual usual -> getName(usual.it().name(), true);
    };
  }
  public String getName(Id.DecId name, boolean isRet) {
    return switch (name.name()) {
      case "base.Int" -> "int64";
      case "base.UInt" -> "uint64";
      case "base.Float" -> "float64";
      case "base.Str" -> "string";
      default -> {
        if (magic.isMagic(Magic.Int, name)) { yield "int64"; }
        if (magic.isMagic(Magic.UInt, name)) { yield "uint64"; }
        if (magic.isMagic(Magic.Float, name)) { yield "float64"; }
        if (magic.isMagic(Magic.Str, name)) { yield "string"; }
        yield getName(name);
      }
    };
  }
  protected String getName(Id.DecId d) {
    if (d.pkg().equals(this.pkg.name())) {
      return getShortName(d);
    }
    return getPkgName(d.pkg())+"."+getShortName(d);
  }
  protected static String getSafeName(Id.DecId d) {
    var pkg = getPkgName(d.pkg());
    return pkg+"φ"+getBase(d.shortName())+"_"+d.gen();
  }
  public static String getShortName(Id.DecId d) {
    return "Φ"+getBase(d.shortName())+"_"+d.gen();
  }
  private String getName(Mdf mdf, Id.MethName m) { return getBase(m.name())+"_"+m.num()+"_"+mdf; }
  public static String getBase(String name) {
    if (name.startsWith(".")) { name = "Φ"+name.substring(1); }
    return name.chars().mapToObj(c->{
      if (c != '\'' && (c == '.' || Character.isAlphabetic(c) || Character.isDigit(c))) {
        return Character.toString(c);
      }
      // We have to start with a capital to export
      return "Φ"+c;
    }).collect(Collectors.joining());
  }
//  protected enum NameKind {
//    LIT, DEF;
//    public String suffix() {
//      return switch (this) {
//        case LIT -> "Impl";
//        case DEF -> "";
//      };
//    }
//  }
}
