package visitors;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import astFull.PosMap;
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
import generated.FearlessParser.NudeProgramContext;
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
import utils.Push;
import astFull.Package;

@SuppressWarnings("serial")
class ParserFailed extends RuntimeException{}

public class FullEAntlrVisitor implements generated.FearlessVisitor<Object>{
  public final Path fileName;
  public final Function<String,Optional<T.IT>> resolve;
  public StringBuilder errors = new StringBuilder();
  public FullEAntlrVisitor(Path fileName,Function<String,Optional<T.IT>> resolve){ 
    this.fileName=fileName; 
    this.resolve=resolve;
  }
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
  @Override public T.Dec visitTopDec(TopDecContext ctx) { throw Bug.unreachable(); }
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
    if(ctx.children==null){ return Optional.empty(); }//subsumes check(ctx);
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
    check(ctx);
    if(ctx.bblock()==null){
      var it=visitIT(ctx.t());
      return new E.Lambda(Mdf.mdf,List.of(it),null,List.of(),T.infer); 
      }
    throw Bug.todo();
    }
  @Override
  public String visitFullCN(FullCNContext ctx) {
    return ctx.getText();
  }
  @Override
  public Mdf visitMdf(MdfContext ctx) {
    if(ctx.getText().isEmpty()){ return Mdf.imm; }
    return Mdf.valueOf(ctx.getText());
  }
  public T.IT visitIT(TContext ctx) {
    T t=visitT(ctx,false);
    return t.match(
      gx->{throw Bug.todo();},
      it->it);
  }
  @Override
  public T visitT(TContext ctx) {
    return visitT(ctx,true);
  }
  public T visitT(TContext ctx,boolean canMdf) {
    if(!canMdf && !ctx.mdf().getText().isEmpty()){
      throw Bug.todo();
    }
    Mdf mdf = visitMdf(ctx.mdf());
    String name = visitFullCN(ctx.fullCN());
    var mGen=visitMGen(ctx.mGen());
    var resolved=resolve.apply(name);
    var isIT = name.contains(".")
      || mGen.isPresent()
      || resolved.isPresent();
    if(!isIT){ return new T(mdf,new T.GX(name)); }
    var ts = mGen.orElse(List.of());
    if(resolved.isEmpty()){return new T(mdf,new T.IT(name,ts));}
    var res = resolved.get();
    ts = Push.of(res.ts(),ts);
    return new T(mdf,res.withTs(ts));
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
  public T.Dec visitTopDec(TopDecContext ctx, String pkg) {
    check(ctx);
    String cName = visitFullCN(ctx.fullCN());
    if (cName.contains(".")) {
      throw Bug.of("You may not declare a trait in a different package than the package the declaration is in.");
    }
    cName = pkg + "." + cName;

    var mGen = opt(ctx.mGen(),this::visitMGen);
    var body = visitBlock(ctx.block());
    return new T.Dec(cName, genDec(mGen), body);
  }
  public List<T> genDec(Optional<List<T>> ts){
    if(ts.isEmpty() || ts.get().isEmpty()){ return List.of(); }
    throw Bug.todo();
    }
  @Override
  public T.Alias visitAlias(AliasContext ctx) {
    check(ctx);
    var in = visitFullCN(ctx.fullCN(0));
    var _inG = opt(ctx.mGen(0),this::visitMGen);
    var inG = Optional.ofNullable(_inG).flatMap(e->e).orElse(List.of());
    var inT=new T.IT(in,inG);
    var out = visitFullCN(ctx.fullCN(1));
    var outG = ctx.mGen(1);
    if(!outG.t().isEmpty()){ throw Bug.of("No gen on out Alias"); }

    var alias = new T.Alias(inT, out);
    var start = ctx.getStart();
    PosMap.add(alias, Pos.of(fileName.toUri(), start.getLine(), start.getCharPositionInLine()));
    return alias;
  }
  @Override
  public Package visitNudeProgram(NudeProgramContext ctx) {
    String name = ctx.Pack().toString();
    assert name.startsWith("package ");
    assert name.endsWith("\n");
    name=name.substring("package ".length(),name.length()-1);
    var as=ctx.alias().stream().map(this::visitAlias).toList();
    var decs=List.copyOf(ctx.topDec());
    return new Package(name,as,decs,decs.stream().map(e->fileName).toList());
  } 
}