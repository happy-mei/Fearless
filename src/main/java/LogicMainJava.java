package main.java;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import codegen.MIRInjectionVisitor;
import codegen.java.JavaCompiler;
import codegen.java.JavaFilesCodegen;
import codegen.java.JavaProgram;
import main.LogicMain;
import program.typesystem.EMethTypeSystem;

public interface LogicMainJava extends LogicMain<JavaProgram>{
  List<String> commandLineArguments();
  String entry();
  default void cachePackageTypes(ast.Program program) {
    HDCache.cachePackageTypes(this, program);
  }
  default JavaProgram codeGeneration(
      ast.Program program,
      ConcurrentHashMap<Long, EMethTypeSystem.TsT> resolvedCalls
      ){
    var mir = new MIRInjectionVisitor(cachedPkg(),program, resolvedCalls).visitProgram();
    var files= new JavaFilesCodegen(cachedPkg(),output(),mir,new JavaCompiler(verbosity()));
    files.generateFiles();
    JavaProgram res= files.getJavaProgram(files.readAllFiles(this.rtDir()));
    //res.writeFiles();//just for debugging
    return res;
  }
  default JavaProgram mainCodeGeneration(
      ast.Program program,
      JavaProgram exe,
      ConcurrentHashMap<Long, EMethTypeSystem.TsT> resolvedCalls 
      ) {
    return exe;
    }

  default Process execution(
      ast.Program program,
      JavaProgram exe,
      JavaProgram mainExe,
      ConcurrentHashMap<Long, EMethTypeSystem.TsT> resolvedCalls
      ){
    return new RunJava(this).execution(exe.pathToMain());
  }
  @Override default void preStart(ProcessBuilder pb) {
    pb.inheritIO();
  }
}

/*
 
 During typechecking, for each package we need to produce a file with
 package pkgName
 abstract Fearless
 and save that one in output/pkgName.
 
 
 */