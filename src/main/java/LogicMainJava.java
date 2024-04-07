package main.java;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import codegen.MIRInjectionVisitor;
import codegen.java.JavaCodegen;
import codegen.java.JavaCompiler;
import codegen.java.JavaFile;
import codegen.java.JavaFilesCodegen;
import codegen.java.JavaProgram;
import failure.Fail;
import id.Id;
import id.Mdf;
import magic.Magic;
import main.LogicMain;
import program.typesystem.EMethTypeSystem;
import program.typesystem.XBs;

public interface LogicMainJava extends LogicMain<JavaProgram>{
  List<String> commandLineArguments();
  String entry();
  default String[] makeJavaCommand(Path pathToMain) {
    pathToMain=pathToMain.resolve("rt/FearlessMain.class");
    var jrePath= Path.of(System.getProperty("java.home"), "bin", "java")
      .toAbsolutePath();
    String entryPoint= "rt." 
      + pathToMain.getFileName().toString().split("\\.class")[0];
    var baseCommand = Stream.of(jrePath.toString(),entryPoint,this.entry());
    return Stream.concat(baseCommand,
        commandLineArguments().stream())
      .toArray(String[]::new);
  }
  default JavaProgram codeGeneration(
      ast.Program program,
      ConcurrentHashMap<Long, EMethTypeSystem.TsT> resolvedCalls
      ){
    var mir = new MIRInjectionVisitor(program, resolvedCalls).visitProgram();
    var codegen= new JavaCodegen(mir);
    var files= new JavaFilesCodegen(mir);
    files.generateFiles();
    files.writeFiles();
    return files.getJavaProgram();
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
    Path pathToMain= new JavaCompiler()
      .compile(verbosity(), exe.files());
    var command= makeJavaCommand(pathToMain);
    System.out.println(List.of(command));
    System.out.println(pathToMain);
    var pb = new ProcessBuilder(command);
    pb.directory(pathToMain.toFile());
    this.preStart(pb);
    Process proc; try { proc = pb.start();}
    catch (IOException e) { throw new UncheckedIOException(e); }
    return proc;
  }

  @Override default void preStart(ProcessBuilder pb) {
    pb.inheritIO();
  }
}
