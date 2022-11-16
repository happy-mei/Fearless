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
        its=[base.List[imm pkg1.Person[]]],
        selfName=null,
        meths=[],
        t=infer]
      """, "mut base.List[imm pkg1.Person]"); }
  @Test void testLamManyGens(){ ok(
      """
      Lambda[mdf=mut,
        its=[base.Either[imm pkg1.Person[], mut pkg1.Blah[]]],
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
  @Test void singleEqSugar1(){ ok(
    """
    
    """, "recv .m1 v = val"); }
  @Test void singleEqSugarPOp1(){ ok(
    """
    MCall[
      receiver=recv:infer,
      name=.m1,
      ts=Optional.empty,
      es=[
        val:infer,
        Lambda[
          mdf=mdf,
          its=[],
          selfName=null,
          meths=[
            [-]([v:infer,fearIntrinsic0:infer]):[-]->fearIntrinsic0:infer],
            t=infer
      ]],t=infer]
    """, "recv .m1 (v = val)"); }
  @Test void singleEqSugarPOp2(){ ok(
    """
    MCall[
      receiver=recv:infer,
      name=.m1,
      ts=Optional.empty,
      es=[
        val:infer,
        Lambda[
          mdf=mdf,
          its=[],
          selfName=null,
          meths=[
            [-]([v:infer,fearIntrinsic0:infer]):[-]->
              MCall[
                receiver=fearIntrinsic0:infer,
                name=.m2,
                ts=Optional.empty,
                es=[],
                t=infer]],
            t=infer
      ]],t=infer]
    """, "recv .m1 (v = val) .m2"); }
  @Test void singleEqSugarPOp3(){ ok(
    """
    MCall[
      receiver=recv:infer,
      name=.m1,
      ts=Optional.empty,
      es=[MCall[
        receiver=val:infer,
        name=.m2,
        ts=Optional.empty,
        es=[],
        t=infer],
        Lambda[
          mdf=mdf,
          its=[],
          selfName=null,
          meths=[
            [-]([v:infer,fearIntrinsic0:infer]):[-]->MCall[
              receiver=fearIntrinsic0:infer,
              name=.m3,
              ts=Optional.empty,
              es=[],
              t=infer]],
          t=infer]],
      t=infer]
    """, "recv .m1 (v = val .m2) .m3"); }
  @Test void singleEqSugar2(){ ok(
    """
    
    """, "recv .m1 v = val .m2"); }
}
