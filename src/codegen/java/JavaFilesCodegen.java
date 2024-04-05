package codegen.java;

import codegen.MIR;
import codegen.MethExprKind;
import codegen.ParentWalker;
import codegen.optimisations.OptimisationBuilder;
import id.Id;
import id.Mdf;
import magic.Magic;
import parser.Parser;
import utils.Box;
import utils.Bug;
import utils.Streams;
import visitors.MIRVisitor;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static magic.MagicImpls.getLiteral;

/*
 JavaCompiler need instance compile
 Files:
   String typeDefContent = gen.visitTypeDef(pkg.name(), def, pkg.funs());
   Is this all the 'class body'? what this looks like?
   makes sense to make a file for each of those?
   
 package: one or many?
 class name mangling: does contain package name or not?
 so I can generate fully qualified names, mangling does not include packages
 reserved package names?  'rt' can not be a package name 
 rt is a package that contains core magic stuff that we have
 to include in the classpath to run/compile
 Or, we can copy those source file as 'base' classes 
 what really is "FProgram"
 interface Top
   interface pk1
     interface Person
 
*/
public class JavaFilesCodegen{
  JavaProgram javaProgram= new JavaProgram(new ArrayList<>());
  JavaCodegen gen;
  MIR.Program program;
  JavaFilesCodegen(MIR.Program program){
    this.program=program;
    this.gen= new JavaCodegen(program);
    }
  static final String userCodeDir = "userCode";
  static final String userCodePkg = "package userCode;\n";
  
  public void generateFiles() {
     String fearlessErrorContent = userCodePkg+"""
       class FearlessError extends RuntimeException {
         // ...
       }
       """;
     String fearlessAuxContent = userCodePkg+"""
       class FAux { static FProgram.base.LList_1 LAUNCH_ARGS; }
       """;
      addFile(userCodeDir, "FearlessError.java", fearlessErrorContent);
      addFile(userCodeDir, "FAux.java", fearlessAuxContent);

      for (MIR.Package pkg : program.pkgs()) {
        for (MIR.TypeDef def : pkg.defs().values()) {
          String typeDefContent = gen.visitTypeDef(pkg.name(), def, pkg.funs());
                String fileName = JavaCodegen.getBase(def.name().shortName()) + "_" + def.name().gen() + ".java";
                addFile(userCodePkg, fileName, typeDefContent);
            }
        }

        // Generate the main entry point file
        //String entryContent = generateEntryPoint();
        //addFile("", "Main.java", entryContent);
    }

    private String generateEntryPoint() {
      return "";
    }

    private void addFile(String dirName, String fileName, String content) {

    }

}
