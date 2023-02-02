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
  public CompileError pos(Pos pos){ this.pos=pos; return this; }
  public CompileError pos(Optional<Pos> pos){ return pos.map(this::pos).orElse(this); }
  public CompileError() {super();}
  public CompileError(Throwable cause) {super(cause);}
  public CompileError(String msg) {super(msg);}
  public CompileError(String msg,Throwable cause) {super(msg,cause);}
  public static CompileError of(){ return new CompileError(); }
  public static CompileError of(Throwable cause){ return new CompileError(cause); }
  public static CompileError of(String msg){ return new CompileError(msg); }

  @Override public String toString(){
    if (this.pos == null) { return this.getMessage(); }
    return "In position "+pos+"\n"+this.getMessage();
  }
}
