package parser;

import failure.CompileError;
import main.Main;
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
  @Test void testMCall(){ ok("""
    x:infer.m/0[-]([]):infer
    """,
    "x.m"); }
  @Test void testTrue(){ ok(
    """
    [-imm base.True[]-][base.True[]]{}
    """, "base.True"); }
  @Test void testLamNoGens(){ ok(
      """
      [-mut animals.Cat[]-][animals.Cat[]]{}
      """, "mut animals.Cat"); }
  @Test void testLamEmptyGens(){ ok(
      """
      [-mut animals.Cat[]-][animals.Cat[]]{}
      """, "mut animals.Cat[]"); }
  @Test void testLamOneGens(){ ok(
      """
      [-mut base.List[imm pkg1.Person[]]-][base.List[imm pkg1.Person[]]]{}
      """, "mut base.List[imm pkg1.Person]"); }
  @Test void testLamManyGens(){ ok(
      """
      [-mut base.Either[imm pkg1.Person[],mut pkg1.Blah[]]-][base.Either[imm pkg1.Person[],mut pkg1.Blah[]]]{}
      """, "mut base.Either[imm pkg1.Person, mut pkg1.Blah]"); }
  @Test void surprisingNumberOfExprs(){ ok(
    """
      we:infer.parse/0[-]([]):infer.a/0[-]([]):infer.surprising/0[-]([]):infer.amount/0[-]([]):infer.of/0[-]([]):infer.stuff/0[-]([]):infer
      """
    ,"we .parse .a .surprising .amount .of .stuff"); }
  @Test void testFail1(){ fail(
    "[###]Dummy.fear:1:3 mismatched input 'parse' expecting {'mut', 'lent', 'read', 'iso', 'recMdf', 'mdf', 'imm', FullCN}"
    ,"We parse a surprising amount of stuff"); }
  @Test void singleEqSugar1(){ ok(
    """
    recv:infer.m1/2[-]([val:infer,[-infer-][]{[-]([v,fear0$]):[-]->fear0$:infer}]):infer
    """, "recv .m1 v = val"); }
  @Test void singleEqSugarPOp1(){ ok(
    """
    recv:infer.m1/2[-]([val:infer,[-infer-][]{[-]([v,fear0$]):[-]->fear0$:infer}]):infer
    """, "recv .m1 (v = val)"); }
  @Test void singleEqSugarPOp2(){ ok(
    """
    recv:infer.m1/2[-]([val:infer,[-infer-][]{[-]([v,fear0$]):[-]->fear0$:infer.m2/0[-]([]):infer}]):infer
    """, "recv .m1 (v = val) .m2"); }
  @Test void singleEqSugarPOp3(){ ok(
    """
    recv:infer.m1/2[-]([val:infer.m2/0[-]([]):infer,[-infer-][]{[-]([v,fear0$]):[-]->fear0$:infer.m3/0[-]([]):infer}]):infer
    """, "recv .m1 (v = val .m2) .m3"); }
  @Test void testVarLast(){ ok("""
    recv:infer.m1/2[-]([v:infer,[-infer-][]{[-]([x,fear0$]):[-]->fear0$:infer}]):infer
    ""","recv .m1 x=v"); }
  @Test void eqSugarSame1() { same("recv .m1 v = val", "recv .m1 (v = val)"); }
  @Test void eqSugarSame2() { same("recv .m1 v = val .m2", "recv .m1 v = (val .m2)"); }
  @Test void chainedMethCall() { ok("""
    recv:infer.m1/1[-]([a:infer]):infer .m2/1[-]([b:infer]):infer
    """, "(recv .m1 a) .m2 b"); }
  @Test void singleEqSugar2(){ ok(
    """
    recv:infer.m1/2[-]([val:infer.m2/0[-]([]):infer,[-infer-][]{[-]([v,fear0$]):[-]->fear0$:infer}]):infer
    """, "recv .m1 v = val .m2"); }
  @Test void nestedCalls1(){ ok("""
    recv:infer.m1/2[-]([v:infer,[-infer-][]{ [-]([x,fear0$]):[-]->fear0$:infer.m2/1[-]([a:infer]):infer}]):infer
    """, "recv .m1 x=v.m2 a"); }
  @Test void nestedCalls2(){ ok("""
    recv:infer.m1/2[-]([v:infer,[-infer-][]{ [-]([x,fear0$]):[-]->fear0$:infer}]):infer.m2/1[-]([a:infer]):infer
    """, "(recv .m1 x=v) .m2 a"); }
  @Test void eqExpasnionNoPar() { ok("""
    recv:infer.m1/2[-]([
      v:infer,
      [-infer-][]{[-]([x,fear0$]):[-]->fear0$:infer.m2/1[-]([a:infer]):infer}
    ]):infer
    """, "recv .m1 x=v.m2 a"); }
  @Test void eqExpasnionPar() { ok("""
    recv:infer.m1/2[-]([v:infer.m2/1[-]([a:infer]):infer,[-infer-][]{[-]([x,fear0$]):[-]->fear0$:infer}]):infer
    """, "recv .m1 x=(v.m2 a)"); }
  @Test void eqExpansionGensNoPar() { ok("""
    recv:infer.m1/2[immA]([v:infer,[-infer-][]{
      [-]([x,fear0$]):[-]->fear0$:infer.m2/1[immB,immbase.C[immD]]([a:infer]):infer
    }]):infer
    """, "recv .m1[A] x=v.m2[B,base.C[D]] a"); }
  @Test void nestedGenerics() { ok("""
    recv:infer .m1/0[imm pkg1.A[imm B]]([]):infer
    """, "recv .m1[pkg1.A[B]]"); }
  @Test void failNestedGenerics() { fail("""
    In position [###]/Dummy.fear:1:5
    [E3 concreteTypeInFormalParams]
    Trait and method declarations may only have generic type parameters. This concrete type was provided instead:
    imm pkg1.A[imm B]
    Alternatively, are you attempting to shadow an existing class name?
    """, "{ .m1[pkg1.A[B]]: base.Void }"); }
  @Test void sameTest1(){ same("m.a", "m.a"); }
  @Test void sameTest2(){ same("recv .m1 a .m2 b .m3 c", "((recv .m1 a) .m2 b) .m3 c"); }
  @Test void sameTest3(){ same("recv .m1 a .m2 b", "(recv .m1 a) .m2 b"); }
  @Test void sameTest4(){ same("recv .m1 a .m2", "recv .m1 (a .m2)"); }
  @Test void sameTest5(){ same("recv .m1 x=v .m2", "recv .m1 x=(v .m2)"); }
  // TODO: (recv .m1 x=v) .m2 a is weird because the = method has executed first, so x is out of scope
  @Test void sameTest6(){ same("recv .m1[A] x=v .m2[B,base.C[D]]", "recv .m1[A] x=(v .m2[B,base.C[D]])"); }
  @Test void sameTestVarLast(){ same("recv.m1(x=v)", "recv .m1 x=v"); }
}
