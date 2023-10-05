package program.typesystem;

import org.junit.jupiter.api.Test;
import static program.typesystem.RunTypeSystem.fail;
import static program.typesystem.RunTypeSystem.ok;

public class TestMdfDispatch {
  @Test void inferringShouldWorkForASingleCandidate() { ok("""
    package test
    A:{
      .m1: A,
      .m2: A,
      }
    B:A{
      .m1 -> this,
      .m2 -> this.m1,
      }
    """); }
  @Test void inferringShouldFailWhenMultipleCandidates() { fail("""
    In position [###]/Dummy0.fear:8:2
    [E51 ambiguousMethodName]
    Unable to lookup the signature of the method: .m1/0. Multiple candidates exist with the same name and number of arguments.
    """, """
    package test
    A:{
      imm .m1: A,
      mut .m1: A,
      .m2: A,
      }
    B:A{
      .m1 -> this,
      .m2 -> this.m1,
      }
    """); }

  @Test void callingMultiSig() { ok("""
    package test
    A:{
      read .m1: read B -> {},
      mut .m1: mut A -> this,
      }
    B:{}
    Test:{
      read .aRead(a: read A): read B -> a.m1[](),
      mut .aMut(a: mut A): mut A -> a.m1[](),
      }
    """); }
  @Test void callingMultiSigFail() { fail("""
    In position [###]/Dummy0.fear:8:36
    [E32 noCandidateMeths]
    When attempting to type check the method call: a .m1/0[]([]), no candidates for .m1/0 returned the expected type mut test.A[]. The candidates were:
    (read test.A[]): read test.B[]
    (imm test.A[]): imm test.B[]
    (readOnly test.A[]): readOnly test.B[]
    """, """
    package test
    A:{
      read .m1: read B -> {},
      mut .m1: mut A -> this,
      }
    B:{}
    Test:{
      read .aRead(a: read A): mut A -> a.m1[](),
      mut .aMut(a: mut A): mut A -> a.m1[](),
      }
    """); }
  @Test void callingMultiSigAmbiguousDiffRet() { ok("""
    package test
    A:{
      read .m1: read B -> {},
      mut .m1: mut A -> this,
      }
    B:{}
    Test:{
      read .aRead(a: mut A): read B -> a.m1[](),
      }
    """); }
  @Test void callingMultiSigAmbiguousSameRet() { ok("""
    package test
    A:{
      read .m1: mut A -> {},
      mut .m1: mut A -> this,
      }
    B:{}
    Test:{
      read .aRead(a: mut A): mut A -> a.m1[](),
      }
    """); }
}