package codegen.java;

import main.CompilerFrontEnd;
import utils.Box;
import utils.Bug;
import rt.ResolveResource;

import javax.tools.Diagnostic;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public class JavaProgram extends SimpleJavaFileObject {
  private final String code;
  public static final String MAIN_CLASS_NAME = "FProgram";
  public JavaProgram(String code) {
    this(MAIN_CLASS_NAME, code);
  }
  public JavaProgram(String topLevelClassName, String code) {
    super(URI.create("string:///" + topLevelClassName + Kind.SOURCE.extension), Kind.SOURCE);
    this.code = code;
  }

  public CharSequence getCharContent(boolean ignoreEncodingErrors) {
    return this.code;
  }

  public static Path compile(CompilerFrontEnd.Verbosity verbosity, JavaProgram... files) {
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
      "CapTry",
      "Str",
      "ResolveResource",
      "NativeRuntime"
    ).map(name -> new JavaProgram(name, ResolveResource.getAndRead("/rt/"+name+".java")));
    var userFiles = Arrays.stream(files);
    var codegenUnits = Stream.concat(userFiles, runtimeFiles);

    boolean success = compiler.getTask(
      null,
      null,
      errors::set,
      options,
      null,
      (Iterable<JavaProgram>) codegenUnits::iterator
    ).call();

    if (!success) {
      var diagnostic = errors.get();
      if (diagnostic == null) {
        throw Bug.of("ICE: Java compilation failed.");
      }
      throw Bug.of("ICE: Java compilation failed:\n"+ diagnostic);
    }

    copyRuntimeLibs(workingDir);
    return workingDir.resolve("FProgram.class");
  }

  static void copyRuntimeLibs(Path workingDir) {
    var resourceLibPath = ResolveResource.of("/rt/libnative");
    try {
      Files.walkFileTree(resourceLibPath, new SimpleFileVisitor<>(){
        @Override public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
          var dest = workingDir.resolve(Path.of("rt", "libnative")).resolve(file.getFileName());
          Files.createDirectories(dest);
          Files.copy(file, dest, StandardCopyOption.REPLACE_EXISTING);
          return FileVisitResult.CONTINUE;
        }
      });
    } catch (IOException err) {
      throw new UncheckedIOException(err);
    }
  }
}
