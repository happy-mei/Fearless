package program.typesystem;

import id.Mdf;
import net.jqwik.api.*;
import org.junit.jupiter.api.Test;

import static program.typesystem.RunTypeSystem.fail;
import static program.typesystem.RunTypeSystem.ok;

public class TestReadImm {
  private static final String BOX = """
    package test
    Box: {#[T](t: T): mut Box[T] -> {
      .get   -> t,
      .rget  -> t,
      .riget -> t,
      }}
    Box[T]: {
      mut .get: T,
      read .rget: read T,
      read .riget: read/imm T,
      }
    """;

  @Test void box() { ok(BOX); }

  @Test void captureAsImmMutGet() { ok("""
    package test
    A: {#(b: imm B): imm B -> Box#b.get }
    B: {}
    """, BOX); }
  @Test void captureAsImmReadImmGet() { ok("""
    package test
    A: {#(b: imm B): imm B -> Box#b.riget }
    B: {}
    """, BOX); }
  @Test void captureAsImmMutGetGen() { ok("""
    package test
    A: {#[B](b: imm B): imm B -> Box#b.get }
    """, BOX); }
  @Test void captureAsImmReadImmGetGen() { ok("""
    package test
    A: {#[B](b: imm B): imm B -> Box#b.riget }
    """, BOX); }

  @Test void immFromReadBox() { ok("""
    package test
    A: {#(b: read Box[imm B]): imm B -> b.riget }
    B: {}
    """, BOX); }
  @Test void immFromReadBoxMethGen() { ok("""
    package test
    A: {#[B](b: read Box[imm B]): imm B -> b.riget }
    """, BOX); }
  @Test void immFromReadBoxTypeGen() { ok("""
    package test
    A[B]: {#(b: read Box[imm B]): imm B -> b.riget }
    """, BOX); }


//  @Test void canCallReadFromReadImm() { ok("""
//    package test
//    A: {read .foo: B -> {}}
//    B: {}
//    Test: {#(a: read/imm A): B -> a.foo}
//    """); }

  @Test void boxIsoPromotionExplicit() { ok("""
    package test
    A: {#[S](s: imm S): iso Box[imm S] -> Box#s}
    """, BOX); }
  @Test void boxIsoPromotionWrong() { fail("""
    In position [###]/Dummy0.fear:2:33
    [E33 callTypeError]
    Type error: None of the following candidates (returning the expected type "mut test.Box[S]") for this method call:
    [-imm-][test.Box[]]{'fear[###]$ } #/1[S]([s])
    were valid:
    ([E28 undefinedName]) <= (imm test.Box[], S): mut test.Box[S]
      The following errors were found when checking this sub-typing:
        In position [###]/Dummy0.fear:2:34
        [E28 undefinedName]
        The identifier "s" is undefined or cannot be captured.
    
    ([E28 undefinedName]) <= (imm test.Box[], iso S): iso test.Box[S]
      The following errors were found when checking this sub-typing:
        In position [###]/Dummy0.fear:2:34
        [E28 undefinedName]
        The identifier "s" is undefined or cannot be captured.
    """, """
    package test
    A: {#[S](s: S): iso Box[S] -> Box#s}
    """, BOX); }

  @Provide Arbitrary<Mdf> capturableMdf() {
    return Arbitraries.of(Mdf.imm, Mdf.read, Mdf.readImm, Mdf.mut, Mdf.mdf);
  }
  @Provide Arbitrary<Mdf> recvMdf() {
    return Arbitraries.of(Mdf.imm, Mdf.read, Mdf.mut, Mdf.readOnly, Mdf.lent, Mdf.iso);
  }
  @Property void shouldGetAsIsForMut(@ForAll("capturableMdf") Mdf mdf) { ok("""
    package test
    A: {#[X](box: mut Box[%s X]): %s X -> box.get}
    """.formatted(mdf, mdf), BOX); }
  @Property void shouldGetAsReadForRead(@ForAll("capturableMdf") Mdf mdf) { ok("""
    package test
    A: {#[X](box: read Box[%s X]): read X -> box.rget}
    """.formatted(mdf), BOX); }

  @Property void shouldGetAsReadOrImmForReadImm(@ForAll("capturableMdf") Mdf mdf) {
    var expected = mdf.isImm() ? "imm" : "read";
    ok("""
    package test
    A: {#[X](box: read Box[%s X]): %s X -> box.riget}
    """.formatted(mdf, expected), BOX);
  }
  @Property void shouldGetAsReadOrImmForReadImmArbitraryRecvMdf(@ForAll("recvMdf") Mdf recvMdf, @ForAll("capturableMdf") Mdf mdf) {
    var expected = mdf.isImm() ? "imm" : "read";
    if (recvMdf.isHyg() && !mdf.isImm()) { expected = "readOnly"; }
    ok("""
    package test
    A: {#[X](box: %s Box[%s X]): %s X -> box.riget}
    B: {}
    """.formatted(recvMdf, mdf, expected), BOX);
  }
}
