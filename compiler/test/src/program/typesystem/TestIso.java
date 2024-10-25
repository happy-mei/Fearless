package program.typesystem;

import org.junit.jupiter.api.Test;

import static program.typesystem.RunTypeSystem.ok;
import static program.typesystem.RunTypeSystem.fail;

public class TestIso {
  @Test void isoMethod1() {fail("""
    In position [###]/Dummy0.fear:3:2
    [E16 invalidMethMdf]
    iso is not a valid modifier for a method (on the method .m1/0).
    """, """
    package test
    A: {
      iso .m1: iso A -> this.m2,
      iso .m2: iso A -> this,
      }
    """);}
  @Test void isoMethod2() {fail("""
    In position [###]/Dummy0.fear:3:2
    [E16 invalidMethMdf]
    iso is not a valid modifier for a method (on the method .m1/0).
    """, """
    package test
    A: {
      iso .m1: iso A -> this,
      }
    """);}
}
