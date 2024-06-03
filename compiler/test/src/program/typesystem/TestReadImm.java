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

  @Provide Arbitrary<Mdf> capturableMdf() {
    return Arbitraries.of(Mdf.imm, Mdf.read, Mdf.readImm, Mdf.mut, Mdf.mdf);
  }
  @Provide Arbitrary<Mdf> recvMdf() {
    return Arbitraries.of(Mdf.imm, Mdf.read, Mdf.mut, Mdf.readH, Mdf.mutH, Mdf.iso);
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

  /* parameter of read X, pass a mdf Y --> becomes read Y */
  @Test void readParamMdfArg() {ok("""
    package test
    A: {.m[X](x: read X): read X -> x}
    B: {.m[Y](y: Y): read Y -> A.m[Y](y)}
    C: {.m[Y](y: read Y): read Y -> A.m[Y](y)}
    """);}
  @Test void readParamMdfArgAIsReadImm() {fail("""
    [E66 invalidMethodArgumentTypes]
    Method .m/1 called in position [###]/Dummy0.fear:3:37 can not be called with current parameters of types:
    [read Y]
    Attempted signatures:
    (imm Y):imm Y kind: IsoHProm
    (imm Y):imm Y kind: IsoProm
    (read/imm Y):read/imm Y kind: Base
    """, """
    package test
    A: {.m[X](x: read/imm X): read/imm X -> x}
    C: {.m[Y](y: read Y): read/imm Y -> A.m[Y](y)}
    D: {.m(foo: read Foo): imm Foo -> C.m[imm Foo](foo)} // unsound
    Foo: {}
    """);}
  @Test void readParamMdfArgAIsReadImmOnTrait() {fail("""
    [E66 invalidMethodArgumentTypes]
    Method .m/1 called in position [###]/Dummy0.fear:3:52 can not be called with current parameters of types:
    [read Y]
    Attempted signatures:
    (imm Y):imm Y kind: IsoHProm
    (imm Y):imm Y kind: IsoProm
    (read/imm Y):read/imm Y kind: Base
    """, """
    package test
    A[X]: {.m(x: read/imm X): read/imm X -> x}
    B[Y]: {.m(y: read Y): read/imm Y -> BrokenA[Y]: A[Y].m(y)}
    C: {.m(foo: read Foo): imm Foo -> BrokenB: B[imm Foo].m(foo)} // unsound
    Foo: {}
    """);}
}
