package codegen.go;

import codegen.MIR;
import utils.Bug;
import visitors.MIRVisitor;

import java.util.HashSet;
import java.util.Optional;
import java.util.stream.Collectors;

public class PackageCodegen implements MIRVisitor<String> {
  public record GoPackage(String name, String src) {}

  protected final MIR.Program p;
  private final MIR.Package pkg;
  private final MagicImpls magic;

  private record ObjLitK(MIR.ObjLit lit, boolean checkMagic) {}
  private HashSet<ObjLitK> objLits = new HashSet<>();
  private HashSet<String> codeGenedObjLits = new HashSet<>();
  private final HashSet<String> imports = new HashSet<>();

  public PackageCodegen(MIR.Program p, MIR.Package pkg) {
    this.p = p;
    this.pkg = pkg;
    this.magic = new MagicImpls(this, p.p());
  }

  public GoPackage visitPackage() {
    this.objLits = new HashSet<>();
    this.codeGenedObjLits = new HashSet<>();
    var typeDefs = pkg.defs().values().stream()
      .map(def->visitTypeDef(pkg.name(), def))
      .collect(Collectors.joining("\n"));
    // TODO: move obj lits to the package level (package of the caller, not of type def).
//    var objLits = p.literals().values().stream()
//      .filter(lit->lit.)
//      .map(def->visitTypeDef(pkg.name(), def))
//      .collect(Collectors.joining("\n"));
    throw Bug.todo();
  }

  public String visitTypeDef(String pkg, MIR.TypeDef def) {
    throw Bug.todo();
  }

  public String visitMeth(MIR.Meth meth, String selfName, boolean signatureOnly, boolean checkMagic) {
    throw Bug.todo();
  }

  public Optional<String> visitObjLit(MIR.ObjLit lit, boolean checkMagic) {
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

  @Override public String visitUnreachable(MIR.Unreachable unreachable) {
    throw Bug.todo();
  }

  private static String getBase(String name) {
    if (name.startsWith(".")) { name = name.substring(1); }
    return name.chars().mapToObj(c->{
      if (c != '\'' && (c == '.' || Character.isAlphabetic(c) || Character.isDigit(c))) {
        return Character.toString(c);
      }
      // We have to start with a capital to export
      return "Φ"+c;
    }).collect(Collectors.joining());
  }
  protected enum NameKind {
    LIT, DEF;
    public String suffix() {
      return switch (this) {
        case LIT -> "Impl";
        case DEF -> "";
      };
    }
  }
}
