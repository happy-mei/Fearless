package program.inference;

import main.CompileError;
import main.Main;
import org.junit.jupiter.api.Assertions;
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

  @Test void inferRefDef() { ok("""
    """, """
    package base
    alias base.NoMutHyg as NoMutHyg,
    Sealed:{} Void:{}
    Let:{ #[V,R](l:Let[V,R]):R -> l.in(l.var) }
    Let[V,R]:{ .var:V, .in(v:V):R }
    Ref:{ #[X](x: mdf X): mut Ref[mdf X] -> this#(x) }
    Ref[X]:NoMutHyg[X],Sealed{
      read * : recMdf X,
      mut .swap(x: mdf X): mdf X,
      mut :=(x: mdf X): Void -> Let#{ .var -> this.swap(x), .in(_)->Void },
    //mut :=(x: mdf X): Void -> Let#[mdf X, Void](imm Let[mdf X, Void]{ .var -> this.swap(x), .in(_)->Void }),
      mut <-(f: UpdateRef[mdf X]): mdf X -> this.swap(f#(this*)),
    }
    UpdateRef[X]:{ mut #(x: mdf X): mdf X }
    """); }
}
