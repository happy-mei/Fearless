package main;

import java.nio.file.Path;

public class RunAntlr {
  public static void main(String[] args) {
    String input=Path.of("antlrGrammars", "Fearless.g4").toAbsolutePath().toString();
    String dest=Path.of("").toAbsolutePath().resolve(Path.of("compiler", "src", "generated")).toString();
    org.antlr.v4.Tool.main(new String[]{"-o",dest, "-visitor",input});
  }
}
