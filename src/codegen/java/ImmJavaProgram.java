package codegen.java;

import utils.Box;
import utils.Bug;
import utils.ResolveResource;

import javax.tools.Diagnostic;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class ImmJavaProgram extends JavaProgram {
  public ImmJavaProgram(String code) {
    super(code);
  }
  public ImmJavaProgram(String topLevelClassName, String code) {
    super(topLevelClassName, code);
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
      workingDir.toString(),
      "-Xdiags:verbose"
    );

    var errors = new Box<Diagnostic<?>>(null);

    var codegenUnits = Arrays.stream(files);
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
