package visitors;

import astFull.E;
import astFull.Package;
import astFull.PosMap;
import astFull.T;
import files.Pos;
import generated.FearlessParser.*;
import id.Id;
import id.Id.MethName;
import id.Mdf;
import main.Fail;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import utils.*;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

class ParserFailed extends RuntimeException{}

public class FullEAntlrVisitor implements generated.FearlessVisitor<Object>{
  public final Path fileName;
  public final Function<String,Optional<Id.IT<T>>> resolve;
  public StringBuilder errors = new StringBuilder();
  public FullEAntlrVisitor(Path fileName,Function<String,Optional<Id.IT<T>>> resolve){
    this.fileName=fileName; 
    this.resolve=resolve;
  }
  Pos pos(ParserRuleContext prc){
    return pos(fileName,prc); 
    }
  public static Pos pos(Path fileName,ParserRuleContext prc){
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
  @Override public Object visitCallOp(CallOpContext ctx) { throw Bug.unreachable(); }
  @Override public Object visitNudeX(NudeXContext ctx){ throw Bug.unreachable(); }
  @Override public Object visitNudeM(NudeMContext ctx){ throw Bug.unreachable(); }
  @Override public Object visitNudeFullCN(NudeFullCNContext ctx){ throw Bug.unreachable(); }
  @Override public MethName visitM(MContext ctx){ throw Bug.unreachable(); }
  @Override public MethName visitBlock(BlockContext ctx){ throw Bug.unreachable(); }

  @Override public E visitNudeE(NudeEContext ctx){
    check(ctx);    
    return visitE(ctx.e());
  }
  
  @Override public E visitE(EContext ctx){
    check(ctx);
    E root = visitPostE(ctx.postE());
    var calls = ctx.callOp();
    if(calls.isEmpty()){ return root; }
    var res = calls.stream()
      .map(c-> new Call(
        new MethName(c.m().getText(),1),
        visitMGen(c.mGen()),
        Optional.ofNullable(c.x()).map(this::visitX),
        List.of(visitPostE(c.postE())),
        pos(c)
      ))
      .toList();

    return desugar(root, res);
    }

  static <A,B> B opt(A a,Function<A,B> f){
    if(a==null){return null;}
    return f.apply(a);
    }
  @Override public E visitPostE(PostEContext ctx){
    check(ctx);
    E root = visitAtomE(ctx.atomE());
    return desugar(root,ctx.pOp().stream().map(this::fromPOp).toList());
  }
  record Call(MethName m, Optional<List<T>> mGen, Optional<E.X> x, List<E> es, Pos pos){}
  Call fromPOp(POpContext ctx) {
    return new Call(
      new MethName(ctx.m().getText(),ctx.e().size()),
      visitMGen(ctx.mGen()),
      Optional.ofNullable(ctx.x()).map(this::visitX),
      ctx.e().stream().map(this::visitE).toList(),
      pos(ctx)
      );
  }
  E desugar(E root,List<Call> tail){
    if(tail.isEmpty()){ return root; }
    var head = tail.get(0);
    var newTail = Pop.left(tail);
    var m = head.m();
    var ts=head.mGen();
    if(head.x().isPresent()){
      var x = head.x().get();
      assert head.es().size() == 1;
      var e = head.es().get(0);
      var freshRoot = new E.X(T.infer);
      var rest = desugar(freshRoot, newTail);
      var cont = PosMap.add(
        new E.Lambda(Optional.empty(), List.of(), null, List.of(
          PosMap.add(new E.Meth(Optional.empty(), Optional.empty(), List.of(x.name(), freshRoot.name()), Optional.of(rest)), head.pos())
        ), Optional.empty()),
        head.pos()
      );
      return PosMap.add(new E.MCall(root, new MethName(m.name(), 2), ts, List.of(e, cont), T.infer), head.pos());
    }
    var es=head.es();
    E res=PosMap.add(new E.MCall(root, new MethName(m.name(), es.size()), ts, es,T.infer), head.pos());
    return desugar(res,newTail);
  }
  @Override public Optional<List<T>> visitMGen(MGenContext ctx){
    if(ctx.children==null){ return Optional.empty(); }//subsumes check(ctx);
    var noTs = ctx.t()==null || ctx.t().isEmpty();
    if(ctx.OS() == null){ return Optional.empty(); }
    if(noTs){ return Optional.of(List.of()); }
    return Optional.of(ctx.t().stream().map(this::visitT).toList());
  }
  public Optional<List<Id.GX<T>>> visitMGenParams(MGenContext ctx){
    var mGens = this.visitMGen(ctx);
    return mGens.map(ts->ts.stream()
      .map(t->t.match(
        gx->gx,
        it->{throw Fail.concreteTypeInFormalParams(t).pos(pos(ctx));}
      ))
      .toList()
    );
  }
  @Override public E.X visitX(XContext ctx){
    check(ctx);
    return PosMap.add(new E.X(ctx.getText(),T.infer),pos(ctx));
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
    var res=visitBlock(ctx.block(), Optional.of(visitMdf(ctx.mdf())));
    if (res.its().isEmpty() && !ctx.mdf().getText().isEmpty()) { throw Fail.modifierOnInferredLambda().pos(pos(ctx)); }
    return res;
    }
  public E.Lambda visitBlock(BlockContext ctx, Optional<Mdf> mdf){
    check(ctx);
    var _its = Optional.ofNullable(ctx.t())
      .map(its->its.stream().map(this::visitIT).toList());
    var rt = _its.flatMap(its->GetO.of(its,0));
    var its = _its.orElse(List.of());
    if (rt.isEmpty()) { mdf = Optional.empty(); }
    if(ctx.bblock()==null){
      return PosMap.add(new E.Lambda(mdf,its,null,List.of(),rt), pos(ctx));
    }
    var bb = ctx.bblock();
    if(bb.children==null){ return PosMap.add(new E.Lambda(mdf, its, null, List.of(), rt), pos(ctx)); }
    var _x=opt(bb.x(),this::visitX);
    var _n=_x==null?null:_x.name();
    var _ms=opt(bb.meth(),ms->ms.stream().map(this::visitMeth).toList());
    var _singleM=opt(bb.singleM(),this::visitSingleM);
    List<E.Meth> mms=_ms==null?List.of():_ms;
    if(mms.isEmpty()&&_singleM!=null){ mms=List.of(_singleM); }
    return PosMap.add(new E.Lambda(Optional.empty(),its,_n,mms,rt), pos(ctx));
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
  public Id.IT<T> visitIT(TContext ctx) {
    T t=visitT(ctx,false);
    return t.match(gx->{throw Fail.expectedConcreteType(t);}, it->it);
  }
  @Override
  public T visitNudeT(NudeTContext ctx) { return visitT(ctx.t()); }
  @Override
  public T visitT(TContext ctx) { return visitT(ctx,true); }
  public T visitT(TContext ctx, boolean canMdf) {
    if(!canMdf && !ctx.mdf().getText().isEmpty()){
      throw Bug.todo();
    }
    Mdf mdf = visitMdf(ctx.mdf());
    String name = visitFullCN(ctx.fullCN());
    var isFullName = name.contains(".");
    var mGen=visitMGen(ctx.mGen());
    Optional<Id.IT<T>> resolved = isFullName ? Optional.empty() : resolve.apply(name);
    var isIT = isFullName || resolved.isPresent();
    if(!isIT){
      var t = new T(mdf, new Id.GX<>(name));
      if(mGen.isPresent()){ throw Fail.concreteTypeInFormalParams(t).pos(pos(ctx)); }
      return t;
    }
    // TODO: TEST alias generic merging
    var ts = mGen.orElse(List.of());
    if(resolved.isEmpty()){return new T(mdf,new Id.IT<>(name,ts));}
    var res = resolved.get();
    ts = Push.of(res.ts(),ts);
    return new T(mdf,res.withTs(ts));
  }
  @Override
  public E.Meth visitSingleM(SingleMContext ctx) {
    check(ctx);
    var _xs = opt(ctx.x(), xs->xs.stream().map(this::visitX).map(E.X::name).toList());
    _xs = _xs==null?List.of():_xs;
    var body = Optional.ofNullable(ctx.e()).map(this::visitE);
    return PosMap.add(new E.Meth(Optional.empty(), Optional.empty(), _xs, body), pos(ctx));
  }
  @Override
  public E.Meth visitMeth(MethContext ctx) {
    check(ctx);
    var mh = Optional.ofNullable(ctx.sig()).map(this::visitSig);
    var xs = mh.map(MethHeader::xs).orElseGet(()->{
      var _xs = opt(ctx.x(), xs1->xs1.stream().map(this::visitX).toList());
      return _xs==null?List.of():_xs;
    });
    var name = mh.map(MethHeader::name)
        .orElseGet(()->new MethName(ctx.m().getText(),xs.size()));
    var body = Optional.ofNullable(ctx.e()).map(this::visitE);
    var sig = mh.map(h->new E.Sig(h.mdf(), h.gens(), xs.stream().map(E.X::t).toList(), h.ret()));
    return PosMap.add(new E.Meth(sig, Optional.of(name), xs.stream().map(E.X::name).toList(), body), pos(ctx));
  }
  private record MethHeader(Mdf mdf, MethName name, List<Id.GX<T>> gens, List<E.X> xs, T ret){}
  @Override
  public MethHeader visitSig(SigContext ctx) {
    check(ctx);
    var mdf = this.visitMdf(ctx.mdf());
    var gens = this.visitMGenParams(ctx.mGen()).orElse(List.of());
    var xs = Optional.ofNullable(ctx.gamma()).map(this::visitGamma).orElse(List.of());
    var name = new MethName(ctx.m().getText(),xs.size());
    var ret = this.visitT(ctx.t());
    return new MethHeader(mdf, name, gens, xs, ret);
  }
  @Override
  public List<E.X> visitGamma(GammaContext ctx) {
    return Streams.zip(ctx.x(), ctx.t())
      .map((xCtx, tCtx)->new E.X(xCtx.getText(), this.visitT(tCtx)))
      .toList();
  }
  public T.Dec visitTopDec(TopDecContext ctx, String pkg, boolean shallow) {
    check(ctx);
    String cName = visitFullCN(ctx.fullCN());
    if (cName.contains(".")) {
      throw Bug.of("You may not declare a trait in a different package than the package the declaration is in.");
    }
    cName = pkg + "." + cName;

    var mGen = Optional.ofNullable(ctx.mGen())
      .flatMap(this::visitMGenParams)
      .orElse(List.of());
    var id = new Id.DecId(cName,mGen.size());
    var body = shallow ? null : visitBlock(ctx.block(), Optional.empty());
    return PosMap.add(new T.Dec(new Id.DecId(cName,mGen.size()), mGen, body),pos(ctx));
  }
  @Override
  public T.Alias visitAlias(AliasContext ctx) {
    check(ctx);
    var in = visitFullCN(ctx.fullCN(0));
    var _inG = opt(ctx.mGen(0),this::visitMGen);
    var inG = Optional.ofNullable(_inG).flatMap(e->e).orElse(List.of());
    var inT=new Id.IT<T>(in,inG);
    var out = visitFullCN(ctx.fullCN(1));
    var outG = ctx.mGen(1);
    if(!outG.t().isEmpty()){ throw Bug.of("No gen on out Alias"); }
    return PosMap.add(new T.Alias(inT, out), pos(ctx));
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