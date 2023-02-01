package program.inference;

import main.CompileError;
import main.Main;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import parser.Parser;
import utils.Err;
import wellFormedness.WellFormednessFullShortCircuitVisitor;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class TestInferBodies {
  void ok(String expected, String... content){
    assert content.length > 0;
    Main.resetAll();
    AtomicInteger pi = new AtomicInteger();
    var ps = Arrays.stream(content)
      .map(code -> new Parser(Path.of("Dummy"+pi.getAndIncrement()+".fear"), code))
      .toList();
    var p = Parser.parseAll(ps);
    new WellFormednessFullShortCircuitVisitor().visitProgram(p).ifPresent(err->{ throw err; });
    var inferredSigs = p.inferSignaturesToCore();
    var inferred = new InferBodies(inferredSigs).inferAll(p);
    Err.strCmpFormat(expected, inferred.toString());
  }
  void fail(String expectedErr, String... content){
    assert content.length > 0;
    Main.resetAll();
    AtomicInteger pi = new AtomicInteger();
    var ps = Arrays.stream(content)
      .map(code -> new Parser(Path.of("Dummy"+pi.getAndIncrement()+".fear"), code))
      .toList();
    var p = Parser.parseAll(ps);
    new WellFormednessFullShortCircuitVisitor().visitProgram(p).ifPresent(err->{ throw err; });
    var inferredSigs = p.inferSignaturesToCore();

    try {
      var inferred = new InferBodies(inferredSigs).inferAll(p);
      Assertions.fail("Did not fail, got:\n" + inferred);
    } catch (CompileError e) {
      Err.strCmp(expectedErr, e.toString());
    }
  }

  @Test void emptyProgram() { ok("""
    {}
    """, """
    package a
    """); }

  @Test void abstractProgram() { ok("""
    {a.Foo/0=Dec[name=a.Foo/0,gxs=[],lambda=[-mdf-][a.Foo[]]{'this
      .nothingToInfer/0([]):Sig[mdf=imm,gens=[],ts=[],ret=imma.Foo[]]->[-]}]}
    ""","""
    package a
    Foo:{ .nothingToInfer: Foo }
    """); }

  @Test void inferSelfFn() { ok("""
    {a.Id/0=Dec[name=a.Id/0,gxs=[],lambda=[-mdf-][a.Id[]]{'this
      .id/0([]):Sig[mdf=imm,gens=[],ts=[],ret=imma.Id[]]->this}]}
    """, """
    package a
    Id:{ .id: Id -> this }
    """); }

  @Test void inferIdentityFn() { ok("""
    {a.Id/0=Dec[name=a.Id/0,gxs=[],lambda=[-mdf-][a.Id[]]{'this
      .id/1([x]):Sig[mdf=imm,gens=[X],ts=[immX],ret=immX]->x}]}
    """, """
    package a
    Id:{ .id[X](x: X): X -> x }
    """); }
  @Test void inferIdentityFnAndSig() { ok("""
    {a.Id/0=Dec[name=a.Id/0,gxs=[],lambda=[-mdf-][a.Id[]]{'this
      .id/1([x]):Sig[mdf=imm,gens=[X],ts=[immX],ret=immX]->[-]}],
    a.Id2/0=Dec[name=a.Id2/0,gxs=[],lambda=[-mdf-][a.Id2[],a.Id[]]{'this
      .id/1([x]):Sig[mdf=imm,gens=[Fear0$],ts=[immFear0$],ret=immFear0$]->x}]}
    """,  """
    package a
    Id:{ .id[X](x: X): X }
    Id2:Id{ x -> x }
    """); }
  @Test void inferLoop() { ok("""
    {a.Id/0=Dec[name=a.Id/0,gxs=[],lambda=[-mdf-][a.Id[]]{'this
      .id/1([x]):Sig[mdf=imm,gens=[X],ts=[immX],ret=immX]->this.id/1[immX]([x])}]}
    """,  """
    package a
    Id:{ .id[X](x: X): X -> this.id[X](x) }
    """); }
  @Test void inferLoop2() { ok("""
    {a.Id/0=Dec[name=a.Id/0,gxs=[],lambda=[-mdf-][a.Id[]]{'this
      .id/1([x]):Sig[mdf=imm,gens=[X],ts=[immX],ret=immX]->this.id/1[mut X]([x])}]}
    """,  """
    package a
    Id:{ .id[X](x: X): X -> this.id[mut X](x) }
    """); }
  @Test void inferLoopMdf() { ok("""
    {a.Id/0=Dec[name=a.Id/0,gxs=[],lambda=[-mdf-][a.Id[]]{'this
      .id/1([x]):Sig[mdf=imm,gens=[X],ts=[mdfX],ret=mdfX]->this.id/1[mdfX]([x])}]}
    """, """
    package a
    Id:{ .id[X](x: mdf X): mdf X -> this.id[mdf X](x) }
    """); }
  @Test void inferLoopMut() { ok("""
    {a.Id/0=Dec[name=a.Id/0,gxs=[],lambda=[-mdf-][a.Id[]]{'this
      .id/1([x]):Sig[mdf=mut,gens=[X],ts=[mutX],ret=mutX]->this.id/1[mutX]([x])}]}
    """, """
    package a
    Id:{ mut .id[X](x: mut X): mut X -> this.id[mut X](x) }
    """); }

  @Test void immOpt() { ok("""
    {base.OptMap/2=Dec[name=base.OptMap/2,gxs=[T,R],lambda=[-mdf-][base.OptMap[mdfT,mdfR],
    base.OptMatch[immT,immbase.Opt[immR]]]{'this
      #/1([t]):Sig[mdf=imm,gens=[],ts=[immT],ret=immR]->[-],
      .some/1([x]):Sig[mdf=imm,gens=[],ts=[immT],ret=immbase.Opt[immR]]->
        [-imm-][base.Opt[],base.Opt[]]{'fear0$}#/1[mdfR]([this#/1[]([x])]),
      .none/0([]):Sig[mdf=imm,gens=[],ts=[],ret=immbase.Opt[immR]]->
        [-imm-][base.Opt[immR]]{'fear1$}}],
    base.OptDo/1=Dec[name=base.OptDo/1,gxs=[T],lambda=[-mdf-][base.OptDo[mdfT],base.OptMatch[immT,immbase.Void[]]]{'this
      #/1([t]):Sig[mdf=imm,gens=[],ts=[immT],ret=immbase.Void[]]->[-],
      .some/1([x]):Sig[mdf=imm,gens=[],ts=[immT],ret=immbase.Void[]]->[-imm-][base.Opt[],base.Opt[]]{'fear2$}#/1[immbase.Opt[immT]]([this._doRes/2[]([this#/1[]([x]),x])]),
      .none/0([]):Sig[mdf=imm,gens=[],ts=[],ret=immbase.Void[]]->[-imm-][base.Void[]]{'fear3$},
      ._doRes/2([y,x]):Sig[mdf=imm,gens=[],ts=[immbase.Void[],immT],ret=immbase.Opt[immT]]->[-imm-][base.Opt[],base.Opt[]]{'fear4$}#/1[mdfT]([x])}],
    base.Opt/0=Dec[name=base.Opt/0,gxs=[],lambda=[-mdf-][base.Opt[]]{'this
      #/1([x]):Sig[mdf=imm,gens=[T],ts=[immT],ret=immbase.Opt[immT]]->
        [-imm-][base.Opt[immT]]{'fear5$.match/1([m]):Sig[mdf=imm,gens=[T],ts=[immbase.OptMatch[immT,immT]],ret=immT]->
          m.some/1[]([x])}}],
    base.Opt/1=Dec[name=base.Opt/1,gxs=[T],lambda=[-mdf-][base.Opt[mdfT],base.NoMutHyg[immT]]{'this
      .match/1([m]):Sig[mdf=imm,gens=[R],ts=[immbase.OptMatch[immT,immR]],ret=immR]->
        m.none/0[]([]),
      .map/1([f]):Sig[mdf=imm,gens=[R],ts=[immbase.OptMap[immT,immR]],ret=immbase.Opt[immR]]->
        this.match/1[mdfR]([f]),
      .do/1([f]):Sig[mdf=imm,gens=[],ts=[immbase.OptDo[immT]],ret=immbase.Opt[immT]]->
          this.match/1[mdfR]([f]),
      .flatMap/1([f]):Sig[mdf=imm,gens=[R],ts=[immbase.OptFlatMap[immT,immR]],ret=immbase.Opt[immR]]->
        this.match/1[mdfR]([f])}],
    base.OptFlatMap/2=Dec[name=base.OptFlatMap/2,gxs=[T,R],lambda=[-mdf-][base.OptFlatMap[mdfT,mdfR],base.OptMatch[immT,immbase.Opt[immR]]]{'this
      .none/0([]):Sig[mdf=imm,gens=[],ts=[],ret=immbase.Opt[immR]]->
        [-imm-][base.Opt[immR]]{'fear6$}}],
    base.NoMutHyg/1=Dec[name=base.NoMutHyg/1,gxs=[X],lambda=[-mdf-][base.NoMutHyg[mdfX]]{'this}],
    base.Void/0=Dec[name=base.Void/0,gxs=[],lambda=[-mdf-][base.Void[]]{'this}],
    base.OptMatch/2=Dec[name=base.OptMatch/2,gxs=[T,R],lambda=[-mdf-][base.OptMatch[mdfT,mdfR]]{'this
      .some/1([x]):Sig[mdf=imm,gens=[],ts=[immT],ret=immR]->[-],
      .none/0([]):Sig[mdf=imm,gens=[],ts=[],ret=immR]->[-]}]}
    """, """
    package base
    Opt[T]:NoMutHyg[T]{
      .match[R](m: OptMatch[T, R]): R -> m.none,
      .map[R](f: OptMap[T,R]): Opt[R]->this.match[R](f),
      .do(f: OptDo[T]):Opt[T]->this.match[R](f),
      .flatMap[R](f: OptFlatMap[T, R]): Opt[R]->this.match[R](f),
      }
    OptMatch[T,R]:{ .some(x:T): R, .none: R }
    OptFlatMap[T,R]:OptMatch[T,Opt[R]]{ .none->{} }
    OptMap[T,R]:OptMatch[T,Opt[R]]{ #(t:T):R, .some(x) -> Opt#(this#x), .none->{} }
    OptDo[T]:OptMatch[T,Void]{
      #(t:T):Void,   //#[R](t:T):R,
      .some(x) -> Opt#(this._doRes(this#x, x)),
      .none->{},
      ._doRes(y:Void,x:T):Opt[T]->Opt#x
      }
    Opt:{ #[T](x: T): Opt[T] -> { .match(m)->m.some(x)} }
    Void:{}
    NoMutHyg[X]:{}
    """); }

  @Test void immOptInferR() { ok("""
    {base.OptMap/2=Dec[name=base.OptMap/2,gxs=[T,R],lambda=[-mdf-][base.OptMap[mdfT,mdfR],
    base.OptMatch[immT,immbase.Opt[immR]]]{'this
      #/1([t]):Sig[mdf=imm,gens=[],ts=[immT],ret=immR]->[-],
      .some/1([x]):Sig[mdf=imm,gens=[],ts=[immT],ret=immbase.Opt[immR]]->
        [-imm-][base.Opt[],base.Opt[]]{'fear0$}#/1[mdfR]([this#/1[]([x])]),
      .none/0([]):Sig[mdf=imm,gens=[],ts=[],ret=immbase.Opt[immR]]->
        [-imm-][base.Opt[immR]]{'fear1$}}],
    base.OptDo/1=Dec[name=base.OptDo/1,gxs=[T],lambda=[-mdf-][base.OptDo[mdfT],base.OptMatch[immT,immbase.Void[]]]{'this
      #/1([t]):Sig[mdf=imm,gens=[],ts=[immT],ret=immbase.Void[]]->[-],
      .some/1([x]):Sig[mdf=imm,gens=[],ts=[immT],ret=immbase.Void[]]->[-imm-][base.Opt[],base.Opt[]]{'fear2$}#/1[immbase.Opt[immT]]([this._doRes/2[]([this#/1[]([x]),x])]),
      .none/0([]):Sig[mdf=imm,gens=[],ts=[],ret=immbase.Void[]]->[-imm-][base.Void[]]{'fear3$},
      ._doRes/2([y,x]):Sig[mdf=imm,gens=[],ts=[immbase.Void[],immT],ret=immbase.Opt[immT]]->[-imm-][base.Opt[],base.Opt[]]{'fear4$}#/1[mdfT]([x])}],
    base.Opt/0=Dec[name=base.Opt/0,gxs=[],lambda=[-mdf-][base.Opt[]]{'this
      #/1([x]):Sig[mdf=imm,gens=[T],ts=[immT],ret=immbase.Opt[immT]]->
        [-imm-][base.Opt[immT]]{'fear5$.match/1([m]):Sig[mdf=imm,gens=[T],ts=[immbase.OptMatch[immT,immT]],ret=immT]->
          m.some/1[]([x])}}],
    base.Opt/1=Dec[name=base.Opt/1,gxs=[T],lambda=[-mdf-][base.Opt[mdfT],base.NoMutHyg[immT]]{'this
      .match/1([m]):Sig[mdf=imm,gens=[R],ts=[immbase.OptMatch[immT,immR]],ret=immR]->
        m.none/0[]([]),
      .map/1([f]):Sig[mdf=imm,gens=[R],ts=[immbase.OptMap[immT,immR]],ret=immbase.Opt[immR]]->
        this.match/1[mdfR]([f]),
      .do/1([f]):Sig[mdf=imm,gens=[],ts=[immbase.OptDo[immT]],ret=immbase.Opt[immT]]->
          this.match/1[mdfR]([f]),
      .flatMap/1([f]):Sig[mdf=imm,gens=[R],ts=[immbase.OptFlatMap[immT,immR]],ret=immbase.Opt[immR]]->
        this.match/1[mdfR]([f])}],
    base.OptFlatMap/2=Dec[name=base.OptFlatMap/2,gxs=[T,R],lambda=[-mdf-][base.OptFlatMap[mdfT,mdfR],base.OptMatch[immT,immbase.Opt[immR]]]{'this
      .none/0([]):Sig[mdf=imm,gens=[],ts=[],ret=immbase.Opt[immR]]->
        [-imm-][base.Opt[immR]]{'fear6$}}],
    base.NoMutHyg/1=Dec[name=base.NoMutHyg/1,gxs=[X],lambda=[-mdf-][base.NoMutHyg[mdfX]]{'this}],
    base.Void/0=Dec[name=base.Void/0,gxs=[],lambda=[-mdf-][base.Void[]]{'this}],
    base.OptMatch/2=Dec[name=base.OptMatch/2,gxs=[T,R],lambda=[-mdf-][base.OptMatch[mdfT,mdfR]]{'this
      .some/1([x]):Sig[mdf=imm,gens=[],ts=[immT],ret=immR]->[-],
      .none/0([]):Sig[mdf=imm,gens=[],ts=[],ret=immR]->[-]}]}
    """, """
    package base
    Opt[T]:NoMutHyg[T]{
      .match[R](m: OptMatch[T, R]): R -> m.none,
      .map[R](f: OptMap[T,R]): Opt[R]->this.match(f),
      .do(f: OptDo[T]):Opt[T]->this.match(f),
      .flatMap[R](f: OptFlatMap[T, R]): Opt[R]->this.match(f),
      }
    OptMatch[T,R]:{ .some(x:T): R, .none: R }
    OptFlatMap[T,R]:OptMatch[T,Opt[R]]{ .none->{} }
    OptMap[T,R]:OptMatch[T,Opt[R]]{ #(t:T):R, .some(x) -> Opt#(this#x), .none->{} }
    OptDo[T]:OptMatch[T,Void]{
      #(t:T):Void,   //#[R](t:T):R,
      .some(x) -> Opt#(this._doRes(this#x, x)),
      .none->{},
      ._doRes(y:Void,x:T):Opt[T]->Opt#x
      }
    Opt:{ #[T](x: T): Opt[T] -> { .match(m)->m.some(x)} }
    Void:{}
    NoMutHyg[X]:{}
    """); }

  @Test void immDelegate() { ok("""
    {base.A/0=Dec[name=base.A/0,gxs=[],lambda=[-mdf-][base.A[]]{'this
      .m1/1([x]):Sig[mdf=imm,gens=[T],ts=[immT],ret=immbase.Void[]]->this.m2/1[immT]([x]),
      .m2/1([k]):Sig[mdf=imm,gens=[K],ts=[immK],ret=immbase.Void[]]->[-]}],
    base.Void/0=Dec[name=base.Void/0,gxs=[],lambda=[-mdf-][base.Void[]]{'this}]}
    """, """
    package base
    A:{
      .m1[T](x:T):Void->this.m2(x),
      .m2[K](k:K):Void
      }
    Void:{}
    """); }
  @Test void immDelegate2() { ok("""
    {base.B/1=Dec[name=base.B/1,gxs=[X],lambda=[-mdf-][base.B[mdfX]]{'this}],base.A/0=Dec[name=base.A/0,gxs=[],lambda=[-mdf-][base.A[]]{'this.m1/1([x]):Sig[mdf=imm,gens=[T],ts=[immbase.B[immT]],ret=immbase.Void[]]->this.m2/1[immT]([x]),.m2/1([k]):Sig[mdf=imm,gens=[K],ts=[immbase.B[immK]],ret=immbase.Void[]]->[-]}],base.Void/0=Dec[name=base.Void/0,gxs=[],lambda=[-mdf-][base.Void[]]{'this}]}
    """, """
    package base
    B[X]:{}
    A:{
      .m1[T](x:B[T]):Void->this.m2(x),
      .m2[K](k:B[K]):Void
      }
    Void:{}
    """); }
  @Test void immDelegate3() { ok("""
    {base.B/1=Dec[name=base.B/1,gxs=[X],lambda=[-mdf-][base.B[mdfX]]{'this}],base.A/0=Dec[name=base.A/0,gxs=[],lambda=[-mdf-][base.A[]]{'this.m1/1([x]):Sig[mdf=imm,gens=[T],ts=[immbase.B[immT]],ret=immbase.Void[]]->this.m2/1[mdfT]([x]),.m2/1([k]):Sig[mdf=imm,gens=[K],ts=[immbase.B[immK]],ret=immbase.Void[]]->[-]}],base.Void/0=Dec[name=base.Void/0,gxs=[],lambda=[-mdf-][base.Void[]]{'this}]}
    """, """
    package base
    B[T,R]:{}
    A[T]:{
      .m1[R](x:B[T,R]):Void->this.m2(x),
      .m2[K](k:B[T,K]):Void
      }
    Void:{}
    """); }

  /*
  Void:{
    #[E](e:E):Void->this
    .and[R](v:Void,r:R):R->r
    }
  * */
  /*
  [-imm base.Let[]-][base.Let[]]{ } #/1[infer, imm base.Void[]]([[-imm base.Let[mdf X, mdf base.Void[]]-][]{
        .var/0([]): Sig[mdf=imm,gens=[],ts=[],ret=mdf X] ->
          this:mut base.Ref[mdf X] .swap/1[]([x:mdf X]):mdf X,
        .in/1([_]): Sig[mdf=imm,gens=[],ts=[imm X],ret=imm base.Void[]] ->
          [-imm base.Void[]-][base.Void[]]{ }}]):imm base.Void[]
   */
  @Disabled
  @Test void inferRefDef() { ok("""
    """, """
    package base
    alias base.NoMutHyg as NoMutHyg,
    Sealed:{} Void:{}
    Let:{ #[V,R](l:Let[mdf V,mdf R]):mdf R -> l.in(l.var) }
    Let[V,R]:{ .var:mdf V, .in(v:mdf V):mdf R }
    Ref:{ #[X](x: mdf X): mut Ref[mdf X] -> this#(x) }
    Ref[X]:NoMutHyg[X],Sealed{ // mut Ref[imm X] --> 
      read * : recMdf X,
      mut .swap(x: mdf X): mdf X,
      mut :=(x: mdf X): Void -> Let#{ .var -> this.swap(x), .in(_)->Void },
      mut <-(f: mut UpdateRef[mut X]): mdf X -> this.swap(f#(this*)),
      // this:mutRef[mdfX].swap[-](immUpdateRef[mdf X]#[-](this:mutRef[mdfX] *[]():infer]):infer):mdf X
      // this:mutRef[mdfX].swap[-](immUpdateRef[mdf X]#[-](this:mutRef[recMdfX]*[]([]):recMdf X):infer):mdf X
    }
    UpdateRef[X]:{ mut #(x: mdf X): mdf X }
    """); }

  @Test void nestedGensClash(){ ok("""
    {base.B/0=Dec[name=base.B/0,gxs=[],lambda=[-mdf-][base.B[],base.A[]]{'this
      .foo/0([]):Sig[mdf=imm,gens=[Par0$],ts=[],ret=immbase.A[]]->
        [-imm-][base.A[]]{'fear0$.foo/0([]):Sig[mdf=imm,gens=[Par0$],ts=[],ret=immbase.A[]]->[-imm-][base.A[]]{'fear1$
          .foo/0([]):Sig[mdf=imm,gens=[Par0$],ts=[],ret=immbase.A[]]->this}}}],
    base.A/0=Dec[name=base.A/0,gxs=[],lambda=[-mdf-][base.A[]]{'this.foo/0([]):Sig[mdf=imm,gens=[X],ts=[],ret=immbase.A[]]->[-]}]}
    """, """
    package base
    A:{
      .foo[X]:A
    }
    B:A{
      .foo -> {
        .foo -> {
          .foo -> this
        }
      }
    }
    """); }
  //TODO: we need to make so that the parameters Xs inferred in signatures are
  //-not clashing
  //-user readable
}
