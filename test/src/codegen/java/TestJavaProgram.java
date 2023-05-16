package codegen.java;

import codegen.MIRInjectionVisitor;
import failure.CompileError;
import id.Id;
import main.Main;
import net.jqwik.api.Example;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import parser.Parser;
import program.inference.InferBodies;
import utils.Base;
import utils.Err;
import utils.RunJava;
import wellFormedness.WellFormednessFullShortCircuitVisitor;
import wellFormedness.WellFormednessShortCircuitVisitor;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static utils.RunJava.Res;

public class TestJavaProgram {
  void ok(Res expected, String entry, String... content) {
    assert content.length > 0;
    Main.resetAll();
    AtomicInteger pi = new AtomicInteger();
    var ps = Stream.concat(Arrays.stream(content), Arrays.stream(Base.baseLib))
      .map(code->new Parser(Path.of("Dummy" + pi.getAndIncrement() + ".fear"), code))
      .toList();
    var p = Parser.parseAll(ps);
    new WellFormednessFullShortCircuitVisitor().visitProgram(p).ifPresent(err->{
      throw err;
    });
    var inferredSigs = p.inferSignaturesToCore();
    var inferred = new InferBodies(inferredSigs).inferAll(p);
    new WellFormednessShortCircuitVisitor(inferred).visitProgram(inferred);
    inferred.typeCheck();
    var mir = new MIRInjectionVisitor(inferred).visitProgram();
    var java = new JavaCodegen(inferred).visitProgram(mir.pkgs(), new Id.DecId(entry, 0));
    System.out.println(java);
    var res = RunJava.of(new JavaProgram(java).compile()).join();
    Assertions.assertEquals(expected, res);
  }
  void fail(String expectedErr, String entry, String... content) {
    assert content.length > 0;
    Main.resetAll();
    AtomicInteger pi = new AtomicInteger();
    var ps = Stream.concat(Arrays.stream(content), Arrays.stream(Base.baseLib))
      .map(code->new Parser(Path.of("Dummy" + pi.getAndIncrement() + ".fear"), code))
      .toList();
    var p = Parser.parseAll(ps);
    new WellFormednessFullShortCircuitVisitor().visitProgram(p).ifPresent(err->{
      throw err;
    });
    var inferredSigs = p.inferSignaturesToCore();
    var inferred = new InferBodies(inferredSigs).inferAll(p);
    new WellFormednessShortCircuitVisitor(inferred).visitProgram(inferred);
    inferred.typeCheck();
    var mir = new MIRInjectionVisitor(inferred).visitProgram();
    try {
      var java = new JavaCodegen(inferred).visitProgram(mir.pkgs(), new Id.DecId(entry, 0));
      var res = RunJava.of(new JavaProgram(java).compile()).join();
      Assertions.fail("Did not fail. Got: "+res);
    } catch (CompileError e) {
      Err.strCmp(expectedErr, e.toString());
    }
  }

  @Example void emptyProgram() { ok(new Res("", "", 0), "test.Test", """
    package test
    alias base.Main as Main,
    Void:{}
    Test:Main[Void]{ _, _ -> {} }
    """);}

  @Example void assertTrue() { ok(new Res("", "", 0), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main[Void]{ _, _ -> Assert#(True, { Void }) }
    """);}
  @Example void assertFalse() { ok(new Res("", "Assertion failed :(", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main[Void]{ _, _ -> Assert#(False, { Void }) }
    """);}
  @Example void assertFalseMsg() { ok(new Res("", "power level less than 9000", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main[Void]{ _, _ -> Assert#(False, "power level less than 9000", { Void }) }
    """);}

  @Example void falseToStr() { ok(new Res("", "False", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main[Void]{ _, _ -> Assert#(False, Foo.bs(False), { Void }) }
    Foo:{ .bs(b: base.Bool): base.Str -> b.str }
    """);}
  @Example void trueToStr() { ok(new Res("", "True", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main[Void]{ _, _ -> Assert#(False, Foo.bs(True), { Void }) }
    Foo:{ .bs(s: base.Stringable): base.Str -> s.str }
    """);}

  @Example void binaryAnd1() { ok(new Res("", "True", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main[Void]{ _, _ -> Assert#(False, (True && True) .str, { Void }) }
    """);}
  @Example void binaryAnd2() { ok(new Res("", "False", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main[Void]{ _, _ -> Assert#(False, (True && False) .str, { Void }) }
    """);}
  @Example void binaryAnd3() { ok(new Res("", "False", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main[Void]{ _, _ -> Assert#(False, (False && False) .str, { Void }) }
    """);}
  @Example void binaryOr1() { ok(new Res("", "True", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main[Void]{ _, _ -> Assert#(False, (True || True) .str, { Void }) }
    """);}
  @Example void binaryOr2() { ok(new Res("", "True", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main[Void]{ _, _ -> Assert#(False, (True || False) .str, { Void }) }
    """);}
  @Example void binaryOr3() { ok(new Res("", "True", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main[Void]{ _, _ -> Assert#(False, (False || True) .str, { Void }) }
    """);}
  @Example void binaryOr4() { ok(new Res("", "False", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main[Void]{ _, _ -> Assert#(False, (False || False) .str, { Void }) }
    """);}

  @Example void conditionals1() { ok(new Res("", "Assertion failed :(", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main[Void]{ _, _ -> Assert#(420 > 9000, { Void }) }
    """);}
  @Example void conditionals2() { ok(new Res("", "Assertion failed :(", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main[Void]{ _, _ -> Assert#("hi".len() > 9000u, { Void }) }
    """);}

  @Example void longToStr() { ok(new Res("", "123456789", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main[Void]{ _, _ -> Assert#(False, 123456789 .str, { Void }) }
    """);}
  @Example void longLongToStr() { ok(new Res("", "9223372036854775807", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main[Void]{ _, _ -> Assert#(False, 9223372036854775807 .str, { Void }) }
    """);}

  @Example void veryLongLongToStr() { ok(new Res("", "9223372036854775808", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main[Void]{ _, _ -> Assert#(False, 9223372036854775808u .str, { Void }) }
    """);}
  @Example void veryLongLongIntFail() { fail("""
    [E31 invalidNum]
    The number 9223372036854775808 is not a valid Int
    """, "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main[Void]{ _, _ -> Assert#(False, 9223372036854775808 .str, { Void }) }
    """);}
  @Example void veryLongLongUIntFail() { fail("""
    [E31 invalidNum]
    The number 10000000000000000000000u is not a valid UInt
    """, "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main[Void]{ _, _ -> Assert#(False, 10000000000000000000000u .str, { Void }) }
    """);}
  @Example void negativeToStr() { ok(new Res("", "-123456789", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main[Void]{ _, _ -> Assert#(False, -123456789 .str, { Void }) }
    """);}

  @Example void addition() { ok(new Res("", "7", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main[Void]{ _, _ -> Assert#(False, (5 + 2) .str, { Void }) }
    """);}
  @Example void subtraction() { ok(new Res("", "3", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main[Void]{ _, _ -> Assert#(False, (5 - 2) .str, { Void }) }
    """);}
  @Example void subtractionNeg() { ok(new Res("", "-2", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main[Void]{ _, _ -> Assert#(False, (0 - 2) .str, { Void }) }
    """);}
  @Example void subtractionUnderflow() { ok(new Res("", "9223372036854775807", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main[Void]{ _, _ -> Assert#(False, ((0 - 2) - 9223372036854775807) .str, { Void }) }
    """);}

  // TODO: using brackets around (io, s') breaks antlr, fix the grammar
  @Example void println() { ok(new Res("Hello, World!", "", 0), "test.Test", """
    package test
    alias base.Main as Main, alias base.Void as Void,
    Test:Main[Void]{ _, s -> s
      .use[base.caps.IO](base.caps.IO', { io, s' -> s'.return{ io.println "Hello, World!" } })
      }
    """);}
  @Disabled
  @Example void printlnInferUse() { ok(new Res("Hello, World!", "", 0), "test.Test", """
    package test
    alias base.Main as Main, alias base.Void as Void,
    Test:Main[Void]{ _, s -> s
      .use(base.caps.IO', { io, s' -> s'.return{ io.println "Hello, World!" } })
      }
    """);}
  @Example void printlnSugar() { ok(new Res("Hello, World!", "", 0), "test.Test", """
    package test
    alias base.Main as Main, alias base.Void as Void,
    alias base.caps.IO as IO, alias base.caps.IO' as IO',
    Test:Main[Void]{ _, s -> s
      .use[IO] io = IO'
      .return{ io.println("Hello, World!") }
      }
    """); }

  @Disabled
  @Example void printlnSugarInferUse() { ok(new Res("Hello, World!", "", 0), "test.Test", """
    package test
    alias base.Main as Main, alias base.Void as Void,
    alias base.caps.IO' as IO',
    Test:Main[Void]{ _, s -> s
      .use io = IO'
      .return{ io.println("Hello, World!") }
      }
    """); }

  @Example void nestedPkgs() { ok(new Res("", "", 0), "test.Test", """
    package test
    Test:base.Main[test.foo.Bar]{ _, _ -> { .a -> test.foo.Bar } }
    Foo:{ .a: Foo }
    """, """
    package test.foo
    Bar:test.Foo{ .a -> this }
    """); }

    @Example void ref1() { ok(new Res("", "", 0), "test.Test", """
    package test
    alias base.Main as Main, alias base.Void as Void, alias base.Assert as Assert,
    alias base.Ref as Ref, alias base.Int as Int,
    Test:Main[Void]{ _, _ -> Assert#((GetRef#5)* == 5, { Void }) }
    GetRef:{ #(n: Int): mut Ref[Int] -> Ref#n }
    """); }
  @Example void ref2() { ok(new Res("", "", 0), "test.Test", """
    package test
    alias base.Main as Main, alias base.Void as Void, alias base.Assert as Assert,
    alias base.Ref as Ref, alias base.Int as Int,
    Test:Main[Void]{ _, _ -> Assert#((GetRef#5).swap(6) == 5, { Void }) }
    GetRef:{ #(n: Int): mut Ref[Int] -> Ref#n }
    """); }
  // TODO: loops if we give a broken value like `.var[mut Ref[Int]](n = Ref#5)` (not a ReturnStmt)
  @Example void ref3() { ok(new Res("", "", 0), "test.Test", """
    package test
    alias base.Main as Main, alias base.Void as Void, alias base.Assert as Assert, alias base.Block as Block,
    alias base.Ref as Ref, alias base.Int as Int, alias base.ReturnStmt as ReturnStmt,
    Test:Main[Void]{ _, _ -> mut Block[Void]
      .var(n = { Ref#[Int]5 })
      .do{ Assert#(n.swap(6) == 5) }
      .do{ Assert#(n* == 6) }
      .return{{}}
      }
    """); }

//  @Example void ref1() { ok(new Res("", "", 0), "test.Test", """
//    package test
//    alias base.Main as Main, alias base.Void as Void, alias base.Assert as Assert,
//    alias base.Ref as Ref, alias base.Int as Int,
//    Test:Main[Void]{ _, _ -> Assert#((Ref#[Int]5)* == 5, { Void }) }
//    """); }
}
