package codegen.java;

import utils.Box;
import utils.Bug;
import utils.ResolveResource;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class JavaProgram extends SimpleJavaFileObject {
  private final String code;
  private static final String MAIN_CLASS_NAME = "FProgram";
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

  public static Path compile(JavaProgram... files) {
    assert files.length > 0;
    assert Arrays.stream(files).anyMatch(f->f.isNameCompatible(MAIN_CLASS_NAME, Kind.SOURCE));
    var compiler = ToolProvider.getSystemJavaCompiler();
    if (compiler == null) {
      throw new RuntimeException("No Java compiler could be found. Please install a JDK >= 10.");
    }

    var workingDir = Paths.get(System.getProperty("java.io.tmpdir"), "fearOut"+System.currentTimeMillis());
    if (!workingDir.toFile().mkdir()) {
      throw Bug.of("Could not create a working directory for building the program in: " + System.getProperty("java.io.tmpdir"));
    }
    var options = List.of(
      "-d",
      workingDir.toString()
    );

    var errors = new Box<Diagnostic<?>>(null);

    var runtimeFiles = Stream.of(
      "FlowRuntime"
    ).map(name -> new JavaProgram(name, ResolveResource.getStringOrThrow("/rt-source/"+name+".java")));
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

    return workingDir.resolve("FProgram.class");
  }
}
