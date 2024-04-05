package codegen.java;

import java.net.URI;

import javax.tools.SimpleJavaFileObject;
import javax.tools.JavaFileObject.Kind;

public class JavaFile extends SimpleJavaFileObject {
  private final String code;
  
  public JavaFile(String topLevelClassName, String code) {
    super(
      URI.create("string:///" + topLevelClassName + Kind.SOURCE.extension),
      Kind.SOURCE);
    this.code = code;
  }

  public CharSequence getCharContent(boolean ignoreEncodingErrors) {
    return this.code;
  }
}
