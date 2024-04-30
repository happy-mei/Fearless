package program.typesystem;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static program.typesystem.RunTypeSystem.ok;

public class TestBounds {
  private static final String BOX = """
    package test
    Box: {#[T](t: T): mut Box[T] -> {t}}
    Box[T]: {
      mut .get: T,
      read .rget: read T,
      read .riget: read/imm T,
      }
    """;
  // TODO: (Nick) I would like to see these work if it's sound.
  @Disabled
  @Test void boxIsoPromotionBounds() { ok("""
    package test
    A: {#[S:imm](s: S): iso Box[S] -> Box#s}
    """, BOX); }
  @Disabled
  @Test void boxIsoPromotionCall() { ok("""
    package test
    B:{#[Y](b: iso Y): mut B -> {}}
    A: {#[S:imm](s: S): mut B -> B#[mut Box[S]](Box#[S]s)}
    """, BOX); }
}
