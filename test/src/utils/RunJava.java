package utils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface RunJava {
  record Res(String stdOut, String stdErr, int exitCode){
    public Res() { this("", "", 0); }

    @Override public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Res res = (Res) o;
      return exitCode == res.exitCode && Err.strCmp(stdOut, res.stdOut) && Err.strCmp(stdErr, res.stdErr);
    }
  }
  static CompletableFuture<Res> of(Path classFile, List<String> cliArgs) {
    var jrePath = Path.of(System.getProperty("java.home"), "bin", "java").toAbsolutePath();
    String[] command = Stream.concat(
      Stream.of(jrePath.toString(), classFile.getFileName().toString().split("\\.class")[0]),
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
}
