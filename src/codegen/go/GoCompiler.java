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
    new Unit.Runtime("go.mod"),
    new Unit.Runtime("base~rt.go")
  );

  public sealed interface Unit permits GoCodegen.MainFile, Unit.Runtime, PackageCodegen.GoPackage {
    String name();
    String src();
    default void write(Path workingDir) throws IOException {
      Files.writeString(workingDir.resolve(this.name()), this.src());
    }
    record Runtime(String name) implements Unit {
      @Override public String src() {
        //return ResolveResource.getString("/rt/" +name);//BAD, never use '/'
        return ResolveResource.read(ResolveResource.of("rt").resolve(name));
      }
    }
  }

  public Path compile() throws IOException {
    assert !units.isEmpty();

    var workingDir = Paths.get(System.getProperty("java.io.tmpdir"), "fearOut"+UUID.randomUUID());
    if (verbosity.printCodegen()) {
      System.err.println("Go codegen working dir: "+workingDir.toAbsolutePath());
    }
    if (!workingDir.toFile().mkdir()) {
      throw Bug.of("Could not create a working directory for building the program in: " + System.getProperty("java.io.tmpdir"));
    }

    this.entry().write(workingDir);
    for (var unit : this.units()) { unit.write(workingDir); }
    for (var unit : this.rt()) { unit.write(workingDir); }
    //var canExecute = ResolveResource.of("/go-compilers/%s/go/bin/go".formatted(goCompilerVersion()), compilerPath->compilerPath.toFile().setExecutable(true));//Really??
    var canExecute = GoVersion.path().toFile().setExecutable(true);
    if (!canExecute) {
      System.err.println("Warning: Could not make the Go compiler executable");
    }
    try {
      runGoCmd(workingDir, "build", "-o", "fear_out").join();
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
        .forEach(File::delete);
    } catch (IOException err) {
      System.err.println("ICE: Could not fully clean up temporary working dir: "+workingDir+"\n"+err.getLocalizedMessage());
    }
  }

  Process goProcess(Path workingDir, String[] args) {
    Path compiler= GoVersion.path();
    String[] command = Stream.concat(Stream.of(compiler.toString()), Arrays.stream(args)).toArray(String[]::new);
    var pb = new ProcessBuilder(command).directory(workingDir.toFile());
    var inheritIo=verbosity.progress() == CompilerFrontEnd.ProgressVerbosity.Full;
    try { return inheritIo? pb.inheritIO().start() : pb.start();}
    catch (IOException e) { throw Bug.of(e); } 
  }
  private CompletableFuture<Void> runGoCmd(Path workingDir, String... args) {
    Process proc= goProcess(workingDir,args);
    return proc.onExit().thenAccept(p->{
      var exitValue = proc.exitValue();
      if (exitValue != 0) {
        throw Bug.of("ICE: Go compilation failed");
      }
    });//are you sure this is not dead code?
  }
}
class GoVersion{
  public static Path path(){ return ResolveResource.of(goStr); }
  private static final String goStr= 
    "/go-compilers/%s/go/bin/go"
    .formatted(goCompilerVersion());
  //TODO: BAD, do not use '/', not portable
  private static String goCompilerVersion() {
    var arch = switch (SystemUtils.OS_ARCH) {
      case "x86_64", "amd64" -> "amd64";
      case "aarch64", "arm64" -> "arm64";
      default -> throw new IllegalStateException("Unsupported architecture: "+System.getProperty(SystemUtils.OS_ARCH));
    };
  return "go-"+osName()+"-"+arch;
  }
  private static String osName() {
    if (SystemUtils.IS_OS_MAC) { return "macos"; }
    if (SystemUtils.IS_OS_WINDOWS) { return "windows"; }
    if (SystemUtils.IS_OS_LINUX) { return "linux"; }
    throw new IllegalStateException("Unsupported OS: "+System.getProperty("os.name"));
  }
}

