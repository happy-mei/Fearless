package program;

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
      .fullType/0([]):Sig[mdf=imm,gens=[Fear0$],ts=[],ret=immFear0$]->this:infer.fullType/0[-]([]):infer}],
    base.A/0=Dec[name=base.A/0,gxs=[],lambda=[-infer-][]{'this.fullType/0([]):Sig[mdf=imm,gens=[X],ts=[],ret=immX]->[-]}]}
    """, """
    package base
    A:{ .fullType[X]: X }
    B:A{ .fullType->this.fullType }
    """);}

  // TODO: Investigating how meths with clashing generic params get inferred/chosen via meths() and 5a.
  @Test void inferOneSigGensClash() { ok("""
    {base.B/1=Dec[name=base.B/1,gxs=[X],lambda=[-infer-][base.A[]]{'this
      .bla/0([]):Sig[mdf=imm,gens=[],ts=[],ret=immX]->[-],
      .fullType/0([]):Sig[mdf=imm,gens=[Fear0$],ts=[],ret=immFear0$]->this:infer.fullType/0[-]([]):infer}],
    base.A/0=Dec[name=base.A/0,gxs=[],lambda=[-infer-][]{'this
      .fullType/0([]):Sig[mdf=imm,gens=[X],ts=[],ret=immX]->[-]}]}
    """, """
    package base
    A:{ .fullType[X]: X }
    B[X]:A{ .bla:X, .fullType->this.fullType }
    """);}

  @Test void inferClashingGenMeth() { fail("""
    uncomposableMethods:18
    These methods could not be composed.
    conflicts:
    ([###]/Dummy0.fear:4:4) base.C[], .g/0
    ([###]/Dummy0.fear:3:4) base.B[], .g/0
    """, """
    package base
    A[AA,BB]:B,C{ .g -> this.g }
    B:{ .g[AA,CC]: AA }
    C:{ .g[BB]: BB }
    """);}

  @Test void inferOneSigGensAndParams() { ok("""
    {base.B/0=Dec[name=base.B/0,gxs=[],lambda=[-infer-][base.A[]]{
      'this
      .fullType/1([y]):Sig[mdf=imm,gens=[Fear0$],ts=[immFear0$],ret=immbase.A[]]->[-immbase.B[]-][base.B[]]{}}],
      base.A/0=Dec[name=base.A/0,gxs=[],lambda=[-infer-][]{'this.fullType/1([x]):Sig[mdf=imm,gens=[X],ts=[immX],ret=immbase.A[]]->[-]}]}
    """, """
    package base
    A:{ .fullType[X](x: X): A }
    B:A{ .fullType(y)->B }
    """);}

  @Test void oneParentImplTopDec() { ok("""
    {base.B/0=Dec[name=base.B/0,gxs=[],lambda=[-infer-][base.A[]]{'this
      .fullType/0([]):Sig[mdf=imm,gens=[],ts=[],ret=immbase.A[]]->[-immbase.B[]-][base.B[]]{}}],
    base.A/0=Dec[name=base.A/0,gxs=[],lambda=[-infer-][]{'this
      .fullType/0([]):Sig[mdf=imm,gens=[],ts=[],ret=immbase.A[]]->[-]}]}
    """, """
    package base
    A:{ .fullType: A }
    B:A{ B }
    """);}

  @Test void complexInfer() { ok("""
    {a.B/1=Dec[name=a.B/1,gxs=[Y],lambda=[-infer-][]{'this
      .m/0([]):Sig[mdf=imm,gens=[X],ts=[],ret=imma.Bi[immX,immY]]->this:infer}],
    a.A/1=Dec[name=a.A/1,gxs=[X],lambda=[-infer-][a.B[immX]]{'this
      .foo/0([]):Sig[mdf=imm,gens=[],ts=[],ret=immX]->[-]}],
    a.Bi/2=Dec[name=a.Bi/2,gxs=[AA,BB],lambda=[-infer-][]{'this}]}
    """, """
    package a
    A[X]:B[X]{ .foo:X }
    B[Y]:{.m[X]:Bi[X,Y]->this}
    Bi[AA,BB]:{}
    """); }
  @Test void complexInfer2() { ok("""
    {a.B/1=Dec[name=a.B/1,gxs=[Y],lambda=[-infer-][]{'this
      .m/0([]):Sig[mdf=imm,gens=[X],ts=[],ret=imma.Bi[immX,immY]]->this:infer}],
    a.A/1=Dec[name=a.A/1,gxs=[X],lambda=[-infer-][a.B[immX]]{'this
      .foo/0([]):Sig[mdf=imm,gens=[],ts=[],ret=immX]->[-],
      .m/0([]):Sig[mdf=imm,gens=[Fear0$],ts=[],ret=imma.Bi[imm Fear0$,imm Fear0$]]->this:infer}],
    a.Bi/2=Dec[name=a.Bi/2,gxs=[AA,BB],lambda=[-infer-][]{'this}]}
    """, """
    package a
    A[X]:B[X]{ .foo:X, .m -> this }
    B[Y]:{.m[X]:Bi[X,Y]->this}
    Bi[AA,BB]:{}
    """); }
  @Test void diamondRename1() { ok("""
    {a.A/0=Dec[name=a.A/0,gxs=[],lambda=[-infer-][a.Id[]]{'this}],
    a.B/1=Dec[name=a.B/1,gxs=[X],lambda=[-infer-][a.Id[]]{'this}],
    a.Id/0=Dec[name=a.Id/0,gxs=[],lambda=[-infer-][]{'this
      .id/1([x]):Sig[mdf=imm,gens=[X],ts=[immX],ret=immX]->[-]}],
    a.C/0=Dec[name=a.C/0,gxs=[],lambda=[-infer-][a.A[],a.B[imma.A[]]]{'this
      .id/1([a]):Sig[mdf=imm,gens=[Fear0$],ts=[immFear0$],ret=immFear0$]->a:infer}]}
    """, """
    package a
    Id:{ .id[X](x:X):X }
    A:Id
    B[X]:Id
    C:A,B[A]{.id a->a}
    """); }//So, how do we 'accept' that the version with X and the version with X0 are compatible
  @Test void diamondRenameNotComposable() { ok("""
    {a.A/0=Dec[name=a.A/0,gxs=[],lambda=[-infer-][a.Id1[]]{'this}],
    a.B/1=Dec[name=a.B/1,gxs=[Y],lambda=[-infer-][a.Id2[]]{'this}],
    a.C/0=Dec[name=a.C/0,gxs=[],lambda=[-infer-][a.A[],a.B[imma.A[]]]{'this
      .id/1([a]):Sig[mdf=imm,gens=[Fear0$],ts=[immFear0$],ret=immFear0$]->a:infer}],
    a.Id2/0=Dec[name=a.Id2/0,gxs=[],lambda=[-infer-][]{'this
      .id/1([x]):Sig[mdf=imm,gens=[Z],ts=[immZ],ret=immZ]->[-]}],
    a.Id1/0=Dec[name=a.Id1/0,gxs=[],lambda=[-infer-][]{'this
      .id/1([x]):Sig[mdf=imm,gens=[X],ts=[immX],ret=immX]->[-]}]}
    """, """
    package a
    Id1:{ .id[X](x:X):X }
    Id2:{ .id[Z](x:Z):Z }
    A:Id1
    B[Y]:Id2
    C:A,B[A]{.id a->a}
    """); }//So, how do we 'accept' that the version with X and the version with X0 are compatible
  @Test void diamondRename2() { ok("""
    {a.A/0=Dec[name=a.A/0,gxs=[],lambda=[-infer-][a.Id1[]]{'this}],
    a.B/1=Dec[name=a.B/1,gxs=[Y],lambda=[-infer-][a.Id2[]]{'this}],
    a.C/0=Dec[name=a.C/0,gxs=[],lambda=[-infer-][a.A[],a.B[imma.A[]]]{'this
      .id/1([a]):Sig[mdf=imm,gens=[Fear0$],ts=[immFear0$],ret=immFear0$]->a:infer}],
    a.Id2/0=Dec[name=a.Id2/0,gxs=[],lambda=[-infer-][]{'this
      .id/1([x]):Sig[mdf=imm,gens=[X],ts=[immX],ret=immX]->[-]}],
    a.Id1/0=Dec[name=a.Id1/0,gxs=[],lambda=[-infer-][]{'this
      .id/1([x]):Sig[mdf=imm,gens=[X],ts=[immX],ret=immX]->[-]}]}
    """, """
    package a
    Id1:{ .id[X](x:X):X }
    Id2:{ .id[X](x:X):X }
    A:Id1
    B[Y]:Id2
    C:A,B[A]{.id a->a}
    """); }//So, how do we 'accept' that the version with X and the version with X0 are compatible
  @Disabled // TODO
  @Test void multiGens1() { fail("""
    
    """, """
    package a
    A[X,Y]:B[X,Y]{ .foo:X }
    B[Y,X]:{.m[Z]:Bi[X,Y]->this.m}
    Bi[AA,BB]:{ .foo -> this.foo }
    """); }

  @Test void multiGens2() { ok("""
    {a.A/0=Dec[name=a.A/0,gxs=[],lambda=[-infer-][]{'this
      .m/2([x,y]):Sig[mdf=imm,gens=[X,Y],ts=[immX,immY],ret=immY]->[-]}],
      a.B/0=Dec[name=a.B/0,gxs=[],lambda=[-infer-][a.A[]]{'this
        .m/2([a1,a2]):Sig[mdf=imm,gens=[Fear0$,Fear1$],ts=[immFear0$,immFear1$],ret=immFear1$]->a2:infer}]}
    """, """
    package a
    A:{ .m[X,Y](x:X,y:Y): Y }
    B:A{ .m(a1,a2) -> a2 }
    """); }
  @Test void multiGensOneConcrete() { ok("""
    {a.A/0=Dec[name=a.A/0,gxs=[],lambda=[-infer-][]{'this
      .m/2([x,y]):Sig[mdf=imm,gens=[X,Y],ts=[imma.Foo[immX],immY],ret=immY]->[-]}],
    a.Foo/1=Dec[name=a.Foo/1,gxs=[X],lambda=[-infer-][]{'this}],
    a.B/0=Dec[name=a.B/0,gxs=[],lambda=[-infer-][a.A[]]{'this
      .m/2([a1,a2]):Sig[mdf=imm,gens=[Fear0$,Fear1$],ts=[imma.Foo[immFear0$],immFear1$],ret=immFear1$]->a2:infer}]}
    """, """
    package a
    Foo[X]:{}
    A:{ .m[X,Y](x:Foo[X],y:Y): Y }
    B:A{ .m(a1,a2) -> a2 }
    """); }
  @Test void multiGensOneConcreteMdfs() { ok("""
    {a.A/0=Dec[name=a.A/0,gxs=[],lambda=[-infer-][]{'this
      .m/2([x,y]):Sig[mdf=read,gens=[X,Y],ts=[muta.Foo[immX],recMdfY],ret=recMdfY]->[-]}],
    a.Foo/1=Dec[name=a.Foo/1,gxs=[X],lambda=[-infer-][]{'this}],
    a.B/0=Dec[name=a.B/0,gxs=[],lambda=[-infer-][a.A[]]{'this
      .m/2([a1,a2]):Sig[mdf=read,gens=[Fear0$,Fear1$],ts=[muta.Foo[immFear0$],recMdfFear1$],ret=recMdfFear1$]->a2:infer}]}
    """, """
    package a
    Foo[X]:{}
    A:{ read .m[X,Y](x:mut Foo[X],y:recMdf Y): recMdf Y }
    B:A{ .m(a1,a2) -> a2 }
    """); }
  @Test void sameGens() {
    ok("""
      {base.B/0=Dec[name=base.B/0,gxs=[],lambda=[-infer-][]{'this
        .g/0([]):Sig[mdf=imm,gens=[AA],ts=[],ret=immAA]->[-]}],
        base.C/0=Dec[name=base.C/0,gxs=[],lambda=[-infer-][]{'this.g/0([]):Sig[mdf=imm,gens=[BB],ts=[],ret=immBB]->[-]}],
        base.A/0=Dec[name=base.A/0,gxs=[],lambda=[-infer-][base.B[],base.C[]]{'this
          .g/0([]):Sig[mdf=imm,gens=[Fear0$],ts=[],ret=immFear0$]->this:infer.g/0[-]([]):infer}]}
      """, """
      package base
      A:B,C{ .g -> this.g }
      B:{ .g[AA]: AA }
      C:{ .g[BB]: BB }
      """);
  }
}


//To discuss: what about the implementation of the 2 meth? do we have both or only one now?
//one of the two needs a twist in the cMOf!!