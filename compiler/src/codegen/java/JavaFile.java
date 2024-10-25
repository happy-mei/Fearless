package codegen.java;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.tools.JavaFileObject;
import java.io.*;
import java.net.URI;
import java.nio.file.Path;

public record JavaFile(Path path, String code) implements JavaFileObject {
  @Override public URI toUri() {
    return path.toUri();
  }

  @Override public String getName() {
    return path.getFileName().toString();
  }

  @Override public Reader openReader(boolean ignoreEncodingErrors) {
    var content = getCharContent(ignoreEncodingErrors);
    return new StringReader(content.toString());
  }
  public CharSequence getCharContent(boolean ignoreEncodingErrors) {
    return this.code;
  }

  @Override public InputStream openInputStream() {throw new UnsupportedOperationException();}
  @Override public OutputStream openOutputStream() {throw new UnsupportedOperationException();}
  @Override public Writer openWriter() {throw new UnsupportedOperationException();}
  @Override public long getLastModified() {return 0;}
  @Override public boolean delete() {return false;}
  @Override public Kind getKind() {return Kind.SOURCE;}
  @Override public boolean isNameCompatible(String simpleName, Kind kind) {
    String baseName = simpleName + kind.extension;
    var p = path.getFileName().toString();
    return kind.equals(getKind()) && baseName.equals(p);
  }
  @Override public NestingKind getNestingKind() {return null;}
  @Override public Modifier getAccessLevel() {return null;}

  @Override public String toString() {
    return path+"["+code.substring(0, Math.min(20, code.length()))+"..]";
  }
}
