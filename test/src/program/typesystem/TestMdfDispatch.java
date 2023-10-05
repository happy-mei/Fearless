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
}