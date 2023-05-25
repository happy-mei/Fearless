package program.inference;

import failure.CompileError;
import main.Main;
import net.jqwik.api.Example;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import parser.Parser;
import utils.Base;
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
    var cleaned = Base.ignoreBase(inferred);
    Err.strCmpFormat(expected, cleaned.toString());
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
      Assertions.fail("Did not fail, got:\n" + Base.ignoreBase(inferred));
    } catch (CompileError e) {
      Err.strCmp(expectedErr, e.toString());
    }
  }

  @Test void baseLib() {ok("""
    {}
    """, Base.baseLib); }

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
      .id/1([x]):Sig[mdf=imm,gens=[X0/0$],ts=[immX0/0$],ret=immX0/0$]->x}]}
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
    {test.OptDo/1=Dec[name=test.OptDo/1,gxs=[T],lambda=[-mdf-][test.OptDo[mdfT],test.OptMatch[immT,immtest.Void[]]]{'this
      #/1([t]):Sig[mdf=imm,gens=[],ts=[immT],ret=immtest.Void[]]->[-],
      .some/1([x]):Sig[mdf=imm,gens=[],ts=[immT],ret=immtest.Void[]]->
        [-imm-][test.Opt[]]{'fear[###]$}#/1[immtest.Opt[immT]]([this._doRes/2[]([this#/1[]([x]),x])]),
      .none/0([]):Sig[mdf=imm,gens=[],ts=[],ret=immtest.Void[]]->[-imm-][test.Void[]]{'fear[###]$},
      ._doRes/2([y,x]):Sig[mdf=imm,gens=[],ts=[immtest.Void[],immT],ret=immtest.Opt[immT]]->
        [-imm-][test.Opt[]]{'fear[###]$}#/1[immT]([x])}],
    test.NoMutHyg/1=Dec[name=test.NoMutHyg/1,gxs=[X],lambda=[-mdf-][test.NoMutHyg[mdfX]]{'this}],
    test.OptFlatMap/2=Dec[name=test.OptFlatMap/2,gxs=[T,R],lambda=[-mdf-][test.OptFlatMap[mdfT,mdfR],test.OptMatch[immT,immtest.Opt[immR]]]{'this
      .none/0([]):Sig[mdf=imm,gens=[],ts=[],ret=immtest.Opt[immR]]->[-imm-][test.Opt[immR]]{'fear[###]$}}],
    test.OptMatch/2=Dec[name=test.OptMatch/2,gxs=[T,R],lambda=[-mdf-][test.OptMatch[mdfT,mdfR]]{'this
      .some/1([x]):Sig[mdf=imm,gens=[],ts=[immT],ret=immR]->[-],
      .none/0([]):Sig[mdf=imm,gens=[],ts=[],ret=immR]->[-]}],
    test.OptMap/2=Dec[name=test.OptMap/2,gxs=[T,R],lambda=[-mdf-][test.OptMap[mdfT,mdfR],test.OptMatch[immT,immtest.Opt[immR]]]{'this
      #/1([t]):Sig[mdf=imm,gens=[],ts=[immT],ret=immR]->[-],
      .some/1([x]):Sig[mdf=imm,gens=[],ts=[immT],ret=immtest.Opt[immR]]->
        [-imm-][test.Opt[]]{'fear[###]$}#/1[immR]([this#/1[]([x])]),
      .none/0([]):Sig[mdf=imm,gens=[],ts=[],ret=immtest.Opt[immR]]->[-imm-][test.Opt[immR]]{'fear[###]$}}],
    test.Opt/0=Dec[name=test.Opt/0,gxs=[],lambda=[-mdf-][test.Opt[]]{'this
      #/1([x]):Sig[mdf=imm,gens=[T],ts=[immT],ret=immtest.Opt[immT]]->
        [-imm-][test.Opt[imm T]]{'fear[###]$
          .match/1([m]):Sig[mdf=imm,gens=[X1/0$],ts=[immtest.OptMatch[immT,immX1/0$]],ret=immX1/0$]->m.some/1[]([x])}}],
    test.Opt/1=Dec[name=test.Opt/1,gxs=[T],lambda=[-mdf-][test.Opt[mdfT],test.NoMutHyg[immT]]{'this
      .match/1([m]):Sig[mdf=imm,gens=[R],ts=[immtest.OptMatch[immT,immR]],ret=immR]->m.none/0[]([]),
      .map/1([f]):Sig[mdf=imm,gens=[R],ts=[immtest.OptMap[immT,immR]],ret=immtest.Opt[immR]]->
        this.match/1[immtest.Opt[immR]]([f]),
      .do/1([f]):Sig[mdf=imm,gens=[],ts=[immtest.OptDo[immT]],ret=immtest.Opt[immT]]->
        this.match/1[immtest.Opt[immT]]([f]),
      .flatMap/1([f]):Sig[mdf=imm,gens=[R],ts=[immtest.OptFlatMap[immT,immR]],ret=immtest.Opt[immR]]->
        this.match/1[immtest.Opt[immR]]([f])}],
    test.Void/0=Dec[name=test.Void/0,gxs=[],lambda=[-mdf-][test.Void[]]{'this}]}
    """, """
    package test
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

  @Test void immOpt2() { ok("""
    {test.OptMatch/2=Dec[name=test.OptMatch/2,gxs=[T,R],lambda=[-mdf-][test.OptMatch[mdfT,mdfR]]{'this
      .some/1([x]):Sig[mdf=imm,gens=[],ts=[immT],ret=immR]->[-],
      .none/0([]):Sig[mdf=imm,gens=[],ts=[],ret=immR]->[-]}],
    test.Opt/0=Dec[name=test.Opt/0,gxs=[],lambda=[-mdf-][test.Opt[]]{'this
      #/1([x]):Sig[mdf=imm,gens=[T],ts=[immT],ret=immtest.Opt[immT]]->
        [-imm-][test.Opt[imm T]]{'fear[###]$
          .match/1([m]):Sig[mdf=imm,gens=[X1/0$],ts=[immtest.OptMatch[immT,immX1/0$]],ret=immX1/0$]->m.some/1[]([x])}}],
    test.Opt/1=Dec[name=test.Opt/1,gxs=[T],lambda=[-mdf-][test.Opt[mdfT]]{'this
      .match/1([m]):Sig[mdf=imm,gens=[R],ts=[immtest.OptMatch[immT,immR]],ret=immR]->
        m.none/0[]([])}]}
    """, """
    package test
    Opt[T]:{ .match[R](m: OptMatch[T, R]): R -> m.none }
    OptMatch[T,R]:{ .some(x:T): R, .none: R }
    Opt:{ #[T](x: T): Opt[T] -> { .match(m)->m.some(x)} }
    """); }

  @Test void immOptInferR() { ok("""
    {test.OptDo/1=Dec[name=test.OptDo/1,gxs=[T],lambda=[-mdf-][test.OptDo[mdfT],test.OptMatch[immT,immtest.Void[]]]{'this#/1([t]):Sig[mdf=imm,gens=[],ts=[immT],ret=immtest.Void[]]->[-],.some/1([x]):Sig[mdf=imm,gens=[],ts=[immT],ret=immtest.Void[]]->[-imm-][test.Opt[]]{'fear[###]$}#/1[immtest.Opt[immT]]([this._doRes/2[]([this#/1[]([x]),x])]),.none/0([]):Sig[mdf=imm,gens=[],ts=[],ret=immtest.Void[]]->[-imm-][test.Void[]]{'fear[###]$},._doRes/2([y,x]):Sig[mdf=imm,gens=[],ts=[immtest.Void[],immT],ret=immtest.Opt[immT]]->[-imm-][test.Opt[]]{'fear[###]$}#/1[immT]([x])}],test.NoMutHyg/1=Dec[name=test.NoMutHyg/1,gxs=[X],lambda=[-mdf-][test.NoMutHyg[mdfX]]{'this}],test.OptFlatMap/2=Dec[name=test.OptFlatMap/2,gxs=[T,R],lambda=[-mdf-][test.OptFlatMap[mdfT,mdfR],test.OptMatch[immT,immtest.Opt[immR]]]{'this.none/0([]):Sig[mdf=imm,gens=[],ts=[],ret=immtest.Opt[immR]]->[-imm-][test.Opt[immR]]{'fear[###]$}}],test.OptMatch/2=Dec[name=test.OptMatch/2,gxs=[T,R],lambda=[-mdf-][test.OptMatch[mdfT,mdfR]]{'this.some/1([x]):Sig[mdf=imm,gens=[],ts=[immT],ret=immR]->[-],.none/0([]):Sig[mdf=imm,gens=[],ts=[],ret=immR]->[-]}],test.OptMap/2=Dec[name=test.OptMap/2,gxs=[T,R],lambda=[-mdf-][test.OptMap[mdfT,mdfR],test.OptMatch[immT,immtest.Opt[immR]]]{'this#/1([t]):Sig[mdf=imm,gens=[],ts=[immT],ret=immR]->[-],.some/1([x]):Sig[mdf=imm,gens=[],ts=[immT],ret=immtest.Opt[immR]]->[-imm-][test.Opt[]]{'fear[###]$}#/1[immR]([this#/1[]([x])]),.none/0([]):Sig[mdf=imm,gens=[],ts=[],ret=immtest.Opt[immR]]->[-imm-][test.Opt[immR]]{'fear[###]$}}],test.Opt/0=Dec[name=test.Opt/0,gxs=[],lambda=[-mdf-][test.Opt[]]{'this#/1([x]):Sig[mdf=imm,gens=[T],ts=[immT],ret=immtest.Opt[immT]]->[-imm-][test.Opt[immT]]{'fear[###]$.match/1([m]):Sig[mdf=imm,gens=[X1/0$],ts=[immtest.OptMatch[immT,immX1/0$]],ret=immX1/0$]->m.some/1[]([x])}}],test.Opt/1=Dec[name=test.Opt/1,gxs=[T],lambda=[-mdf-][test.Opt[mdfT],test.NoMutHyg[immT]]{'this.match/1([m]):Sig[mdf=imm,gens=[R],ts=[immtest.OptMatch[immT,immR]],ret=immR]->m.none/0[]([]),.map/1([f]):Sig[mdf=imm,gens=[R],ts=[immtest.OptMap[immT,immR]],ret=immtest.Opt[immR]]->this.match/1[immtest.Opt[immR]]([f]),.do/1([f]):Sig[mdf=imm,gens=[],ts=[immtest.OptDo[immT]],ret=immtest.Opt[immT]]->this.match/1[immtest.Opt[immT]]([f]),.flatMap/1([f]):Sig[mdf=imm,gens=[R],ts=[immtest.OptFlatMap[immT,immR]],ret=immtest.Opt[immR]]->this.match/1[immtest.Opt[immR]]([f])}],test.Void/0=Dec[name=test.Void/0,gxs=[],lambda=[-mdf-][test.Void[]]{'this}]}
    """, """
    package test
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

  @Test void immDelegateExplicit() { ok("""
    {test.A/0=Dec[name=test.A/0,gxs=[],lambda=[-mdf-][test.A[]]{'this
      .m1/1([x]):Sig[mdf=imm,gens=[T],ts=[immT],ret=immtest.Void[]]->this.m2/1[mdfT]([x]),.
      m2/1([k]):Sig[mdf=imm,gens=[K],ts=[immK],ret=immtest.Void[]]->[-]}],
    test.Void/0=Dec[name=test.Void/0,gxs=[],lambda=[-mdf-][test.Void[]]{'this}]}
    """, """
    package test
    A:{
      .m1[T](x:T):Void->this.m2[mdf T](x),
      .m2[K](k:K):Void
      }
    Void:{}
    """); }

  @Test void immDelegateExplicitImmGen() { ok("""
    {test.A/0=Dec[name=test.A/0,gxs=[],lambda=[-mdf-][test.A[]]{'this
      .m1/1([x]):Sig[mdf=imm,gens=[T],ts=[immT],ret=immtest.Void[]]->this.m2/1[imm T]([x]),.
      m2/1([k]):Sig[mdf=imm,gens=[K],ts=[immK],ret=immtest.Void[]]->[-]}],
    test.Void/0=Dec[name=test.Void/0,gxs=[],lambda=[-mdf-][test.Void[]]{'this}]}
    """, """
    package test
    A:{
      .m1[T](x:T):Void->this.m2[T](x),
      .m2[K](k:K):Void
      }
    Void:{}
    """); }

  @Test void immDelegate() { ok("""
    {test.A/0=Dec[name=test.A/0,gxs=[],lambda=[-mdf-][test.A[]]{'this
      .m1/1([x]):Sig[mdf=imm,gens=[T],ts=[immT],ret=immtest.Void[]]->this.m2/1[immT]([x]),
      .m2/1([k]):Sig[mdf=imm,gens=[K],ts=[immK],ret=immtest.Void[]]->[-]}],
    test.Void/0=Dec[name=test.Void/0,gxs=[],lambda=[-mdf-][test.Void[]]{'this}]}
    """, """
    package test
    A:{
      .m1[T](x:T):Void->this.m2(x),
      .m2[K](k:K):Void
      }
    Void:{}
    """); }
  @Test void immDelegate2() { ok("""
    {test.B/1=Dec[name=test.B/1,gxs=[X],lambda=[-mdf-][test.B[mdfX]]{'this}],test.A/0=Dec[name=test.A/0,gxs=[],lambda=[-mdf-][test.A[]]{'this.m1/1([x]):Sig[mdf=imm,gens=[T],ts=[immtest.B[immT]],ret=immtest.Void[]]->this.m2/1[immT]([x]),.m2/1([k]):Sig[mdf=imm,gens=[K],ts=[immtest.B[immK]],ret=immtest.Void[]]->[-]}],test.Void/0=Dec[name=test.Void/0,gxs=[],lambda=[-mdf-][test.Void[]]{'this}]}
    """, """
    package test
    B[X]:{}
    A:{
      .m1[T](x:B[T]):Void->this.m2(x),
      .m2[K](k:B[K]):Void
      }
    Void:{}
    """); }
  @Test void immDelegate3() { ok("""
    {test.B/2=Dec[name=test.B/2,gxs=[T,R],lambda=[-mdf-][test.B[mdfT,mdfR]]{'this}],test.A/1=Dec[name=test.A/1,gxs=[T],lambda=[-mdf-][test.A[mdfT]]{'this.m1/1([x]):Sig[mdf=imm,gens=[R],ts=[immtest.B[immT,immR]],ret=immtest.Void[]]->this.m2/1[immR]([x]),.m2/1([k]):Sig[mdf=imm,gens=[K],ts=[immtest.B[immT,immK]],ret=immtest.Void[]]->[-]}],test.Void/0=Dec[name=test.Void/0,gxs=[],lambda=[-mdf-][test.Void[]]{'this}]}
    """, """
    package test
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
  [-imm test.Let[]-][test.Let[]]{ } #/1[infer, imm test.Void[]]([[-imm test.Let[mdf X, mdf test.Void[]]-][]{
        .var/0([]): Sig[mdf=imm,gens=[],ts=[],ret=mdf X] ->
          this:mut test.Ref[mdf X] .swap/1[]([x:mdf X]):mdf X,
        .in/1([_]): Sig[mdf=imm,gens=[],ts=[imm X],ret=imm test.Void[]] ->
          [-imm test.Void[]-][test.Void[]]{ }}]):imm test.Void[]
   */
  @Test void inferRefDef() { ok("""
    {test.NoMutHyg/1=Dec[name=test.NoMutHyg/1,gxs=[X],lambda=[-mdf-][test.NoMutHyg[mdfX]]{'this}],
    test.Let/0=Dec[name=test.Let/0,gxs=[],lambda=[-mdf-][test.Let[]]{'this
      #/1([l]):Sig[mdf=imm,gens=[V,R],ts=[immtest.Let[mdfV,mdfR]],ret=mdfR]->l.in/1[]([l.var/0[]([])])}],
    test.Let/2=Dec[name=test.Let/2,gxs=[V,R],lambda=[-mdf-][test.Let[mdfV,mdfR]]{'this
      .var/0([]):Sig[mdf=imm,gens=[],ts=[],ret=mdfV]->[-],
      .in/1([v]):Sig[mdf=imm,gens=[],ts=[mdfV],ret=mdfR]->[-]}],
    test.Ref/1=Dec[name=test.Ref/1,gxs=[X],lambda=[-mdf-][test.Ref[mdfX],test.NoMutHyg[immX],test.Sealed[]]{'this
      */0([]):Sig[mdf=read,gens=[],ts=[],ret=recMdfX]->[-],
      .swap/1([x]):Sig[mdf=mut,gens=[],ts=[mdfX],ret=mdfX]->[-],
      :=/1([x]):Sig[mdf=mut,gens=[],ts=[mdfX],ret=immtest.Void[]]->
        [-imm-][test.Let[]]{'fear[###]$}#/1[mdfX,immtest.Void[]]([[-imm-][test.Let[mdfX,immtest.Void[]]]{'fear[###]$
          .var/0([]):Sig[mdf=imm,gens=[],ts=[],ret=mdfX]->this.swap/1[]([x]),
          .in/1([fear[###]$]):Sig[mdf=imm,gens=[],ts=[mdfX],ret=immtest.Void[]]->[-imm-][test.Void[]]{'fear[###]$}}]),
      <-/1([f]):Sig[mdf=mut,gens=[],ts=[muttest.UpdateRef[mutX]],ret=mdfX]->this.swap/1[]([f#/1[]([this*/0[]([])])])}],
      test.UpdateRef/1=Dec[name=test.UpdateRef/1,gxs=[X],lambda=[-mdf-][test.UpdateRef[mdfX]]{'this
        #/1([x]):Sig[mdf=mut,gens=[],ts=[mdfX],ret=mdfX]->[-]}],
      test.Sealed/0=Dec[name=test.Sealed/0,gxs=[],lambda=[-mdf-][test.Sealed[]]{'this}],
      test.Void/0=Dec[name=test.Void/0,gxs=[],lambda=[-mdf-][test.Void[]]{'this}],
      test.Ref/0=Dec[name=test.Ref/0,gxs=[],lambda=[-mdf-][test.Ref[]]{'this
        #/1([x]):Sig[mdf=imm,gens=[X],ts=[mdfX],ret=muttest.Ref[mdfX]]->this#/1[mdfX]([x])}]}
    """, """
    package test
    NoMutHyg[X]:{}
    Sealed:{} Void:{}
    Let:{ #[V,R](l:Let[mdf V,mdf R]):mdf R -> l.in(l.var) }
    Let[V,R]:{ .var:mdf V, .in(v:mdf V):mdf R }
    Ref:{ #[X](x: mdf X): mut Ref[mdf X] -> this#(x) }
    Ref[X]:NoMutHyg[X],Sealed{
      read * : recMdf X,
      mut .swap(x: mdf X): mdf X,
      mut :=(x: mdf X): Void -> Let#{ .var -> this.swap(x), .in(_)->Void },
      mut <-(f: mut UpdateRef[mut X]): mdf X -> this.swap(f#(this*)),
    }
    UpdateRef[X]:{ mut #(x: mdf X): mdf X }
    """); }
  @Test void inferCallGens(){ ok("""
    {test.NoMutHyg/1=Dec[name=test.NoMutHyg/1,gxs=[X],lambda=[-mdf-][test.NoMutHyg[mdfX]]{'this}],test.LetMut/0=Dec[name=test.LetMut/0,gxs=[],lambda=[-mdf-][test.LetMut[]]{'this#/1([l]):Sig[mdf=imm,gens=[V,R],ts=[muttest.LetMut[mdfV,mdfR]],ret=mdfR]->l.in/1[]([l.var/0[]([])])}],test.Ref/1=Dec[name=test.Ref/1,gxs=[X],lambda=[-mdf-][test.Ref[mdfX],test.NoMutHyg[immX],test.Sealed[]]{'this*/0([]):Sig[mdf=read,gens=[],ts=[],ret=recMdfX]->[-],.swap/1([x]):Sig[mdf=mut,gens=[],ts=[mdfX],ret=mdfX]->[-],.test1/1([x]):Sig[mdf=mut,gens=[],ts=[mdfX],ret=muttest.LetMut[mdfX,immtest.Void[]]]->[-mut-][test.LetMut[mdfX,immtest.Void[]]]{'fear[###]$.var/0([]):Sig[mdf=mut,gens=[],ts=[],ret=mdfX]->this.swap/1[]([x]),.in/1([fear[###]$]):Sig[mdf=mut,gens=[],ts=[mdfX],ret=immtest.Void[]]->[-imm-][test.Void[]]{'fear[###]$}},.test2/1([x]):Sig[mdf=mut,gens=[],ts=[mdfX],ret=immtest.Void[]]->[-imm-][test.LetMut[]]{'fear[###]$}#/1[mdfX,immtest.Void[]]([this.test1/1[]([x])]),:=/1([x]):Sig[mdf=mut,gens=[],ts=[mdfX],ret=immtest.Void[]]->[-imm-][test.LetMut[]]{'fear[###]$}#/1[mdfX,immtest.Void[]]([[-mut-][test.LetMut[mdfX,immtest.Void[]]]{'fear[###]$.var/0([]):Sig[mdf=mut,gens=[],ts=[],ret=mdfX]->this.swap/1[]([x]),.in/1([fear[###]$]):Sig[mdf=mut,gens=[],ts=[mdfX],ret=immtest.Void[]]->[-imm-][test.Void[]]{'fear[###]$}}]),<-/1([f]):Sig[mdf=mut,gens=[],ts=[muttest.UpdateRef[mdfX]],ret=mdfX]->this.swap/1[]([f#/1[]([this*/0[]([])])])}],test.UpdateRef/1=Dec[name=test.UpdateRef/1,gxs=[X],lambda=[-mdf-][test.UpdateRef[mdfX]]{'this#/1([x]):Sig[mdf=mut,gens=[],ts=[mdfX],ret=mdfX]->[-]}],test.Ref/0=Dec[name=test.Ref/0,gxs=[],lambda=[-mdf-][test.Ref[]]{'this#/1([x]):Sig[mdf=imm,gens=[X],ts=[mdfX],ret=muttest.Ref[mdfX]]->this#/1[mdfX]([x])}],test.Void/0=Dec[name=test.Void/0,gxs=[],lambda=[-mdf-][test.Void[]]{'this}],test.Sealed/0=Dec[name=test.Sealed/0,gxs=[],lambda=[-mdf-][test.Sealed[]]{'this}],test.LetMut/2=Dec[name=test.LetMut/2,gxs=[V,R],lambda=[-mdf-][test.LetMut[mdfV,mdfR]]{'this.var/0([]):Sig[mdf=mut,gens=[],ts=[],ret=mdfV]->[-],.in/1([v]):Sig[mdf=mut,gens=[],ts=[mdfV],ret=mdfR]->[-]}]}
    """, """
    package test
    Ref:{ #[X](x: mdf X): mut Ref[mdf X] -> this#(x) }
    Ref[X]:NoMutHyg[X],Sealed{
      read * : recMdf X,
      mut .swap(x: mdf X): mdf X,
      mut .test1(x: mdf X): mut LetMut[mdf X,Void]->{
        .var ->this.swap(x), .in(_) -> Void },
      mut .test2(x:mdf X): Void -> LetMut#(this.test1(x)),
      mut :=(x: mdf X): Void -> LetMut#[mdf X,Void]mut LetMut[mdf X,Void]{ .var ->
       this.swap(x), .in(_) -> Void },
      mut <-(f: mut UpdateRef[mdf X]): mdf X -> this.swap(f#(this*)),
      }
    UpdateRef[X]:{ mut #(x: mdf X): mdf X }
    """, """
    package test
    NoMutHyg[X]:{} Void:{} Sealed:{}
    LetMut:{ #[V,R](l: mut LetMut[mdf V, mdf R]): mdf R -> l.in(l.var) }
    LetMut[V,R]:{ mut .var: mdf V, mut .in(v: mdf V): mdf R }
    """); }

  @Test void nestedGensClash(){ ok("""
    {test.B/0=Dec[name=test.B/0,gxs=[],lambda=[-mdf-][test.B[],test.A[]]{'this
      .foo/0([]):Sig[mdf=imm,gens=[X0/0$],ts=[],ret=immtest.A[]]->
        [-imm-][test.A[]]{'fear[###]$
          .foo/0([]):Sig[mdf=imm,gens=[X1/0$],ts=[],ret=immtest.A[]]->
            [-imm-][test.A[]]{'fear[###]$.foo/0([]):Sig[mdf=imm,gens=[X2/0$],ts=[],ret=immtest.A[]]->this}}}],
    test.A/0=Dec[name=test.A/0,gxs=[],lambda=[-mdf-][test.A[]]{'this.foo/0([]):Sig[mdf=imm,gens=[X],ts=[],ret=immtest.A[]]->[-]}]}
    """, """
    package test
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

  @Test void bool() { ok("""
    {test.True/0=Dec[name=test.True/0,gxs=[],lambda=[-mdf-][test.True[],test.Bool[]]{'this
      .and/1([b]):Sig[mdf=imm,gens=[],ts=[immtest.Bool[]],ret=immtest.Bool[]]->b,
      .or/1([b]):Sig[mdf=imm,gens=[],ts=[immtest.Bool[]],ret=immtest.Bool[]]->this,
      .not/0([]):Sig[mdf=imm,gens=[],ts=[],ret=immtest.Bool[]]->[-imm-][test.False[]]{'fear[###]$},
      ?/1([f]):Sig[mdf=imm,gens=[X0/0$],ts=[muttest.ThenElse[immX0/0$]],ret=immX0/0$]->f.then/0[]([])}],
    test.False/0=Dec[name=test.False/0,gxs=[],lambda=[-mdf-][test.False[],test.Bool[]]{'this
      .and/1([b]):Sig[mdf=imm,gens=[],ts=[immtest.Bool[]],ret=immtest.Bool[]]->this,
      .or/1([b]):Sig[mdf=imm,gens=[],ts=[immtest.Bool[]],ret=immtest.Bool[]]->b,
      .not/0([]):Sig[mdf=imm,gens=[],ts=[],ret=immtest.Bool[]]->[-imm-][test.True[]]{'fear[###]$},
      ?/1([f]):Sig[mdf=imm,gens=[X0/0$],ts=[muttest.ThenElse[immX0/0$]],ret=immX0/0$]->f.else/0[]([])}],
    test.ThenElse/1=Dec[name=test.ThenElse/1,gxs=[R],lambda=[-mdf-][test.ThenElse[mdfR]]{'this
      .then/0([]):Sig[mdf=mut,gens=[],ts=[],ret=immR]->[-],
      .else/0([]):Sig[mdf=mut,gens=[],ts=[],ret=immR]->[-]}],
    test.Bool/0=Dec[name=test.Bool/0,gxs=[],lambda=[-mdf-][test.Bool[],test.Sealed[]]{'this
      .and/1([b]):Sig[mdf=imm,gens=[],ts=[immtest.Bool[]],ret=immtest.Bool[]]->[-],
      .or/1([b]):Sig[mdf=imm,gens=[],ts=[immtest.Bool[]],ret=immtest.Bool[]]->[-],
      .not/0([]):Sig[mdf=imm,gens=[],ts=[],ret=immtest.Bool[]]->[-],
      ?/1([f]):Sig[mdf=imm,gens=[R],ts=[muttest.ThenElse[immR]],ret=immR]->[-]}],
    test.Sealed/0=Dec[name=test.Sealed/0,gxs=[],lambda=[-mdf-][test.Sealed[]]{'this}]}
    """, """
    package test
    Bool:Sealed{
      .and(b: Bool): Bool,
      .or(b: Bool): Bool,
      .not: Bool,
      ?[R](f: mut ThenElse[R]): R, // ?  because `bool ? { .then->aa, .else->bb }` is kinda like a ternary
      }
    Sealed:{}
    True:Bool{ .and(b) -> b, .or(b) -> this, .not -> False, ?(f) -> f.then() }
    False:Bool{ .and(b) -> this, .or(b) -> b, .not -> True, ?(f) -> f.else() }
    ThenElse[R]:{ mut .then: R, mut .else: R, }
    """); }

  @Test void boolUsage() { ok("""
    {test.Test/0=Dec[name=test.Test/0,gxs=[],lambda=[-mdf-][test.Test[],base.Main[immbase.Int[]]]{'this
      #/2([fear[###]$,fear[###]$]):Sig[mdf=imm,gens=[],ts=[immbase.LList[immbase.Str[]],lentbase.caps.System[immbase.Int[]]],ret=immbase.Int[]]->
        [-imm-][base.False[]]{'fear[###]$}
          .or/1[]([[-imm-][base.True[]]{'fear[###]$}])
          ?/1[immbase.Int[]]([[-mut-][base.ThenElse[immbase.Int[]]]{'fear[###]$
            .then/0([]):Sig[mdf=mut,gens=[],ts=[],ret=imm42[]]->[-imm-][42[]]{'fear[###]$},
            .else/0([]):Sig[mdf=mut,gens=[],ts=[],ret=imm0[]]->[-imm-][0[]]{'fear[###]$}}])}]}
    """, """
    package test
    alias base.Main as Main, alias base.Int as Int, alias base.False as False, alias base.True as True,
    Test:Main[Int]{
      _,_->False.or(True)?{.then->42,.else->0}
    }
    """, Base.load("lang.fear"), Base.load("caps/caps.fear"), Base.load("nums.fear"), Base.load("bools.fear"), Base.load("strings.fear"), Base.load("lists.fear"), Base.load("optionals.fear")); }
  @Test void boolUsageExplicitGens() { ok("""
    {test.Test/0=Dec[name=test.Test/0,gxs=[],lambda=[-mdf-][test.Test[],base.Main[immbase.Int[]]]{'this
      #/2([fear[###]$,fear[###]$]):Sig[mdf=imm,gens=[],ts=[immbase.LList[immbase.Str[]],lentbase.caps.System[immbase.Int[]]],ret=immbase.Int[]]->
        [-imm-][base.False[]]{'fear[###]$}
          .or/1[]([[-imm-][base.True[]]{'fear[###]$}])
          ?/1[immbase.Int[]]([[-mut-][base.ThenElse[immbase.Int[]]]{'fear[###]$
            .then/0([]):Sig[mdf=mut,gens=[],ts=[],ret=imm42[]]->[-imm-][42[]]{'fear[###]$},
            .else/0([]):Sig[mdf=mut,gens=[],ts=[],ret=imm0[]]->[-imm-][0[]]{'fear[###]$}}])}]}
    """, """
    package test
    alias base.Main as Main, alias base.Int as Int, alias base.False as False, alias base.True as True,
    Test:Main[Int]{
      _,_->False.or(True) ?[Int]{ .then -> 42, .else -> 0 }
    }
    """, Base.load("lang.fear"), Base.load("caps/caps.fear"), Base.load("nums.fear"), Base.load("bools.fear"), Base.load("strings.fear"), Base.load("lists.fear"), Base.load("optionals.fear")); }
  // TODO: why isn't this inferring gens?
  @Test void boolUsageExplicitGensBasicSameT() { ok("""
    {test.Test/0=Dec[name=test.Test/0,gxs=[],lambda=[-mdf-][test.Test[],base.Main[imm42[]]]{'this
      #/1([fear[###]$]):Sig[mdf=imm,gens=[],ts=[lentbase.System[]],ret=imm42[]]->
        [-imm-][base.False[]]{'fear[###]$}
          .or/1[]([[-imm-][base.True[]]{'fear[###]$}])
          ?/1[imm42[]]([[-mut-][base.ThenElse[imm42[]]]{'fear[###]$
            .then/0([]):Sig[mdf=mut,gens=[],ts=[],ret=imm42[]]->[-imm-][42[]]{'fear[###]$},
            .else/0([]):Sig[mdf=mut,gens=[],ts=[],ret=imm42[]]->[-imm-][42[]]{'fear[###]$}}])}]}
    """, """
    package test
    alias base.Main as Main, alias base.Int as Int, alias base.False as False, alias base.True as True,
    Test:Main[42]{
      _->False.or(True) ?[42]{ .then -> 42, .else -> 42 }
    }
    """, """
    package base
    Sealed:{}
    Main[R]:{ #(s: lent System): R }
    System:{} // Root capability
    Bool:Sealed{
    .and(b: Bool): Bool,
    .or(b: Bool): Bool,
    .not: Bool,
    ?[R](f: mut ThenElse[R]): R, // ?  because `bool ? { .then->aa, .else->bb }` is kinda like a ternary
    }
    True:Bool{ .and(b) -> b, .or(b) -> this, .not -> False, ?(f) -> f.then() }
    False:Bool{ .and(b) -> this, .or(b) -> b, .not -> True, ?(f) -> f.else() }
    ThenElse[R]:{ mut .then: R, mut .else: R, }
    """); }
  @Test void boolUsageExplicitGensBasic() { ok("""
    {test.Test/0=Dec[name=test.Test/0,gxs=[],lambda=[-mdf-][test.Test[],base.Main[immbase.Int[]]]{'this
      #/1([fear[###]$]):Sig[mdf=imm,gens=[],ts=[lentbase.System[]],ret=immbase.Int[]]->
        [-imm-][base.False[]]{'fear[###]$}
          .or/1[]([[-imm-][base.True[]]{'fear[###]$}])
          ?/1[immbase.Int[]]([[-mut-][base.ThenElse[immbase.Int[]]]{'fear[###]$
            .then/0([]):Sig[mdf=mut,gens=[],ts=[],ret=imm42[]]->[-imm-][42[]]{'fear[###]$},
            .else/0([]):Sig[mdf=mut,gens=[],ts=[],ret=imm0[]]->[-imm-][0[]]{'fear[###]$}}])}]}
    """, """
    package test
    alias base.Main as Main, alias base.Int as Int, alias base.False as False, alias base.True as True,
    Test:Main[Int]{
      _->False.or(True) ?[Int]{ .then -> 42, .else -> 0 }
    }
    """, """
    package base
    Sealed:{}
    Main[R]:{ #(s: lent System): R }
    System:{} // Root capability
    Bool:Sealed{
    .and(b: Bool): Bool,
    .or(b: Bool): Bool,
    .not: Bool,
    ?[R](f: mut ThenElse[R]): R, // ?  because `bool ? { .then->aa, .else->bb }` is kinda like a ternary
    }
    Int:{}
    _IntInstance:Int{}
    True:Bool{ .and(b) -> b, .or(b) -> this, .not -> False, ?(f) -> f.then() }
    False:Bool{ .and(b) -> this, .or(b) -> b, .not -> True, ?(f) -> f.else() }
    ThenElse[R]:{ mut .then: R, mut .else: R, }
    """); }
  @Test void boolUsageExplicitGensRTBasic1() { ok("""
    {test.Test/0=Dec[name=test.Test/0,gxs=[],lambda=[-mdf-][test.Test[],base.Main[immbase.Int[]]]{'this
      #/1([fear[###]$]):Sig[mdf=imm,gens=[],ts=[lentbase.System[]],ret=immbase.Int[]]->
        [-imm-][base.False[]]{'fear[###]$}
          .or/1[]([[-imm-][base.True[]]{'fear[###]$}])
          ?/1[immbase.Int[]]([[-mut-][base.ThenElse[immbase.Int[]]]{'fear[###]$
            .then/0([]):Sig[mdf=imm,gens=[],ts=[],ret=imm42[]]->[-imm-][42[]]{'fear[###]$},
            .else/0([]):Sig[mdf=imm,gens=[],ts=[],ret=imm0[]]->[-imm-][0[]]{'fear[###]$}}])}]}
    """, """
    package test
    alias base.Main as Main, alias base.Int as Int, alias base.False as False, alias base.True as True,
    Test:Main[Int]{
      _->False.or(True) ?[Int]{ .then: Int -> 42, .else: Int -> 0 }
    }
    """, """
    package base
    Sealed:{}
    Main[R]:{ #(s: lent System): R }
    System:{} // Root capability
    Bool:Sealed{
    .and(b: Bool): Bool,
    .or(b: Bool): Bool,
    .not: Bool,
    ?[R](f: mut ThenElse[R]): R, // ?  because `bool ? { .then->aa, .else->bb }` is kinda like a ternary
    }
    True:Bool{ .and(b) -> b, .or(b) -> this, .not -> False, ?(f) -> f.then() }
    False:Bool{ .and(b) -> this, .or(b) -> b, .not -> True, ?(f) -> f.else() }
    ThenElse[R]:{ mut .then: R, mut .else: R, }
    Int:{}
    _IntInstance:Int{}
    IntOpts:{}
    """); }
  @Test void boolUsageExplicitGensRTBasic2() { ok("""
    {test.Test/0=Dec[name=test.Test/0,gxs=[],lambda=[-mdf-][test.Test[],base.Main[imm base.Int[]]]{'this
      #/1([fear[###]$]):Sig[mdf=imm,gens=[],ts=[lentbase.System[]],ret=imm base.Int[]]->
        [-imm-][base.False[]]{'fear[###]$}
          .or/1[]([[-imm-][base.True[]]{'fear[###]$}])
          ?/1[immbase.Int[]]([[-mut-][base.ThenElse[imm base.Int[]]]{'fear[###]$
            .then/0([]):Sig[mdf=imm,gens=[],ts=[],ret=imm42[]]->[-imm-][42[]]{'fear[###]$},
            .else/0([]):Sig[mdf=mut,gens=[],ts=[],ret=imm0[]]->[-imm-][0[]]{'fear[###]$}}])}]}
    """, """
    package test
    alias base.Main as Main, alias base.Int as Int, alias base.False as False, alias base.True as True,
    Test:Main[Int]{
      _->False.or(True) ?[Int]{ .then: Int -> 42, .else -> 0 }
    }
    """, """
    package base
    Sealed:{}
    Main[R]:{ #(s: lent System): mdf R }
    System:{} // Root capability
    Bool:Sealed{
    .and(b: Bool): Bool,
    .or(b: Bool): Bool,
    .not: Bool,
    ?[R](f: mut ThenElse[R]): R, // ?  because `bool ? { .then->aa, .else->bb }` is kinda like a ternary
    }
    True:Bool{ .and(b) -> b, .or(b) -> this, .not -> False, ?(f) -> f.then() }
    False:Bool{ .and(b) -> this, .or(b) -> b, .not -> True, ?(f) -> f.else() }
    ThenElse[R]:{ mut .then: R, mut .else: R, }
    Int:{}
    _IntInstance:Int{}
    IntOpts:{}
    """); }
  @Test void boolUsageExplicitGensRTBasic3() { ok("""
    {test.Test/0=Dec[name=test.Test/0,gxs=[],lambda=[-mdf-][test.Test[],base.Main[immbase.Int[]]]{'this
      #/1([fear[###]$]):Sig[mdf=imm,gens=[],ts=[lentbase.System[]],ret=immbase.Int[]]->
        [-imm-][base.False[]]{'fear[###]$}
          .or/1[]([[-imm-][base.True[]]{'fear[###]$}])
          ?/1[immbase.Int[]]([[-mut-][base.ThenElse[immbase.Int[]]]{'fear[###]$
            .then/0([]):Sig[mdf=mut,gens=[],ts=[],ret=imm42[]]->[-imm-][42[]]{'fear[###]$},
            .else/0([]):Sig[mdf=imm,gens=[],ts=[],ret=imm0[]]->[-imm-][0[]]{'fear[###]$}}])}]}
    """, """
    package test
    alias base.Main as Main, alias base.Int as Int, alias base.False as False, alias base.True as True,
    Test:Main[Int]{
      _->False.or(True) ?[Int]{ .then -> 42, .else: Int -> 0 }
    }
    """, """
    package base
    Sealed:{}
    Main[R]:{ #(s: lent System): mdf R }
    System:{} // Root capability
    Bool:Sealed{
    .and(b: Bool): Bool,
    .or(b: Bool): Bool,
    .not: Bool,
    ?[R](f: mut ThenElse[R]): R, // ?  because `bool ? { .then->aa, .else->bb }` is kinda like a ternary
    }
    True:Bool{ .and(b) -> b, .or(b) -> this, .not -> False, ?(f) -> f.then() }
    False:Bool{ .and(b) -> this, .or(b) -> b, .not -> True, ?(f) -> f.else() }
    ThenElse[R]:{ mut .then: R, mut .else: R, }
    Int:{}
    _IntInstance:Int{}
    IntOpts:{}
    """); }

  @Test void factoryTrait() { ok("""
    {test.MyContainer/0=Dec[name=test.MyContainer/0,gxs=[],lambda=[-mdf-][test.MyContainer[]]{'this
      #/1([x]):Sig[mdf=imm,gens=[T],ts=[immT],ret=immtest.MyContainer[immT]]->
        [-imm-][test.MyContainer[immT]]{'fear[###]$.get/0([]):Sig[mdf=imm,gens=[],ts=[],ret=immT]->x}}],
    test.MyContainer/1=Dec[name=test.MyContainer/1,gxs=[T],lambda=[-mdf-][test.MyContainer[mdfT],base.NoMutHyg[immT]]{'this
      .get/0([]):Sig[mdf=imm,gens=[],ts=[],ret=immT]->[-]}]}
    """, """
    package test
    MyContainer[T]:base.NoMutHyg[T]{ .get: T }
    MyContainer:{
      #[T](x: T): MyContainer[T] -> { x }
    }
    """, """
    package base
    NoMutHyg[X]:{}
    """); }
  @Test void factoryTrait2() { ok("""
    {test.Opt/0=Dec[name=test.Opt/0,gxs=[],lambda=[-mdf-][test.Opt[]]{'this
      #/1([x]):Sig[mdf=imm,gens=[T],ts=[immT],ret=immtest.Opt[immT]]->[-imm-][test.Opt[immT]]{'fear[###]$}}],
    test.Opt/1=Dec[name=test.Opt/1,gxs=[T],lambda=[-mdf-][test.Opt[mdfT]]{'this}]}
    """, """
    package test
    Opt[T]:{}
    Opt:{ #[T](x: T): Opt[T] -> {} }
    """); }

  @Test void recMdfInSubHygMethGens() { ok("""
    {test.A/1=Dec[name=test.A/1,gxs=[X],lambda=[-mdf-][test.A[mdfX]]{'this
      .foo/1([x]):Sig[mdf=imm,gens=[],ts=[immX],ret=immX]->
        [-imm-][test.B[]]{'fear[###]$
          .argh/0([]):Sig[mdf=read,gens=[X1/0$],ts=[],ret=recMdfX1/0$]->x
          }.argh/0[immX]([])}],
    test.B/0=Dec[name=test.B/0,gxs=[],lambda=[-mdf-][test.B[]]{'this
      .argh/0([]):Sig[mdf=read,gens=[X],ts=[],ret=recMdfX]->[-]}]}
    """, """
    package test
    A[X]:{ .foo(x: X): X -> B{ x }.argh }
    B:{ read .argh[X]: recMdf X }
    """); }
  @Test void recMdfInSubHyg() { ok("""
    {test.A/1=Dec[name=test.A/1,gxs=[X],lambda=[-mdf-][test.A[mdfX]]{'this
      .foo/1([x]):Sig[mdf=imm,gens=[],ts=[immX],ret=immX]->
        [-imm-][test.B[immX]]{'fear[###]$
          .argh/0([]):Sig[mdf=read,gens=[],ts=[],ret=immX]->x}.argh/0[]([])}],
    test.B/1=Dec[name=test.B/1,gxs=[X],lambda=[-mdf-][test.B[mdfX]]{'this
      .argh/0([]):Sig[mdf=read,gens=[],ts=[],ret=recMdfX]->[-]}]}
    """, """
    package test
    A[X]:{ .foo(x: X): X -> B[X]{ x }.argh }
    B[X]:{ read .argh: recMdf X }
    """); }
  @Test void recMdfInSubHygMut() { ok("""
    {test.A/1=Dec[name=test.A/1,gxs=[X],lambda=[-mdf-][test.A[mdfX]]{'this
      .foo/1([x]):Sig[mdf=imm,gens=[],ts=[mutX],ret=mutX]->
        [-mut-][test.B[mutX]]{'fear[###]$
          .argh/0([]):Sig[mdf=read,gens=[],ts=[],ret=mut X]->x}.argh/0[]([])}],
    test.B/1=Dec[name=test.B/1,gxs=[X],lambda=[-mdf-][test.B[mdfX]]{'this
      .argh/0([]):Sig[mdf=read,gens=[],ts=[],ret=recMdfX]->[-]}]}
    """, """
    package test
    A[X]:{ .foo(x: mut X): mut X -> mut B[mut X]{ x }.argh }
    B[X]:{ read .argh: recMdf X }
    """); }
  @Test void inferRecMdf() { ok("""
    {test.FooBox/0=Dec[name=test.FooBox/0,gxs=[],lambda=[-mdf-][test.FooBox[],test.Box[immtest.Foo[]]]{'this
      .getFoo/0([]):Sig[mdf=read,gens=[],ts=[],ret=recMdftest.Foo[]]->this.get/0[]([])}],
    test.Foo/0=Dec[name=test.Foo/0,gxs=[],lambda=[-mdf-][test.Foo[]]{'this}],
    test.Box/1=Dec[name=test.Box/1,gxs=[T],lambda=[-mdf-][test.Box[mdfT]]{'this
      .get/0([]):Sig[mdf=read,gens=[],ts=[],ret=recMdfT]->[-]}]}
    """, """
    package test
    Foo:{}
    Box[T]:{ read .get: recMdf T }
    FooBox:Box[Foo]{ read .getFoo: recMdf Foo -> this.get }
    """); }
  @Test void doNotChangeExplicitLambdaMdf1() { ok("""
    {test.Bar/0=Dec[name=test.Bar/0,gxs=[],lambda=[-mdf-][test.Bar[]]{'this
      .a/0([]):Sig[mdf=imm,gens=[],ts=[],ret=imm test.Foo[]]->[-mut-][test.Foo[]]{'fear[###]$}}],
    test.Foo/0=Dec[name=test.Foo/0,gxs=[],lambda=[-mdf-][test.Foo[]]{'this}]}
    """, """
    package test
    Foo:{}
    Bar:{ .a: Foo -> mut Foo }
    """); }
  @Test void doNotChangeExplicitLambdaMdf2() { ok("""
    {test.Bar/0=Dec[name=test.Bar/0,gxs=[],lambda=[-mdf-][test.Bar[]]{'this
      .a/0([]):Sig[mdf=imm,gens=[],ts=[],ret=imm test.Foo[]]->[-recMdf-][test.Foo[]]{'fear[###]$}}],
    test.Foo/0=Dec[name=test.Foo/0,gxs=[],lambda=[-mdf-][test.Foo[]]{'this}]}
    """, """
    package test
    Foo:{}
    Bar:{ .a: Foo -> recMdf Foo }
    """); }

  @Test void numImpls1() { ok("""
    {test.Bar/0=Dec[name=test.Bar/0,gxs=[],lambda=[-mdf-][test.Bar[]]{'this
      .nm/1([n]):Sig[mdf=imm,gens=[],ts=[immbase.Int[]],ret=immbase.Int[]]->n,
      .check/0([]):Sig[mdf=imm,gens=[],ts=[],ret=immbase.Int[]]->
        this.nm/1[]([[-imm-][test.Foo[]]{'fear[###]$}.bar/0[]([])])}],
    test.Foo/0=Dec[name=test.Foo/0,gxs=[],lambda=[-mdf-][test.Foo[]]{'this
      .bar/0([]):Sig[mdf=imm,gens=[],ts=[],ret=imm5[]]->[-imm-][5[]]{'fear[###]$}}]}
    """, """
    package test
    alias base.Int as Int,
    Foo:{ .bar: 5 -> 5 }
    Bar:{
      .nm(n: Int): Int -> n,
      .check: Int -> this.nm(Foo.bar)
      }
    """, Base.load("lang.fear"), Base.load("caps/caps.fear"), Base.load("nums.fear"), Base.load("strings.fear"), Base.load("bools.fear"), Base.load("lists.fear"), Base.load("optionals.fear"));}

  @Test void numImpls2() { ok("""
    {test.Bar/0=Dec[name=test.Bar/0,gxs=[],lambda=[-mdf-][test.Bar[]]{'this
      .nm/1([n]):Sig[mdf=imm,gens=[],ts=[immbase.Int[]],ret=immbase.Int[]]->n,
      .check/0([]):Sig[mdf=imm,gens=[],ts=[],ret=immbase.Int[]]->
        this.nm/1[]([[-imm-][5[]]{'fear[###]$}])}]}
    """, """
    package test
    alias base.Int as Int,
    Bar:{
      .nm(n: Int): Int -> n,
      .check: Int -> this.nm(5)
      }
    """, Base.load("lang.fear"), Base.load("caps/caps.fear"), Base.load("nums.fear"), Base.load("strings.fear"), Base.load("bools.fear"), Base.load("lists.fear"), Base.load("optionals.fear"));}

  @Test void assertions() { ok("""
    {test.Assert/0=Dec[name=test.Assert/0,gxs=[],lambda=[-mdf-][test.Assert[]]{'this
      #/2([assertion,cont]):Sig[mdf=imm,gens=[R],ts=[immbase.Bool[],muttest.AssertCont[mdfR]],ret=mdfR]->
        assertion?/1[mdfR]([[-mut-][base.ThenElse[mdf R]]{'fear[###]$
          .then/0([]):Sig[mdf=mut,gens=[],ts=[],ret=mdf R]->cont#/0[]([]),
          .else/0([]):Sig[mdf=mut,gens=[],ts=[],ret=mdfR]->this._fail/0[mdfR]([])}]),
      #/3([assertion,msg,cont]):Sig[mdf=imm,gens=[R],ts=[immbase.Bool[],immbase.Str[],muttest.AssertCont[mdfR]],ret=mdfR]->
        assertion?/1[mdfR]([[-mut-][base.ThenElse[mdf R]]{'fear[###]$
          .then/0([]):Sig[mdf=mut,gens=[],ts=[],ret=mdf R]->cont#/0[]([]),
          .else/0([]):Sig[mdf=mut,gens=[],ts=[],ret=mdfR]->this._fail/1[mdfR]([msg])}]),
      ._fail/0([]):Sig[mdf=imm,gens=[R],ts=[],ret=mdfR]->this._fail/0[mdfR]([]),
      ._fail/1([msg]):Sig[mdf=imm,gens=[R],ts=[immbase.Str[]],ret=mdfR]->this._fail/1[mdfR]([msg])}],
    test.AssertCont/1=Dec[name=test.AssertCont/1,gxs=[R],lambda=[-mdf-][test.AssertCont[mdfR]]{'this
      #/0([]):Sig[mdf=mut,gens=[],ts=[],ret=mdfR]->[-]}]}
    """, """
    package test
    alias base.Bool as Bool, alias base.Str as Str,
    Assert:{
      #[R](assertion: Bool, cont: mut AssertCont[mdf R]): mdf R -> assertion ? {
        .then -> cont#,
        .else -> this._fail[mdf R]()
        },
      #[R](assertion: Bool, msg: Str, cont: mut AssertCont[mdf R]): mdf R -> assertion ? {
        .then -> cont#,
        .else -> this._fail[mdf R](msg)
        },
      ._fail[R]: mdf R -> this._fail,
      ._fail[R](msg: Str): mdf R -> this._fail(msg),
      }
    AssertCont[R]:{ mut #: mdf R }
    """, """
    package base
    Sealed:{} Str:{}
    Bool:Sealed{
      .and(b: Bool): Bool,
      .or(b: Bool): Bool,
      .not: Bool,
      ?[R](f: mut ThenElse[mdf R]): mdf R, // ?  because `bool ? { .then->aa, .else->bb }` is kinda like a ternary
      }
    True:Bool{ .and(b) -> b, .or(b) -> this, .not -> False, ?(f) -> f.then() }
    False:Bool{ .and(b) -> this, .or(b) -> b, .not -> True, ?(f) -> f.else() }
    ThenElse[R]:{ mut .then: mdf R, mut .else: mdf R, }
    """); }

  @Test void sugar1() { ok("""
    {test.Cont/2=Dec[name=test.Cont/2,gxs=[X,R],lambda=[-mdf-][test.Cont[mdfX,mdfR]]{'this
      #/2([x,cont]):Sig[mdf=mut,gens=[],ts=[mdfX,muttest.Candy[mdfR]],ret=mdfR]->[-]}],
    test.ReturnStmt/1=Dec[name=test.ReturnStmt/1,gxs=[R],lambda=[-mdf-][test.ReturnStmt[mdfR]]{'this
      #/0([]):Sig[mdf=mut,gens=[],ts=[],ret=mdfR]->[-]}],
    test.Candy/1=Dec[name=test.Candy/1,gxs=[R],lambda=[-mdf-][test.Candy[mdfR]]{'this
      .sugar/2([x,cont]):Sig[mdf=mut,gens=[X],ts=[mdfX,muttest.Cont[mdfX,mdfR]],ret=mdfR]->cont#/2[]([x,this]),
      .return/1([a]):Sig[mdf=mut,gens=[],ts=[muttest.ReturnStmt[mdfR]],ret=mdfR]->a#/0[]([])}],
    test.Usage/0=Dec[name=test.Usage/0,gxs=[],lambda=[-mdf-][test.Usage[]]{'this
      .foo/0([]):Sig[mdf=imm,gens=[],ts=[],ret=immtest.Void[]]->
        [-imm-][test.Candy[immtest.Void[]]]{'fear[###]$}
          .sugar/2[immtest.Foo[]]([
            [-imm-][test.Foo[]]{'fear[###]$},
            [-mut-][test.Cont[immtest.Foo[],immtest.Void[]]]{'fear[###]$
              #/2([f,fear[###]$]):Sig[mdf=mut,gens=[],ts=[immtest.Foo[],muttest.Candy[immtest.Void[]]],ret=immtest.Void[]]->
                fear[###]$.return/1[]([[-mut-][test.ReturnStmt[immtest.Void[]]]{'fear[###]$#/0([]):Sig[mdf=mut,gens=[],ts=[],ret=immtest.Void[]]->f.v/0[]([])}])}])}],
    test.Foo/0=Dec[name=test.Foo/0,gxs=[],lambda=[-mdf-][test.Foo[]]{'this.v/0([]):Sig[mdf=imm,gens=[],ts=[],ret=immtest.Void[]]->[-imm-][test.Void[]]{'fear[###]$}}],
    test.Void/0=Dec[name=test.Void/0,gxs=[],lambda=[-mdf-][test.Void[]]{'this}]}
    """, """
    package test
    Void:{}
    Foo:{ .v: Void -> {} }
    Cont[X,R]:{ mut #(x: mdf X, cont: mut Candy[mdf R]): mdf R }
    ReturnStmt[R]:{ mut #: mdf R }
    Candy[R]:{
      mut .sugar[X](x: mdf X, cont: mut Cont[mdf X, mdf R]): mdf R -> cont#(x, this),
      mut .return(a: mut ReturnStmt[mdf R]): mdf R -> a#,
      }
    Usage:{
      .foo: Void -> Candy[Void]
        .sugar[Foo] f = Foo
        .return{ f.v }
      }
    """); }
  @Test void sugar1InferSugar() { ok("""
    {test.Cont/2=Dec[name=test.Cont/2,gxs=[X,R],lambda=[-mdf-][test.Cont[mdfX,mdfR]]{'this
      #/2([x,cont]):Sig[mdf=mut,gens=[],ts=[mdfX,muttest.Candy[mdfR]],ret=mdfR]->[-]}],
    test.ReturnStmt/1=Dec[name=test.ReturnStmt/1,gxs=[R],lambda=[-mdf-][test.ReturnStmt[mdfR]]{'this
      #/0([]):Sig[mdf=mut,gens=[],ts=[],ret=mdfR]->[-]}],
    test.Candy/1=Dec[name=test.Candy/1,gxs=[R],lambda=[-mdf-][test.Candy[mdfR]]{'this
      .sugar/2([x,cont]):Sig[mdf=mut,gens=[X],ts=[mdfX,muttest.Cont[mdfX,mdfR]],ret=mdfR]->cont#/2[]([x,this]),
      .return/1([a]):Sig[mdf=mut,gens=[],ts=[muttest.ReturnStmt[mdfR]],ret=mdfR]->a#/0[]([])}],
    test.Usage/0=Dec[name=test.Usage/0,gxs=[],lambda=[-mdf-][test.Usage[]]{'this
      .foo/0([]):Sig[mdf=imm,gens=[],ts=[],ret=immtest.Void[]]->
        [-imm-][test.Candy[immtest.Void[]]]{'fear[###]$}
          .sugar/2[immtest.Foo[]]([
            [-imm-][test.Foo[]]{'fear[###]$},
            [-mut-][test.Cont[immtest.Foo[],immtest.Void[]]]{'fear[###]$
              #/2([f,fear[###]$]):Sig[mdf=mut,gens=[],ts=[immtest.Foo[],muttest.Candy[immtest.Void[]]],ret=immtest.Void[]]->
                fear[###]$.return/1[]([[-mut-][test.ReturnStmt[immtest.Void[]]]{'fear[###]$#/0([]):Sig[mdf=mut,gens=[],ts=[],ret=immtest.Void[]]->f.v/0[]([])}])}])}],
    test.Foo/0=Dec[name=test.Foo/0,gxs=[],lambda=[-mdf-][test.Foo[]]{'this.v/0([]):Sig[mdf=imm,gens=[],ts=[],ret=immtest.Void[]]->[-imm-][test.Void[]]{'fear[###]$}}],
    test.Void/0=Dec[name=test.Void/0,gxs=[],lambda=[-mdf-][test.Void[]]{'this}]}
    """, """
    package test
    Void:{}
    Foo:{ .v: Void -> {} }
    Cont[X,R]:{ mut #(x: mdf X, cont: mut Candy[mdf R]): mdf R }
    ReturnStmt[R]:{ mut #: mdf R }
    Candy[R]:{
      mut .sugar[X](x: mdf X, cont: mut Cont[mdf X, mdf R]): mdf R -> cont#(x, this),
      mut .return(a: mut ReturnStmt[mdf R]): mdf R -> a#,
      }
    Usage:{
      .foo: Void -> Candy[Void]
        .sugar f = Foo
        .return{ f.v }
      }
    """); }
  @Test void sugar1MismatchedXs() { ok("""
    {test.Cont/2=Dec[name=test.Cont/2,gxs=[X,R],lambda=[-mdf-][test.Cont[mdfX,mdfR]]{'this
      #/2([y,cont]):Sig[mdf=mut,gens=[],ts=[mdfX,muttest.Candy[mdfR]],ret=mdfR]->[-]}],
    test.ReturnStmt/1=Dec[name=test.ReturnStmt/1,gxs=[R],lambda=[-mdf-][test.ReturnStmt[mdfR]]{'this
      #/0([]):Sig[mdf=mut,gens=[],ts=[],ret=mdfR]->[-]}],
    test.Candy/1=Dec[name=test.Candy/1,gxs=[R],lambda=[-mdf-][test.Candy[mdfR]]{'this
      .sugar/2([z,cont]):Sig[mdf=mut,gens=[X],ts=[mdfX,muttest.Cont[mdfX,mdfR]],ret=mdfR]->cont#/2[]([z,this]),
      .return/1([a]):Sig[mdf=mut,gens=[],ts=[muttest.ReturnStmt[mdfR]],ret=mdfR]->a#/0[]([])}],
    test.Usage/0=Dec[name=test.Usage/0,gxs=[],lambda=[-mdf-][test.Usage[]]{'this
      .foo/0([]):Sig[mdf=imm,gens=[],ts=[],ret=immtest.Void[]]->
        [-imm-][test.Candy[immtest.Void[]]]{'fear[###]$}
          .sugar/2[immtest.Foo[]]([
            [-imm-][test.Foo[]]{'fear[###]$},
            [-mut-][test.Cont[immtest.Foo[],immtest.Void[]]]{'fear[###]$
              #/2([f,fear[###]$]):Sig[mdf=mut,gens=[],ts=[immtest.Foo[],muttest.Candy[immtest.Void[]]],ret=immtest.Void[]]->
                fear[###]$.return/1[]([[-mut-][test.ReturnStmt[immtest.Void[]]]{'fear[###]$#/0([]):Sig[mdf=mut,gens=[],ts=[],ret=immtest.Void[]]->f.v/0[]([])}])}])}],
    test.Foo/0=Dec[name=test.Foo/0,gxs=[],lambda=[-mdf-][test.Foo[]]{'this.v/0([]):Sig[mdf=imm,gens=[],ts=[],ret=immtest.Void[]]->[-imm-][test.Void[]]{'fear[###]$}}],
    test.Void/0=Dec[name=test.Void/0,gxs=[],lambda=[-mdf-][test.Void[]]{'this}]}
    """, """
    package test
    Void:{}
    Foo:{ .v: Void -> {} }
    Cont[X,R]:{ mut #(y: mdf X, cont: mut Candy[mdf R]): mdf R }
    ReturnStmt[R]:{ mut #: mdf R }
    Candy[R]:{
      mut .sugar[X](z: mdf X, cont: mut Cont[mdf X, mdf R]): mdf R -> cont#(z, this),
      mut .return(a: mut ReturnStmt[mdf R]): mdf R -> a#,
      }
    Usage:{
      .foo: Void -> Candy[Void]
        .sugar[Foo] f = Foo
        .return{ f.v }
      }
    """); }
  @Test void sugar1Brackets() { ok("""
    {test.Cont/2=Dec[name=test.Cont/2,gxs=[X,R],lambda=[-mdf-][test.Cont[mdfX,mdfR]]{'this
      #/2([x,cont]):Sig[mdf=mut,gens=[],ts=[mdfX,muttest.Candy[mdfR]],ret=mdfR]->[-]}],
    test.ReturnStmt/1=Dec[name=test.ReturnStmt/1,gxs=[R],lambda=[-mdf-][test.ReturnStmt[mdfR]]{'this
      #/0([]):Sig[mdf=mut,gens=[],ts=[],ret=mdfR]->[-]}],
    test.Candy/1=Dec[name=test.Candy/1,gxs=[R],lambda=[-mdf-][test.Candy[mdfR]]{'this
      .sugar/2([x,cont]):Sig[mdf=mut,gens=[X],ts=[mdfX,muttest.Cont[mdfX,mdfR]],ret=mdfR]->cont#/2[]([x,this]),
      .return/1([a]):Sig[mdf=mut,gens=[],ts=[muttest.ReturnStmt[mdfR]],ret=mdfR]->a#/0[]([])}],
    test.Usage/0=Dec[name=test.Usage/0,gxs=[],lambda=[-mdf-][test.Usage[]]{'this
      .foo/0([]):Sig[mdf=imm,gens=[],ts=[],ret=immtest.Void[]]->
        [-imm-][test.Candy[immtest.Void[]]]{'fear[###]$}
          .sugar/2[immtest.Foo[]]([
            [-imm-][test.Foo[]]{'fear[###]$},
            [-mut-][test.Cont[immtest.Foo[],immtest.Void[]]]{'fear[###]$
              #/2([f,fear[###]$]):Sig[mdf=mut,gens=[],ts=[immtest.Foo[],muttest.Candy[immtest.Void[]]],ret=immtest.Void[]]->
                fear[###]$.return/1[]([[-mut-][test.ReturnStmt[immtest.Void[]]]{'fear[###]$#/0([]):Sig[mdf=mut,gens=[],ts=[],ret=immtest.Void[]]->f.v/0[]([])}])}])}],
    test.Foo/0=Dec[name=test.Foo/0,gxs=[],lambda=[-mdf-][test.Foo[]]{'this.v/0([]):Sig[mdf=imm,gens=[],ts=[],ret=immtest.Void[]]->[-imm-][test.Void[]]{'fear[###]$}}],
    test.Void/0=Dec[name=test.Void/0,gxs=[],lambda=[-mdf-][test.Void[]]{'this}]}
    """, """
    package test
    Void:{}
    Foo:{ .v: Void -> {} }
    Cont[X,R]:{ mut #(x: mdf X, cont: mut Candy[mdf R]): mdf R }
    ReturnStmt[R]:{ mut #: mdf R }
    Candy[R]:{
      mut .sugar[X](x: mdf X, cont: mut Cont[mdf X, mdf R]): mdf R -> cont#(x, this),
      mut .return(a: mut ReturnStmt[mdf R]): mdf R -> a#,
      }
    Usage:{
      .foo: Void -> Candy[Void]
        .sugar[Foo](f = Foo)
        .return{ f.v }
      }
    """); }
  @Test void sugar1InferSugarComplex() { ok("""
    {test.Cont/2=Dec[name=test.Cont/2,gxs=[X,R],lambda=[-mdf-][test.Cont[mdfX,mdfR]]{'this
      #/2([x,cont]):Sig[mdf=mut,gens=[],ts=[mdfX,muttest.Candy[mdfR]],ret=mdfR]->[-]}],
    test.ReturnStmt/1=Dec[name=test.ReturnStmt/1,gxs=[R],lambda=[-mdf-][test.ReturnStmt[mdfR]]{'this
      #/0([]):Sig[mdf=mut,gens=[],ts=[],ret=mdfR]->[-]}],
    test.Candy/1=Dec[name=test.Candy/1,gxs=[R],lambda=[-mdf-][test.Candy[mdfR]]{'this
      .sugar/2([x,cont]):Sig[mdf=mut,gens=[X],ts=[mdfX,muttest.Cont[mdfX,mdfR]],ret=mdfR]->cont#/2[]([x,this]),
      .return/1([a]):Sig[mdf=mut,gens=[],ts=[muttest.ReturnStmt[mdfR]],ret=mdfR]->a#/0[]([])}],
    test.Usage/0=Dec[name=test.Usage/0,gxs=[],lambda=[-mdf-][test.Usage[]]{'this
      .foo/0([]):Sig[mdf=imm,gens=[],ts=[],ret=immtest.Void[]]->
        [-imm-][test.Candy[immtest.Void[]]]{'fear[###]$}
          .sugar/2[immtest.Foo[]]([
            [-imm-][test.Foo'[]]{'fear[###]$}#/0[]([]),
            [-mut-][test.Cont[immtest.Foo[],immtest.Void[]]]{'fear[###]$
              #/2([f,fear[###]$]):Sig[mdf=mut,gens=[],ts=[immtest.Foo[],muttest.Candy[immtest.Void[]]],ret=immtest.Void[]]->
                fear[###]$.return/1[]([[-mut-][test.ReturnStmt[immtest.Void[]]]{'fear[###]$#/0([]):Sig[mdf=mut,gens=[],ts=[],ret=immtest.Void[]]->f.v/0[]([])}])}])}],
    test.Foo/0=Dec[name=test.Foo/0,gxs=[],lambda=[-mdf-][test.Foo[]]{'this.v/0([]):Sig[mdf=imm,gens=[],ts=[],ret=immtest.Void[]]->[-imm-][test.Void[]]{'fear[###]$}}],
    test.New/1=Dec[name=test.New/1,gxs=[R],lambda=[-mdf-][test.New[mdfR]]{'this
      #/0([]):Sig[mdf=imm,gens=[],ts=[],ret=immR]->[-]}],
    test.Foo'/0=Dec[name=test.Foo'/0,gxs=[],lambda=[-mdf-][test.Foo'[],test.New[immtest.Foo[]]]{'this
      #/0([]):Sig[mdf=imm,gens=[],ts=[],ret=immtest.Foo[]]->[-imm-][test.Foo[]]{'fear[###]$}}],
    test.Void/0=Dec[name=test.Void/0,gxs=[],lambda=[-mdf-][test.Void[]]{'this}]}
    """, """
    package test
    Void:{}
    New[R]:{ #: R }
    Foo:{ .v: Void -> {} }
    Foo':New[Foo]{ Foo }
    Cont[X,R]:{ mut #(x: mdf X, cont: mut Candy[mdf R]): mdf R }
    ReturnStmt[R]:{ mut #: mdf R }
    Candy[R]:{
      mut .sugar[X](x: mdf X, cont: mut Cont[mdf X, mdf R]): mdf R -> cont#(x, this),
      mut .return(a: mut ReturnStmt[mdf R]): mdf R -> a#,
      }
    Usage:{
      .foo: Void -> Candy[Void]
        .sugar(f = Foo'#)
        .return{ f.v }
      }
    """); }
  @Test void sugar1InferSugarComplex2() { ok("""
    {test.Cont/2=Dec[name=test.Cont/2,gxs=[X,R],lambda=[-mdf-][test.Cont[mdfX,mdfR]]{'this
      #/2([x,cont]):Sig[mdf=mut,gens=[],ts=[mdfX,muttest.Candy[mdfR]],ret=mdfR]->[-]}],
    test.ReturnStmt/1=Dec[name=test.ReturnStmt/1,gxs=[R],lambda=[-mdf-][test.ReturnStmt[mdfR]]{'this
      #/0([]):Sig[mdf=mut,gens=[],ts=[],ret=mdfR]->[-]}],
    test.Foo/1=Dec[name=test.Foo/1,gxs=[X],lambda=[-mdf-][test.Foo[mdfX]]{'this
      .v/0([]):Sig[mdf=imm,gens=[],ts=[],ret=immtest.Void[]]->[-imm-][test.Void[]]{'fear[###]$}}],
    test.Candy/1=Dec[name=test.Candy/1,gxs=[R],lambda=[-mdf-][test.Candy[mdfR]]{'this
      .sugar/2([x,cont]):Sig[mdf=mut,gens=[X],ts=[mdfX,muttest.Cont[mdfX,mdfR]],ret=mdfR]->cont#/2[]([x,this]),
      .return/1([a]):Sig[mdf=mut,gens=[],ts=[muttest.ReturnStmt[mdfR]],ret=mdfR]->a#/0[]([])}],
    test.Usage/0=Dec[name=test.Usage/0,gxs=[],lambda=[-mdf-][test.Usage[]]{'this
      .foo/0([]):Sig[mdf=imm,gens=[],ts=[],ret=immtest.Void[]]->
        [-imm-][test.Candy[immtest.Void[]]]{'fear[###]$}
          .sugar/2[immtest.Foo[imm test.Void[]]]([
            [-imm-][test.Foo'[]]{'fear[###]$}#/1[imm test.Void[]]([[-imm-][test.Void[]]{'fear[###]$}]),
            [-mut-][test.Cont[immtest.Foo[immtest.Void[]],immtest.Void[]]]{'fear[###]$
              #/2([f,fear[###]$]):Sig[mdf=mut,gens=[],ts=[immtest.Foo[immtest.Void[]],muttest.Candy[immtest.Void[]]],ret=immtest.Void[]]->
                fear[###]$.return/1[]([[-mut-][test.ReturnStmt[immtest.Void[]]]{'fear[###]$#/0([]):Sig[mdf=mut,gens=[],ts=[],ret=immtest.Void[]]->f.v/0[]([])}])}])}],
    test.Foo'/0=Dec[name=test.Foo'/0,gxs=[],lambda=[-mdf-][test.Foo'[]]{'this
      #/1([x]):Sig[mdf=imm,gens=[X],ts=[mdfX],ret=immtest.Foo[mdfX]]->[-imm-][test.Foo[mdfX]]{'fear[###]$}}],
    test.Void/0=Dec[name=test.Void/0,gxs=[],lambda=[-mdf-][test.Void[]]{'this}]}
    """, """
    package test
    Void:{}
    Foo[X]:{ .v: Void -> {} }
    Foo':{ #[X](x: mdf X): Foo[mdf X] -> {} }
    Cont[X,R]:{ mut #(x: mdf X, cont: mut Candy[mdf R]): mdf R }
    ReturnStmt[R]:{ mut #: mdf R }
    Candy[R]:{
      mut .sugar[X](x: mdf X, cont: mut Cont[mdf X, mdf R]): mdf R -> cont#(x, this),
      mut .return(a: mut ReturnStmt[mdf R]): mdf R -> a#,
      }
    Usage:{
      .foo: Void -> Candy[Void]
        .sugar(f = Foo'#Void)
        .return{ f.v }
      }
    """); }
  @Test void sugar1InferSugarComplex3() { ok("""
    {test.Cont/2=Dec[name=test.Cont/2,gxs=[X,R],lambda=[-mdf-][test.Cont[mdfX,mdfR]]{'this
      #/2([x,cont]):Sig[mdf=mut,gens=[],ts=[mdfX,muttest.Candy[mdfR]],ret=mdfR]->[-]}],
    test.ReturnStmt/1=Dec[name=test.ReturnStmt/1,gxs=[R],lambda=[-mdf-][test.ReturnStmt[mdfR]]{'this
      #/0([]):Sig[mdf=mut,gens=[],ts=[],ret=mdfR]->[-]}],
    test.Foo/1=Dec[name=test.Foo/1,gxs=[X],lambda=[-mdf-][test.Foo[mdfX]]{'this
      .v/0([]):Sig[mdf=imm,gens=[],ts=[],ret=immtest.Void[]]->[-imm-][test.Void[]]{'fear[###]$}}],
    test.Candy/1=Dec[name=test.Candy/1,gxs=[R],lambda=[-mdf-][test.Candy[mdfR]]{'this
      .sugar/2([x,cont]):Sig[mdf=mut,gens=[X],ts=[muttest.ReturnStmt[mdfX],muttest.Cont[mdfX,mdfR]],ret=mdfR]->cont#/2[]([x#/0[]([]),this]),
      .return/1([a]):Sig[mdf=mut,gens=[],ts=[muttest.ReturnStmt[mdfR]],ret=mdfR]->a#/0[]([])}],
    test.Usage/0=Dec[name=test.Usage/0,gxs=[],lambda=[-mdf-][test.Usage[]]{'this
      .foo/0([]):Sig[mdf=imm,gens=[],ts=[],ret=immtest.Void[]]->
        [-imm-][test.Candy[immtest.Void[]]]{'fear[###]$}
          .sugar/2[immtest.Foo[imm test.Void[]]]([
            [-mut-][test.ReturnStmt[immtest.Foo[immtest.Void[]]]]{'fear[###]$
              #/0([]):Sig[mdf=mut,gens=[],ts=[],ret=immtest.Foo[immtest.Void[]]]->
                [-imm-][test.Foo'[]]{'fear[###]$}#/1[immtest.Void[]]([[-imm-][test.Void[]]{'fear[###]$}])},
            [-mut-][test.Cont[immtest.Foo[immtest.Void[]],immtest.Void[]]]{'fear[###]$
              #/2([f,fear[###]$]):Sig[mdf=mut,gens=[],ts=[immtest.Foo[immtest.Void[]],muttest.Candy[immtest.Void[]]],ret=immtest.Void[]]->
                fear[###]$.return/1[]([[-mut-][test.ReturnStmt[immtest.Void[]]]{'fear[###]$#/0([]):Sig[mdf=mut,gens=[],ts=[],ret=immtest.Void[]]->f.v/0[]([])}])}])}],
    test.Foo'/0=Dec[name=test.Foo'/0,gxs=[],lambda=[-mdf-][test.Foo'[]]{'this
      #/1([x]):Sig[mdf=imm,gens=[X],ts=[mdfX],ret=immtest.Foo[mdfX]]->[-imm-][test.Foo[mdfX]]{'fear[###]$}}],
    test.Void/0=Dec[name=test.Void/0,gxs=[],lambda=[-mdf-][test.Void[]]{'this}]}
    """, """
    package test
    Void:{}
    Foo[X]:{ .v: Void -> {} }
    Foo':{ #[X](x: mdf X): Foo[mdf X] -> {} }
    Cont[X,R]:{ mut #(x: mdf X, cont: mut Candy[mdf R]): mdf R }
    ReturnStmt[R]:{ mut #: mdf R }
    Candy[R]:{
      mut .sugar[X](x: mut ReturnStmt[mdf X], cont: mut Cont[mdf X, mdf R]): mdf R -> cont#(x#, this),
      mut .return(a: mut ReturnStmt[mdf R]): mdf R -> a#,
      }
    Usage:{
      .foo: Void -> Candy[Void]
        .sugar(f = { Foo'#Void })
        .return{ f.v }
      }
    """); }
  @Test void sugar1InferSugarComplexNo() { ok("""
    {test.Cont/2=Dec[name=test.Cont/2,gxs=[X,R],lambda=[-mdf-][test.Cont[mdfX,mdfR]]{'this
      #/2([x,cont]):Sig[mdf=mut,gens=[],ts=[mdfX,muttest.Candy[mdfR]],ret=mdfR]->[-]}],
    test.ReturnStmt/1=Dec[name=test.ReturnStmt/1,gxs=[R],lambda=[-mdf-][test.ReturnStmt[mdfR]]{'this
      #/0([]):Sig[mdf=mut,gens=[],ts=[],ret=mdfR]->[-]}],
    test.Foo/1=Dec[name=test.Foo/1,gxs=[X],lambda=[-mdf-][test.Foo[mdfX]]{'this
      .v/0([]):Sig[mdf=imm,gens=[],ts=[],ret=immtest.Void[]]->[-imm-][test.Void[]]{'fear[###]$}}],
    test.Candy/1=Dec[name=test.Candy/1,gxs=[R],lambda=[-mdf-][test.Candy[mdfR]]{'this
      .sugar/2([x,cont]):Sig[mdf=mut,gens=[X],ts=[muttest.ReturnStmt[mdfX],muttest.Cont[mdfX,mdfR]],ret=mdfR]->cont#/2[]([x#/0[]([]),this]),
      .return/1([a]):Sig[mdf=mut,gens=[],ts=[muttest.ReturnStmt[mdfR]],ret=mdfR]->a#/0[]([])}],
    test.Usage/0=Dec[name=test.Usage/0,gxs=[],lambda=[-mdf-][test.Usage[]]{'this
      .foo/0([]):Sig[mdf=imm,gens=[],ts=[],ret=immtest.Void[]]->
        [-imm-][test.Candy[immtest.Void[]]]{'fear[###]$}
          .sugar/2[immtest.Foo[imm test.Void[]]]([
            [-mut-][test.ReturnStmt[immtest.Foo[immtest.Void[]]]]{'fear[###]$
              #/0([]):Sig[mdf=mut,gens=[],ts=[],ret=immtest.Foo[immtest.Void[]]]->
                [-imm-][test.Foo'[]]{'fear[###]$}#/1[immtest.Void[]]([[-imm-][test.Void[]]{'fear[###]$}])},
            [-mut-][test.Cont[immtest.Foo[immtest.Void[]],immtest.Void[]]]{'fear[###]$
              #/2([f,fear[###]$]):Sig[mdf=mut,gens=[],ts=[immtest.Foo[immtest.Void[]],muttest.Candy[immtest.Void[]]],ret=immtest.Void[]]->
                fear[###]$.return/1[]([[-mut-][test.ReturnStmt[immtest.Void[]]]{'fear[###]$#/0([]):Sig[mdf=mut,gens=[],ts=[],ret=immtest.Void[]]->f.v/0[]([])}])}])}],
    test.Foo'/0=Dec[name=test.Foo'/0,gxs=[],lambda=[-mdf-][test.Foo'[]]{'this
      #/1([x]):Sig[mdf=imm,gens=[X],ts=[mdfX],ret=immtest.Foo[mdfX]]->[-imm-][test.Foo[mdfX]]{'fear[###]$}}],
    test.Void/0=Dec[name=test.Void/0,gxs=[],lambda=[-mdf-][test.Void[]]{'this}]}
    """, """
    package test
    Void:{}
    Foo[X]:{ .v: Void -> {} }
    Foo':{ #[X](x: mdf X): Foo[mdf X] -> {} }
    Cont[X,R]:{ mut #(x: mdf X, cont: mut Candy[mdf R]): mdf R }
    ReturnStmt[R]:{ mut #: mdf R }
    Candy[R]:{
      mut .sugar[X](x: mut ReturnStmt[mdf X], cont: mut Cont[mdf X, mdf R]): mdf R -> cont#(x#, this),
      mut .return(a: mut ReturnStmt[mdf R]): mdf R -> a#,
      }
    Usage:{
      .foo: Void -> Candy[Void]
        .sugar(f = { Foo'#Void })
        .return{ f.v }
      }
    """); }

  @Disabled
  @Test void sugar1InferSugarIO() { ok("""
    """, """
    package test
    Usage:{
      .foo: Void -> System[Void]
        .use(io = IO')
        .return{ io.println(Str{}) }
      }
    """, """
    package test
    Str:{} Void:{}
    LentReturnStmt[R]:{ lent #: mdf R }
    System[R]:{
      lent .use[C](c: CapFactory[lent _RootCap, lent C], cont: mut UseCapCont[C, mdf R]): mdf R ->
        cont#(c#_RootCap, this),
      lent .return(ret: lent LentReturnStmt[mdf R]): mdf R -> ret#
      }
    _RootCap:IO{
      .print(msg) -> this.print(msg),
      .println(msg) -> this.println(msg),
      }
    UseCapCont[C, R]:{ mut #(cap: lent C, self: lent System[mdf R]): mdf R }
    CapFactory[C,R]:{
      #(auth: lent C): lent R,
      .close(c: lent R): Void,
      }
    IO:{
      lent .print(msg: Str): Void,
      lent .println(msg: Str): Void,
      }
    IO':CapFactory[lent _RootCap, lent IO]{
      #(auth: lent _RootCap): lent IO -> this.scope(auth),
      .scope(auth: lent IO): lent IO -> auth,
      .close(c: lent IO): Void -> {},
      }
    """); }

  @Test void mdfMismatch() { ok("""
    {test.Box/1=Dec[name=test.Box/1,gxs=[X],lambda=[-mdf-][test.Box[mdfX],base.NoMutHyg[immX]]{'this
      .get/0([]):Sig[mdf=read,gens=[],ts=[],ret=recMdfX]->[-]}],
    test.Test/0=Dec[name=test.Test/0,gxs=[],lambda=[-mdf-][test.Test[]]{'this
      #/1([t]):Sig[mdf=imm,gens=[],ts=[readtest.Test[]],ret=lenttest.Box[readtest.Test[]]]->
        [-imm-][test.Box'[]]{'fear[###]$}.mut2lent/1[readtest.Test[]]([t])}],
    test.Box'/0=Dec[name=test.Box'/0,gxs=[],lambda=[-mdf-][test.Box'[]]{'this
      .mut2lent/1([x]):Sig[mdf=imm,gens=[X],ts=[mdfX],ret=muttest.Box[mdfX]]->
        [-mut-][test.Box[mdfX]]{'fear[###]$.get/0([]):Sig[mdf=read,gens=[],ts=[],ret=recMdfX]->x}}]}
    """, """
    package test
    alias base.NoMutHyg as NoMutHyg,
    Box[X]:NoMutHyg[X]{
      read .get: recMdf X
      }
    Box':{
      .mut2lent[X](x: mdf X): mut Box[mdf X] -> { x }
      }
    Test:{
      #(t: read Test): lent Box[read Test] -> Box'.mut2lent(t),
      }
    """, """
    package base
    NoMutHyg[X]:{}
    """); }

  @Test void immToReadCapture() { ok("""
    {test.L/1=Dec[name=test.L/1,gxs=[X],lambda=[-mdf-][test.L[mdfX]]{'this
      .absMeth/0([]):Sig[mdf=imm,gens=[],ts=[],ret=readX]->[-]}],
    test.B/0=Dec[name=test.B/0,gxs=[],lambda=[-mdf-][test.B[]]{'this}],
    test.A/0=Dec[name=test.A/0,gxs=[],lambda=[-mdf-][test.A[]]{'this
      .m/1([par]):Sig[mdf=read,gens=[T],ts=[immT],ret=readtest.L[immT]]->
        [-read-][test.L[imm T]]{'fear[###]$.absMeth/0([]):Sig[mdf=imm,gens=[],ts=[],ret=readT]->par}}]}
    """, """
    package test
    B:{}
    L[X]:{ imm .absMeth: read X }
    A:{ read .m[T](par: imm T) : read L[imm T] -> read L[imm T]{.absMeth->par} }
    """); }

  @Test void dontOverrideMdf() { ok("""
    {test.Box/1=Dec[name=test.Box/1,gxs=[X],lambda=[-mdf-][test.Box[mdfX],base.NoMutHyg[mdfX]]{'this
      .get/0([]):Sig[mdf=read,gens=[],ts=[],ret=recMdfX]->[-]}],
    test.Box'/0=Dec[name=test.Box'/0,gxs=[],lambda=[-mdf-][test.Box'[]]{'this
      .mut2lent/1([x]):Sig[mdf=imm,gens=[X],ts=[mdfX],ret=muttest.Box[mdfX]]->
        [-mut-][test.Box[mdf X]]{'fear[###]$
          .get/0([]):Sig[mdf=read,gens=[],ts=[],ret=recMdfX]->x}}]}
    """, """
    package test
    alias base.NoMutHyg as NoMutHyg,
    Box[X]:NoMutHyg[mdf X]{
      read .get: recMdf X
      }
    Box':{
      .mut2lent[X](x: mdf X): mut Box[mdf X] -> mut Box[mdf X]{ x }
      }
    """, """
    package base
    NoMutHyg[X]:{}
    """); }

  @Test void nestedRecMdfExplicitMdf() { ok("""
    {test.F/1=Dec[name=test.F/1,gxs=[X],lambda=[-mdf-][test.F[mdfX]]{'this#/1([x]):Sig[mdf=imm,gens=[],ts=[mdfX],ret=mdfX]->x}],
    test.A/1=Dec[name=test.A/1,gxs=[X],lambda=[-mdf-][test.A[mdfX]]{'this
      .m1/2([a,b]):Sig[mdf=read,gens=[],ts=[recMdfX,immtest.F[recMdfX]],ret=recMdfX]->b#/1[]([a])}],
    test.B/1=Dec[name=test.B/1,gxs=[Y],lambda=[-mdf-][test.B[mdfY]]{'this
      #/1([a]):Sig[mdf=read,gens=[],ts=[muttest.A[muttest.B[recMdfY]]],ret=recMdftest.B[recMdfY]]->
        a.m1/2[]([[-recMdf-][test.B[recMdfY]]{'fear0$},[-imm-][test.F[recMdftest.B[recMdfY]]]{'fear1$}])}],
    test.C/0=Dec[name=test.C/0,gxs=[],lambda=[-mdf-][test.C[]]{'this
      #/1([b]):Sig[mdf=imm,gens=[],ts=[muttest.B[muttest.C[]]],ret=muttest.B[muttest.C[]]]->
        b#/1[]([[-mut-][test.A[muttest.B[recMdf test.C[]]]]{'fear2$}]),
      .i/1([b]):Sig[mdf=imm,gens=[],ts=[muttest.B[immtest.C[]]],ret=muttest.B[immtest.C[]]]->
        b#/1[]([[-mut-][test.A[muttest.B[imm test.C[]]]]{'fear3$}])}]}
    """, """
    package test
    A[X]:{
      read .m1(a: recMdf X, b: imm F[recMdf X]): recMdf X -> b#a,
      }
    F[X]:{ imm #(x: mdf X): mdf X -> x, }
    B[Y]:{
      read #(a: mut A[mut B[recMdf Y]]): recMdf B[recMdf Y] -> a.m1(recMdf B[recMdf Y], F[recMdf B[recMdf Y]]),
      }
    C:{
      #(b: mut B[mut C]): mut B[mut C] -> b#({}),
      .i(b: mut B[imm C]): mut B[imm C] -> b#(mut A[mut B[imm C]]),
      }
    """); }

  // TODO: this should eventually fail with an "inference failed" message when I add that error
  @Disabled
  @Test void callingEphemeralMethod() { fail("""
    """, """
    package base
    A[X]:{ .foo(x: X): X -> { .foo: recMdf X -> x}.foo }
    B:{}
    """); }
}
