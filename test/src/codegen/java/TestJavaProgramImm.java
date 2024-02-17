package codegen.java;

import ast.E;
import codegen.MIRInjectionVisitor;
import id.Id;
import main.Main;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import parser.Parser;
import program.TypeSystemFeatures;
import program.inference.InferBodies;
import program.typesystem.EMethTypeSystem;
import utils.Base;
import utils.Bug;
import utils.RunJava;
import wellFormedness.WellFormednessFullShortCircuitVisitor;
import wellFormedness.WellFormednessShortCircuitVisitor;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static utils.RunJava.Res;

public class TestJavaProgramImm {
  void ok(Res expected, String entry, String... content) {
    okWithArgs(expected, entry, List.of(), content);
  }
  void okWithArgs(Res expected, String entry, List<String> args, String... content) {
    assert content.length > 0;
    Main.resetAll();
    AtomicInteger pi = new AtomicInteger();
    var ps = Stream.concat(Arrays.stream(content), Arrays.stream(Base.immBaseLib))
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
    var mir = new MIRInjectionVisitor(inferred, resolvedCalls).visitProgram();
    var java = new ImmJavaCodegen(mir).visitProgram(new Id.DecId(entry, 0));
    var res = RunJava.of(ImmJavaProgram.compile(new JavaProgram(java)), args).join();
    Assertions.assertEquals(expected, res);
  }

  void fail(String expectedErr, String entry, String... content) {
    failWithArgs(expectedErr, entry, List.of(), content);
  }
  void failWithArgs(String expectedErr, String entry, List<String> args, String... content) {
    assert content.length > 0;
    Main.resetAll();
    AtomicInteger pi = new AtomicInteger();
    var ps = Stream.concat(Arrays.stream(content), Arrays.stream(Base.immBaseLib))
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
    var mir = new MIRInjectionVisitor(inferred, resolvedCalls).visitProgram();
    throw Bug.todo();
//    try {
//      var java = new ImmJavaCodegen(mir).visitProgram(new Id.DecId(entry, 0));
//      var res = RunJava.of(JavaProgram.compile(new JavaProgram(java)), args).join();
//      Assertions.fail("Did not fail. Got: "+res);
//    } catch (CompileError e) {
//      Err.strCmp(expectedErr, e.toString());
//    }
  }

  @Test void emptyProgram() { ok(new Res("", "", 0), "test.Test", """
    package test
    alias base.Main as Main,
    Test:Main{ _ -> "" }
    """);}

  @Test void lists() { ok(new Res("2", "", 0), "test.Test", """
    package test
    alias base.Main as Main, alias base.LList as LList, alias base.Int as Int,
    Test:Main{_ -> A.m1.get(1u).match{.some(n) -> n.str, .none -> base.Abort!}}
    A:{
      .m1: LList[Int] -> LList[Int] + 1 + 2 + 3,
      }
    """);}


  @Test void assertTrue() { ok(new Res("", "", 0), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main{ _ -> Assert!(True, { "" }) }
    """);}
  @Test void assertFalse() { ok(new Res("", "Assertion failed :(", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main{ _ -> Assert!(False, { "" }) }
    """);}
  @Test void assertFalseMsg() { ok(new Res("", "power level less than 9000", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main{ _ -> Assert!(False, "power level less than 9000", { "" }) }
    """);}

  @Test void falseToStr() { ok(new Res("", "False", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main{ _ -> Assert!(False, Foo.bs(False), { "" }) }
    Foo:{ .bs(b: base.Bool): base.Str -> b.str }
    """);}
  @Test void trueToStr() { ok(new Res("", "True", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main{ _ -> Assert!(False, Foo.bs(True), { "" }) }
    Foo:{ .bs(s: base.Stringable): base.Str -> s.str }
    """);}

  @Test void binaryAnd1() { ok(new Res("", "True", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main{ _ -> Assert!(False, (True && True) .str, { "" }) }
    """);}
  @Test void binaryAnd2() { ok(new Res("", "False", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main{ _ -> Assert!(False, (True && False) .str, { "" }) }
    """);}
  @Test void binaryAnd3() { ok(new Res("", "False", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main{ _ -> Assert!(False, (False && False) .str, { "" }) }
    """);}
  @Test void binaryOr1() { ok(new Res("", "True", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main{ _ -> Assert!(False, (True || True) .str, { "" }) }
    """);}
  @Test void binaryOr2() { ok(new Res("", "True", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main{ _ -> Assert!(False, (True || False) .str, { "" }) }
    """);}
  @Test void binaryOr3() { ok(new Res("", "True", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main{ _ -> Assert!(False, (False || True) .str, { "" }) }
    """);}
  @Test void binaryOr4() { ok(new Res("", "False", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main{ _ -> Assert!(False, (False || False) .str, { "" }) }
    """);}

  @Test void conditionals1() { ok(new Res("", "Assertion failed :(", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main{ _ -> Assert!(420 > 9000, { "" }) }
    """);}
  @Test void conditionals2() { ok(new Res("", "Assertion failed :(", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main{ _ -> Assert!("hi".size > 9000u, { "" }) }
    """);}

  @Test void longToStr() { ok(new Res("", "123456789", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main{ _ -> Assert!(False, 123456789 .str, { "" }) }
    """);}
  @Test void longLongToStr() { ok(new Res("", "9223372036854775807", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main{ _ -> Assert!(False, 9223372036854775807 .str, { "" }) }
    """);}

  @Test void veryLongLongToStr() { ok(new Res("", "9223372036854775808", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main{ _ -> Assert!(False, 9223372036854775808u .str, { "" }) }
    """);}
  @Test void veryLongLongIntFail() { fail("""
    [E31 invalidNum]
    The number 9223372036854775808 is not a valid Int
    """, "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main{ _ -> Assert!(False, 9223372036854775808 .str, { "" }) }
    """);}
  @Test void veryLongLongUIntFail() { fail("""
    [E31 invalidNum]
    The number 10000000000000000000000u is not a valid UInt
    """, "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main{ _ -> Assert!(False, 10000000000000000000000u .str, { "" }) }
    """);}
  @Test void negativeToStr() { ok(new Res("", "-123456789", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main{ _ -> Assert!(False, -123456789 .str, { "" }) }
    """);}

  @Test void addition() { ok(new Res("", "7", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main{ _ -> Assert!(False, (5 + 2) .str, { "" }) }
    """);}
  @Test void subtraction() { ok(new Res("", "3", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main{ _ -> Assert!(False, (5 - 2) .str, { "" }) }
    """);}
  @Test void subtractionNeg() { ok(new Res("", "-2", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main{ _ -> Assert!(False, (0 - 2) .str, { "" }) }
    """);}
  @Test void subtractionUnderflow() { ok(new Res("", "9223372036854775807", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main{ _ -> Assert!(False, ((0 - 2) - 9223372036854775807) .str, { "" }) }
    """);}

  @Test void launchArg() { okWithArgs(new Res("yeet", "", 0), "test.Test", List.of("yeet"), """
    package test
    Test:base.Main{ args -> args.head.match{ .none -> "boo", .some(msg) -> msg } }
    """);}

  @Test void nestedPkgs() { ok(new Res("", "", 0), "test.Test", """
    package test
    Test:base.Main[]{ _ -> test.foo.Bar{ .a -> test.foo.Bar }.str }
    Foo:{ .a: Foo }
    """, """
    package test.foo
    alias base.Str as Str,
    Bar:test.Foo{ .a -> this, .str: Str -> "" }
    """); }
}
