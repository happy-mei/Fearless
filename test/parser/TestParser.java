package parser;

import failure.CompileError;
import main.Main;
import net.jqwik.api.Example;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import utils.Bug;
import utils.Err;

import java.util.Optional;

class TestParser {
  void ok(String expected, String content){
    Main.resetAll();
    String res = new Parser(Parser.dummy,content)
      .parseFullE(Bug::err,s->Optional.empty())
      .toString();
    Err.strCmpFormat(expected,res);
  }
  void same(String content1, String content2){
    Main.resetAll();
    String res1 = new Parser(Parser.dummy,content1).parseFullE(Bug::err,s->Optional.empty()).toString();
    Main.resetAll();
    String res2 = new Parser(Parser.dummy,content2).parseFullE(Bug::err,s->Optional.empty()).toString();
    Err.strCmpFormat(res1,res2);
  }
  void fail(String expectedErr,String content){
    Main.resetAll();
    var b=new StringBuffer();
    try {
      var res=new Parser(Parser.dummy,content)
        .parseFullE(s->{b.append(s);return null;},s->Optional.empty());
      if (res == null) { return; }
      Assertions.fail("Parsing did not fail. Got: "+res);
    }
    catch (CompileError e) {
      Err.strCmp(expectedErr, e.toString());
    }
  }
  @Example void testMCall(){ ok("""
    x:infer.m/0[-]([]):infer
    """,
    "x.m"); }
  @Example void testTrue(){ ok(
    """
    [-base.True[]-][base.True[]]{}
    """, "base.True"); }
  @Example void testLamNoGens(){ ok(
      """
      [-mut animals.Cat[]-][animals.Cat[]]{}
      """, "mut animals.Cat"); }
  @Example void testLamEmptyGens(){ ok(
      """
      [-mut animals.Cat[]-][animals.Cat[]]{}
      """, "mut animals.Cat[]"); }
  @Example void testLamOneGens(){ ok(
      """
      [-mut base.List[imm pkg1.Person[]]-][base.List[imm pkg1.Person[]]]{}
      """, "mut base.List[imm pkg1.Person]"); }
  @Example void testLamManyGens(){ ok(
      """
      [-mut base.Either[imm pkg1.Person[],mut pkg1.Blah[]]-][base.Either[imm pkg1.Person[],mut pkg1.Blah[]]]{}
      """, "mut base.Either[imm pkg1.Person, mut pkg1.Blah]"); }
  @Example void surprisingIntberOfExprs(){ ok(
    """
      we:infer.parse/0[-]([]):infer.a/0[-]([]):infer.surprising/0[-]([]):infer.amount/0[-]([]):infer.of/0[-]([]):infer.stuff/0[-]([]):infer
      """
    ,"we .parse .a .surprising .amount .of .stuff"); }
  @Example void testFail1(){ fail(
    "[###]Dummy.fear:1:3 mismatched input 'parse' expecting {'mut', 'lent', 'read', 'iso', 'recMdf', 'mdf', 'imm', FullCN}"
    ,"We parse a surprising amount of stuff"); }
  @Example void singleEqSugar1(){ ok(
    """
    recv:infer.m1/2[-]([val:infer,[-infer-][]{[-]([v,fear0$]):[-]->fear0$:infer}]):infer
    """, "recv .m1 v = val"); }
  @Example void singleEqSugarPOp1(){ ok(
    """
    recv:infer.m1/2[-]([val:infer,[-infer-][]{[-]([v,fear0$]):[-]->fear0$:infer}]):infer
    """, "recv .m1 (v = val)"); }
  @Example void singleEqSugarPOp2(){ ok(
    """
    recv:infer.m1/2[-]([val:infer,[-infer-][]{[-]([v,fear0$]):[-]->fear0$:infer.m2/0[-]([]):infer}]):infer
    """, "recv .m1 (v = val) .m2"); }
  @Example void singleEqSugarPOp3(){ ok(
    """
    recv:infer.m1/2[-]([val:infer.m2/0[-]([]):infer,[-infer-][]{[-]([v,fear0$]):[-]->fear0$:infer.m3/0[-]([]):infer}]):infer
    """, "recv .m1 (v = val .m2) .m3"); }
  @Example void testVarLast(){ ok("""
    recv:infer.m1/2[-]([v:infer,[-infer-][]{[-]([x,fear0$]):[-]->fear0$:infer}]):infer
    ""","recv .m1 x=v"); }
  @Example void eqSugarSame1() { same("recv .m1 v = val", "recv .m1 (v = val)"); }
  @Example void eqSugarSame2() { same("recv .m1 v = val .m2", "recv .m1 v = (val .m2)"); }
  @Example void chainedMethCall() { ok("""
    recv:infer.m1/1[-]([a:infer]):infer .m2/1[-]([b:infer]):infer
    """, "(recv .m1 a) .m2 b"); }
  @Example void singleEqSugar2(){ ok(
    """
    recv:infer.m1/2[-]([val:infer.m2/0[-]([]):infer,[-infer-][]{[-]([v,fear0$]):[-]->fear0$:infer}]):infer
    """, "recv .m1 v = val .m2"); }
  @Example void nestedCalls1(){ ok("""
    recv:infer.m1/2[-]([v:infer,[-infer-][]{ [-]([x,fear0$]):[-]->fear0$:infer.m2/1[-]([a:infer]):infer}]):infer
    """, "recv .m1 x=v.m2 a"); }
  @Example void nestedCalls2(){ ok("""
    recv:infer.m1/2[-]([v:infer,[-infer-][]{ [-]([x,fear0$]):[-]->fear0$:infer}]):infer.m2/1[-]([a:infer]):infer
    """, "(recv .m1 x=v) .m2 a"); }
  @Example void eqExpasnionNoPar() { ok("""
    recv:infer.m1/2[-]([
      v:infer,
      [-infer-][]{[-]([x,fear0$]):[-]->fear0$:infer.m2/1[-]([a:infer]):infer}
    ]):infer
    """, "recv .m1 x=v.m2 a"); }
  @Example void eqExpasnionPar() { ok("""
    recv:infer.m1/2[-]([v:infer.m2/1[-]([a:infer]):infer,[-infer-][]{[-]([x,fear0$]):[-]->fear0$:infer}]):infer
    """, "recv .m1 x=(v.m2 a)"); }
  @Example void eqExpansionGensNoPar() { ok("""
    recv:infer.m1/2[immA]([v:infer,[-infer-][]{
      [-]([x,fear0$]):[-]->fear0$:infer.m2/1[immB,immbase.C[immD]]([a:infer]):infer
    }]):infer
    """, "recv .m1[A] x=v.m2[B,base.C[D]] a"); }
  @Example void nestedGenerics() { ok("""
    recv:infer .m1/0[imm pkg1.A[imm B]]([]):infer
    """, "recv .m1[pkg1.A[B]]"); }
  @Example void failNestedGenerics() { fail("""
    In position [###]/Dummy.fear:1:5
    [E3 concreteTypeInFormalParams]
    Trait and method declarations may only have generic type parameters. This concrete type was provided instead:
    imm pkg1.A[imm B]
    Alternatively, are you attempting to shadow an existing class name?
    """, "{ .m1[pkg1.A[B]]: base.Void }"); }
  @Example void sameTest1(){ same("m.a", "m.a"); }
  @Example void sameTest2(){ same("recv .m1 a .m2 b .m3 c", "((recv .m1 a) .m2 b) .m3 c"); }
  @Example void sameTest3(){ same("recv .m1 a .m2 b", "(recv .m1 a) .m2 b"); }
  @Example void sameTest4(){ same("recv .m1 a .m2", "recv .m1 (a .m2)"); }
  @Example void sameTest5(){ same("recv .m1 x=v .m2", "recv .m1 x=(v .m2)"); }
  // TODO: (recv .m1 x=v) .m2 a is weird because the = method has executed first, so x is out of scope
  @Example void sameTest6(){ same("recv .m1[A] x=v .m2[B,base.C[D]]", "recv .m1[A] x=(v .m2[B,base.C[D]])"); }
  @Example void sameTestVarLast(){ same("recv.m1(x=v)", "recv .m1 x=v"); }
  @Example void implicitLambdaMdf(){ ok("[-pkg1.L[]-][pkg1.L[]]{}", "pkg1.L{}"); }
  @Example void explicitMdfLambdaIso(){ ok("[-iso pkg1.L[]-][pkg1.L[]]{}", "iso pkg1.L{}"); }
  @Example void explicitMdfLambdaImm(){ ok("[-imm pkg1.L[]-][pkg1.L[]]{}", "imm pkg1.L{}"); }
  @Example void explicitMdfLambdaMut(){ ok("[-mut pkg1.L[]-][pkg1.L[]]{}", "mut pkg1.L{}"); }
  @Example void explicitMdfLambdaRead(){ ok("[-read pkg1.L[]-][pkg1.L[]]{}", "read pkg1.L{}"); }
  @Example void explicitMdfLambdaLent(){ ok("[-lent pkg1.L[]-][pkg1.L[]]{}", "lent pkg1.L{}"); }
  @Example void explicitMdfLambdaRecMdf(){ ok("[-recMdf pkg1.L[]-][pkg1.L[]]{}", "recMdf pkg1.L{}"); }
  @Example void explicitMdfLambdaMdf(){ fail("""
    [E11 invalidMdf]
    The modifier 'mdf' can only be used on generic type variables. 'mdf' found on type pkg1.L[]
    """, "mdf pkg1.L{}"); }

  /*
  Right now a method call without parentheses have less binding power of
a method call with parenthesis.
Except for a no argument method call, a.foo behaves the same as a.foo()

I wonder if we should remove the 'Except' part.

right now we are doing
list.flow
  .map{..}
  .filter{..}
  .to List
because if it was
list.flow
  .map{..}
  .filter{..}
  .toList
it would have precedence as follows:
list.flow
  .map{..}
  .filter
  ({..}.toList)

But, I'm not sure if we will always have arguments on our flows, and
for may cases, like the builder pattern, it is very natural to have
just a call.

Stuff.build
  .name(...)
  .age(..)
  .build

We originally designed our precedence because we wanted
a .and b .not
to be interpreted as
a .and (b .not)
But... maybe it was a mistake?

Would  the interpretation (a .and b) .not  become more natural going forward?
   */
  @Example void flowPrecedence1() { ok("""
    list:infer.flow/0[-]([]):infer.map/1[-]([[-infer-][]{}]):infer.filter/1[-]([[-infer-][]{}]):infer.to/1[-]([[-base.List[]-][base.List[]]{}]):infer
    """, """
    list.flow
      .map{}
      .filter{}
      .to base.List
    """); }
  @Example void flowPrecedence2() { ok("""
    list:infer.flow/0[-]([]):infer.map/1[-]([[-infer-][]{}]):infer.filter/1[-]([[-infer-][]{}.toList/0[-]([]):infer]):infer
    """, """
    list.flow
      .map{}
      .filter{}
      .toList
    """); }
  @Example void flowPrecedence2a() { ok("""
    list:infer.flow/0[-]([]):infer.map/1[-]([[-infer-][]{}]):infer.filter/1[-]([[-infer-][]{}.toList/0[-]([]):infer]):infer
    """, """
    list.flow
      .map{}
      .filter
      ({}.toList)
    """); }
  @Example void flowPrecedence2b() { ok("""
    list:infer.flow/0[-]([]):infer.map/1[-]([[-infer-][]{}]):infer.filter/1[-]([[-infer-][]{}]):infer.toList/0[-]([]):infer
    """, """
    (list.flow
      .map{}
      .filter{})
      .toList
    """); }
  @Example void precedence3() { same("""
    (a + b) - c
    """, """
    a + b - c
    """); }
}
