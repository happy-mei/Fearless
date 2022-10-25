package parser;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import utils.Bug;
import utils.Err;

class TestParser {
  void ok(String expected, String content){
    String res = new Parser(Parser.dummy,content)
      .parseFullE(Bug::err)
      .toString();
    assertEquals(expected,res);
  }
  void fail(String expectedErr,String content){
    var b=new StringBuffer();
    var res=new Parser(Parser.dummy,content)
      .parseFullE(s->{b.append(s);return null;});
    assertNull(res);
    Err.strCmp(expectedErr,b.toString());    
  }
  @Test void testMCall(){ ok("MCall[]","MCall"); }
  @Test void testX(){ ok("X[]","X"); }
  @Test void testLambda(){ ok("Lambda[]","Lambda"); }
  @Test void testFail1(){ fail(
    "file:[###]Dummy:1(col=0)token recognition error at: 'N'"
   +"file:[###]Dummy:1(col=1)token recognition error at: 'O'"
   +"file:[###]Dummy:1(col=2)token recognition error at: 'P'"
   +"file:[###]Dummy:1(col=3)token recognition error at: 'E'"
    ,"NOPE"); }

}
