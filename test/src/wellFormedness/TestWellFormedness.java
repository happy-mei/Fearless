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
  @Test void recMdfAllowedInHyg() { ok("""
    package base
    A[X]:{ read .foo(): recMdf X }
    B[X]:{ lent .foo(): recMdf X }
    C[X]:{ lent .foo(c: recMdf X): recMdf X -> c }
    """); }
  @Test void recMdfAllowedInSubHyg() { ok("""
    package base
    A[X]:{ .foo(x: X): X -> B[X]{ x }.argh }
    B[X]:{ read .argh: recMdf X }
    """); }
  @Test void noRecMdfInNonReadRet() { fail("""
    In position [###]/Dummy0.fear:2:7
    [E26 recMdfInNonHyg]
    Invalid modifier for recMdf X.
    recMdf may only be used in read or lent methods. The method .foo/0 has the imm modifier.
    """, """
    package base
    A[X]:{ .foo(): recMdf X }
    """); }
  @Test void noRecMdfInNonReadRetNested() { fail("""
    In position [###]/Dummy0.fear:2:7
    [E26 recMdfInNonHyg]
    Invalid modifier for recMdf X.
    recMdf may only be used in read or lent methods. The method .foo/0 has the imm modifier.
    """, """
    package base
    A[X]:{ .foo(): A[recMdf X] }
    """); }
  @Test void noRecMdfInNonReadArgs() { fail("""
    In position [###]/Dummy0.fear:3:7
    [E26 recMdfInNonHyg]
    Invalid modifier for recMdf base.Foo[].
    recMdf may only be used in read or lent methods. The method .foo/1 has the imm modifier.
    """, """
    package base
    Foo:{}
    A[X]:{ .foo(f: recMdf Foo): Foo -> f }
    """); }
  @Test void noRecMdfInNonReadArgsNested() { fail("""
    In position [###]/Dummy0.fear:3:7
    [E26 recMdfInNonHyg]
    Invalid modifier for recMdf X.
    recMdf may only be used in read or lent methods. The method .foo/1 has the imm modifier.
    """, """
    package base
    Foo:{}
    A[X]:{ .foo(f: A[recMdf X]): Foo -> f }
    """); }

  @Test void explicitMdfLambdaRecMdf1(){ ok("""
    package test
    Foo:{}
    Bar:{ read .a: recMdf Foo -> recMdf Foo }
    """); }
  @Test void explicitMdfLambdaRecMdf2(){ ok("""
    package test
    Foo:{}
    Bar:{ lent .a: recMdf Foo -> recMdf Foo }
    """); }
  @Test void explicitMdfLambdaRecMdfONonHyg1(){ fail("""
    In position [###]/Dummy0.fear:3:6
    [E26 recMdfInNonHyg]
    Invalid modifier for recMdf test.Foo[].
    recMdf may only be used in read or lent methods. The method .a/0 has the imm modifier.
    """, """
    package test
    Foo:{}
    Bar:{ .a: recMdf Foo -> recMdf Foo }
    """); }
  @Test void explicitMdfLambdaRecMdfONonHyg2(){ fail("""
    In position [###]/Dummy0.fear:3:24
    [E26 recMdfInNonHyg]
    Invalid lambda modifier.
    recMdf may only be used in read or lent methods. The method .a/0 has the imm modifier.
    """, """
    package test
    Foo:{}
    Bar:{ .a: Foo -> recMdf Foo }
    """); }

  @Test void sealedOutsidePkg() { fail("""
    In position [###]/Dummy1.fear:2:2
    [E35 sealedCreation]
    The sealed trait a.A/0 cannot be created in a different package (b).
    """, """
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
  @Test void sealedOutsidePkgNested() { fail("""
    In position [###]/Dummy1.fear:2:2
    [E35 sealedCreation]
    The sealed trait a.A/0 cannot be created in a different package (b).
    """, """
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
    A:Sealed{ .a: Foo -> {} }
    B:A{}
    Foo:{}
    """, """
    package b
    alias a.A as A, alias a.Foo as Foo,
    C:{
      .foo(): Foo -> A.a
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

  @Test void noMutHygType1() { fail("""
    In position [###]/Dummy0.fear:2:7
    [E40 mutCapturesHyg]
    The type mut a.A[read a.A[imm X]] is not valid because a mut lambda may not capture hygienic references.
    """, """
    package a
    A[X]:{ .no: mut A[read A[X]] }
    """); }
  @Test void noMutHygType2() { fail("""
    In position [###]/Dummy0.fear:2:7
    [E40 mutCapturesHyg]
    The type mut a.A[lent a.A[imm X]] is not valid because a mut lambda may not capture hygienic references.
    """, """
    package a
    A[X]:{ .no: mut A[lent A[X]] }
    """); }
  @Test void noMutHygType3() { ok("""
    package a
    A[X]:{ .no: mut A[X] }
    """); }
  @Test void noMutHygType4() { ok("""
    package a
    A[X]:{ .no: read A[read A[X]] }
    """); }

  @Property void recMdfRetOnlyOnReadOrLentHappy(@ForAll("hygMdf") Mdf mdf) { ok(String.format("""
    package test
    A:{ %s .foo: recMdf Res }
    Res:{}
    """, mdf)); }
  @Property void recMdfRetOnlyOnReadOrLentSad(@ForAll("nonHygMdf") Mdf mdf) { fail(String.format("""
    In position [###]/Dummy0.fear:2:4
    [E26 recMdfInNonHyg]
    Invalid modifier for recMdf test.Res[].
    recMdf may only be used in read or lent methods. The method .foo/0 has the %s modifier.
    """, mdf), String.format("""
    package test
    A:{ %s .foo: recMdf Res }
    Res:{}
    """, mdf));}

  @Provide Arbitrary<Mdf> hygMdf() {
    return Arbitraries.of(Mdf.read, Mdf.lent);
  }
  @Provide Arbitrary<Mdf> nonHygMdf() {
    return Arbitraries.of(Arrays.stream(Mdf.values()).filter(mdf->!mdf.isHyg() && !mdf.is(Mdf.mdf, Mdf.recMdf)).toList());
  }
}
