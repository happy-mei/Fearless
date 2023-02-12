package wellFormedness;

import failure.CompileError;
import main.Main;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import parser.Parser;
import program.inference.InferBodies;
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
    var res = new WellFormednessShortCircuitVisitor().visitProgram(inferred);
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
    var inferredSigs = p.inferSignaturesToCore();
    var inferred = new InferBodies(inferredSigs).inferAll(p);

    try {
      var error = new WellFormednessShortCircuitVisitor().visitProgram(inferred);
      if (error.isEmpty()) { Assertions.fail("Did not fail"); }
      Err.strCmp(expectedErr, error.map(Object::toString).orElseThrow());
    } catch (CompileError e) {
      Err.strCmp(expectedErr, e.toString());
    }
  }

  @Test
  void noRecMdfInImplements() { fail("""
    In position [###]/Dummy0.fear:3:5
    [E27 recMdfInImpls]
    Invalid modifier for recMdf Y.
    recMdf may not be used in the list of implemented traits.
    """, """
    package base
    A[X]:{}
    B[Y]:A[recMdf Y]{}
    """); }
  @Test
  void recMdfAllowedInHyg() { ok("""
    package base
    A[X]:{ read .foo(): recMdf X }
    B[X]:{ lent .foo(): recMdf X }
    C[X]:{ lent .foo(c: recMdf X): recMdf X -> c }
    """); }
  @Test
  void recMdfAllowedInSubHyg() { ok("""
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
}
