package codegen.java;

import ast.E;
import codegen.MIRInjectionVisitor;
import failure.CompileError;
import id.Id;
import main.Main;
import org.junit.jupiter.api.Assertions;
import parser.Parser;
import program.TypeSystemFeatures;
import program.inference.InferBodies;
import program.typesystem.EMethTypeSystem;
import utils.Base;
import utils.Bug;
import utils.Err;
import utils.RunJava;
import wellFormedness.WellFormednessFullShortCircuitVisitor;
import wellFormedness.WellFormednessShortCircuitVisitor;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class RunJavaProgramTests {
  public static void ok(RunJava.Res expected, String entry, String... content) {
    okWithArgs(expected, entry, List.of(), content);
  }
  public static void okWithArgs(RunJava.Res expected, String entry, List<String> args, String... content) {
    assert content.length > 0;
    Main.resetAll();
    AtomicInteger pi = new AtomicInteger();
    var ps = Stream.concat(Arrays.stream(content), Arrays.stream(Base.baseLib))
      .map(code->new Parser(Path.of("Dummy" + pi.getAndIncrement() + ".fear"), code))
      .toList();
    var p = Parser.parseAll(ps, new TypeSystemFeatures());
    new WellFormednessFullShortCircuitVisitor().visitProgram(p).ifPresent(err->{
      throw err;
    });
    var inferred = InferBodies.inferAll(p);
    new WellFormednessShortCircuitVisitor(inferred).visitProgram(inferred).ifPresent(err->{
      throw err;
    });
    IdentityHashMap<E.MCall, EMethTypeSystem.TsT> resolvedCalls = new IdentityHashMap<>();
    inferred.typeCheck(resolvedCalls);
    var mirInjectionVisitor = new MIRInjectionVisitor(inferred, resolvedCalls);
    var mir = mirInjectionVisitor.visitProgram();
    var java = new codegen.java.JavaCodegen(mir).visitProgram(new Id.DecId(entry, 0));
    System.out.println("Running...");
    var res = RunJava.of(JavaProgram.compile(new JavaProgram(java)), args).join();
    Assertions.assertEquals(expected, res);
  }

  public static void fail(String expectedErr, String entry, String... content) {
    failWithArgs(expectedErr, entry, List.of(), content);
  }
  public static void failWithArgs(String expectedErr, String entry, List<String> args, String... content) {
    assert content.length > 0;
    Main.resetAll();
    AtomicInteger pi = new AtomicInteger();
    var ps = Stream.concat(Arrays.stream(content), Arrays.stream(Base.baseLib))
      .map(code->new Parser(Path.of("Dummy" + pi.getAndIncrement() + ".fear"), code))
      .toList();
    var p = Parser.parseAll(ps, new TypeSystemFeatures());
    new WellFormednessFullShortCircuitVisitor().visitProgram(p).ifPresent(err->{
      throw err;
    });
    var inferred = InferBodies.inferAll(p);
    new WellFormednessShortCircuitVisitor(inferred).visitProgram(inferred);
    IdentityHashMap<E.MCall, EMethTypeSystem.TsT> resolvedCalls = new IdentityHashMap<>();
    inferred.typeCheck(resolvedCalls);
    var mirInjectionVisitor = new MIRInjectionVisitor(inferred, resolvedCalls);
    var mir = mirInjectionVisitor.visitProgram();
    throw Bug.todo();
//    try {
//      var java = new codegen.java.JavaCodegen(mir).visitProgram(new Id.DecId(entry, 0));
//      var res = RunJava.of(JavaProgram.compile(new JavaProgram(java)), args).join();
//      Assertions.fail("Did not fail. Got: "+res);
//    } catch (CompileError e) {
//      Err.strCmp(expectedErr, e.toString());
//    }
  }
}
