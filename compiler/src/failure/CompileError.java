package failure;

import files.HasPos;
import files.Pos;

import java.io.Serial;
import java.util.Optional;

public class CompileError extends RuntimeException implements HasPos {
  public <Any> FailOr<Any> fail(){ return new FailOr.Fail<Any>(()->this); }
  @Serial private static final long serialVersionUID = 1L;
  private static final String UNKNOWN_ERROR_MSG = "Unknown Error";
  private Pos pos;
  private int code = -1;
  private String name = null;
  public CompileError pos(Pos pos){ this.pos=pos; return this; }
  public CompileError pos(Optional<Pos> pos){ return pos.map(this::pos).orElse(this); }
  CompileError(Throwable cause) {super(cause);}
  CompileError(String msg) {super(msg);}
  public static CompileError of(Throwable cause){ return new CompileError(cause); }
  public static CompileError of(String msg){ return new CompileError(msg); }
  public static <Any> Any err(String msg){ throw new CompileError(msg); }

  public Optional<Pos> pos() {
    return Optional.ofNullable(this.pos);
  }

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
