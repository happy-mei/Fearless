package codegen.go;

import main.CompilerFrontEnd;
import org.apache.commons.lang3.SystemUtils;
import utils.Bug;
import utils.ResolveResource;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Stream;

public record GoCompiler(Unit entry, List<? extends Unit> rt, List<? extends Unit> units, CompilerFrontEnd.Verbosity verbosity) {
  public static List<Unit.Runtime> IMM_RUNTIME_UNITS = List.of(
    new Unit.Runtime("", "go.mod"),
    new Unit.Runtime("rt/base~rt", "base~rt.go")
  );

  public sealed interface Unit permits GoCodegen.MainFile, Unit.Runtime, PackageCodegen.GoPackage {
    String pkg();
    String name();
    String src();
    default void write(Path workingDir) throws IOException {
      var pkgDir = workingDir.resolve(this.pkg());
      if (!this.pkg().isEmpty() && !pkgDir.toFile().mkdir()) { throw Bug.of("Could not create: "+pkgDir.toAbsolutePath()); }
      Files.writeString(pkgDir.resolve(this.name()), this.src());
    }
    record Runtime(String pkg, String name) implements Unit {
      @Override public String src() {
        return ResolveResource.getStringOrThrow("/rt-source/"+name);
      }

      @Override public void write(Path workingDir) throws IOException {
        if (!this.pkg.isEmpty()) {
          var rtDir = workingDir.resolve("rt");
          if (!Files.exists(rtDir)) {
            if (!rtDir.toFile().mkdir()) { throw Bug.of("Could not create: "+rtDir.toAbsolutePath()); }
          }
        }
        Unit.super.write(workingDir);
      }
    }
  }

  public Path compile() throws IOException {
    assert !units.isEmpty();

    var workingDir = Paths.get(System.getProperty("java.io.tmpdir"), "fearOut"+UUID.randomUUID());
    if (verbosity.printCodegen()) {
      System.out.println("Go codegen working dir: "+workingDir.toAbsolutePath());
    }
    if (!workingDir.toFile().mkdir()) {
      throw Bug.of("Could not create a working directory for building the program in: " + System.getProperty("java.io.tmpdir"));
    }

    this.entry().write(workingDir);
    for (var unit : this.units()) { unit.write(workingDir); }
    for (var unit : this.rt()) { unit.write(workingDir); }

    try {
      runGoCmd(workingDir, "build", "-o", "fear_out", "entry.go").join();
    } catch (CompletionException err) {
      var cause = err.getCause();
      if (cause instanceof Bug bug) { throw bug; }
      throw Bug.of(cause);
    }

    if (!verbosity.printCodegen()) {
      cleanUp(workingDir);
    }

    // TODO: maybe fear_out.exe on windows?
    return workingDir.resolve("fear_out");
  }

  private void cleanUp(Path workingDir) {
    try (Stream<Path> walk = Files.walk(workingDir)) {
      walk.sorted(Comparator.reverseOrder())
        .map(Path::toFile)
        .peek(System.out::println)
        .forEach(File::delete);
    } catch (IOException err) {
      System.err.println("ICE: Could not fully clean up temporary working dir: "+workingDir+"\n"+err.getLocalizedMessage());
    }
  }

  private CompletableFuture<Void> runGoCmd(Path workingDir, String... args) {
    Process proc; try { proc = ResolveResource.of("/go-compilers/%s/go/bin/go".formatted(goCompilerVersion()), compiler->{
      String[] command = Stream.concat(Stream.of(compiler.toString()), Arrays.stream(args)).toArray(String[]::new);
      var pb = new ProcessBuilder(command).directory(workingDir.toFile());
      try {
        return verbosity.progress() == CompilerFrontEnd.ProgressVerbosity.Full ? pb.inheritIO().start() : pb.start();
      } catch (IOException e) {
        throw Bug.of(e);
      }
    });
    } catch (IOException | URISyntaxException e) {
      throw Bug.of(e);
    }

    return proc.onExit().thenApply(p->{
      var exitValue = proc.exitValue();
      if (exitValue != 0) {
        throw Bug.of("ICE: Go compilation failed");
      }
      return null;
    });
  }

  private String goCompilerVersion() {
    var arch = switch (SystemUtils.OS_ARCH) {
      case "x86_64", "amd64" -> "amd64";
      case "aarch64", "arm64" -> "arm64";
      default -> throw new IllegalStateException("Unsupported architecture: "+System.getProperty(SystemUtils.OS_ARCH));
    };
    return "go-"+osName()+"-"+arch;
  }

  private String osName() {
    if (SystemUtils.IS_OS_MAC) { return "macos"; }
    if (SystemUtils.IS_OS_WINDOWS) { return "windows"; }
    if (SystemUtils.IS_OS_LINUX) { return "linux"; }
    throw new IllegalStateException("Unsupported OS: "+System.getProperty("os.name"));
  }
}
