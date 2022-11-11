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
  void fail(String expectedErr,String content){
    Main.resetAll();
    var b=new StringBuffer();
    var res=new Parser(Parser.dummy,content)
      .parseFullE(s->{b.append(s);return null;},s->Optional.empty());
    assertNull(res);
    Err.strCmp(expectedErr,b.toString());    
  }
  @Test void testMCall(){ ok("""
    MCall[
      receiver=x:infer,
      name=.m,
      ts=Optional.empty,
      es=[],
      t=infer
      ]
    """,
    "x.m"); }
  @Test void testTrue(){ ok(
    """
    Lambda[mdf=imm,
      its=[base.True[]],
      selfName=null,
      meths=[],
      t=infer]
    """, "base.True"); }
  @Test void testLamNoGens(){ ok(
      """
      Lambda[mdf=mut,
        its=[animals.Cat[]],
        selfName=null,
        meths=[],
        t=infer]
      """, "mut animals.Cat"); }
  @Test void testLamEmptyGens(){ ok(
      """
      Lambda[mdf=mut,
        its=[animals.Cat[]],
        selfName=null,
        meths=[],
        t=infer]
      """, "mut animals.Cat[]"); }
  @Test void testLamOneGens(){ ok(
      """
      Lambda[mdf=mut,
        its=[base.List[imm pkg1.Person]],
        selfName=null,
        meths=[],
        t=infer]
      """, "mut base.List[imm pkg1.Person]"); }
  @Test void testLamManyGens(){ ok(
      """
      Lambda[mdf=mut,
        its=[base.List[imm pkg1.Person]],
        selfName=null,
        meths=[],
        t=infer]
      """, "mut base.Either[imm pkg1.Person, mut pkg1.Blah]"); }
  @Test void surprisingNumberOfExprs(){ ok(
    """
      MCall[receiver=
        MCall[receiver=MCall[receiver=MCall[receiver=MCall[receiver=MCall[
          receiver=we:infer,
          name=.parse,
          ts=Optional.empty,
          es=[],
          t=infer],
          name=.a,
          ts=Optional.empty,
          es=[],
          t=infer],
          name=.surprising,
          ts=Optional.empty,
          es=[],
          t=infer],
          name=.amount,
          ts=Optional.empty,
          es=[],
          t=infer],
          name=.of,
          ts=Optional.empty,
          es=[],
          t=infer],
          name=.stuff,
          ts=Optional.empty,
          es=[],
          t=infer]
      """
    ,"we .parse .a .surprising .amount .of .stuff"); }
  @Test void testFail1(){ fail(
    "[###]Dummy.fear:1:3 mismatched input 'parse' expecting {'mut', 'lent', 'read', 'iso', 'recMdf', 'mdf', 'imm', FullCN}"
    ,"We parse a surprising amount of stuff"); }

}
