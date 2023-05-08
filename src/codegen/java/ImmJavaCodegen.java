package codegen.java;

import ast.Program;
import codegen.MIR;
import id.Id;
import utils.Bug;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ImmJavaCodegen extends JavaCodegen {
  public ImmJavaCodegen(Program p) { super(p); }

  @Override public String visitProgram(Map<String, List<MIR.Trait>> pkgs, Id.DecId entry) {
    if (!pkgs.containsKey("base")) {
      throw Bug.todo();
    }
    var entryName = getName(entry);
    var init = "\nstatic void main(String[] args){ base.Main_0 entry = new "+entryName+"(){}; System.out.println(entry.$35$()); }\n";

    return "interface FProgram{" + pkgs.entrySet().stream()
      .map(pkg->visitPackage(pkg.getKey(), pkg.getValue()))
      .collect(Collectors.joining("\n"))+init+"}";
  }
}
