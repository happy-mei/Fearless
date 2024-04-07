package codegen.java;

import java.net.URI;
import java.nio.file.Path;

import javax.tools.SimpleJavaFileObject;
import javax.tools.JavaFileObject.Kind;

public class JavaFile extends SimpleJavaFileObject {
  private final String code;
   
  public JavaFile(String topLevelClassName, String code) {
    //TODO:refactor away
    super(
      URI.create("string:///" + topLevelClassName + Kind.SOURCE.extension),
      Kind.SOURCE);
    this.code = code;
  }
  public JavaFile(Path path, String code) {
    super(path.toUri(), Kind.SOURCE);
    this.code = code;
  }
  public String code(){ return code; }
  public CharSequence getCharContent(boolean ignoreEncodingErrors) {
    return this.code;
  }
}
