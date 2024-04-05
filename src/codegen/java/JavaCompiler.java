package codegen.java;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject.Kind;
import javax.tools.ToolProvider;

import main.CompilerFrontEnd;
import utils.Box;
import utils.Bug;
import utils.ResolveResource;

public class JavaCompiler{
  public static final String MAIN_CLASS_NAME = "FProgram";
  public Path compile(CompilerFrontEnd.Verbosity verbosity, JavaFile... files) {
    assert files.length > 0;
    assert Arrays.stream(files).anyMatch(f->f.isNameCompatible(MAIN_CLASS_NAME, Kind.SOURCE));
    var compiler = ToolProvider.getSystemJavaCompiler();
    if (compiler == null) {
      throw new RuntimeException("No Java compiler could be found. Please install a JDK >= 10.");
    }
  
    var workingDir = Paths.get(System.getProperty("java.io.tmpdir"), "fearOut"+UUID.randomUUID());
    if (!workingDir.toFile().mkdir()) {
      throw Bug.of("Could not create a working directory for building the program in: " + System.getProperty("java.io.tmpdir"));
    }
    if (verbosity.printCodegen()) {
      System.err.println("Java codegen working dir: "+workingDir.toAbsolutePath());
    }
  
    var options = List.of(
      "-d",
      workingDir.toString(),
      "-Xdiags:verbose"
    );
  
    var errors = new Box<Diagnostic<?>>(null);
  
    var runtimeFiles = Stream.of(
      "FearlessError",
      "FlowRuntime",
      "PipelineParallelFlow",
      "IO",
      "Random",
      "Error",
      "Try",
      "CapTry"
    ).map(name -> new JavaFile(name, ResolveResource.getAndRead("/rt/"+name+".java")));
    var userFiles = Arrays.stream(files);
    var codegenUnits = Stream.concat(userFiles, runtimeFiles);
  
    boolean success = compiler.getTask(
      null,
      null,
      errors::set,
      options,
      null,
      (Iterable<JavaFile>) codegenUnits::iterator
    ).call();
  
    if (!success) {
      var diagnostic = errors.get();
      if (diagnostic == null) {
        throw Bug.of("ICE: Java compilation failed.");
      }
      throw Bug.of("ICE: Java compilation failed:\n"+ diagnostic);
    }
  
    return workingDir.resolve("FProgram.class");
  }}