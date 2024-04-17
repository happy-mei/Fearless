package codegen.java;

import main.CompilerFrontEnd;
import utils.Box;
import utils.Bug;
import utils.ResolveResource;
import utils.DeleteOnExit;
import utils.IoErr;

import javax.tools.Diagnostic;
import javax.tools.ToolProvider;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class ImmJavaProgram extends JavaFile {
  public ImmJavaProgram(String code) {
    super(Bug.<String>err(),code);//TODO:dead code anyway
  }
  public ImmJavaProgram(String topLevelClassName, String code) {
    super(topLevelClassName, code);
  }

  public static Path compile(CompilerFrontEnd.Verbosity verbosity, JavaFile... files) {
    assert Arrays.stream(files).anyMatch(f->f.isNameCompatible(Bug.<String>err(), Kind.SOURCE));
    var compiler = ToolProvider.getSystemJavaCompiler();
    if (compiler == null) {
      throw new RuntimeException("No Java compiler could be found. Please install a JDK >= 10.");
    }

    var workingDir = IoErr.of(()->Files.createTempDirectory("fearOut"));
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
      "Str",
      "ResolveResource",
      "NativeRuntime",
      "FearlessError"
    ).map(name->new JavaFile(name, ResolveResource.getAndReadAsset("/rt/"+name+".java")));
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

    CopyRuntimeLibs.of(workingDir);
    if (!verbosity.printCodegen()) {
      DeleteOnExit.of(workingDir);
    }
    return workingDir.resolve("FProgram.class");
  }
}
