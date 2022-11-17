package parser;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

import main.Main;
import org.junit.jupiter.api.Test;

import utils.Bug;
import utils.Err;

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
    var res=new Parser(Parser.dummy,content)
      .parseFullE(s->{b.append(s);return null;},s->Optional.empty());
    assertNull(res);
    Err.strCmp(expectedErr,b.toString());    
  }
  @Test void testMCall(){ ok("""
    x:infer.m[-]([]):infer
    """,
    "x.m"); }
  @Test void testTrue(){ ok(
    """
    Lambda[mdf=imm,
      its=[base.True[]],
      selfName=null,
      meths=[],
      t=imm base.True[]]
    """, "base.True"); }
  @Test void testLamNoGens(){ ok(
      """
      Lambda[mdf=mut,
        its=[animals.Cat[]],
        selfName=null,
        meths=[],
        t=mut animals.Cat[]]
      """, "mut animals.Cat"); }
  @Test void testLamEmptyGens(){ ok(
      """
      Lambda[mdf=mut,
        its=[animals.Cat[]],
        selfName=null,
        meths=[],
        t=mut animals.Cat[]]
      """, "mut animals.Cat[]"); }
  @Test void testLamOneGens(){ ok(
      """
      Lambda[mdf=mut,
        its=[base.List[imm pkg1.Person[]]],
        selfName=null,
        meths=[],
        t=mut base.List[imm pkg1.Person[]]]
      """, "mut base.List[imm pkg1.Person]"); }
  @Test void testLamManyGens(){ ok(
      """
      Lambda[mdf=mut,
        its=[base.Either[imm pkg1.Person[], mut pkg1.Blah[]]],
        selfName=null,
        meths=[],
        t=mut base.Either[imm pkg1.Person[], mut pkg1.Blah[]]]
      """, "mut base.Either[imm pkg1.Person, mut pkg1.Blah]"); }
  @Test void surprisingNumberOfExprs(){ ok(
    """
      we:infer.parse[-]([]):infer.a[-]([]):infer.surprising[-]([]):infer.amount[-]([]):infer.of[-]([]):infer.stuff[-]([]):infer
      """
    ,"we .parse .a .surprising .amount .of .stuff"); }
  @Test void testFail1(){ fail(
    "[###]Dummy.fear:1:3 mismatched input 'parse' expecting {'mut', 'lent', 'read', 'iso', 'recMdf', 'mdf', 'imm', FullCN}"
    ,"We parse a surprising amount of stuff"); }
  @Test void singleEqSugar1(){ ok(
    """
    recv:infer.m1[-]([val:infer,Lambda[mdf=null,its=[],selfName=null,meths=[[-]([v:infer,fear0$:infer]):[-]->fear0$:infer],t=infer]]):infer
    """, "recv .m1 v = val"); }
  @Test void singleEqSugarPOp1(){ ok(
    """
    recv:infer.m1[-]([
      val:infer,
      Lambda[mdf=null,its=[],selfName=null,meths=[[-]([v:infer,fear0$:infer]):[-]->fear0$:infer],t=infer]
    ]):infer
    """, "recv .m1 (v = val)"); }
  @Test void singleEqSugarPOp2(){ ok(
    """
    recv:infer.m1[-]([val:infer,Lambda[mdf=null,its=[],selfName=null,meths=[[-]([v:infer,fear0$:infer]):[-]->fear0$:infer.m2[-]([]):infer],t=infer]]):infer
    """, "recv .m1 (v = val) .m2"); }
  @Test void singleEqSugarPOp3(){ ok(
    """
    recv:infer .m1[-]([
      val:infer.m2[-]([]):infer,
      Lambda[mdf=null,its=[],selfName=null,meths=[[-]([v:infer,fear0$:infer]):[-]->fear0$:infer.m3[-]([]):infer],t=infer]
    ]):infer
    """, "recv .m1 (v = val .m2) .m3"); }
  @Test void eqSugarSame1() { same("recv .m1 v = val", "recv .m1 (v = val)"); }
  @Test void eqSugarSame2() { same("recv .m1 v = val .m2", "recv .m1 (v = val) .m2"); }
  @Test void chainedMethCall() { ok("""
    recv:infer.m1[-]([a:infer]):infer .m2[-]([b:infer]):infer
    """, "(recv .m1 a) .m2 b"); }
  @Test void singleEqSugar2(){ ok(
    """
    recv:infer.m1[-]([val:infer.m2[-]([]):infer,Lambda[mdf=null,its=[],selfName=null,meths=[[-]([v:infer,fear0$:infer]):[-]->fear0$:infer],t=infer]]):infer
    """, "recv .m1 v = val .m2"); }
  @Test void sameTest1(){ same("m.a", "m.a"); }
  @Test void sameTest2(){ same("recv .m1 a .m2 b .m3 c", "((recv .m1 a) .m2 b) .m3 c"); }
  @Test void sameTest3(){ same("recv .m1 a .m2 b", "(recv .m1 a) .m2 b"); }
  @Test void sameTest4(){ same("recv .m1 a .m2", "recv .m1 (a .m2)"); }
  @Test void sameTest5(){ same("recv .m1 x=v .m2", "recv .m1 x=(v .m2)"); }
  @Test void sameTest6(){ same("recv .m1 x=v.m2 a", "(recv .m1 x=v) .m2 a"); }
  @Test void sameTest7(){ same("recv .m1[A] x=v .m2[B,C[D]]", "recv .m1[A] x=(v .m2[B,C[D]])"); }
  @Test void sameTest8(){ same("recv .m1[A] x=v.m2[B,C[D]] a", "(recv .m1[A] x=v) .m2[B,C[D]] a"); }
}
