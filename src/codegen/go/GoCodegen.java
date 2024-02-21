package codegen.go;

import codegen.MIR;
import id.Id;

import java.util.List;

public class GoCodegen {
  public record GoProgram(String mainFile, List<PackageCodegen.GoPackage> pkgs) {}
  protected final MIR.Program p;


  public GoCodegen(MIR.Program p) {
    this.p = p;
  }

  public GoProgram visitProgram(Id.DecId entry) {
    var pkgs = p.pkgs().stream()
      .map(pkg->new PackageCodegen(p, pkg).visitPackage())
      .toList();

    var entryPkg = entry.pkg();
    var entryImpl = "Φ"+PackageCodegen.getBase(entry.shortName())+"Impl";

    return new GoProgram(
      """
        package main
        import (
          "fmt"
          "%s"
        )
        func main() {
          fmt.Println(%s.%s{}.Φ35_1_immφ(nil))
        }
        """.formatted(pkgPath(entryPkg), entryPkg, entryImpl),
      pkgs
    );
  }

  public static String pkgPath(String pkg) {
    return "fProgram/userCode/"+getPkgName(pkg);
  }
  static String getPkgName(String pkg) {
    return pkg.replace(".", "φ"+(int)'.');
  }
}
