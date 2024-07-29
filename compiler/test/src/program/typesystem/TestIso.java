package program.typesystem;

import org.junit.jupiter.api.Test;

import static program.typesystem.RunTypeSystem.ok;
import static program.typesystem.RunTypeSystem.fail;

public class TestIso {
  @Test void isoMethod1() {ok("""
    package test
    A: {
      iso .m1: iso A -> this.m2,
      iso .m2: iso A -> this,
      }
    """);}
  @Test void isoMethod2() {ok("""
    package test
    A: {
      iso .m1: iso A -> this,
      }
    """);}
  @Test void isoMethod3() {fail("""
    In position [###]/Dummy0.fear:3:28
    [E45 multipleIsoUsage]
    The isolated reference "this" is used more than once.
    """, """
    package test
    A: {
      iso .m1: iso A -> this.m2(this),
      iso .m2(a: iso A): iso A -> this,
      }
    """);}
  @Test void isoAsMut() {ok("""
    package test
    A: {
      iso .m1: mut A -> this.m2(this),
      mut .m2(a: mut A): mut A -> this,
      }
    """);}
}
