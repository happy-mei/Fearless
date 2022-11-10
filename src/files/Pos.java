package files;
import java.net.URI;
//Note: we plan to soft connect positions with AST node by having a static external hash map
public record Pos(URI fileName, int line, int column) implements java.io.Serializable {
  public static Pos of(URI fileName, int line, int column){
    return new Pos(fileName,line,column);
  }
  @Override
  public String toString() {
    return fileName + ":" + line() + ":" + column();
  }
  public Pos withFileName(URI fileName) {
    return this.fileName == fileName ? this : new Pos(fileName, this.line, this.column);
  }
  public Pos withLine(int line) {
    return this.line == line ? this : new Pos(this.fileName, line, this.column);
  }
  public Pos withColumn(int column) {
    return this.column == column ? this : new Pos(this.fileName, this.line, column);
  }
}