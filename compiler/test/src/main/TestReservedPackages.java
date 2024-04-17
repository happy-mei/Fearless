package main;

import org.junit.jupiter.api.Test;

import static codegen.java.RunJavaProgramTests.fail;

public class TestReservedPackages {
  @Test void shouldNotAllowBasePackages() {fail("""
    [E60 specialPackageConflict]
    The following package names are reserved for use in the Fearless standard library
    conflicts:
    ([###]/Dummy0.fear:0:0) base
    """, """
    package base
    A: {}
    """);}
  @Test void shouldNotAllowRtPackages() {fail("""
    [E60 specialPackageConflict]
    The following package names are reserved for use in the Fearless standard library
    conflicts:
    ([###]/Dummy0.fear:0:0) rt
    """, """
    package rt
    A: {}
    """);}
  @Test void shouldNotAllowMultiBadPackages() {fail("""
    [E60 specialPackageConflict]
    The following package names are reserved for use in the Fearless standard library
    conflicts:
    ([###]/Dummy0.fear:0:0) rt
    ([###]/Dummy2.fear:0:0) base
    """, """
    package rt
    A: {}
    """, """
    package ok
    A: {}
    """, """
    package base
    A: {}
    """);}
}
