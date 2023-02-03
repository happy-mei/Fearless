package program;

import failure.CompileError;
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
      .fullType/0([]):Sig[mdf=imm,gens=[X0/0$],ts=[],ret=immX0/0$]->this:infer.fullType/0[-]([]):infer}],
    base.A/0=Dec[name=base.A/0,gxs=[],lambda=[-infer-][]{'this.fullType/0([]):Sig[mdf=imm,gens=[X],ts=[],ret=immX]->[-]}]}
    """, """
    package base
    A:{ .fullType[X]: X }
    B:A{ .fullType->this.fullType }
    """);}

  @Test void inferOneSigGensClash() { ok("""
    {base.B/1=Dec[name=base.B/1,gxs=[X],lambda=[-infer-][base.A[]]{'this
      .bla/0([]):Sig[mdf=imm,gens=[],ts=[],ret=immX]->[-],
      .fullType/0([]):Sig[mdf=imm,gens=[X0/0$],ts=[],ret=immX0/0$]->this:infer.fullType/0[-]([]):infer}],
    base.A/0=Dec[name=base.A/0,gxs=[],lambda=[-infer-][]{'this
      .fullType/0([]):Sig[mdf=imm,gens=[X],ts=[],ret=immX]->[-]}]}
    """, """
    package base
    A:{ .fullType[X]: X }
    B[X]:A{ .bla:X, .fullType->this.fullType }
    """);}

  @Test void inferClashingGenMeth() { fail("""
    In position [###]:2:14
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
      .fullType/1([y]):Sig[mdf=imm,gens=[X0/0$],ts=[immX0/0$],ret=immbase.A[]]->[-immbase.B[]-][base.B[]]{}}],
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

  @Test void oneParentImplTopDecParams() { ok("""
    {base.B/0=Dec[name=base.B/0,gxs=[],lambda=[-infer-][base.A[]]{'this
      .fullType/1([b]):Sig[mdf=imm,gens=[],ts=[immbase.A[]],ret=immbase.A[]]->b:infer}],
    base.A/0=Dec[name=base.A/0,gxs=[],lambda=[-infer-][]{'this
      .fullType/1([a]):Sig[mdf=imm,gens=[],ts=[immbase.A[]],ret=immbase.A[]]->[-]}]}
    """, """
    package base
    A:{ .fullType(a: A): A }
    B:A{ b -> b }
    """);}

  @Test void oneParentImplGens() { ok("""
    {base.B/0=Dec[name=base.B/0,gxs=[],lambda=[-infer-][base.A[]]{'this
      .fullType/1([x]):Sig[mdf=imm,gens=[X0/0$],ts=[immX0/0$],ret=immX0/0$]->x:infer}],
    base.A/0=Dec[name=base.A/0,gxs=[],lambda=[-infer-][]{'this
      .fullType/1([a]):Sig[mdf=imm,gens=[X],ts=[immX],ret=immX]->[-]}]}
    """, """
    package base
    A:{ .fullType[X](a: X): X }
    B:A{ x -> x }
    """);}

  @Test void oneParentImplDecGens() { ok("""
    {base.B/0=Dec[name=base.B/0,gxs=[],lambda=[-infer-][base.A[immbase.B[]]]{'this
      .fullType/1([x]):Sig[mdf=imm,gens=[],ts=[immbase.B[]],ret=immbase.B[]]->[-immbase.B[]-][base.B[]]{}}],
    base.A/1=Dec[name=base.A/1,gxs=[X],lambda=[-infer-][]{'this
      .fullType/1([a]):Sig[mdf=imm,gens=[],ts=[immX],ret=immX]->[-]}]}
    """, """
    package base
    A[X]:{ .fullType(a: X): X }
    B:A[B]{ x -> B }
    """);}

  @Test void onlyAbsGens() { ok("""
    {base.B/0=Dec[name=base.B/0,gxs=[],lambda=[-infer-][base.A[immbase.B[]]]{'this
      .fullType/0([]):Sig[mdf=imm,gens=[],ts=[],ret=immbase.B[]]->[-immbase.B[]-][base.B[]]{}}],
    base.A/1=Dec[name=base.A/1,gxs=[X],lambda=[-infer-][]{'this
      .fullType/0([]):Sig[mdf=imm,gens=[],ts=[],ret=immX]->[-]}],
    base.A/0=Dec[name=base.A/0,gxs=[],lambda=[-infer-][]{'this
      .fullType/0([]):Sig[mdf=imm,gens=[],ts=[],ret=immbase.Void[]]->[-]}],
    base.Void/0=Dec[name=base.Void/0,gxs=[],lambda=[-infer-][]{'this}]}
    """, """
    package base
    Void:{}
    A:{ .fullType: Void }
    A[X]:{ .fullType: X }
    B:A[B]{ B }
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
      .m/0([]):Sig[mdf=imm,gens=[X0/0$],ts=[],ret=imma.Bi[imm X0/0$,imm X0/0$]]->this:infer}],
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
      .id/1([a]):Sig[mdf=imm,gens=[X0/0$],ts=[immX0/0$],ret=immX0/0$]->a:infer}]}
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
      .id/1([a]):Sig[mdf=imm,gens=[X0/0$],ts=[immX0/0$],ret=immX0/0$]->a:infer}],
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
      .id/1([a]):Sig[mdf=imm,gens=[X0/0$],ts=[immX0/0$],ret=immX0/0$]->a:infer}],
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

  @Test void noMethodExists() { fail("""
    In position [###]Dummy0.fear:4:12
    cannotInferSig:19
    Could not infer the signature for .foo/0 in a.Bi/2.
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
        .m/2([a1,a2]):Sig[mdf=imm,gens=[X0/0$,X0/1$],ts=[immX0/0$,immX0/1$],ret=immX0/1$]->a2:infer}]}
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
      .m/2([a1,a2]):Sig[mdf=imm,gens=[X0/0$,X0/1$],ts=[imma.Foo[immX0/0$],immX0/1$],ret=immX0/1$]->a2:infer}]}
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
      .m/2([a1,a2]):Sig[mdf=read,gens=[X0/0$,X0/1$],ts=[muta.Foo[immX0/0$],recMdfX0/1$],ret=recMdfX0/1$]->a2:infer}]}
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
          .g/0([]):Sig[mdf=imm,gens=[X0/0$],ts=[],ret=immX0/0$]->this:infer.g/0[-]([]):infer}]}
      """, """
      package base
      A:B,C{ .g -> this.g }
      B:{ .g[AA]: AA }
      C:{ .g[BB]: BB }
      """);
  }

  @Test void abstractNoCandidate() { fail("""
    In position [###]/Dummy0.fear:3:6
    cannotInferAbsSig:22
    Could not infer the signature for the abstract lambda in a.Id2/0. There must be one abstract lambda in the trait.
    """, """
    package a
    Id:{ .id[X](x: X): X }
    Id2:{ x -> x }
    """); }
  @Test void abstractOneArg() { ok("""
    {a.Id/0=Dec[name=a.Id/0,gxs=[],lambda=[-infer-][]{'this
      .id/1([x]):Sig[mdf=imm,gens=[X],ts=[immX],ret=immX]->[-]}],
    a.Id2/0=Dec[name=a.Id2/0,gxs=[],lambda=[-infer-][a.Id[]]{'this
      .id/1([x]):Sig[mdf=imm,gens=[X0/0$],ts=[immX0/0$],ret=immX0/0$]->x:infer}]}
    """, """
    package a
    Id:{ .id[X](x: X): X }
    Id2:Id{ x -> x }
    """); }
  @Test void adaptGens() { ok("""
    {a.A/1=Dec[name=a.A/1,gxs=[X],lambda=[-infer-][]{'this
      .m1/0([]):Sig[mdf=imm,gens=[],ts=[],ret=mdfX]->[-],
      .m2/0([]):Sig[mdf=imm,gens=[],ts=[],ret=mutX]->[-],
      .m3/0([]):Sig[mdf=imm,gens=[],ts=[],ret=isoX]->[-],
      .m4/0([]):Sig[mdf=imm,gens=[],ts=[],ret=lentX]->[-],
      .m5/0([]):Sig[mdf=read,gens=[],ts=[],ret=recMdfX]->[-],
      .m6/0([]):Sig[mdf=lent,gens=[],ts=[],ret=recMdfX]->[-],
      .m7/0([]):Sig[mdf=imm,gens=[],ts=[],ret=immX]->[-]}],
    a.C/0=Dec[name=a.C/0,gxs=[],lambda=[-infer-][a.A[muta.B[]]]{'this
      .m1/0([]):Sig[mdf=imm,gens=[],ts=[],ret=muta.B[]]->this:infer.m1/0[-]([]):infer,
      .m2/0([]):Sig[mdf=imm,gens=[],ts=[],ret=muta.B[]]->this:infer.m2/0[-]([]):infer,
      .m3/0([]):Sig[mdf=imm,gens=[],ts=[],ret=isoa.B[]]->this:infer.m3/0[-]([]):infer,
      .m4/0([]):Sig[mdf=imm,gens=[],ts=[],ret=lenta.B[]]->this:infer.m4/0[-]([]):infer,
      .m5/0([]):Sig[mdf=read,gens=[],ts=[],ret=recMdfa.B[]]->this:infer.m5/0[-]([]):infer,
      .m6/0([]):Sig[mdf=lent,gens=[],ts=[],ret=recMdfa.B[]]->this:infer.m6/0[-]([]):infer,
      .m7/0([]):Sig[mdf=imm,gens=[],ts=[],ret=imma.B[]]->this:infer.m7/0[-]([]):infer}],
    a.B/0=Dec[name=a.B/0,gxs=[],lambda=[-infer-][a.A[imma.B[]]]{'this
      .m1/0([]):Sig[mdf=imm,gens=[],ts=[],ret=imma.B[]]->this:infer.m1/0[-]([]):infer,
      .m2/0([]):Sig[mdf=imm,gens=[],ts=[],ret=muta.B[]]->this:infer.m2/0[-]([]):infer,
      .m3/0([]):Sig[mdf=imm,gens=[],ts=[],ret=isoa.B[]]->this:infer.m3/0[-]([]):infer,
      .m4/0([]):Sig[mdf=imm,gens=[],ts=[],ret=lenta.B[]]->this:infer.m4/0[-]([]):infer,
      .m5/0([]):Sig[mdf=read,gens=[],ts=[],ret=recMdfa.B[]]->this:infer.m5/0[-]([]):infer,
      .m6/0([]):Sig[mdf=lent,gens=[],ts=[],ret=recMdfa.B[]]->this:infer.m6/0[-]([]):infer,
      .m7/0([]):Sig[mdf=imm,gens=[],ts=[],ret=imma.B[]]->this:infer.m7/0[-]([]):infer}]}
    """, """
    package a
    A[X]:{ .m1:mdf X,  .m2: mut X, .m3: iso X, .m4: lent X,
            read .m5: recMdf X,  lent .m6: recMdf X, .m7: imm X
          }
    B:A[B]{ .m1->this.m1, .m2->this.m2, .m3->this.m3, .m4->this.m4, .m5->this.m5,
            .m6->this.m6,.m7->this.m7,}
    C:A[mut B]{ .m1->this.m1, .m2->this.m2, .m3->this.m3, .m4->this.m4, .m5->this.m5, .m6->this.m6, .m7->this.m7 }
    """); }

  @Test void refDef() { ok("""
    {base.Let/2=Dec[name=base.Let/2,gxs=[V,R],lambda=[-infer-][]{'this
      .var/0([]):Sig[mdf=imm,gens=[],ts=[],ret=immV]->[-],
      .in/1([v]):Sig[mdf=imm,gens=[],ts=[immV],ret=immR]->[-]}],
      
    base.Ref/1=Dec[name=base.Ref/1,gxs=[X],lambda=[-infer-][base.NoMutHyg[immX],base.Sealed[]]{'this
      */0([]):Sig[mdf=read,gens=[],ts=[],ret=recMdfX]->[-],
      .swap/1([x]):Sig[mdf=mut,gens=[],ts=[mdfX],ret=mdfX]->[-],
      :=/1([x]):Sig[mdf=mut,gens=[],ts=[mdfX],ret=immbase.Void[]]->
        [-immbase.Let[]-][base.Let[]]{}#/1[-]([[-infer-][]{
          .var/0([]):[-]->this:infer.swap/1[-]([x:infer]):infer,
          .in/1([_]):[-]->[-immbase.Void[]-][base.Void[]]{}}]):infer,
      <-/1([f]):Sig[mdf=mut,gens=[],ts=[immbase.UpdateRef[mdfX]],ret=mdfX]->
        this:infer.swap/1[-]([f:infer#/1[-]([this:infer*/0[-]([]):infer]):infer]):infer}],
        
    base.Sealed/0=Dec[name=base.Sealed/0,gxs=[],lambda=[-infer-][]{'this}],
    
    base.Ref/0=Dec[name=base.Ref/0,gxs=[],lambda=[-infer-][]{'this
      #/1([x]):Sig[mdf=imm,gens=[X],ts=[mdfX],ret=mutbase.Ref[mdfX]]->this:infer#/1[-]([x:infer]):infer}],
      
    base.Let/0=Dec[name=base.Let/0,gxs=[],lambda=[-infer-][]{'this
      #/1([l]):Sig[mdf=imm,gens=[V,R],ts=[immbase.Let[immV,immR]],ret=immR]->
        l:infer.in/1[-]([l:infer.var/0[-]([]):infer]):infer}],
        
    base.NoMutHyg/1=Dec[name=base.NoMutHyg/1,gxs=[X],lambda=[-infer-][]{'this}],
    base.Void/0=Dec[name=base.Void/0,gxs=[],lambda=[-infer-][]{'this}],
    
    base.UpdateRef/1=Dec[name=base.UpdateRef/1,gxs=[X],lambda=[-infer-][]{'this
      #/1([x]):Sig[mdf=mut,gens=[],ts=[mdfX],ret=mdfX]->[-]}]}
    """, """
    package base
    NoMutHyg[X]:{}
    Sealed:{} Void:{}
    Let:{ #[V,R](l:Let[V,R]):R -> l.in(l.var) }
    Let[V,R]:{ .var:V, .in(v:V):R }
    Ref:{ #[X](x: mdf X): mut Ref[mdf X] -> this#(x) }
    Ref[X]:NoMutHyg[X],Sealed{
      read * : recMdf X,
      mut .swap(x: mdf X): mdf X,
      mut :=(x: mdf X): Void -> Let#{ .var -> this.swap(x), .in(_)->Void },
      mut <-(f: UpdateRef[mdf X]): mdf X -> this.swap(f#(this*)),
    }
    UpdateRef[X]:{ mut #(x: mdf X): mdf X }
    """); }

  @Test void immDelegate() { ok("""
    {base.B/0=Dec[name=base.B/0,gxs=[],lambda=[-infer-][base.A[]]{'this
      .m2/1([k]):Sig[mdf=imm,gens=[X0/0$],ts=[immX0/0$],ret=immbase.Void[]]->
        this:infer.m1/1[-]([k:infer]):infer}],
    base.A/0=Dec[name=base.A/0,gxs=[],lambda=[-infer-][]{'this
      .m1/1([x]):Sig[mdf=imm,gens=[T],ts=[immT],ret=immbase.Void[]]->
        this:infer.m2/1[-]([x:infer]):infer,
      .m2/1([k]):Sig[mdf=imm,gens=[K],ts=[immK],ret=immbase.Void[]]->[-]}],
    base.Void/0=Dec[name=base.Void/0,gxs=[],lambda=[-infer-][]{'this}]}
    """, """
    package base
    A:{
      .m1[T](x:T):Void->this.m2(x),
      .m2[K](k:K):Void
      }
    B:A{ .m2(k)->this.m1(k) }
    Void:{}
    """); }


  // TODO: These are clearly too permissive for example:
  /*
  i.e.
mut List[mut P] p=mutMyFamilyImm
p.get():mut Person
mutMyFamilyImm.get():imm Person
   */
  @Test void familyGensExtension() { ok("""
    {test.Family2/0=Dec[name=test.Family2/0,gxs=[],lambda=[-infer-][test.List[muttest.Person[]]]{'this
      .get/0([]):Sig[mdf=read,gens=[],ts=[],ret=muttest.Person[]]->[-]}],
    test.Family1/0=Dec[name=test.Family1/0,gxs=[],lambda=[-infer-][test.List[muttest.Person[]]]{'this}],
    test.Person/0=Dec[name=test.Person/0,gxs=[],lambda=[-infer-][]{'this}],
    test.Family1a/0=Dec[name=test.Family1a/0,gxs=[],lambda=[-infer-][test.Family1[]]{'this
      .get/0([]):Sig[mdf=read,gens=[],ts=[],ret=recMdftest.Person[]]->[-immtest.Person[]-][test.Person[]]{}}],
    test.List/1=Dec[name=test.List/1,gxs=[X],lambda=[-infer-][]{'this
      .get/0([]):Sig[mdf=read,gens=[],ts=[],ret=recMdfX]->[-]}]}
    """, """
    package test
    Person:{}
    List[X]:{ read .get(): recMdf X }
    Family1:List[mut Person]{}
    Family1a:Family1{ Person }
    Family2:List[mut Person]{ read .get(): mut Person }
    //Family2a:Family2{ Person }
    //Family3:Family1,Family2{ Person }
    """); }
  @Test void familyGensExtension1a() { ok("""
    {test.Family2/0=Dec[name=test.Family2/0,gxs=[],lambda=[-infer-][test.List[muttest.Person[]]]{'this.get/0([]):Sig[mdf=read,gens=[],ts=[],ret=muttest.Person[]]->[-infer-][]{}}],test.Family1/0=Dec[name=test.Family1/0,gxs=[],lambda=[-infer-][test.List[muttest.Person[]]]{'this}],test.Person/0=Dec[name=test.Person/0,gxs=[],lambda=[-infer-][]{'this}],test.Family1a/0=Dec[name=test.Family1a/0,gxs=[],lambda=[-infer-][test.Family1[]]{'this.get/0([]):Sig[mdf=read,gens=[],ts=[],ret=recMdftest.Person[]]->[-immtest.Person[]-][test.Person[]]{}}],test.List/1=Dec[name=test.List/1,gxs=[X],lambda=[-infer-][]{'this.get/0([]):Sig[mdf=read,gens=[],ts=[],ret=recMdfX]->[-]}]}
    """, """
    package test
    Person:{}
    List[X]:{ read .get(): recMdf X }
    Family1:List[mut Person]{}
    Family1a:Family1{ Person }
    Family2:List[mut Person]{ read .get(): mut Person -> {} }
    //Family2a:Family2{ read .get(): mut Person -> {} }
    //Family3:Family1,Family2{ Person }
    """); }
  @Test void familyGensExtension1b() { ok("""
    """, """
    package test
    Person:{}
    List[X]:{ read .get(): recMdf X }
    FamilyImm2:List[imm Person]{ read .get(): imm Person -> {} }
    FamilyMut:List[mut Person]{ read .get(): mut Person -> {}}
    FamilyRead:List[mut Person]{ read .get(): read Person -> {}}
    FamilyIso:List[mut Person]{ read .get(): iso Person -> {}}
    FamilyImm:List[mut Person]{ read .get(): imm Person -> {}}
    FamilyLent:List[mut Person]{ read .get(): lent Person -> {}}
    FamilyRecMdf:List[mut Person]{ read .get(): recMdf Person -> {}}
    """); }
  @Test void familyGensExtension2() { fail("""
    In position file:///Users/nick/Programming/PhD/fearless/Dummy0.fear:8:25
    uncomposableMethods:18
    These methods could not be composed.
    conflicts:
    (file:///Users/nick/Programming/PhD/fearless/Dummy0.fear:3:10) test.List[mut test.Person[]], .get/0
    (file:///Users/nick/Programming/PhD/fearless/Dummy0.fear:6:26) test.Family2[], .get/0
    """, """
    package test
    Person:{}
    List[X]:{ read .get(): recMdf X }
    Family1:List[mut Person]{}
    Family1a:Family1{ Person }
    Family2:List[mut Person]{ read .get(): mut Person }
    Family2a:Family2{ Person }
    Family3:Family1,Family2{ Person }
    """); }
}


//To discuss: what about the implementation of the 2 meth? do we have both or only one now?
//one of the two needs a twist in the cMOf!!