package codegen.java;

import utils.Box;
import utils.Bug;

import javax.tools.Diagnostic;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class JavaProgram extends SimpleJavaFileObject {
  private final String code;
  public JavaProgram(String code) {
    super(URI.create("string:///" + "FProgram" + Kind.SOURCE.extension), Kind.SOURCE);
    this.code = code;
  }

  public CharSequence getCharContent(boolean ignoreEncodingErrors) {
    return this.code;
  }

  public Path compile() {
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
    boolean success = compiler.getTask(
      null,
      null,
      errors::set,
      options,
      null,
      Collections.singleton(this)
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
