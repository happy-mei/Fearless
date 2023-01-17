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
    Main.resetAll();
    AtomicInteger pi = new AtomicInteger();
    var ps = Arrays.stream(content)
      .map(code -> new Parser(Path.of("Dummy"+pi.getAndIncrement()+".fear"), code))
      .toList();
    var p = Parser.parseAll(ps);
    new WellFormednessFullShortCircuitVisitor().visitProgram(p).ifPresent(err->{ throw err; });
    var inferredSigs = p.inferSignatures();
    var inferred = new InferBodies(inferredSigs).inferAll();
    Err.strCmpFormat(expected, inferred.toString());
  }
  void fail(String expectedErr, String... content){
    Main.resetAll();
    AtomicInteger pi = new AtomicInteger();
    var ps = Arrays.stream(content)
      .map(code -> new Parser(Path.of("Dummy"+pi.getAndIncrement()+".fear"), code))
      .toList();
    var p = Parser.parseAll(ps);
    new WellFormednessFullShortCircuitVisitor().visitProgram(p).ifPresent(err->{ throw err; });
    var inferredSigs = p.inferSignatures();

    try {
      var inferred = new InferBodies(inferredSigs).inferAll();
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
    {a.Foo/0=Dec[name=a.Foo/0,gxs=[],lambda=Lambda[mdf=mdf,its=[a.Foo[]],selfName=this,meths=[
      .nothingToInfer/0([]):Sig[mdf=imm,gens=[],ts=[],ret=imma.Foo[]]->[-]]]]}
    """, """
    package a
    Foo:{ .nothingToInfer: Foo }
    """); }

  @Test void inferSelfFn() { ok("""
    {a.Id/0=Dec[name=a.Id/0,gxs=[],lambda=Lambda[mdf=mdf,its=[a.Id[]],selfName=this,meths=[
      .id/0([]):Sig[mdf=imm,gens=[],ts=[],ret=imma.Id[]]->this]]]}
    """, """
    package a
    Id:{ .id: Id -> this }
    """); }

  @Test void inferIdentityFn() { ok("""
    {a.Id/0=Dec[name=a.Id/0,gxs=[],lambda=Lambda[mdf=mdf,its=[a.Id[]],selfName=this,meths=[.id/1([x]):Sig[mdf=imm,gens=[X],ts=[immX],ret=immX]->x]]]}
    """, """
    package a
    Id:{ .id[X](x: X): X -> x }
    """); }
  @Test void inferLoop() { ok("""
    {a.Id/0=Dec[name=a.Id/0,gxs=[],lambda=Lambda[mdf=mdf,its=[a.Id[]],selfName=this,meths=[.id/1([x]):Sig[mdf=imm,gens=[X],ts=[immX],ret=immX]->x]]]}
    """, """
    package a
    Id:{ .id[X](x: X): X -> this.id[X](x) }
    """); }
}
