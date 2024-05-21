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
    extra info for experts:
    [test.A[],imm .meth1/0()[][]:imm test.A[] impl]
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
    In position [###]/Dummy0.fear:6:26
    [E36 undefinedMethod]
    .meh1/0 does not exist in imm test.Fear1$[]. The following methods exist on that type: N/A
    extra info for experts:
    [test.A[],imm .meth1/0()[][]:imm test.A[] impl]
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
