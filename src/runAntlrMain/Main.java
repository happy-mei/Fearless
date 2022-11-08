package runAntlrMain;

import java.nio.file.Path;

public class Main {
  public static void main(String[] args) {
    String input=Path.of("antlrGrammars","Fearless.g4").toAbsolutePath().toString();
    String dest=Path.of("src","generated").toAbsolutePath().toString();    
    org.antlr.v4.Tool.main(new String[]{"-o",dest,"-visitor", input});
  }
}
