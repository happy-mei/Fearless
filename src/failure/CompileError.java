package failure;

import ast.T;
import files.Pos;

import java.io.Serial;
import java.util.Optional;
import java.util.function.Function;

public class CompileError extends RuntimeException implements Res{
  @Serial private static final long serialVersionUID = 1L;
  public <R> R resMatch(Function<T, R> ok, Function<CompileError, R> err){ return err.apply(this); }
  Pos pos;
  int code = 0;
  String name = "Unknown Error";
  public CompileError pos(Pos pos){ this.pos=pos; return this; }
  public CompileError pos(Optional<Pos> pos){ return pos.map(this::pos).orElse(this); }

  public CompileError(int code, String name, String msg) {
    super(msg);
    this.code = code;
    this.name = name;
  }
  public CompileError(Throwable cause) {super(cause);}
  public CompileError(String msg) {super(msg);}
  public CompileError(String msg,Throwable cause) {super(msg,cause);}
  public static CompileError of(int code, String name, String msg){ return new CompileError(code, name, msg); }
  public static CompileError of(Throwable cause){ return new CompileError(cause); }
  public static CompileError of(String msg){ return new CompileError(msg); }
  public static <T> T err(String msg){ throw new CompileError(msg); }

  /** Returns a CompileError with the provided position if none is already set.
   * Useful for ensuring some context is available without overriding a more specific position.
   */
  public CompileError parentPos(Optional<Pos> pos){
    if (this.pos != null || pos.isEmpty()) { return this; }
    return pos(pos.get());
  }

  public String header() {
    return "[E"+code+" "+name+"]";
  }

  @Override public String toString(){
    var msg = header()+"\n"+this.getMessage();
    if (this.pos == null) { return msg; }
    return "In position "+pos+"\n"+msg;
  }
}
