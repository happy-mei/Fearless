package program.inference;

import org.junit.jupiter.api.Test;

import static program.typesystem.RunTypeSystem.ok;

public class TestNorm {
  @Test void shouldWorkWithSameGens() {ok("""
    package test
    A: {.m1[X](x: X): X -> x}
    B: A{.m1[X](y: X): X -> y}
    """);}
  @Test void shouldWorkWithDifferentGens() {ok("""
    package test
    A: {.m1[X](x: X): X -> x}
    B: A{.m1[Y](y: Y): Y -> y}
    """);}

//  @Test void
}
