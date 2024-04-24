package errors;

import org.junit.jupiter.api.Test;

import static program.typesystem.RunTypeSystem.ok;
import static program.typesystem.RunTypeSystem.fail;

public class TestMethodLookupErrors {
  @Test void methodThatExists() {ok("""
    package test
    A: {
      .meth1: A -> this,
      }
    B: {
      .callAMethod: A -> A.meth1,
      }
    """);}
  @Test void nonExistentMethod() {fail("""
    In position [###]/Dummy0.fear:6:26
    [E36 undefinedMethod]
    .meh1/0 does not exist in imm test.A[].
    """, """
    package test
    A: {
      .meth1: A -> this,
      }
    B: {
      .callNonExistent: A -> A.meh1,
      }
    """);}
  @Test void nonExistentMethodTypeSystem() {fail("""
    In position [###]/Dummy0.fear:6:2
    [E36 undefinedMethod]
    .meh1/0 does not exist in imm test.A[]. The following methods exist on that type: imm .meth1/0
    """, """
    package test
    A: {
      .meth1: A -> this,
      }
    B: {
      .callNonExistent: A -> A.meh1[],
      }
    """);}
}
