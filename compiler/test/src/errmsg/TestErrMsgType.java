package errmsg;

import org.junit.jupiter.api.Test;

import static program.typesystem.RunTypeSystem.ok;
import static program.typesystem.RunTypeSystem.fail;

public class TestErrMsgType {
  @Test void methodThatExists() {ok("""
    package test
    A: {
      .meth1: A -> this,
      }
    B: {
      .callAMethod: A -> A.meth1,
      }
    """);}

  @Test void nonExistentMethod() {
    fail("""
    In position [###]/Dummy0.fear:6:26
    [E36 undefinedMethod]
    Method .meh1 does not exist in imm test.A[]
    Did you mean .meth1?
    
    Other candidates:
    test.A[].meth1
    """, """
    package test
    A: {
      .meth1: A -> this,
      }
    B: {
      .callNonExistent: A -> A.meh1,
      }
    """);
  }

  @Test void multipleAlternatives() {fail("""
   In position [###]/Dummy0.fear:9:26
   [E36 undefinedMethod]
   Method .meh1 does not exist in imm test.A[]
   Did you mean .meth1?
   
   Other candidates:
   test.A[].meth1
   test.A[].meth2
   test.A[].randommeth
   test.A[].k
   """, """
    package test
    A: {
      .meth1: A -> this,
      .randommeth: A -> this,
      .k: A -> this,
      .meth2: A -> this,
      }
    B: {
      .callNonExistent: A -> A.meh1,
      }
    """);}
}
