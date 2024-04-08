package main.java;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ast.T;
import codegen.MIRInjectionVisitor;
import codegen.java.JavaCompiler;
import codegen.java.JavaFilesCodegen;
import codegen.java.JavaProgram;
import main.LogicMain;
import program.typesystem.EMethTypeSystem;
import utils.IoErr;

public interface LogicMainJava extends LogicMain<JavaProgram>{
  List<String> commandLineArguments();
  String entry();
  Path cachedBase();
  default String[] makeJavaCommand(Path pathToMain) {
    Path fearlessMainPath = pathToMain.resolve("base/FearlessMain.class");
    var jrePath = Path.of(System.getProperty("java.home"), "bin", "java")
        .toAbsolutePath().toString();
    String entryPoint = "base." 
        + fearlessMainPath.getFileName().toString().split("\\.class")[0];
    String classpath = pathToMain.toString()
      + File.pathSeparator + cachedBase().toString();

    var baseCommand = Stream.of(
      jrePath, "-cp", classpath, entryPoint, this.entry());
    return Stream.concat(baseCommand,
      commandLineArguments().stream())
        .toArray(String[]::new);
  }
  default void cachePackageTypes(ast.Program program) {
    Map<String,List<T.Dec>> mapped= program.ds().values()
      .stream().collect(Collectors.groupingBy(d->d.name().pkg()));
    mapped.entrySet().stream().forEach(e
      ->new HDCache(output()).cacheTypeInfo(e.getKey(),e.getValue()));
    new HDCache(output()).cacheBase(cachedBase());
  }
  default JavaProgram codeGeneration(
      ast.Program program,
      ConcurrentHashMap<Long, EMethTypeSystem.TsT> resolvedCalls
      ){
    var mir = new MIRInjectionVisitor(program, resolvedCalls).visitProgram();
    var files= new JavaFilesCodegen(output(),mir,new JavaCompiler(verbosity()));
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
    Path pathToMain= exe.pathToMain();
    var command= makeJavaCommand(pathToMain);
    System.out.println(List.of(command));
    var pb = new ProcessBuilder(command);
    this.preStart(pb);
    Process proc= IoErr.of(pb::start);
    return proc;
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