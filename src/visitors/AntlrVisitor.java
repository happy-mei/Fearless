package visitors;

import java.nio.file.Path;
import java.util.function.Function;
import java.util.stream.Stream;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;

import astFull.E;
import astFull.Lambda;
import astFull.MCall;
import astFull.X;
import files.Pos;
import generated.ExampleParser.EContext;
import generated.ExampleParser.LambdaContext;
import generated.ExampleParser.MCallContext;
import generated.ExampleParser.NudeEContext;
import generated.ExampleParser.XContext;
import utils.Bug;
import utils.OneOr;

@SuppressWarnings("serial")
class ParserFailed extends RuntimeException{}

public class AntlrVisitor implements generated.ExampleVisitor<Object>{
  public Path fileName;
  public StringBuilder errors = new StringBuilder();
  public AntlrVisitor(Path fileName){ this.fileName=fileName; }
  Pos pos(ParserRuleContext prc){
    return Pos.of(fileName.toUri(),prc.getStart().getLine(),prc.getStart().getCharPositionInLine()); 
    }
  void check(ParserRuleContext ctx){  
    if(ctx.children==null){ throw new ParserFailed(); }
    }
  @Override public Void visit(ParseTree arg0){ throw Bug.of(); }
  @Override public Void visitChildren(RuleNode arg0){ throw Bug.of(); }
  @Override public Void visitErrorNode(ErrorNode arg0){ throw Bug.of(); }
  @Override public Void visitTerminal(TerminalNode arg0){ throw Bug.of(); }
  @Override public E visitNudeE(NudeEContext ctx){
    check(ctx);    
    return visitE(ctx.e());
  } 
  @Override public E visitE(EContext ctx){
    check(ctx);
    return OneOr.of("",Stream.of(
      opt(ctx.lambda(),this::visitLambda),
      opt(ctx.mCall(),this::visitMCall),
      opt(ctx.x(),this::visitX))
      .filter(a->a!=null));
    }
  static <A,B> B opt(A a,Function<A,B> f){
    if(a==null){return null;}
    return f.apply(a);
    }
  @Override public E visitLambda(LambdaContext ctx){ return new Lambda(); } 
  @Override public E visitMCall(MCallContext ctx){  return new MCall(); } 
  @Override public E visitX(XContext ctx){  return new X(); }
}