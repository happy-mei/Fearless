package main;

import files.Pos;

public class CompileError extends RuntimeException{
  private static final long serialVersionUID = 1L;
  Pos pos;
  CompileError pos(Pos pos){ this.pos=pos; return this; }
  public CompileError() {super();}
  public CompileError(Throwable cause) {super(cause);}
  public CompileError(String msg) {super(msg);}
  public CompileError(String msg,Throwable cause) {super(msg,cause);}
  public static CompileError of(){ return new CompileError(); }
  public static CompileError of(Throwable cause){ return new CompileError(cause); }
  public static CompileError of(String msg){ return new CompileError(msg); }

  @Override public String toString(){
    return "In position "+pos+"\n"+this.getMessage();
  }
}
