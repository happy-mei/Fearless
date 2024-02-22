package codegen.go;

import codegen.MIR;
import id.Id;

import java.util.List;

public class GoCodegen {
  public record GoProgram(MainFile mainFile, List<PackageCodegen.GoPackage> pkgs) {}
  public record MainFile(String src) implements GoCompiler.Unit {
    @Override public String pkg() { return ""; }
    @Override public String name() { return "entry.go"; }
  }
  protected final MIR.Program p;


  public GoCodegen(MIR.Program p) {
    this.p = p;
  }

  public GoProgram visitProgram(Id.DecId entry) {
    var pkgs = p.pkgs().stream()
      .map(pkg->new PackageCodegen(p, pkg).visitPackage())
      .toList();

    var entryPkg = entry.pkg();
    var entryImpl = PackageCodegen.getShortName(entry)+"Impl";

    return new GoProgram(
      new MainFile("""
        package main
        import (
          "fmt"
          "%s"
        )
        func main() {
          fmt.Println(%s.%s{}.Φ35_1_immφ(nil))
        }
        """.formatted(pkgPath(entryPkg), entryPkg, entryImpl)),
      pkgs
    );
  }

  public static String pkgPath(String pkg) {
    return "main/userCode/"+getPkgFileName(pkg);
  }
  static String getPkgName(String pkg) {
    return pkg.replace(".", "φ"+(int)'.');
  }
  static String getPkgFileName(String pkg) {
    return pkg.replace(".", "~"+(int)'.');
  }
}
