package visitors;

import astFull.E;
import astFull.Package;
import astFull.T;
import files.Pos;
import generated.FearlessParser.*;
import id.Id;
import id.Id.GX;
import id.Id.MethName;
import id.Mdf;
import failure.Fail;
import magic.LiteralKind;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;

import astFull.E.Lambda.LambdaId;
import astFull.E.Meth;
import astFull.E.X;
import parser.ParseDirectLambdaCall;
import utils.*;
import wellFormedness.FullUndefinedGXsVisitor;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@SuppressWarnings("serial")
class ParserFailed extends RuntimeException{}

public class FullEAntlrVisitor implements generated.FearlessVisitor<Object>{
  public final Path fileName;
  public final Function<String,Optional<Id.IT<T>>> resolve;
  public StringBuilder errors = new StringBuilder();
  private String pkg;
  private Map<Id.GX<T>, Set<Mdf>> xbs = Map.of();
  public FullEAntlrVisitor(Path fileName,String pkg,Function<String,Optional<Id.IT<T>>> resolve){
    this.fileName= fileName;
    this.resolve= resolve;
    this.pkg= pkg;
  }
  Pos pos(ParserRuleContext prc){
    return pos(fileName,prc);
    }
  public static Pos pos(Path fileName,ParserRuleContext prc){
    return Pos.of(fileName.toUri(),prc.getStart().getLine(),prc.getStart().getCharPositionInLine());
    }

  void check(ParserRuleContext ctx){
    assert this.pkg!=null;
    if(ctx.children==null){ throw new ParserFailed(); }
    }
  @Override public Void visit(ParseTree arg0){ throw Bug.unreachable(); }
  @Override public Void visitChildren(RuleNode arg0){ throw Bug.unreachable(); }
  @Override public Void visitErrorNode(ErrorNode arg0){ throw Bug.unreachable(); }
  @Override public Void visitTerminal(TerminalNode arg0){ throw Bug.unreachable(); }
  @Override public Object visitRoundE(RoundEContext ctx){ throw Bug.unreachable(); }
  @Override public Object visitGenDecl(GenDeclContext ctx) { throw Bug.unreachable();}
  @Override public Object visitMGen(MGenContext ctx) { throw Bug.unreachable(); }
  @Override public Object visitNudeX(NudeXContext ctx){ throw Bug.unreachable(); }
  @Override public Object visitNudeM(NudeMContext ctx){ throw Bug.unreachable(); }
  @Override public Object visitNudeFullCN(NudeFullCNContext ctx){ throw Bug.unreachable(); }
  @Override public MethName visitM(MContext ctx){ throw Bug.unreachable(); }
  @Override public Package visitNudeProgram(NudeProgramContext ctx){ throw Bug.unreachable(); }

  @Override public E visitNudeE(NudeEContext ctx){
    check(ctx);
    return visitE(ctx.e());
  }
  @Override public E visitE(EContext ctx){
    check(ctx);
    E root = visitAtomE(ctx.atomE());
    var calls = ctx.pOp();
    if(calls.isEmpty()){ return root; }
    root = ParseDirectLambdaCall.of(root, xbs); 
    return visitAllPOp(calls).apply(root);
    }
  public Function<E,E> visitAllPOp(List<POpContext> pops){
    return pops.stream()
      .map(popi->this.visitPOp(popi))
      .reduce(x->x,Function::andThen);
    }
  E.MCall buildMCall(E recv, MGenContext mGen, List<EContext> e, AtomEContext atomE, String mName, XContext x, List<POpContext> pops, Pos pos){
    var gen= visitMGen(mGen, true);
    var es=  e.stream().map(this::visitE).toList();
    var optAtomE= Optional.ofNullable(atomE).map(this::visitAtomE);
    if(optAtomE.isPresent()){ es = Push.of(optAtomE.get(),es); }
    var m=   new MethName(Optional.empty(), mName, es.size());
    var mcall= new E.MCall(recv,m,gen,es,T.infer,Optional.of(pos));
    Optional<X> xOpt= Optional.ofNullable(x).map(this::visitX);
    Function<E,E> pOp= this.visitAllPOp(pops);
    return xOpt
      .map(xi->buildEqSugar(mcall,xi,pOp))
      .orElse(mcall);
    }
  @Override public Function<E,E> visitPOp(POpContext ctx){
    check(ctx);
    return recv->buildMCall(
      recv, ctx.mGen(), ctx.e(), ctx.atomE(),ctx.m().getText(),
      ctx.x(),ctx.pOp(),pos(ctx));
    }
  E.MCall buildEqSugar(E.MCall m, X x,Function<E,E> pops){
    //e0.m(atom.pops), x --> e0.m1(atom,{x,self->self.pops}
    assert m.es().size() == 1;
    var atom= m.es().get(0);
    var freshSelf= new E.X(T.infer);
    var body= Optional.of(pops.apply(freshSelf));
    var pos= atom.pos();
    var id= new E.Lambda.LambdaId(Id.DecId.fresh(pkg, 0), List.of(), this.xbs);
    var xs= List.of(x.name(), freshSelf.name());
    var contM = new E.Meth(Optional.empty(), Optional.empty(), xs, body, pos);
    var cont = new E.Lambda(id, Optional.empty(), List.of(), null,
      List.of(contM), Optional.empty(), pos);
    List<E> es=List.of(atom,cont);
    var n=new MethName(m.name().name(),2);
    return new E.MCall(m.receiver(), n, m.ts(), es, m.t(),m.pos());
  }
  static <A,B> B opt(A a,Function<A,B> f){
    if(a==null){return null;}
    return f.apply(a);
    }
  public Optional<List<T>> visitMGen(MGenContext ctx, boolean isDecl){
    if(ctx.children==null){ return Optional.empty(); }//subsumes check(ctx);
    var noTs = !some(ctx.genDecl());
    if(ctx.OS() == null){ return Optional.empty(); }
    if(noTs){ return Optional.of(List.of()); }
    return Optional.of(ctx.genDecl().stream().map(declCtx->{
      var t = visitT(declCtx.t(), isDecl);
      if (!some(declCtx.mdf())) { return t; }
      var gx = t.gxOrThrow();
      return new T(t.mdf(), new Id.GX<>(gx.name()));
    }).toList());
  }
  public record GenericParams(List<Id.GX<T>> gxs, Map<Id.GX<T>, Set<Mdf>> bounds) {
    public GenericParams{
      assert gxs.size()==bounds.size():gxs+" "+bounds;
      assert bounds.keySet().stream().allMatch(k->gxs.contains(k)):gxs+" "+bounds;
    }
  }
  private Set<Mdf> mdfStarSugar(GenDeclContext gen){
    if (gen.SysInM()==null){ return Set.of(Mdf.imm); }
    String text= gen.SysInM().getText();
    var res= new LinkedHashSet<Mdf>();
    res.add(Mdf.imm);res.add(Mdf.mut);res.add(Mdf.read);
    if (text.equals("*")){return res;}
    if (!text.equals("**")){ throw Bug.todo("better error"); }
    res.add(Mdf.iso);res.add(Mdf.mutH);res.add(Mdf.readH);
    return res;
  }
  private GenericParams genericParams(List<T> ts, MGenContext ctx){
    List<GX<T>> gxs= toGxsOrFail(ts,pos(ctx));
    Map<Id.GX<astFull.T>, Set<Mdf>> boundsMap = Mapper.of(acc->
      Streams.zip(ctx.genDecl(), gxs)
        .forEach((declCtx, gx) -> {
          var bounds = !some(declCtx.mdf())
            ?mdfStarSugar(declCtx)
            :new LinkedHashSet<>(declCtx.mdf().stream()
              .map(this::visitMdf)
              .toList());
          acc.put(gx, bounds);
          }));
    return new GenericParams(gxs, boundsMap);
  }
  private List<GX<T>> toGxsOrFail(List<T> ts, Pos pos){
    return ts.stream()
      .map(t->t.match(
        gx->gx,
        it->{ throw Fail.concreteTypeInFormalParams(t).pos(pos); }
        ))
      .toList();
  }
  public Optional<GenericParams> visitMGenParams(MGenContext ctx){
    Optional<List<T>> mGens = this.visitMGen(ctx, false);    
    return mGens.map(ts->genericParams(ts,ctx));
  }
  @Override public E.X visitX(XContext ctx){
    check(ctx);
    var name = ctx.getText();
    name = name.equals("_") ? E.X.freshName() : name;
    return new E.X(name, T.infer, Optional.of(pos(ctx)));
  }
  @Override public E visitAtomE(AtomEContext ctx){
    check(ctx);
    return OneOr.of("",Stream.of(
        opt(ctx.x(),xCtx->{
          if (xCtx.getText().equals("_")) { throw Fail.ignoredIdentInExpr(); }
          return this.visitX(xCtx);
        }),
        opt(ctx.lambda(),this::visitLambda),
        opt(ctx.roundE(),(re->this.visitE(re.e()))))
        .filter(Objects::nonNull));
  }
  Map<Id.GX<T>, Set<Mdf>> freeXbs(List<Id.IT<T>> its,List<Meth> ms){
    var visitor = new FullUndefinedGXsVisitor(Set.copyOf(xbs.keySet()));
    its.forEach(visitor::visitIT);
    ms.forEach(visitor::visitMeth);
    var vres= visitor.res();
    return Mapper.of(m->vres.forEach(b->m.put(b, xbs.get(b))));
  }
  E.Lambda visitUnnamedLambda(LambdaContext ctx){
    T t= opt(ctx.t(),this::visitT);
    assert t==null || !t.isInfer();
    Mdf mdf=opt(ctx.mdf(),this::visitExplicitMdf);
    if (mdf==null && t!=null){ mdf = visitMdf(ctx.t().mdf()); }
    Id.IT<T> it= opt(t,ti->ti.match(gx->{throw Fail.expectedConcreteType(ti).pos(pos(ctx));}, iti->iti));
    List<Id.IT<T>> its= it==null?List.of():List.of(it);
    BBlock block= opt(ctx.bblock(),this::visitBblock);
    var decId= Id.DecId.fresh(pkg, 0);
    Map<Id.GX<T>, Set<Mdf>> idBounds=block==null?Map.of():freeXbs(List.of(),block.ms());
    var id= new E.Lambda.LambdaId(decId, List.of(), idBounds);
    if(block==null){
      assert it!=null;
      return new E.Lambda(
          id,                       //LambdaId id
          Optional.ofNullable(mdf), //Optional<Mdf> mdf
          its,                      //List<Id.IT<T>>its
          null,                     //String selfName
          List.of(),                //List<Meth> meths
          Optional.of(it),          //Optional<Id.IT<T>> it
          Optional.of(pos(ctx))     //Optional<Pos> pos
        );
    }
    assert block!=null;
    return new E.Lambda(
        id,                       //LambdaId id
        Optional.ofNullable(mdf), //Optional<Mdf> mdf
        its,                      //List<Id.IT<T>>its
        block.selfName(),         //String selfName
        block.ms(),                //List<Meth> meths
        Optional.ofNullable(it),  //Optional<Id.IT<T>> it
        Optional.of(pos(ctx))     //Optional<Pos> pos
      );    
  }
  E.Lambda visitNamedLambda(LambdaContext ctx){
    Mdf mdf= opt(ctx.mdf(),this::visitMdf);
    return visitNamedLambda(mdf,ctx.topDec());
  }
  E.Lambda visitNamedLambda(Mdf mdf,TopDecContext ctx){
    GenericParams mGen= Optional.ofNullable(ctx.mGen())
        .flatMap(this::visitMGenParams)
        .orElse(new GenericParams(List.of(), Map.of()));
    return withXBs(mGen.bounds,()->visitNamedLambda(mGen,mdf,ctx));
  }
  E.Lambda visitNamedLambda(GenericParams mGen, Mdf mdf, TopDecContext ctx){
    String cName= completeDeclName(visitFullCN(ctx.fullCN()), pos(ctx));
    var decId= new Id.DecId(cName, mGen.gxs.size());
    LambdaId id= new LambdaId(decId, mGen.gxs, mGen.bounds);
    List<T> nameTs= id.gens().stream()
      .map(gx->new T(Mdf.mdf, gx))
      .toList();
    Id.IT<T> nameId= new Id.IT<T>(decId,nameTs);
    List<Id.IT<T>> its= ctx.t() == null
      ?List.of()
      :ctx.t().stream()
        .map(ti->visitT(ti,false))
        .map(ti->ti.match(
          gx->{ 
            throw Fail.expectedConcreteType(ti).pos(pos(ctx)); },
          iti->iti))
        .toList();
    var block= visitBblock(ctx.bblock());
    return new E.Lambda(
      id,                       //LambdaId id
      Optional.ofNullable(mdf), //Optional<Mdf> mdf
      its,                      //List<Id.IT<T>>its
      block.selfName(),         //String selfName
      block.ms(),                //List<Meth> meths
      Optional.ofNullable(nameId),  //Optional<Id.IT<T>> it
      Optional.of(pos(ctx))     //Optional<Pos> pos
    );
  }
  record BBlock(String selfName, List<Meth> ms){};
  public BBlock visitMethsBlock(BblockContext ctx){
    var selfName=opt(ctx.SelfX(),xi->xi.getText().substring(1));
    List<Meth> ms= ctx.meth().stream().map(this::visitMeth).toList();
    return new BBlock(selfName,ms);
  }
  public BBlock visitSingleBlock(BblockContext ctx){
    var selfName=opt(ctx.SelfX(),xi->xi.getText().substring(1));
    List<Meth> ms=List.of(visitSingleM(ctx.singleM()));
    return new BBlock(selfName,ms);
  }
  String sugarName(String mName, String opName){
    if (mName!=null){
      assert mName.startsWith("::");
      return "."+mName.substring(2); 
      }
    assert opName!=null;
    if (!opName.startsWith("::")){ throw Bug.todo("better error"); }
    return opName.substring(2);    
  }
  record XE(X x,E e){}
  XE computeSugarBlockStart(BblockContext ctx){
    var x= new E.X(T.infer);
    boolean id= ctx.ColonColon()!=null;
    if (id){ return new XE(x,x); }
    String mName=sugarName(ctx.CCMName().getText(), ctx.CCMName().getText());
    E e= buildMCall(
      x, ctx.mGen(), ctx.e(), ctx.atomE(),mName,
      ctx.x(),ctx.pOp(),pos(ctx));
    return new XE(x,e);    
  }
  public BBlock visitSugarBlock(BblockContext ctx){
    XE xe=computeSugarBlockStart(ctx);
    List<String> xs= List.of(xe.x().name());
    List<POpContext> pop= ctx.pOp()==null?List.of():ctx.pOp();
    E e=visitAllPOp(pop).apply(xe.e());
    Meth m= new E.Meth(
      Optional.empty(), Optional.empty(), xs,
      Optional.of(e), Optional.of(pos(ctx)));
    return new BBlock(null,List.of(m));
  }
  @Override public BBlock visitBblock(BblockContext ctx){
    //NO: can be empty check(ctx);
    if (ctx.children==null){ new BBlock(null,List.of()); }
    boolean sugar= ctx.ColonColon()!=null || ctx.CCMName() !=null|| ctx.SysInM()!=null;
    if (sugar){ return visitSugarBlock(ctx); }
    if (ctx.singleM()!=null){return visitSingleBlock(ctx);}
    assert ctx.meth()!=null;
    return visitMethsBlock(ctx);
  }
  @Override public E.Lambda visitLambda(LambdaContext ctx){
    check(ctx);
    return ctx.topDec() == null
      ? visitUnnamedLambda(ctx)
      : visitNamedLambda(ctx);
  }
  private <R> R withXBs(Map<Id.GX<T>, Set<Mdf>> xbs, Supplier<R> s){
    var oldXbs = this.xbs;
    this.xbs = Map.copyOf(xbs);
    try{ return s.get(); }
    finally { this.xbs = oldXbs; }
  }
/*  public E.Lambda visitBlock(BlockContext ctx, Optional<Mdf> mdf, Optional<E.Lambda.LambdaId> name){
    check(ctx);
    var _its = Optional.ofNullable(ctx.t())
      .map(its->its.stream().map(this::visitIT).toList());
    var its = _its.orElse(List.of());
    boolean nakedMdf= mdf.isPresent() && name.isEmpty() && its.isEmpty();
    if (nakedMdf){ throw Fail.mustProvideImplsIfMdfProvided().pos(pos(ctx)); }
    if (name.isPresent() && mdf.isEmpty()) { mdf = Optional.of(Mdf.imm); }
    assert mdf.filter(Mdf::isMdf).isEmpty();
    Supplier<E.Lambda.LambdaId> emptyTopName = ()->new E.Lambda.LambdaId(Id.DecId.fresh(pkg, 0), List.of(), this.xbs);
    LambdaId id= name.orElseGet(emptyTopName);
    boolean givenName= mdf.isPresent() && !id.id().isFresh();
    var inferredOpt= Optional.<Id.IT<T>>empty();
    if(givenName){
      Id.IT<astFull.T> nameId= new Id.IT<>(id.id(),
        id.gens().stream().map(gx->new T(Mdf.mdf, gx)).toList());
      inferredOpt = Optional.of(nameId);
    }
    if(inferredOpt.isEmpty() && !its.isEmpty()) {
      inferredOpt = Optional.of(its.getFirst());
      if(mdf.isEmpty()){ mdf = Optional.of(Mdf.imm); }
    }
    //TODO: inferredOpt may itself disappear since we have nameId in id.
    var bb = ctx.bblock();
    if (bb==null || bb.children==null) {
      return new E.Lambda(
        name.orElseGet(emptyTopName),
        mdf,
        its,
        null,
        List.of(),
        inferredOpt,
        Optional.of(pos(ctx))
      );
    }
    var _x=bb.SelfX();
    var _n=_x==null?null:_x.getText().substring(1);
    var _ms=opt(bb.meth(),ms->ms.stream().map(this::visitMeth).toList());
    var _singleM=opt(bb.singleM(),this::visitSingleM);
    List<E.Meth> mms=_ms==null?List.of():_ms;
    if(mms.isEmpty()&&_singleM!=null){ mms=List.of(_singleM); }
    var meths = mms;
    return new E.Lambda(id, mdf, its, _n, meths, inferredOpt, Optional.of(pos(ctx)));
  }*/
  
  @Override public String visitFullCN(FullCNContext ctx) {
    return ctx.getText();
  }

  @Override public Mdf visitMdf(MdfContext ctx) {
    if(ctx.getText().isEmpty()){ return Mdf.imm; }
    if(ctx.getText().equals("read/imm")) { return Mdf.readImm; }
    return Mdf.valueOf(ctx.getText());
  }
  public Mdf visitExplicitMdf(MdfContext ctx) {
    if(ctx.getText().isEmpty()){ return null; }
    if(ctx.getText().equals("read/imm")) { return Mdf.readImm; }
    return Mdf.valueOf(ctx.getText());
  }
  public Mdf visitGenricMdf(MdfContext ctx) {
    if(ctx.getText().isEmpty()){ return Mdf.mdf; }
    if(ctx.getText().equals("read/imm")) { return Mdf.readImm; }
    return Mdf.valueOf(ctx.getText());
  }

  public Id.IT<T> visitIT(TContext ctx) {
    T t=visitT(ctx,false);
    return t.match(gx->{throw Fail.expectedConcreteType(t).pos(pos(ctx));}, it->it);
  }
  @Override public T visitNudeT(NudeTContext ctx) { return visitT(ctx.t()); }

  private static final Pattern regexPkg = Pattern.compile("^(_*[a-z][0-9A-Za-z_]*(?:\\.[_]*[a-z][0-9A-Za-z_]*)*)");
  private static final Pattern regexGenName = Pattern.compile("^_*[A-Z][0-9A-Za-z_]*$");

  private String extractPkgname(String name){
    Matcher matcher = regexPkg.matcher(name);
    if (matcher.find()) { return matcher.group(1); }
    else { return ""; }
  }
  private boolean isGenOk(String name){
    return regexGenName.matcher(name).matches();
  }
  @Override public T visitT(TContext ctx) { return visitT(ctx,true); }
  public T visitT(TContext ctx, boolean canMdf) {
    if(!canMdf && !ctx.mdf().getText().isEmpty()){
      throw Fail.noMdfInFormalParams(ctx.getText()).pos(pos(ctx));
    }
    String name = visitFullCN(ctx.fullCN());
    String pkgName= extractPkgname(name);
    String simpleName= pkgName.isEmpty()? name : name.substring(pkgName.length()+1);
    var canBeGen =  isGenOk(simpleName);
    if(!canBeGen && pkgName.isEmpty()){
      name = LiteralKind.toFullName(name).orElse(this.pkg+"."+name);
      }
    var isFullName = !canBeGen || !pkgName.isEmpty();
    var mGen=visitMGen(ctx.mGen(), true);
    Optional<Id.IT<T>> resolved = isFullName ? Optional.empty() : resolve.apply(name);
    var isIT = isFullName || resolved.isPresent();
    if(!isIT){
      var t = new T(visitGenricMdf(ctx.mdf()), new Id.GX<>(name));
      if(mGen.isPresent()){ throw Fail.concreteTypeInFormalParams(t).pos(pos(ctx)); }
      return t;
    }
    // TODO: TEST alias generic merging
    Mdf mdf = visitMdf(ctx.mdf());
    var ts = mGen.orElse(List.of());
    if(resolved.isEmpty()){return new T(mdf,new Id.IT<>(name,ts));}
    var res = resolved.get();
    ts = Push.of(res.ts(),ts);
    return new T(mdf,res.withTs(ts));
  }
  @Override
  public E.Meth visitSingleM(SingleMContext ctx) {
    check(ctx);
    var xs = ctx.x() == null
      ?List.<String>of()
      :ctx.x().stream()
        .map(this::visitX)
        .map(E.X::name)
        .toList();
    var body = Optional.ofNullable(ctx.e()).map(this::visitE);
    return new E.Meth(Optional.empty(), Optional.empty(), xs, body, Optional.of(pos(ctx)));
  }
  @Override
  public E.Meth visitMeth(MethContext ctx) {
    check(ctx);
    var oldXbs = this.xbs;
    var mh = Optional.ofNullable(ctx.sig()).map(this::visitSig);
    mh.ifPresent(header->
      this.xbs = Mapper.of(xbs_->{
        xbs_.putAll(oldXbs);
        xbs_.putAll(header.bounds);
      }));
    var xs = mh.map(MethHeader::xs).orElseGet(()->{
      var _xs = opt(ctx.x(), xs1->xs1.stream().map(this::visitX).toList());
      return _xs==null?List.of():_xs;
    });
    var name = mh.map(MethHeader::name)
        .orElseGet(()->new MethName(mh.map(MethHeader::mdf), ctx.m().getText(),xs.size()));
    var body = Optional.ofNullable(ctx.e()).map(this::visitE);
    var sig = mh.map(h->new E.Sig(h.gens(), h.bounds(), xs.stream().map(E.X::t).toList(), h.ret(), Optional.of(pos(ctx))));
    var res = new E.Meth(sig, Optional.of(name), xs.stream().map(E.X::name).toList(), body, Optional.of(pos(ctx)));
    this.xbs = oldXbs;
    return res;
  }
  public record MethHeader(Mdf mdf, MethName name, List<Id.GX<T>> gens, Map<Id.GX<astFull.T>, Set<Mdf>> bounds, List<E.X> xs, T ret){}
  @Override
  public MethHeader visitSig(SigContext ctx) {
    check(ctx);
    var mdf = this.visitMdf(ctx.mdf());
    var gens = this.visitMGenParams(ctx.mGen()).orElse(new GenericParams(List.of(), Map.of()));
    var xs = Optional.ofNullable(ctx.gamma()).map(this::visitGamma).orElse(List.of());
    var name = new MethName(Optional.of(mdf), ctx.m().getText(), xs.size());
    var ret = this.visitT(ctx.t());
    return new MethHeader(mdf, name, gens.gxs, gens.bounds, xs, ret);
  }
  @Override
  public List<E.X> visitGamma(GammaContext ctx) {
    return Streams.zip(ctx.x(), ctx.t())
      .map((xCtx, tCtx)->new E.X(xCtx.getText(), this.visitT(tCtx), Optional.of(pos(xCtx))))
      .toList();
  }
  String completeDeclName(String cName, Pos pos){
    String pkgName= extractPkgname(cName);
    if (!pkgName.isEmpty()){ throw Fail.crossPackageDeclaration().pos(pos); }
    Optional<String> litFullName= LiteralKind.toFullName(cName);
    if (litFullName.isPresent()) {
      //TODO: will be changed when we add the alias pkg.* syntax
      throw Fail.syntaxError(pkg + "." + cName + " is not a valid type name.").pos(pos);
    }
    assert pkg!=null;
    return pkg+"."+cName;
  }
  @Override public T.Dec visitTopDec(TopDecContext ctx) {
    check(ctx);
    E.Lambda l= visitNamedLambda(Mdf.mut,ctx);
    var pos= Optional.of(pos(ctx));
    return new T.Dec(l.id().id(), l.id().gens(), l.id().bounds(), l, pos);
  }
  @Override
  public T.Alias visitAlias(AliasContext ctx) {
    check(ctx);
    var in = visitFullCN(ctx.fullCN(0));
    var _inG = opt(ctx.mGen(0), mGenCtx->visitMGen(mGenCtx, true));
    var inG = Optional.ofNullable(_inG).flatMap(e->e).orElse(List.of());
    var inT=new Id.IT<>(in,inG);
    var out = visitFullCN(ctx.fullCN(1));
    if(some(ctx.mGen(1).genDecl())){ throw Bug.of("No gen on out Alias"); }
    return new T.Alias(inT, out, Optional.of(pos(ctx)));
  }
  public static Package fileToPackage(
      Path fileName,Function<String,Optional<Id.IT<T>>> resolve, NudeProgramContext ctx){
    String name = ctx.Pack().getText();
    assert name.startsWith("package ");
    assert name.endsWith("\n");
    name = name.substring("package ".length(),name.length()-1);
    FullEAntlrVisitor self=new FullEAntlrVisitor(fileName,name,resolve);
    var as = ctx.alias().stream().map(self::visitAlias).toList();
    var decs = List.copyOf(ctx.topDec());
    return new Package(name,as,decs,decs.stream().map(e->fileName).toList());
  }
  @Override
  public Object visitFStringMulti(FStringMultiContext arg0) {
    throw Bug.unreachable();
  }
  private boolean some(Object ctx){
    if (ctx==null){ return false; }
    if (ctx instanceof Collection<?> c && c.isEmpty()){ return false; }
    return true;
  }
}