package main.java;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import utils.IoErr;

record RunJava(LogicMainJava main) {
  public Process execution(Path pathToMain) {
    var command= makeJavaCommand(pathToMain);
    System.out.println(List.of(command));
    var pb = new ProcessBuilder(command);
    main.preStart(pb);
    Process proc= IoErr.of(pb::start);
    return proc;
  }
  private String[] makeJavaCommand(Path pathToMain) {
    Path fearlessMainPath = pathToMain.resolve("base/FearlessMain.class");
    var jrePath = Path.of(System.getProperty("java.home"), "bin", "java")
        .toAbsolutePath().toString();
    String entryPoint = "base." 
        + fearlessMainPath.getFileName().toString().split("\\.class")[0];
    String classpath = pathToMain.toString()
      + File.pathSeparator + main.cachedBase().toString();

    var baseCommand = Stream.of(
      jrePath, "-cp", classpath, entryPoint, main.entry());
    return Stream.concat(baseCommand,
      main.commandLineArguments().stream())
        .toArray(String[]::new);
  }

}