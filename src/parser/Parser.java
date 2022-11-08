package parser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ConsoleErrorListener;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

import astFull.E;
import files.Pos;
import generated.FearlessLexer;
import generated.FearlessParser;
import generated.FearlessParser.NudeEContext;
import utils.Bug;
import visitors.FullEAntlrVisitor;

public record Parser(Path fileName,String content){
  public Parser of(String fileName){
    return of(Paths.get(fileName));
  }
  public static final Path dummy = Path.of("Dummy");
  public Parser of(Path path){
    assert Files.exists(path);
    assert !Files.isDirectory(path);
    try{ return new Parser(path,codeFromPath(path)); }
    catch(IOException e){ throw Bug.of(e); }
  }
  public static String codeFromPath(Path path) throws IOException{
    String code = Files.readString(path,StandardCharsets.US_ASCII);
    code = code.replace("\r","");
    preCheck(code);
    return code;
  }
  public static void preCheck(String code){
    //TODO: we may want to check a bunch of stuff
    //No strange characters
    //balanced parenthesis with decent error
  }
  public E parseFullE(Function<String,E> orElse){
      var l = new FearlessLexer(CharStreams.fromString(content));
      var p = new FearlessParser(new CommonTokenStream(l));
      var errorst = new StringBuilder();
      var errorsp = new StringBuilder();
      FailConsole.setFail(fileName, l, p, errorst, errorsp);
      NudeEContext res = p.nudeE();
      var ok = errorst.isEmpty() && errorsp.isEmpty();
      if(ok){ return new FullEAntlrVisitor(fileName).visitNudeE(res); }
      //TODO: better errors below
      if(!errorst.isEmpty()){ return orElse.apply(errorst.toString()); }
      return orElse.apply(errorsp.toString());
  }
}
class FailConsole extends ConsoleErrorListener{
  public final StringBuilder sb;
  public final Path fileName;
  public FailConsole(Path fileName,StringBuilder sb){ this.fileName=fileName;this.sb=sb; }
  @Override public void syntaxError(Recognizer<?, ?> r,Object o,int line,int charPos,String msg,RecognitionException e){
    sb.append(Pos.of(fileName.toUri(),line,charPos)+ msg);
    }
  static void setFail(Path fileName, Lexer l, org.antlr.v4.runtime.Parser p, StringBuilder errorst, StringBuilder errorsp) {
    l.removeErrorListener(ConsoleErrorListener.INSTANCE);
    l.addErrorListener(new FailConsole(fileName,errorst));
    p.removeErrorListener(ConsoleErrorListener.INSTANCE);
    p.addErrorListener(new FailConsole(fileName,errorsp));
  }
}
