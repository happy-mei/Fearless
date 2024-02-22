package codegen.go;

import codegen.MIR;
import id.Id;
import id.Mdf;
import magic.Magic;
import utils.Bug;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class GoCodegen {
  public record GoProgram(MainFile mainFile, List<PackageCodegen.GoPackage> pkgs) {}
  public record MainFile(String src) implements GoCompiler.Unit {
    @Override public String name() { return "entry.go"; }
  }

  private final MIR.Program p;

  public GoCodegen(MIR.Program p) {
    this.p = p;
  }

  public GoProgram visitProgram(Id.DecId entry) {
    var pkgs = p.pkgs().stream()
      .map(pkg->new PackageCodegen(p, pkg).visitPackage())
      .toList();

    var entryImpl = getName(entry)+"Impl";

    return new GoProgram(
      new MainFile("""
        package main
        import (
          "fmt"
        )
        func main() {
          fmt.Println(%s{}.Φ35_1_immφ(nil))
        }
        """.formatted(entryImpl)),
      pkgs
    );
  }

  static String getPkgName(String pkg) {
    return pkg.replace(".", "φ"+(int)'.');
  }
  static String getPkgFileName(String pkg) {
    return pkg.replace(".", "~"+(int)'.');
  }
  public static String getName(Id.DecId d) {
    return "Φ"+getPkgName(d.pkg())+"φ"+getBase(d.shortName())+"_"+d.gen();
  }
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
}
