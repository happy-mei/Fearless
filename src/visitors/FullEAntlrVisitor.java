package visitors;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;

import ast.Mdf;
import astFull.E;
import astFull.T;
import files.Pos;
import generated.FearlessParser.AliasContext;
import generated.FearlessParser.AtomEContext;
import generated.FearlessParser.BblockContext;
import generated.FearlessParser.BlockContext;
import generated.FearlessParser.EContext;
import generated.FearlessParser.FullCNContext;
import generated.FearlessParser.GammaContext;
import generated.FearlessParser.LambdaContext;
import generated.FearlessParser.MContext;
import generated.FearlessParser.MGenContext;
import generated.FearlessParser.MdfContext;
import generated.FearlessParser.MethContext;
import generated.FearlessParser.NudeEContext;
import generated.FearlessParser.POpContext;
import generated.FearlessParser.PostEContext;
import generated.FearlessParser.RoundEContext;
import generated.FearlessParser.SigContext;
import generated.FearlessParser.SingleMContext;
import generated.FearlessParser.TContext;
import generated.FearlessParser.TopDecContext;
import generated.FearlessParser.XContext;
import utils.Bug;
import utils.OneOr;
import utils.Pop;

@SuppressWarnings("serial")
class ParserFailed extends RuntimeException{}

public class FullEAntlrVisitor implements generated.FearlessVisitor<Object>{
  public Path fileName;
  public StringBuilder errors = new StringBuilder();
  public FullEAntlrVisitor(Path fileName){ this.fileName=fileName; }
  Pos pos(ParserRuleContext prc){
    return Pos.of(fileName.toUri(),prc.getStart().getLine(),prc.getStart().getCharPositionInLine()); 
    }
  void check(ParserRuleContext ctx){  
    if(ctx.children==null){ throw new ParserFailed(); }
    }
  @Override public Void visit(ParseTree arg0){ throw Bug.unreachable(); }
  @Override public Void visitChildren(RuleNode arg0){ throw Bug.unreachable(); }
  @Override public Void visitErrorNode(ErrorNode arg0){ throw Bug.unreachable(); }
  @Override public Void visitTerminal(TerminalNode arg0){ throw Bug.unreachable(); }
  @Override public Object visitRoundE(RoundEContext ctx){ throw Bug.unreachable(); }
  @Override public Object visitBblock(BblockContext ctx){ throw Bug.unreachable(); }
  @Override public Object visitPOp(POpContext ctx){ throw Bug.unreachable(); }
  @Override public E visitNudeE(NudeEContext ctx){
    check(ctx);    
    return visitE(ctx.e());
  } 
  
  @Override public E visitE(EContext ctx){
    check(ctx);
    var es = ctx.postE();
    E root = visitPostE(es.get(0));
    if(es.size()==1){ return root; }
    throw Bug.of();
    /*
    E e=visitE(ctx.e());
    var m=visitM(ctx.m());
    var mGen=visitMGen(ctx.mGen());
    if(ctx.x()!=null) { throw Bug.of(); }
    return new E.MCall(root,m,mGen,List.of(e),T.infer);
    */
    }
  static <A,B> B opt(A a,Function<A,B> f){
    if(a==null){return null;}
    return f.apply(a);
    }
  @Override public E visitPostE(PostEContext ctx){
    check(ctx);
    E root = visitAtomE(ctx.atomE());
    return desugar(root,ctx.pOp());
  }
  E desugar(E root,List<POpContext> tail){
    if(tail.isEmpty()){ return root; }
    var top = tail.get(0);
    var pop = Pop.left(tail);
    if(top.x()!=null){ throw Bug.of();}
    E.MethName m=visitM(top.m());
    var ts=visitMGen(top.mGen());
    var es=top.e().stream().map(this::visitE).toList();
    E res=new E.MCall(root,m, ts, es,T.infer);
    return desugar(res,pop);
  }
  @Override public Optional<List<T>> visitMGen(MGenContext ctx){
    check(ctx);
    var noTs = ctx.t()==null || ctx.t().isEmpty();
    if(ctx.OS() == null){ return Optional.empty(); }
    if(noTs){ return Optional.of(List.of()); }
    throw Bug.of();
  }
  @Override public E.MethName visitM(MContext ctx){
    check(ctx);
    return new E.MethName(ctx.getText());
  }
  @Override public E visitX(XContext ctx){
    check(ctx);
    return new E.X(ctx.getText(),T.infer); 
    }
  @Override public E visitAtomE(AtomEContext ctx){
    check(ctx);
    return OneOr.of("",Stream.of(
        opt(ctx.x(),this::visitX),
        opt(ctx.lambda(),this::visitLambda),
        opt(ctx.roundE(),(re->this.visitE(re.e()))))
        .filter(a->a!=null));
  }
  @Override public E.Lambda visitLambda(LambdaContext ctx){
    Mdf mdf=visitMdf(ctx.mdf());
    var res=visitBlock(ctx.block());
    return new E.Lambda(mdf,res.its(), res.selfName(), res.meths(), T.infer);
    }
  @Override public E.Lambda visitBlock(BlockContext ctx){
    throw Bug.todo();
    }
  @Override
  public Object visitFullCN(FullCNContext ctx) {
    // TODO Auto-generated method stub
    return null;
  }
  @Override
  public Mdf visitMdf(MdfContext ctx) {
    return Mdf.valueOf(ctx.getText());
  }
  @Override
  public Object visitT(TContext ctx) {
    // TODO Auto-generated method stub
    return null;
  }
  @Override
  public Object visitSingleM(SingleMContext ctx) {
    // TODO Auto-generated method stub
    return null;
  }
  @Override
  public Object visitMeth(MethContext ctx) {
    // TODO Auto-generated method stub
    return null;
  }
  @Override
  public Object visitSig(SigContext ctx) {
    // TODO Auto-generated method stub
    return null;
  }
  @Override
  public Object visitGamma(GammaContext ctx) {
    // TODO Auto-generated method stub
    return null;
  }
  @Override
  public Object visitTopDec(TopDecContext ctx) {
    check(ctx);
    String cName = ctx.fullCN().getText();
    var mGen = opt(ctx.mGen(),this::visitMGen);
    var body=visitBlock(ctx.block());
    return new T.Dec(cName, genDec(mGen), body);
  }
  public List<T.GX> genDec(Optional<List<T>>ts){
    if(ts.isEmpty() || ts.get().isEmpty()){ return List.of(); }
    throw Bug.todo();
    }
  @Override
  public T.Alias visitAlias(AliasContext ctx) {
    check(ctx);
    var in = ctx.fullCN(0);
    var _inG = opt(ctx.mGen(0),this::visitMGen);
    var inG = Optional.ofNullable(_inG).flatMap(e->e).orElse(List.of());
    var inT=new T.GIT(in.getText(),inG);
    var out = ctx.fullCN(1);
    var outG = ctx.mGen(1);
    if(outG!=null){ throw Bug.of("No gen on out Alias"); }    
    return new T.Alias(inT, out.getText());
  } 
}