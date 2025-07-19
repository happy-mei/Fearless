package codegen.java;

import failure.CompileError;
import main.CompilerFrontEnd.Verbosity;
import main.InputOutput;
import utils.Box;
import utils.Bug;
import utils.IoErr;
import utils.ResolveResource;

import javax.lang.model.SourceVersion;
import javax.tools.Diagnostic;
import javax.tools.ToolProvider;
import java.nio.file.Files;
import java.util.List;

public record JavaCompiler(Verbosity verbosity, InputOutput io){
  public void compile(List<JavaFile> files) {
    assert !files.isEmpty();
    var compiler = ToolProvider.getSystemJavaCompiler();
    assert compiler != null
      :"No Java compiler could be found. Please use a JDK >= 10";
    IoErr.of(()->Files.createDirectories(io.output()));

    // This is looser than it probably should be because we are using preview features that may break in
    // JDK 25, but there's no way to get the _latest_ JDK version of a tool (only of the currently running JRE).
    if (!compiler.getSourceVersions().contains(SourceVersion.RELEASE_24)) {
      throw CompileError.of("Fearless code generation with the Java backend requires JDK 24 or later.");
    }

    CopyRuntimeLibs.of(io.output().toAbsolutePath());
    
    var options = List.of(
      "-d", io.output().toAbsolutePath().toString(),
      "-cp", io.cachedBase().toAbsolutePath().toString(),
      "-Xdiags:verbose",
      "-Xlint:preview",
      "--enable-preview",
      "--release", ResolveResource.javaVersion
      );
    var errors = new Box<Diagnostic<?>>(null);
    boolean success = compiler.getTask(
      null,
      null,
      errors::set,
      options,
      null,
      files
      ).call();
  
    if (success){ return; }
    var diagnostic = errors.get();
    if (diagnostic == null) {
      throw Bug.of("Java compilation failed.");
    }
    throw Bug.of("Java compilation failed:\n"+ diagnostic);
  }
}