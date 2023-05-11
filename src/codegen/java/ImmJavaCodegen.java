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
    var string2Str = MagicImpls.STR_TEMPLATE.formatted("arg");
    var loadArgs = """
      var cons = new base.Cons_0(){};
      base.LList_1 tail = new base.LList_1(){};
      for (int i = args.length - 1; i >= 0; --i) {
        var arg = args[i];
        var fArg = %s;
        tail = cons.$35$(fArg, tail);
      }
      """.formatted(string2Str);
    var init = "\nstatic void main(String[] args){ "+loadArgs+" base.Main_0 entry = new "+entryName+"(){}; System.out.println(entry.$35$(tail)); }\n";

    return "interface FProgram{" + pkgs.entrySet().stream()
      .map(pkg->visitPackage(pkg.getKey(), pkg.getValue()))
      .collect(Collectors.joining("\n"))+init+"}";
  }
}
