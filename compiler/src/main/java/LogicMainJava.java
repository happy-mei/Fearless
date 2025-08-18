package main.java;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import ast.Program;
import codegen.MIR;
import codegen.MIRInjectionVisitor;
import codegen.java.JavaCompiler;
import codegen.java.JavaMagicImpls;
import codegen.java.JavaProgram;
import codegen.optimisations.OptimisationBuilder;
import main.CompilerFrontEnd.Verbosity;
import main.InputOutput;
import main.FullLogicMain;
import program.typesystem.TsT;

public interface LogicMainJava extends FullLogicMain<JavaProgram> {
  @Override default void cachePackageTypes(ast.Program program) {
    HDCache.cachePackageTypes(this, program);
  }

  @Override default MIR.Program lower(Program program, ConcurrentHashMap<Long, TsT> resolvedCalls) {
    var mir = new MIRInjectionVisitor(cachedPkg(),program, resolvedCalls).visitProgram();
    var magic = new JavaMagicImpls(null, null, mir.p());
    return new OptimisationBuilder(magic)
      .withAsIdFnOptimisation()
      .withBoolIfOptimisation()
      .withBlockOptimisation()
      .run(mir);
  }
  default JavaProgram codeGeneration(MIR.Program mir) {
    var res= new JavaProgram(this,mir);

    if (verbosity().printCodegen()) {
      var tmp = utils.IoErr.of(()->java.nio.file.Files.createTempDirectory("fgen"));
      res.writeJavaFiles(tmp);
      System.out.println("saved to "+tmp);
      // Write Java files to the output directory for testing purposes
//      try {
//        Path outputDir = Path.of("test/output-java");
//        // before writing, clean the output directory
//        if (Files.exists(outputDir)) {
//          Files.walk(outputDir)
//            .sorted((a, b) -> b.compareTo(a)) // reverse order to delete files before directories
//            .forEach(path -> {
//              try { Files.delete(path); } catch (IOException e) { /* ignore */ }
//            });
//        }
//        Files.createDirectories(outputDir);
//        res.writeJavaFiles(outputDir);
//        System.out.println("Java files saved to " + outputDir.toAbsolutePath());
//      } catch (IOException e) {
//        System.err.println("Failed to write java files: " + e.getMessage());
//      }
    }
    return res;
  }

  @Override default void compileBackEnd(JavaProgram src) {
    var c= new JavaCompiler(verbosity(),io());
    c.compile(src.files());
  }

  default ProcessBuilder execution(JavaProgram exe){
    return MakeJavaProcess.of(io());
  }
  static LogicMainJava of(
		  InputOutput io, Verbosity verbosity){
    var cachedPkg=new HashSet<String>();
    return new LogicMainJava(){
      public InputOutput io(){ return io; }
      public HashSet<String> cachedPkg(){ return cachedPkg; }
      public Verbosity verbosity(){ return verbosity; }
    };
  }
}
class MakeJavaProcess{
  static public ProcessBuilder of(InputOutput io) {
    var command= makeJavaCommand(io);
    //System.out.println(List.of(command));
    return new ProcessBuilder(command);
  }
  static private String[] makeJavaCommand(InputOutput io) {
    Path fearlessMainPath = io.cachedBase()
            .resolve("base/FearlessMain.class");
    var jrePath = Path.of(System.getProperty("java.home"), "bin", "java")
            .toAbsolutePath().toString();
    String entryPoint = "base."
            + fearlessMainPath.getFileName().toString().split("\\.class")[0];
    String classpath = io.output().toAbsolutePath()
            + File.pathSeparator
            + io.cachedBase().toAbsolutePath();
    var baseCommand = Stream.of(
      jrePath,
      "-cp", classpath,
      "--enable-preview",
      "--enable-native-access=ALL-UNNAMED",
      "-ea",
      entryPoint,
      io.entry()
    );
    return Stream.concat(
      baseCommand,
      io.commandLineArguments().stream()
    ).toArray(String[]::new);
  }
}