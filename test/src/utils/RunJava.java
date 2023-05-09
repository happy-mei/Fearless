package utils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public interface RunJava {
  record Res(String stdOut, String stdErr, int exitCode){}
  static CompletableFuture<Res> of(Path classFile) {
    var pb = new ProcessBuilder("java", classFile.getFileName().toString().split("\\.class")[0]);
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
