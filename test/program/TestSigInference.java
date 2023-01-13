package program;

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

public class TestSigInference {
  void ok(String expected, String... content){
    Main.resetAll();
    AtomicInteger pi = new AtomicInteger();
    var ps = Arrays.stream(content)
      .map(code -> new Parser(Path.of("Dummy"+pi.getAndIncrement()+".fear"), code))
      .toList();
    var p = Parser.parseAll(ps);
    new WellFormednessFullShortCircuitVisitor().visitProgram(p).ifPresent(err->{ throw err; });
    var inferred = p.inferSignatures();
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

    try {
      var inferred = p.inferSignatures();
      Assertions.fail("Did not fail, got:\n" + inferred);
    } catch (CompileError e) {
      Err.strCmp(expectedErr, e.toString());
    }
  }

  @Test void noInference() { ok("""
    {base.A/0=Dec[name=base.A/0,gxs=[],lambda=[-infer-][]{'this
      .fullType/0([]):Sig[mdf=imm,gens=[],ts=[],ret=imm base.A[]]->[-imm base.A[]-][base.A[]]{}
    }]}
    """, """
    package base
    A:{ .fullType: A -> A }
    """);}

  @Test void inferOneSig() { ok("""
    {base.B/0=Dec[name=base.B/0,gxs=[],lambda=[-infer-][base.A[]]{
       'this
       .fullType/0([]):Sig[mdf=imm,gens=[],ts=[],ret=immbase.A[]]
        ->[-immbase.B[]-][base.B[]]{}}],
     base.A/0=Dec[name=base.A/0,gxs=[],lambda=[-infer-][]{
       'this
       .fullType/0([]):Sig[mdf=imm,gens=[],ts=[],ret=immbase.A[]]
         ->[-]}]}
    """, """
    package base
    A:{ .fullType: A }
    B:A{ .fullType->B }
    """);}

  @Test void inferOneSigParams() { ok("""
    {base.B/0=Dec[name=base.B/0,gxs=[],lambda=[-infer-][base.A[]]{
    'this
    .fullType/1([a]):Sig[mdf=imm,gens=[],ts=[immbase.A[]],ret=immbase.A[]]->[-immbase.B[]-][base.B[]]{}}],
  base.A/0=Dec[name=base.A/0,gxs=[],lambda=[-infer-][]{'this.fullType/1([a]):Sig[mdf=imm,gens=[],ts=[immbase.A[]],ret=immbase.A[]]->[-]}]}
    """, """
    package base
    A:{ .fullType(a: A): A }
    B:A{ .fullType a->B }
    """);}

  @Test void inferOneSigGens() { ok("""
    {base.B/0=Dec[name=base.B/0,gxs=[],lambda=[-infer-][base.A[]]{
      'this
      .fullType/0([]):Sig[mdf=imm,gens=[X],ts=[],ret=immX]->this:infer.fullType/0[-]([]):infer}],
    base.A/0=Dec[name=base.A/0,gxs=[],lambda=[-infer-][]{'this.fullType/0([]):Sig[mdf=imm,gens=[X],ts=[],ret=immX]->[-]}]}
    """, """
    package base
    A:{ .fullType[X]: X }
    B:A{ .fullType->this.fullType }
    """);}

  // TODO: Investigating how meths with clashing generic params get inferred/chosen via meths() and 5a.
  @Test void inferOneSigGensClash() { ok("""
    {base.B/1=Dec[name=base.B/1,gxs=[X],lambda=[-infer-][base.A[]]{
    'this
    .bla/0([]):Sig[mdf=imm,gens=[],ts=[],ret=immX]->[-],
    .fullType/0([]):Sig[mdf=imm,gens=[X_NOPE],ts=[],ret=immX]->this:infer.fullType/0[-]([]):infer}],
  base.A/0=Dec[name=base.A/0,gxs=[],lambda=[-infer-][]{'this.fullType/0([]):Sig[mdf=imm,gens=[X],ts=[],ret=immX]->[-]}]}
    """, """
    package base
    A:{ .fullType[X]: X }
    B[X]:A{ .bla:X, .fullType->this.fullType }
    """);}

  @Test void inferClashingGenMeth() { ok("""
  {base.B/0=Dec[name=base.B/0,gxs=[],lambda=[-infer-][]{'this.g/0([]):Sig[mdf=imm,gens=[AA],ts=[],ret=immAA]->[-]}],
  base.C/0=Dec[name=base.C/0,gxs=[],lambda=[-infer-][]{'this.g/0([]):Sig[mdf=imm,gens=[BB],ts=[],ret=immBB]->[-]}],
  base.A/2=Dec[name=base.A/2,gxs=[AA,BB],lambda=[-infer-][base.B[],base.C[]]{
    'this
    .g/0([]):Sig[mdf=imm,gens=[AA],ts=[],ret=immAA]->this:infer.g/0[-]([]):infer
    }]}
    """, """
    package base
    A[AA,BB]:B,C{ .g -> this.g }
    B:{ .g[AA,CC]: AA }
    C:{ .g[BB]: BB }
    """);}

  @Test void inferOneSigGensAndParams() { ok("""
    {base.B/0=Dec[name=base.B/0,gxs=[],lambda=[-infer-][base.A[]]{
      'this
      .fullType/1([x]):Sig[mdf=imm,gens=[X],ts=[immX],ret=immbase.A[]]->[-immbase.B[]-][base.B[]]{}}],
      base.A/0=Dec[name=base.A/0,gxs=[],lambda=[-infer-][]{'this.fullType/1([x]):Sig[mdf=imm,gens=[X],ts=[immX],ret=immbase.A[]]->[-]}]}
    """, """
    package base
    A:{ .fullType[X](x: X): A }
    B:A{ .fullType(x)->B }
    """);}

  @Test void oneParentImplTopDec() { ok("""
    {base.A/0=Dec[name=base.A/0,gxs=[],lambda=[-infer-][]{'this
      .fullType/0([]):Sig[mdf=imm,gens=[],ts=[],ret=imm base.A[]]->[-imm base.A[]-][base.A[]]{}
    }]}
    """, """
    package base
    A:{ .fullType: A }
    B:A{ B }
    """);}
}
