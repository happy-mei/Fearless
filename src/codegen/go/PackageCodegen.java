package codegen.go;

import codegen.MIR;
import codegen.ParentWalker;
import id.Id;
import id.Mdf;
import magic.Magic;
import utils.Mapper;
import utils.Streams;
import visitors.MIRVisitor;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static codegen.go.GoCodegen.getBase;
import static codegen.go.GoCodegen.getPkgFileName;
import static magic.MagicImpls.getLiteral;

public class PackageCodegen implements MIRVisitor<String> {
  public record GoPackage(String pkg, String src) implements GoCompiler.Unit {
    public String pkg() {
      return this.pkg;
    }
    @Override public String name() {
      return pkg+".go";
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
      """.formatted(this.imports.stream()
        .map("\"%s\""::formatted)
        .collect(Collectors.joining("\n"))
        );

    var src = """
      package main
      %s
      %s
      %s
      %s
      """.formatted(imports, typeDefs, freshStructs, funs);

    return new GoPackage(getPkgFileName(pkg.name()), src);
  }

  public String visitTypeDef(String pkg, MIR.TypeDef def) {
    if (pkg.equals("base") && def.name().name().endsWith("Instance")) {
      return "";
    }
    if (getLiteral(p.p(), def.name()).isPresent()) {
      return "";
    }

    var leastSpecific = this.leastSpecificSigs(def);
    var ms = def.sigs().stream()
      .map(sig->leastSpecific.get(sig.name()))
      .map(this::visitSig)
      .collect(Collectors.joining("\n"));

    def.singletonInstance().ifPresent(k->visitCreateObjNoSingleton(k, true));

    return """
      type %s interface {
        %s
      }
      """.formatted(getName(def.name()), ms);
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
    if (fun.name().d().pkg().equals("base") && fun.name().d().name().endsWith("Instance")) {
      return "";
    }
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
      var res = magicImpl.get().instantiate();
      this.imports.addAll(res.imports());
      return res.output();
    }

    var id = createObj.concreteT().id();

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

    var leastSpecific = this.leastSpecificSigs(p.of(createObj.concreteT().id()));

    var structName = getName(name)+"Impl";
    if (!this.freshStructs.containsKey(name)) {
      var ms = createObj.meths().stream()
        .map(m->m.withSig(leastSpecific.get(m.sig().name())))
        .map(m->this.visitMeth(m, name, true))
        .collect(Collectors.joining("\n"));
      var unreachableMs = createObj.unreachableMs().stream()
        .map(m->m.withSig(leastSpecific.get(m.sig().name())))
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
      if (impl.isPresent()) {
        var magic = impl.get();
        this.imports.addAll(magic.imports());
        return magic.output();
      }
    }

    var magicImpl = magic.get(call.recv());
    if (checkMagic && magicImpl.isPresent()) {
      var impl = magicImpl.get().call(call.name(), call.args(), call.variant(), call.t());
      if (impl.isPresent()) {
        var magic = impl.get();
        this.imports.addAll(magic.imports());
        return magic.output();
      }
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
    return getName(name.d())+"φ"+name(getName(name.mdf(), name.m()))+"φ"+capturesSelf;
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
  public String getName(Id.DecId d) {
    return GoCodegen.getName(d);
  }
  private String getName(Mdf mdf, Id.MethName m) { return getBase(m.name())+"_"+m.num()+"_"+mdf; }

  /**
   * Go does not support covariant return types, they must match exactly.
   * Because Mearless _does_ support this (or more accurately has no semantics around it)
   * we need to find the original type here if there is one.
   * TODO: this might be broken if B and C both offer different .m1 to D
   */
  private Map<Id.MethName, MIR.Sig> leastSpecificSigs(MIR.TypeDef root) {
    return ParentWalker.of(p, root)
      .flatMap(def->def.sigs().stream())
      .collect(Collectors.toMap(MIR.Sig::name, sig->sig, (sigA,sigB)->sigB));
  }
}
