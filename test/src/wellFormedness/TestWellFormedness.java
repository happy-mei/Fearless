package wellFormedness;

import failure.CompileError;
import id.Mdf;
import main.Main;
import net.jqwik.api.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import parser.Parser;
import program.inference.InferBodies;
import utils.Base;
import utils.Err;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static program.typesystem.RunTypeSystem.ok;

public class TestWellFormedness {
  void ok(String... content){
    Main.resetAll();
    AtomicInteger pi = new AtomicInteger();
    var ps = Arrays.stream(content)
      .map(code -> new Parser(Path.of("Dummy"+pi.getAndIncrement()+".fear"), code))
      .toList();
    var p = Parser.parseAll(ps);
    new WellFormednessFullShortCircuitVisitor().visitProgram(p).ifPresent(err->{ throw err; });
    var inferredSigs = p.inferSignaturesToCore();
    var inferred = new InferBodies(inferredSigs).inferAll(p);
    var res = new WellFormednessShortCircuitVisitor(inferred).visitProgram(inferred);
    var isWellFormed = res.isEmpty();
    assertTrue(isWellFormed, res.map(Object::toString).orElse(""));
  }
  void fail(String expectedErr, String... content){
    Main.resetAll();
    AtomicInteger pi = new AtomicInteger();
    var ps = Arrays.stream(content)
      .map(code -> new Parser(Path.of("Dummy"+pi.getAndIncrement()+".fear"), code))
      .toList();
    var p = Parser.parseAll(ps);
    new WellFormednessFullShortCircuitVisitor().visitProgram(p).ifPresent(err->{ throw err; });
    var inferredSigs = p.inferSignaturesToCore();
    var inferred = new InferBodies(inferredSigs).inferAll(p);

    try {
      var error = new WellFormednessShortCircuitVisitor(inferred).visitProgram(inferred);
      if (error.isEmpty()) { Assertions.fail("Did not fail"); }
      Err.strCmp(expectedErr, error.map(Object::toString).orElseThrow());
    } catch (CompileError e) {
      Err.strCmp(expectedErr, e.toString());
    }
  }

  @Test void sealedOutsidePkg() { fail("""
    In position [###]/Dummy1.fear:2:2
    [E35 sealedCreation]
    The sealed trait a.A/0 cannot be created in a different package (b).
    """, """
    package a
    alias base.Sealed as Sealed,
    A:Sealed{}
    B:A{ .m1: A }
    """, """
    package b
    C:a.A{ .m1: a.A }
    """, """
    package base
    Sealed:{}
    """); }
  @Test void sealedOutsidePkgNested() { fail("""
    In position [###]/Dummy1.fear:2:2
    [E35 sealedCreation]
    The sealed trait a.A/0 cannot be created in a different package (b).
    """, """
    package a
    alias base.Sealed as Sealed,
    A:Sealed{}
    B:A{ .m1: A }
    """, """
    package b
    C:a.B{ this }
    """, """
    package base
    Sealed:{}
    """); }
  @Test void sealedOutsidePkgNoOverrides() { ok("""
    package a
    alias base.Sealed as Sealed,
    A:Sealed{}
    B:A{}
    """, """
    package b
    C:a.A{}
    """, """
    package base
    Sealed:{}
    """); }
  @Test void sealedOutsidePkgNestedNoOverrides() { ok("""
    package a
    alias base.Sealed as Sealed,
    A:Sealed{}
    B:A{}
    """, """
    package b
    C:a.B{}
    """, """
    package base
    Sealed:{}
    """); }
  @Test void sealedOutsidePkgInline() { fail("""
    In position [###]/Dummy1.fear:4:17
    [E35 sealedCreation]
    The sealed trait a.A/0 cannot be created in a different package (b).
    """, """
    package a
    alias base.Sealed as Sealed,
    A:Sealed{ .a: Foo }
    B:A{ .a -> {} }
    Foo:{}
    """, """
    package b
    alias a.A as A, alias a.Foo as Foo,
    C:{
      .foo(): Foo -> a.A{ Foo }.a
      }
    """, """
    package base
    Sealed:{}
    """); }
  @Test void sealedOutsidePkgConstructor() { ok("""
    package a
    alias base.Sealed as Sealed,
    A:Sealed{ .a: Foo -> {} }
    A':{ #: A -> {} }
    B:A{}
    Foo:{}
    """, """
    package b
    alias a.A' as A', alias a.Foo as Foo,
    C:{
      .foo(): Foo -> A'#.a
      }
    """, """
    package base
    Sealed:{}
    """); }

  // This well formedness requirement doesn't make sense because it only considers boxes (but not functions that do not capture)
//  @Test void noMutHygType1() { fail("""
//    In position [###]/Dummy0.fear:2:7
//    [E40 mutCapturesHyg]
//    The type mut a.A[read a.A[imm X]] is not valid because a mut lambda may not capture hygienic references.
//    """, """
//    package a
//    A[X]:{ .no: mut A[read A[X]] }
//    """); }
//  @Test void noMutHygType2() { fail("""
//    In position [###]/Dummy0.fear:2:7
//    [E40 mutCapturesHyg]
//    The type mut a.A[lent a.A[imm X]] is not valid because a mut lambda may not capture hygienic references.
//    """, """
//    package a
//    A[X]:{ .no: mut A[lent A[X]] }
//    """); }
  @Test void noMutHygType3() { ok("""
    package a
    A[X]:{ .no: mut A[X] }
    """); }
  @Test void noMutHygType4() { ok("""
    package a
    A[X]:{ .no: read A[read A[X]] }
    """); }

  @Test void noRecMdfInImplements() { fail("""
    In position [###]/Dummy0.fear:3:5
    [E27 recMdfInImpls]
    Invalid modifier for recMdf Y.
    recMdf may not be used in the list of implemented traits.
    """, """
    package base
    A[X]:{}
    B[Y]:A[recMdf Y]{}
    """); }

  @Test void noIsoMoreThanOnce() { fail("""
    In position [###]/Dummy0.fear:3:63
    [E45 multipleIsoUsage]
    The isolated reference "x1" is used more than once.
    """, """
    package test
    Caps:{} Void:{}
    A:{ .break(x1: iso Caps, x2: iso Caps): Void -> this.break(x1, x1) }
    """); }
  @Test void isoOnce() { ok("""
    package test
    Caps:{} Void:{}
    A:{ .notBreak(x1: iso Caps, x2: iso Caps): Void -> this.notBreak(x1, x2) }
    """); }
}
