package codegen.mir2;

import ast.E;
import codegen.MIRInjectionVisitor;
import failure.CompileError;
import main.Main;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import parser.Parser;
import program.TypeSystemFeatures;
import program.inference.InferBodies;
import program.typesystem.EMethTypeSystem;
import utils.Err;
import wellFormedness.WellFormednessFullShortCircuitVisitor;
import wellFormedness.WellFormednessShortCircuitVisitor;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class TestMIR {
  void ok(String expected, String... content){
    assert content.length > 0;
    Main.resetAll();
    AtomicInteger pi = new AtomicInteger();
    var ps = Arrays.stream(content)
      .map(code -> new Parser(Path.of("Dummy"+pi.getAndIncrement()+".fear"), code))
      .toList();
    var p = Parser.parseAll(ps, new TypeSystemFeatures());
    new WellFormednessFullShortCircuitVisitor().visitProgram(p).ifPresent(err->{ throw err; });
    var inferred = InferBodies.inferAll(p);
    new WellFormednessShortCircuitVisitor(inferred).visitProgram(inferred);
    IdentityHashMap<E.MCall, EMethTypeSystem.TsT> resolvedCalls = new IdentityHashMap<>();
    inferred.typeCheck(resolvedCalls);
    var mir = new MIRInjectionVisitor(inferred, resolvedCalls).visitProgram();
    Err.strCmpFormat(expected, mir.toString());
  }
  void fail(String expectedErr, String... content){
    assert content.length > 0;
    Main.resetAll();
    AtomicInteger pi = new AtomicInteger();
    var ps = Arrays.stream(content)
      .map(code -> new Parser(Path.of("Dummy"+pi.getAndIncrement()+".fear"), code))
      .toList();
    var p = Parser.parseAll(ps, new TypeSystemFeatures());
    new WellFormednessFullShortCircuitVisitor().visitProgram(p).ifPresent(err->{ throw err; });
    var inferred = InferBodies.inferAll(p);
    IdentityHashMap<E.MCall, EMethTypeSystem.TsT> resolvedCalls = new IdentityHashMap<>();
    inferred.typeCheck(resolvedCalls);
    try {
      var res = new MIRInjectionVisitor(inferred, resolvedCalls).visitProgram();
      Assertions.fail("Did not fail, got:\n" + res);
    } catch (CompileError e) {
      Err.strCmp(expectedErr, e.toString());
    }
  }

  @Test void emptyProgram() { ok("""
    """, """
    package test
    A:{ .foo(a: A): A -> this.foo({}) }
    """);}
}
