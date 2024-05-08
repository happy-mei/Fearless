package codegen.go;

import codegen.MIRInjectionVisitor;
import failure.CompileError;
import id.Id;
import main.CompilerFrontEnd;
import main.Main;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import parser.Parser;
import program.TypeSystemFeatures;
import program.inference.InferBodies;
import program.typesystem.EMethTypeSystem;
import utils.Base;
import utils.Bug;
import utils.Err;
import utils.RunOutput;
import wellFormedness.WellFormednessFullShortCircuitVisitor;
import wellFormedness.WellFormednessShortCircuitVisitor;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static utils.RunOutput.Res;

@Disabled
public class TestGoProgramImm {
  void ok(Res expected, String... content) {
    okWithArgs(expected, List.of(), content);
  }
  void okWithArgs(Res expected, List<String> args, String... content) {
    assert content.length > 0;
    Main.resetAll();
    AtomicInteger pi = new AtomicInteger();
    var ps = Stream.concat(Arrays.stream(content), Arrays.stream(Base.immBaseLib))
      .map(code->new Parser(Path.of("Dummy" + pi.getAndIncrement() + ".fear"), code))
      .toList();
    var p = Parser.parseAll(ps, TypeSystemFeatures.of());
    new WellFormednessFullShortCircuitVisitor().visitProgram(p).ifPresent(err->{
      throw err;
    });
    var inferred = InferBodies.inferAll(p);
    new WellFormednessShortCircuitVisitor(inferred).visitProgram(inferred);
    ConcurrentHashMap<Long, EMethTypeSystem.TsT> resolvedCalls = new ConcurrentHashMap<>();
    inferred.typeCheck(resolvedCalls);
    var mir = new MIRInjectionVisitor(List.of(),inferred, resolvedCalls).visitProgram();
    var go = new GoCodegen(mir).visitProgram(new Id.DecId("test.Test", 0));
    var vb = new CompilerFrontEnd.Verbosity(true, false, CompilerFrontEnd.ProgressVerbosity.Full);
    Res res; try {
      var binary = new GoCompiler(go.mainFile(), GoCompiler.IMM_RUNTIME_UNITS, go.pkgs(), vb).compile();
      res = RunOutput.go(binary, args).join();
    } catch (IOException e) {
      throw Bug.of(e);
    }
    Assertions.assertEquals(expected, res);
  }
  void fail(String expectedErr, String... content) {
    failWithArgs(expectedErr, List.of(), content);
  }
  void failWithArgs(String expectedErr, List<String> args, String... content) {
    assert content.length > 0;
    Main.resetAll();
    AtomicInteger pi = new AtomicInteger();
    var ps = Stream.concat(Arrays.stream(content), Arrays.stream(Base.immBaseLib))
      .map(code->new Parser(Path.of("Dummy" + pi.getAndIncrement() + ".fear"), code))
      .toList();
    var p = Parser.parseAll(ps, TypeSystemFeatures.of());
    new WellFormednessFullShortCircuitVisitor().visitProgram(p).ifPresent(err->{
      throw err;
    });
    var inferred = InferBodies.inferAll(p);
    new WellFormednessShortCircuitVisitor(inferred).visitProgram(inferred);
    ConcurrentHashMap<Long, EMethTypeSystem.TsT> resolvedCalls = new ConcurrentHashMap<>();
    inferred.typeCheck(resolvedCalls);
    var mir = new MIRInjectionVisitor(List.of(),inferred, resolvedCalls).visitProgram();
    var vb = new CompilerFrontEnd.Verbosity(true, false, CompilerFrontEnd.ProgressVerbosity.Full);
    try {
      var go = new GoCodegen(mir).visitProgram(new Id.DecId("test.Test", 0));
      Res res; try {
        var binary = new GoCompiler(go.mainFile(), GoCompiler.IMM_RUNTIME_UNITS, go.pkgs(), vb).compile();
        res = RunOutput.go(binary, args).join();
      } catch (IOException e) {
        throw Bug.of(e);
      }
      Assertions.fail("Did not fail. Got: "+res);
    } catch (CompileError e) {
      Err.strCmp(expectedErr, e.toString());
    }
  }

  @Test void emptyProgram() { ok(new Res("", "", 0), """
    package test
    alias base.Main as Main,
    Test:Main{ _ -> "" }
    """);}

  @Test void fib43() { ok(new Res("433494437", "", 0), """
    package test
    alias base.Main as Main, alias base.Nat as Nat,
    Test:Main{ _ -> Fib#(43).str }
    Fib: {
      #(n: Nat): Nat -> n <= 1 ? {
        .then -> n,
        .else -> this#(n - 1) + (this#(n - 2))
        }
      }
    """);}

  @Test void lists() { ok(new Res("2", "", 0), """
    package test
    alias base.Main as Main, alias base.LList as LList, alias base.Int as Int,
    Test:Main{_ -> A.m1.get(1).match{.some(n) -> n.str, .none -> base.Abort!}}
    A:{
      .m1: LList[Int] -> LList[Int] + 1 + 2 + 3,
      }
    """);}

  @Test void inheritedFnNoSingleton() { ok(new Res("5", "", 0), """
    package test
    alias base.Main as Main, alias base.Int as Int,
    A: {.m1: Int -> 5, .unrelated: Int,}
    B: A{.m2: Int -> this.m1, .unrelated -> 123}
    Test: Main{_ -> B.m2.str}
    """);}

  @Test void assertTrue() { ok(new Res("", "", 0), """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main{ _ -> Assert!(True, { "" }) }
    """);}
  @Test void assertFalse() { ok(new Res("", "Assertion failed :(", 1), """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main{ _ -> Assert!(False, { "" }) }
    """);}
  @Test void assertFalseMsg() { ok(new Res("", "power level less than 9000", 1), """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main{ _ -> Assert!(False, "power level less than 9000", { "" }) }
    """);}

  @Test void falseToStr() { ok(new Res("", "False", 1), """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main{ _ -> Assert!(False, Foo.bs(False), { "" }) }
    Foo:{ .bs(b: base.Bool): base.Str -> b.str }
    """);}
  @Test void trueToStr() { ok(new Res("", "True", 1), """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main{ _ -> Assert!(False, Foo.bs(True), { "" }) }
    Foo:{ .bs(s: base.Stringable): base.Str -> s.str }
    """);}

  @Test void binaryAnd1() { ok(new Res("", "True", 1), """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main{ _ -> Assert!(False, (True && True) .str, { "" }) }
    """);}
  @Test void binaryAnd2() { ok(new Res("", "False", 1), """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main{ _ -> Assert!(False, (True && False) .str, { "" }) }
    """);}
  @Test void binaryAnd3() { ok(new Res("", "False", 1), """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main{ _ -> Assert!(False, (False && False) .str, { "" }) }
    """);}
  @Test void binaryOr1() { ok(new Res("", "True", 1), """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main{ _ -> Assert!(False, (True || True) .str, { "" }) }
    """);}
  @Test void binaryOr2() { ok(new Res("", "True", 1), """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main{ _ -> Assert!(False, (True || False) .str, { "" }) }
    """);}
  @Test void binaryOr3() { ok(new Res("", "True", 1), """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main{ _ -> Assert!(False, (False || True) .str, { "" }) }
    """);}
  @Test void binaryOr4() { ok(new Res("", "False", 1), """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main{ _ -> Assert!(False, (False || False) .str, { "" }) }
    """);}

  @Test void conditionals1() { ok(new Res("", "Assertion failed :(", 1), """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main{ _ -> Assert!(420 > 9000, { "" }) }
    """);}
  @Test void conditionals2() { ok(new Res("", "Assertion failed :(", 1), """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main{ _ -> Assert!("hi".size > 9000, { "" }) }
    """);}

  @Test void longToStr() { ok(new Res("", "123456789", 1), """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main{ _ -> Assert!(False, 123456789 .str, { "" }) }
    """);}
  @Test void longLongToStr() { ok(new Res("", "9223372036854775807", 1), """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main{ _ -> Assert!(False, 9223372036854775807 .str, { "" }) }
    """);}

  @Test void veryLongLongToStr() { ok(new Res("", "9223372036854775808", 1), """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main{ _ -> Assert!(False, 9223372036854775808 .str, { "" }) }
    """);}
  @Test void veryLongLongIntFail() { fail("""
    [E31 invalidNum]
    The number +9223372036854775808 is not a valid Int
    """, """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main{ _ -> Assert!(False, +9223372036854775808 .str, { "" }) }
    """);}
  @Test void veryLongLongNatFail() { fail("""
    [E31 invalidNum]
    The number 10000000000000000000000 is not a valid Nat
    """, """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main{ _ -> Assert!(False, 10000000000000000000000 .str, { "" }) }
    """);}
  @Test void negativeToStr() { ok(new Res("", "-123456789", 1), """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main{ _ -> Assert!(False, -123456789 .str, { "" }) }
    """);}

  @Test void addition() { ok(new Res("", "7", 1), """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main{ _ -> Assert!(False, (5 + 2) .str, { "" }) }
    """);}
  @Test void subtraction() { ok(new Res("", "3", 1), """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main{ _ -> Assert!(False, (5 - 2) .str, { "" }) }
    """);}
  @Test void subtractionNeg() { ok(new Res("", "-2", 1), """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main{ _ -> Assert!(False, (0 - 2) .str, { "" }) }
    """);}
  @Test void subtractionUnderflow() { ok(new Res("", "9223372036854775807", 1), """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main{ _ -> Assert!(False, ((0 - 2) - 9223372036854775807) .str, { "" }) }
    """);}

  @Test void launchArg() { okWithArgs(new Res("yeet", "", 0), List.of("yeet"), """
    package test
    Test:base.Main{ args -> args.head.match{ .none -> "boo", .some(msg) -> msg } }
    """);}

  @Test void nestedPkgs() { ok(new Res("", "", 0), """
    package test
    Test:base.Main[]{ _ -> test.foo.Bar{ .a -> test.foo.Bar }.str }
    Foo:{ .a: Foo }
    """, """
    package test.foo
    alias base.Str as Str,
    Bar:test.Foo{ .a -> this, .str: Str -> "" }
    """); }
}
