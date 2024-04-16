package utils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public interface RunOutput {
  record Res(String stdOut, String stdErr, int exitCode){
    public Res() { this("", "", 0); }

    @Override public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Res res = (Res) o;
      return exitCode == res.exitCode && Err.strCmp(stdOut, res.stdOut) && Err.strCmp(stdErr, res.stdErr);
    }
  }
  static CompletableFuture<Res> java(Path classFile, List<String> cliArgs) {
    var jrePath = Path.of(System.getProperty("java.home"), "bin", "java").toAbsolutePath();
    String[] command = Stream.concat(
            Stream.of(jrePath.toString(), "userCode."+classFile.getFileName().toString().split("\\.class")[0]),
            cliArgs.stream()
    ).toArray(String[]::new);
    var pb = new ProcessBuilder(command);
    pb.directory(classFile.getParent().toFile());
    Process proc; try { proc = pb.start(); }
    catch (IOException e) { throw new RuntimeException(e); }

    return proc.onExit().thenApply(p->new Res(
            p.inputReader().lines().collect(Collectors.joining("\n")),
            p.errorReader().lines().collect(Collectors.joining("\n")),
            p.exitValue()
    ));
  }

  static CompletableFuture<Res> go(Path compiledBinary, List<String> cliArgs) {
    String[] command = Stream.concat(
            Stream.of(compiledBinary.toAbsolutePath().toString()),
            cliArgs.stream()
    ).toArray(String[]::new);
    var pb = new ProcessBuilder(command);
    pb.directory(compiledBinary.getParent().toFile());
    Process proc; try { proc = pb.start(); }
    catch (IOException e) { throw new RuntimeException(e); }

    return proc.onExit().thenApply(p->new Res(
            p.inputReader().lines().collect(Collectors.joining("\n")),
            p.errorReader().lines().collect(Collectors.joining("\n")),
            p.exitValue()
    ));
  }

  static CompletableFuture<Res> node(Path jsFile, List<String> cliArgs) {
    String[] command = Stream.concat(
            Stream.of("node", jsFile.toAbsolutePath().toString()),
            cliArgs.stream()
    ).toArray(String[]::new);
    var pb = new ProcessBuilder(command);
    pb.directory(jsFile.getParent().toFile());
    Process proc; try { proc = pb.start(); }
    catch (IOException e) { throw new RuntimeException(e); }

    return proc.onExit().thenApply(p->new Res(
            p.inputReader().lines().collect(Collectors.joining("\n")),
            p.errorReader().lines().collect(Collectors.joining("\n")),
            p.exitValue()
    ));
  }

  static void assertResMatch(ProcessBuilder pb, Res expected) {
    var proc = IoErr.of(pb::start);
    var res = proc.onExit().join();
    Err.strCmp(expected.stdErr, res.errorReader().lines().collect(Collectors.joining("\n")));
    assertEquals(expected.exitCode, res.exitValue());
    Err.strCmp(expected.stdOut, res.inputReader().lines().collect(Collectors.joining("\n")));
  }
}
