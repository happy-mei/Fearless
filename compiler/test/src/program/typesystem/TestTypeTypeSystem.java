package program.typesystem;

import ast.Program;
import failure.CompileError;
import id.Id;
import id.Mdf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import parser.Parser;
import program.TypeSystemFeatures;
import program.inference.InferBodies;
import utils.Err;
import wellFormedness.WellFormednessFullShortCircuitVisitor;
import wellFormedness.WellFormednessShortCircuitVisitor;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class TestTypeTypeSystem {
  private static void ok(XBs xbs, String t, Set<Mdf> expected, String... content) {
    var fullT = new Parser(Parser.dummy, t).parseFullT();
    var coreT = fullT.toAstT();
    var p = toProgram(content);
    var inferred = coreT.accept(new TypeTypeSystem(p, xbs, expected)).get();
    Assertions.assertEquals(expected, inferred);
  }
  private static void fail(String expectedErr, XBs xbs, String t, Set<Mdf> expected, String... content) {
    var fullT = new Parser(Parser.dummy, t).parseFullT();
    var coreT = fullT.toAstT();
    var p = toProgram(content);
    try {
      coreT.accept(new TypeTypeSystem(p, xbs, expected)).get();
      Assertions.fail("Did not fail!\n");
    } catch (CompileError e) {
      Err.strCmp(expectedErr, e.toString());
    }
  }
  private static Program toProgram(String... content) {
    AtomicInteger pi = new AtomicInteger();
    var ps = Arrays.stream(content)
      .map(code -> new Parser(Path.of("Dummy"+pi.getAndIncrement()+".fear"), code))
      .toList();
    var p = Parser.parseAll(ps, TypeSystemFeatures.of());
    new WellFormednessFullShortCircuitVisitor().visitProgram(p).ifPresent(err->{ throw err; });
    var inferred = InferBodies.inferAll(p);
    new WellFormednessShortCircuitVisitor(inferred).visitProgram(inferred).ifPresent(err->{ throw err; });
    inferred.typeCheck(new ConcurrentHashMap<>());
    return inferred;
  }

  @Test void mutSubsumption() {ok(
    XBs.empty(),
    "mut X",
    Set.of(Mdf.mut, Mdf.imm, Mdf.read)
  );}
  @Test void noMutInExpected() {fail("""
    [E5 invalidMdfBound]
    The type mut X is not valid because its capability is not in the required bounds. The allowed modifiers are: read, imm.
    """,
    XBs.empty(),
    "mut X",
    Set.of(Mdf.imm, Mdf.read)
  );}

  @Test void mutSubsumptionX() {ok(
    XBs.empty()
      .addBounds(List.of(new Id.GX<>("X")), Map.of(new Id.GX<>("X"), Set.of(Mdf.mut))),
    "X",
    Set.of(Mdf.mut, Mdf.imm, Mdf.read)
  );}
  @Test void noMutInExpectedX() {fail("""
    [E5 invalidMdfBound]
    The type mut X is not valid because its capability is not in the required bounds. The allowed modifiers are: read, imm.
    """,
    XBs.empty()
      .addBounds(List.of(new Id.GX<>("X")), Map.of(new Id.GX<>("X"), Set.of(Mdf.mut))),
    "mut X",
    Set.of(Mdf.imm, Mdf.read)
  );}
  @Test void multiBoundsXFail() {fail("""
    [E5 invalidMdfBound]
    The type X is not valid because its capability is not in the required bounds. The allowed modifiers are: read, mut, imm.
    """,
    XBs.empty()
      .addBounds(List.of(new Id.GX<>("X")), Map.of(new Id.GX<>("X"), Set.of(Mdf.mut, Mdf.iso))),
    "X",
    Set.of(Mdf.mut, Mdf.imm, Mdf.read)
  );}
  @Test void multiBoundsX() {ok(
    XBs.empty()
      .addBounds(List.of(new Id.GX<>("X")), Map.of(new Id.GX<>("X"), Set.of(Mdf.mut, Mdf.imm, Mdf.read))),
    "X",
    Set.of(Mdf.mut, Mdf.imm, Mdf.read)
  );}
  @Test void readImm() {ok(
    XBs.empty()
      .addBounds(List.of(new Id.GX<>("X")), Map.of(new Id.GX<>("X"), Set.of(Mdf.mut, Mdf.imm, Mdf.read))),
    "read/imm X",
    Set.of(Mdf.read, Mdf.imm)
  );}
  @Test void readImmImmInBounds() {ok(
    XBs.empty()
      .addBounds(List.of(new Id.GX<>("X")), Map.of(new Id.GX<>("X"), Set.of(Mdf.imm))),
    "read/imm X",
    Set.of(Mdf.imm)
  );}
  @Test void readImmImmAndIsoInBounds() {ok(
    XBs.empty()
      .addBounds(List.of(new Id.GX<>("X")), Map.of(new Id.GX<>("X"), Set.of(Mdf.imm, Mdf.iso))),
    "read/imm X",
    Set.of(Mdf.imm)
  );}
  @Test void readImmWillOnlyBeImm() {fail("""
    [E5 invalidMdfBound]
    The type read/imm X is not valid because its capability is not in the required bounds. The allowed modifiers are: read.
    """,
    XBs.empty()
      .addBounds(List.of(new Id.GX<>("X")), Map.of(new Id.GX<>("X"), Set.of(Mdf.imm, Mdf.iso))),
    "read/imm X",
    Set.of(Mdf.read)
  );}
  @Test void readImmReadInBounds() {ok(
    XBs.empty()
      .addBounds(List.of(new Id.GX<>("X")), Map.of(new Id.GX<>("X"), Set.of(Mdf.mut, Mdf.read))),
    "read/imm X",
    Set.of(Mdf.read)
  );}

  @Test void immIsoLimitX() {ok(
    XBs.empty()
      .addBounds(List.of(new Id.GX<>("X")), Map.of(new Id.GX<>("X"), Set.of(Mdf.imm))),
    "X",
    Set.of(Mdf.imm, Mdf.iso)
  );}
  @Test void immIsoLimitXFail() {fail("""
    [E5 invalidMdfBound]
    The type X is not valid because its capability is not in the required bounds. The allowed modifiers are: iso, imm.
    """,
    XBs.empty()
      .addBounds(List.of(new Id.GX<>("X")), Map.of(new Id.GX<>("X"), Set.of(Mdf.imm, Mdf.mut, Mdf.read))),
    "X",
    Set.of(Mdf.imm, Mdf.iso)
  );}

  @Test void literalOk() {ok(
    XBs.empty(),
    "mut a.A[]",
    Set.of(Mdf.mut, Mdf.read, Mdf.imm),
    """
      package a
      A: {}
      """
  );}
  @Test void literalFail() {fail("""
    [E5 invalidMdfBound]
    The type mut a.A[] is not valid because its capability is not in the required bounds. The allowed modifiers are: imm.
    """,
    XBs.empty(),
    "mut a.A[]",
    Set.of(Mdf.imm),
    """
      package a
      A: {}
      """
  );}

  @Test void literalGenOk() {ok(
    XBs.empty(),
    "mut a.A[a.B[]]",
    Set.of(Mdf.mut, Mdf.read, Mdf.imm),
    """
      package a
      A[X]: {}
      B: {}
      """
  );}

  @Test void literalImplOk() {ok(
    XBs.empty(),
    "mut a.A[a.B[]]",
    Set.of(Mdf.mut, Mdf.read, Mdf.imm),
    """
      package a
      A[X]: Z[X]{}
      Z[X]: {}
      B: {}
      """
  );}
  @Test void literalImplFail1() {fail("""
    [E5 invalidMdfBound]
    The type mut a.B[] is not valid because its capability is not in the required bounds. The allowed modifiers are: imm.
    """,
    XBs.empty(),
    "mut a.A[mut a.B[]]",
    Set.of(Mdf.mut, Mdf.read, Mdf.imm),
    """
      package a
      A[X:imm]: Z[X]{}
      Z[X:imm]: {}
      B: {}
      """
  );}
}
