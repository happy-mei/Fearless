package main;

import files.Pos;

import java.io.Serial;

public class CompileError extends RuntimeException{
  @Serial private static final long serialVersionUID = 1L;
  Pos pos;
  public CompileError pos(Pos pos){ this.pos=pos; return this; }
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
