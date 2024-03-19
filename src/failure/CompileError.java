package failure;

import ast.T;
import files.Pos;

import java.io.Serial;
import java.util.Optional;
import java.util.function.Function;

public class CompileError extends RuntimeException implements Res{
  @Serial private static final long serialVersionUID = 1L;
  private static final String UNKNOWN_ERROR_MSG = "Unknown Error";
  public <R> R resMatch(Function<T, R> ok, Function<CompileError, R> err){ return err.apply(this); }
  Pos pos;
  private int code = -1;
  private String name = null;
  public CompileError pos(Pos pos){ this.pos=pos; return this; }
  public CompileError pos(Optional<Pos> pos){ return pos.map(this::pos).orElse(this); }
  public CompileError(Throwable cause) {super(cause);}
  public CompileError(String msg) {super(msg);}
  public CompileError(String msg,Throwable cause) {super(msg,cause);}
  public static CompileError of(Throwable cause){ return new CompileError(cause); }
  public static CompileError of(String msg){ return new CompileError(msg); }
  public static <T> T err(String msg){ throw new CompileError(msg); }

  public int code() {
    if (this.code != -1) { return this.code; }
    deriveFailure();
    return this.code;
  }
  public String name() {
    if (this.name != null) { return this.name; }
    deriveFailure();
    return this.name;
  }

  /** Returns a CompileError with the provided position if none is already set.
   * Useful for ensuring some context is available without overriding a more specific position.
   */
  public CompileError parentPos(Optional<Pos> pos){
    if (this.pos != null || pos.isEmpty()) { return this; }
    return pos(pos.get());
  }

  public String header() {
    var code = this.code();
    var name = this.name();
    if (code == ErrorCode.typeError.code()) { return ""; }
    return "[E"+code+" "+name+"]";
  }

  @Override public String toString(){
    var msg = this.header()+"\n"+this.getMessage();
    if (this.pos == null) { return msg; }
    return "In position "+pos+"\n"+msg;
  }

  private void deriveFailure() {
    // getting a stack trace is expensive, only bother to do this when we need to print something
    var arr = this.getStackTrace();
    var kind = arr[1].getMethodName();
    ErrorCode errorInfo; try { errorInfo = ErrorCode.valueOf(kind);
    } catch (IllegalArgumentException ignored) {
      this.code = 0;
      this.name = UNKNOWN_ERROR_MSG;
      return;
    }
    this.code = errorInfo.code();
    this.name = errorInfo.name();
  }
}
