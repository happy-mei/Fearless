package program.typesystem;

import org.junit.jupiter.api.Test;

import static program.typesystem.RunTypeSystem.ok;

public class TestBounds {
  private static final String BOX = """
    package test
    Box: {#[T](t: T): mut Box[T] -> {
      .get -> t,
      .rget -> t,
      .riget -> t,
      }}
    Box[T]: {
      mut .get: T,
      read .rget: read T,
      read .riget: read/imm T,
      }
    """;
  @Test void boxIsoPromotionBounds() { ok("""
    package test
    A: {#[S:imm](s: S): iso Box[S] -> Box#s}
    """, BOX); }
  @Test void boxIsoPromotionCall() { ok("""
    package test
    B:{#[Y](b: iso Y): mut B -> {}}
    A: {#[S:imm](s: S): mut B -> B#[mut Box[S]](Box#[S]s)}
    """, BOX); }
}
