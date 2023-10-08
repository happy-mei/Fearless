package codegen.java;

import ast.E;
import codegen.MIRInjectionVisitor;
import failure.CompileError;
import id.Id;
import main.Main;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import parser.Parser;
import program.inference.InferBodies;
import program.typesystem.EMethTypeSystem;
import utils.Base;
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

import static program.typesystem.RunTypeSystem.ok;
import static utils.RunJava.Res;

public class TestJavaProgram {
  void ok(Res expected, String entry, String... content) {
    okWithArgs(expected, entry, List.of(), content);
  }
  void okWithArgs(Res expected, String entry, List<String> args, String... content) {
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
    var inferred = InferBodies.inferAll(p);
    new WellFormednessShortCircuitVisitor(inferred).visitProgram(inferred).ifPresent(err->{
      throw err;
    });
    IdentityHashMap<E.MCall, EMethTypeSystem.TsT> resolvedCalls = new IdentityHashMap<>();
    inferred.typeCheck(resolvedCalls);
    var mirInjectionVisitor = new MIRInjectionVisitor(inferred, resolvedCalls);
    var mir = mirInjectionVisitor.visitProgram();
    var java = new JavaCodegen(mirInjectionVisitor.getProgram(), resolvedCalls).visitProgram(mir.pkgs(), new Id.DecId(entry, 0));
    System.out.println("Running...");
    var res = RunJava.of(new JavaProgram(java).compile(), args).join();
    Assertions.assertEquals(expected, res);
  }

  void fail(String expectedErr, String entry, String... content) {
    failWithArgs(expectedErr, entry, List.of(), content);
  }
  void failWithArgs(String expectedErr, String entry, List<String> args, String... content) {
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
    var inferred = InferBodies.inferAll(p);
    new WellFormednessShortCircuitVisitor(inferred).visitProgram(inferred);
    IdentityHashMap<E.MCall, EMethTypeSystem.TsT> resolvedCalls = new IdentityHashMap<>();
    inferred.typeCheck(resolvedCalls);
    var mirInjectionVisitor = new MIRInjectionVisitor(inferred, resolvedCalls);
    var mir = mirInjectionVisitor.visitProgram();
    try {
      var java = new JavaCodegen(mirInjectionVisitor.getProgram(), resolvedCalls).visitProgram(mir.pkgs(), new Id.DecId(entry, 0));
      var res = RunJava.of(new JavaProgram(java).compile(), args).join();
      Assertions.fail("Did not fail. Got: "+res);
    } catch (CompileError e) {
      Err.strCmp(expectedErr, e.toString());
    }
  }

  @Test void emptyProgram() { ok(new Res("", "", 0), "test.Test", """
    package test
    alias base.Main as Main,
    alias base.Void as Void,
    Test:Main{ _ -> {} }
    """);}

  @Test void captureTest() { ok(new Res("", "", 0), "test.Test", """
    package test
    alias base.Main as Main,
    alias base.Void as Void,
    Test:Main{ _ -> {} }
    A:{ #: A -> A{ # -> A { # -> this } }# }
    """);}

  @Test void assertTrue() { ok(new Res("", "", 0), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert!(True, { Void }) }
    """);}
  @Test void assertFalse() { ok(new Res("", "Assertion failed :(", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert!(False, { Void }) }
    """);}
  @Test void assertFalseMsg() { ok(new Res("", "power level less than 9000", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert!(False, "power level less than 9000", { Void }) }
    """);}

  @Test void falseToStr() { ok(new Res("", "False", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert!(False, Foo.bs(False), { Void }) }
    Foo:{ .bs(b: base.Bool): base.Str -> b.str }
    """);}
  @Test void trueToStr() { ok(new Res("", "True", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert!(False, Foo.bs(True), { Void }) }
    Foo:{ .bs(s: base.Stringable): base.Str -> s.str }
    """);}

  @Test void binaryAnd1() { ok(new Res("", "True", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert!(False, (True && True) .str, { Void }) }
    """);}
  @Test void binaryAnd2() { ok(new Res("", "False", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert!(False, (True && False) .str, { Void }) }
    """);}
  @Test void binaryAnd3() { ok(new Res("", "False", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert!(False, (False && False) .str, { Void }) }
    """);}
  @Test void binaryOr1() { ok(new Res("", "True", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert!(False, (True || True) .str, { Void }) }
    """);}
  @Test void binaryOr2() { ok(new Res("", "True", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert!(False, (True || False) .str, { Void }) }
    """);}
  @Test void binaryOr3() { ok(new Res("", "True", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert!(False, (False || True) .str, { Void }) }
    """);}
  @Test void binaryOr4() { ok(new Res("", "False", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert!(False, (False || False) .str, { Void }) }
    """);}

  @Test void conditionals1() { ok(new Res("", "Assertion failed :(", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert!(420 > 9000, { Void }) }
    """);}
  @Test void conditionals2() { ok(new Res("", "Assertion failed :(", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert!("hi".len() > 9000u, { Void }) }
    """);}

  @Test void longToStr() { ok(new Res("", "123456789", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert!(False, 123456789 .str, { Void }) }
    """);}
  @Test void longLongToStr() { ok(new Res("", "9223372036854775807", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert!(False, 9223372036854775807 .str, { Void }) }
    """);}

  @Test void veryLongLongToStr() { ok(new Res("", "9223372036854775808", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert!(False, 9223372036854775808u .str, { Void }) }
    """);}
  @Test void veryLongLongIntFail() { fail("""
    [E31 invalidNum]
    The number 9223372036854775808 is not a valid Int
    """, "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert!(False, 9223372036854775808 .str, { Void }) }
    """);}
  @Test void veryLongLongUIntFail() { fail("""
    [E31 invalidNum]
    The number 10000000000000000000000u is not a valid UInt
    """, "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert!(False, 10000000000000000000000u .str, { Void }) }
    """);}
  @Test void negativeToStr() { ok(new Res("", "-123456789", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert!(False, -123456789 .str, { Void }) }
    """);}

  @Test void addition() { ok(new Res("", "7", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert!(False, (5 + 2) .str, { Void }) }
    """);}
  @Test void subtraction() { ok(new Res("", "3", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert!(False, (5 - 2) .str, { Void }) }
    """);}
  @Test void subtractionNeg() { ok(new Res("", "-2", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert!(False, (0 - 2) .str, { Void }) }
    """);}
  @Test void subtractionUnderflow() { ok(new Res("", "9223372036854775807", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert!(False, ((0 - 2) - 9223372036854775807) .str, { Void }) }
    """);}

  @Test void println() { ok(new Res("Hello, World!", "", 0), "test.Test", """
    package test
    alias base.Main as Main, alias base.Void as Void, alias base.Do as Do,
    Test:Main{ s -> Do#
      .var({ base.caps.FIO#s }, { io, s' -> s'.return{ io.println "Hello, World!" } })
      }
    """);}
  @Test void printlnSugar() { ok(new Res("Hello, World!", "", 0), "test.Test", """
    package test
    alias base.Main as Main, alias base.Void as Void, alias base.Do as Do,
    alias base.caps.IO as IO, alias base.caps.FIO as FIO,
    Test:Main{ s -> Do#
      .var[mut IO] io = { FIO#s }
      .return{ io.println("Hello, World!") }
      }
    """); }
  @Test void printlnDeeper() { ok(new Res("IO begets IO", "", 0), "test.Test", """
    package test
    alias base.Main as Main, alias base.Void as Void, alias base.Do as Do,
    alias base.caps.IO as IO, alias base.caps.FIO as FIO,
    Test:Main{ s -> Do#
      .var[mut IO] io = { FIO#s }
      .return{ Do#
        .var io2 = { base.caps.FIO#s }
        .return{ io2.println("IO begets IO") }
        }
      }
    """); }
  @Test void print() { ok(new Res("Hello, World!", "", 0), "test.Test", """
    package test
    alias base.Main as Main, alias base.Void as Void, alias base.Do as Do,
    alias base.caps.IO as IO, alias base.caps.FIO as FIO,
    Test:Main{ s -> Do#
      .var[mut IO] io = { FIO#s }
      .do{ io.print("Hello") }
      .return{ io.print(", World!") }
      }
    """); }
  @Test void printlnErr() { ok(new Res("", "Hello, World!", 0), "test.Test", """
    package test
    alias base.Main as Main, alias base.Void as Void, alias base.Do as Do,
    alias base.caps.IO as IO, alias base.caps.FIO as FIO,
    Test:Main{ s -> Do#
      .var[mut IO] io = { FIO#s }
      .return{ io.printlnErr("Hello, World!") }
      }
    """); }
  @Test void printErr() { ok(new Res("", "Hello, World!", 0), "test.Test", """
    package test
    alias base.Main as Main, alias base.Void as Void, alias base.Do as Do,
    alias base.caps.IO as IO, alias base.caps.FIO as FIO,
    Test:Main{ s -> Do#
      .var[mut IO] io = { FIO#s }
      .do{ io.printErr("Hello") }
      .return{ io.printErr(", World!") }
      }
    """); }
  @Test void printlnShareLent() { ok(new Res("Hello, World!", "", 0), "test.Test", """
    package test
    alias base.Main as Main, alias base.Void as Void, alias base.Do as Do,
    alias base.caps.IO as IO, alias base.caps.FIO as FIO,
    Test:Main{ s -> Do#
      .var[mut IO] io = { FIO#s }
      .return{ Usage#io }
      }
    Usage:{
      #(io: lent IO): Void -> io.println("Hello, World!"),
      }
    """); }
  @Test void printlnShareLentCapture() { ok(new Res("Hello, World!", "", 0), "test.Test", """
    package test
    alias base.Main as Main, alias base.Void as Void, alias base.Do as Do,
    alias base.caps.IO as IO, alias base.caps.FIO as FIO,
    Test:Main{ s -> Do#
      .var[mut IO] io = { FIO#s }
      .return{ lent Usage{ io }# }
      }
    Usage:{
      lent .io: lent IO,
      lent #: Void -> this.io.println("Hello, World!"),
      }
    """); }

  @Test void printlnSugarInferUse() { ok(new Res("Hello, World!", "", 0), "test.Test", """
    package test
    alias base.Main as Main, alias base.Void as Void, alias base.Do as Do,
    alias base.caps.FIO as FIO,
    Test:Main{ s -> Do#
      .var io = { FIO#s }
      .return{ io.println("Hello, World!") }
      }
    """); }

  @Test void nestedPkgs() { ok(new Res("", "", 0), "test.Test", """
    package test
    Test:base.Main{ _ -> {} }
    Bloop:{ #: test.foo.Bar -> { .a -> test.foo.Bar } }
    Foo:{ .a: Foo }
    """, """
    package test.foo
    Bar:test.Foo{ .a -> this }
    """); }

    @Test void ref1() { ok(new Res("", "", 0), "test.Test", """
    package test
    alias base.Main as Main, alias base.Void as Void, alias base.Assert as Assert,
    alias base.Ref as Ref, alias base.Int as Int,
    Test:Main{ _ -> Assert!((GetRef#5)* == 5, { Void }) }
    GetRef:{ #(n: Int): mut Ref[Int] -> Ref#n }
    """); }
  @Test void ref2() { ok(new Res("", "", 0), "test.Test", """
    package test
    alias base.Main as Main, alias base.Void as Void, alias base.Assert as Assert,
    alias base.Ref as Ref, alias base.Int as Int,
    Test:Main{ _ -> Assert!((GetRef#5).swap(6) == 5, { Void }) }
    GetRef:{ #(n: Int): mut Ref[Int] -> Ref#n }
    """); }
  // TODO: loops if we give a broken value like `.var[mut Ref[Int]](n = Ref#5)` (not a ReturnStmt)
  @Test void ref3() { ok(new Res("", "", 0), "test.Test", """
    package test
    alias base.Main as Main, alias base.Void as Void, alias base.Assert as Assert, alias base.Block as Block,
    alias base.Ref as Ref, alias base.Int as Int, alias base.ReturnStmt as ReturnStmt,
    Test:Main{ _ -> mut Block[Void]
      .var(n = { Ref#[Int]5 })
      .do{ Assert!(n.swap(6) == 5) }
      .do{ Assert!(n* == 6) }
      .return{{}}
      }
    """); }

  static String cliArgsOrElseGet = """
    package test
    MyApp:Main{ s -> Do#
      .var io = { FIO#s }
      .var env = { FEnv.io(io) }
      .return{ io.println(ImmMain#(env.launchArgs)) }
      }
    ImmMain:{
      #(args: LList[Str]): Str -> args.get(1u) || { (this.errMsg((args.head).isSome)) * },
      .errMsg(retCounter: Bool): mut Ref[Str] -> Do#
        .var res = { Ref#[mut Ref[Str]](Ref#[Str]"Sad") }
        .var counter = { Count.int(42) }
        .do{ res* := "mutability!" }
        .do{ Yeet#(counter++) }
        .if{ False }.return{ Ref#[Str]"Short cut" }
        .if{ True }.do{ Yeet#[Int](counter *= 9000) } // MY POWER LEVELS ARE OVER 9000!!!!!!
        .if{ True }.do{ res* := "moar mutability" }
        .if{ retCounter.not }.return{ res* }
        .return{ Ref#(counter*.str) }
      }
    """;
  @Test void cliArgs1a() { okWithArgs(new Res("moar mutability", "", 0), "test.MyApp", List.of(), cliArgsOrElseGet, Base.mutBaseAliases); }
  @Test void cliArgs1b() { okWithArgs(new Res("387000", "", 0), "test.MyApp", List.of(
    "hi"
  ), cliArgsOrElseGet, Base.mutBaseAliases); }
  @Test void cliArgs1c() { okWithArgs(new Res("bye", "", 0), "test.MyApp", List.of(
    "hi",
    "bye"
  ), cliArgsOrElseGet, Base.mutBaseAliases); }
  String getCliArgsOrElse = """
    package test
    MyApp:Main{ s -> Do#
      .var io = { FIO#s }
      .var env = { FEnv#s }
      .return{ io.println(ImmMain#(env.launchArgs)) }
      }
    ImmMain:{
      #(args: LList[Str]): Str -> args.get(1u) | (this.errMsg(args.head.isSome)*),
      .errMsg(retCounter: Bool): mut Ref[Str] -> Do#
        .var res = { Ref#[mut Ref[Str]](Ref#[Str]"Sad") }
        .var counter = { Count.int(42) }
        .do{ res* := "mutability!" }
        .do{ Yeet#(counter++) }
        .if{ False }.return{ Ref#[Str]"Short cut" }
        .if{ True }.do{ Yeet#[Int](counter *= 9000) } // MY POWER LEVELS ARE OVER 9000!!!!!!
        .if{ True }.do{ res* := "moar mutability" }
        .if{ retCounter.not }.return{ res* }
        .return{ Ref#(counter*.str) }
      }
    """;
  @Test void cliArgs2a() { okWithArgs(new Res("moar mutability", "", 0), "test.MyApp", List.of(), getCliArgsOrElse, Base.mutBaseAliases); }
  @Test void cliArgs2b() { okWithArgs(new Res("387000", "", 0), "test.MyApp", List.of(
    "hi"
  ), getCliArgsOrElse, Base.mutBaseAliases); }
  @Test void cliArgs2c() { okWithArgs(new Res("bye", "", 0), "test.MyApp", List.of(
    "hi",
    "bye"
  ), getCliArgsOrElse, Base.mutBaseAliases); }

  @Test void findClosestInt() { ok(new Res("", "", 0), "test.Test", """
    package test
    Test:Main{ _ -> Do#
      .var[Int] closest = { Closest#(LList[Int] + 35 + 52 + 84 + 14, 49) }
      .return{ Assert!(closest == 52, closest.str, {{}}) }
      }
    Closest:{
      #(ns: LList[Int], target: Int): Int -> Do#
        .do{ Assert!(ns.isEmpty.not, "empty list :-(", {{}}) }
        .var[mut Ref[Int]] closest = { Ref#(ns.head!) }
        .do{ mut Closest'{ 'self
          h, t -> h.match{
            .empty -> {},
            .some(n) -> (target - n).abs < ((target - (closest*)).abs) ? {
              .then -> closest := n,
              .else -> self#(t.head, t.tail)
              }
            }
          }#(ns.head, ns.tail) }
        .return{ closest* }
      }
    Closest':{ mut #(h: Opt[Int], t: LList[Int]): Void }
    """, Base.mutBaseAliases); }
  @Test void findClosestIntMut1() { ok(new Res("", "", 0), "test.Test", """
    package test
    Test:Main{ _ -> Do#
      .var[Int] closest = { Closest#(LList[Int] + 35 + 52 + 84 + 14, 49) }
      .return{ Assert!(closest == 52, closest.str, {{}}) }
      }
    Closest:{
      #(ns: LList[Int], target: Int): Int -> Do#
        .do{ Assert!(ns.isEmpty.not, "empty list :-(", {{}}) }
        .var[Int] closest' = { ns.get(0u)! }
        .var closest = { Ref#[Int](closest') }
        .do{ mut Closest'{ 'self
          h, t -> h.match{
            .empty -> {},
            .some(n) -> (target - n).abs < ((target - (closest*)).abs) ? {
              .then -> closest := n,
              .else -> self#(t.head, t.tail)
              }
            }
          }#(ns.head, ns.tail) }
        .return{ closest* }
      }
    Closest':{ mut #(h: Opt[Int], t: LList[Int]): Void }
    """, Base.mutBaseAliases); }
  @Test void findClosestIntMut2() { ok(new Res("", "", 0), "test.Test", """
    package test
    Test:Main{ _ -> Do#
      .var[Int] closest = { Closest#(LList[Int] + 35 + 52 + 84 + 14, 49) }
      .return{ Assert!(closest == 52, closest.str, {{}}) }
      }
    Closest:{
      #(ns: LList[Int], target: Int): Int -> Do#
        .do{ Assert!(ns.isEmpty.not, "empty list :-(", {{}}) }
        .var[mut Ref[Int]] closest = { Ref#[Int](ns.head!) }
        .do{ mut Closest'{ 'self
          h, t -> h.match{
            .empty -> {},
            .some(n) -> (target - n).abs < ((target - (closest*)).abs) ? {
              .then -> closest := n,
              .else -> self#(t.head, t.tail)
              }
            }
          }#(ns.head, ns.tail) }
        .return{ closest* }
      }
    Closest':{ mut #(h: Opt[Int], t: LList[Int]): Void }
    """, Base.mutBaseAliases); }
  @Test void findClosestIntMut3() { ok(new Res("", "", 0), "test.Test", """
    package test
    Test:Main{ _ -> Do#
      .var[Int] closest = { Closest#(LList[Int] + 35 + 52 + 84 + 14, 49) }
      .return{ Assert!(closest == 52, closest.str, {{}}) }
      }
    Closest:{
      #(ns: LList[Int], target: Int): Int -> Do#
        .do{ Assert!(ns.isEmpty.not, "empty list :-(", {{}}) }
        .var[mut Ref[Int]] closest = { Ref#[Int]((ns.get(0u))!) }
        .do{ mut Closest'{ 'self
          h, t -> h.match{
            .empty -> {},
            .some(n) -> (target - n).abs < ((target - (closest*)).abs) ? {
              .then -> closest := n,
              .else -> self#(t.head, t.tail)
              }
            }
          }#(ns.head, ns.tail) }
        .return{ closest* }
      }
    Closest':{ mut #(h: Opt[Int], t: LList[Int]): Void }
    """, Base.mutBaseAliases); }
  @Test void findClosestIntMutWithMutLList() { ok(new Res("", "", 0), "test.Test", """
    package test
    Test:Main{ _ -> Do#
      .var[Int] closest = { Closest#(mut LList[Int] + 35 + 52 + 84 + 14, 49) }
      .return{ Assert!(closest == 52, closest.str, {{}}) }
      }
    Closest:{
      #(ns: mut LList[Int], target: Int): Int -> Do#
        .do{ Assert!(ns.isEmpty.not, "empty list :-(", {{}}) }
        .var closest = { Ref#[Int](ns.get[](0u)!) }
        .do{ mut Closest'{ 'self
          h, t -> h.match mut OptMatch[Int,Void]{
            .empty -> {},
            .some(n) -> (target - n).abs < ((target - (closest*[])).abs) ? {
              .then -> closest := n,
              .else -> self#(t.head, t.tail)
              }
            }
          }#(ns.tail[].head[], ns.tail[].tail[]) }
        .return{ closest* }
      }
    Closest':{ mut #(h: mut Opt[Int], t: mut LList[Int]): Void }
    """, Base.mutBaseAliases); }
  @Test void findClosestIntMutWithMutList() { ok(new Res("", "", 0), "test.Test", """
    package test
    Test:Main{ _ -> Do#
      .var[Int] closest = { Closest#(LList[Int] + 35 + 52 + 84 + 14 .list, 49) }
      .return{ Assert!(closest == 52, closest.str, {{}}) }
      }
    Closest:{
      #(ns: mut List[Int], target: Int): Int -> Do#
        .do{ Assert!(ns.isEmpty.not, "empty list :-(", {{}}) }
        .var closest = { Ref#[Int](ns.get(0u)!) }
        .do{ mut Closest'{ 'self
          i -> ns.get(i).match(mut OptMatch[Int, Void]{
            .empty -> {},
            .some(n) -> (target - n).abs < ((target - (closest*)).abs) ? {
              .then -> closest := n,
              .else -> self#(i + 1u)
              }
            })
          }#(1u) }
        .return{ closest* }
      }
    Closest':{ mut #(i: UInt): Void }
    """, Base.mutBaseAliases); }
  
  @Test void LListItersIterImm() { ok(new Res("", "", 0), "test.Test", """
    package test
    Test:Main{ _ -> Do#
      .var[LList[Int]] l1 = { LList[Int] + 35 + 52 + 84 + 14 }
      .do{ Assert!(l1.head! == (l1.iter.next!), "sanity", {{}}) }
      .do{ Assert!((l1.iter.find{n -> n > 60})! == 84, "find some", {{}}) }
      .do{ Assert!((l1.iter.find{n -> n > 100}).isNone, "find empty", {{}}) }
      .do{ Assert!((l1.iter
                      .map{n -> n * 10}
                      .find{n -> n == 140})
                      .isSome,
        "map", {{}})}
      .return{{}}
      }
    """, Base.mutBaseAliases); }
  @Test void LListItersIterMut() { ok(new Res("", "", 0), "test.Test", """
    package test
    Test:Main{ _ -> Do#
      .var[mut LList[Int]] l1 = { mut LList[Int] +[] 35 +[] 52 +[] 84 +[] 14 }
      .do{ Assert!(l1.head! == (l1.iter.next!), "sanity", {{}}) }
      .do{ Assert!((l1.iter.find{n -> n > 60})! == 84, "find some", {{}}) }
      .do{ Assert!((l1.iter.find{n -> n > 100}).isNone, "find empty", {{}}) }
      .do{ Assert!(l1.iter
                      .map{n -> n * 10}
                      .find{n -> n == 140}
                      .isSome,
        "map", {{}})}
      .return{{}}
      }
    """, Base.mutBaseAliases); }
  @Test void listIterMut() { ok(new Res("", "", 0), "test.Test", """
    package test
    Test:Main{ _ -> Do#
      .var[mut List[Int]] l1 = { (LList[Int] + 35 + 52 + 84 + 14).list }
      .assert({ l1.get(0u)! == (ListIter#l1.next!) }, "sanity") // okay, time to use this for new tests
      .do{ Assert!((ListIter#l1.find{n -> n > 60})! == 84, "find some", {{}}) }
      .do{ Assert!((ListIter#l1.find{n -> n > 100}).isNone, "find empty", {{}}) }
      .do{ Assert!(ListIter#l1
                      .map{n -> n * 10}
                      .find{n -> n == 140}
                      .isSome,
        "map", {{}})}
      .do{ Assert!(ListIter#l1
                      .filter{n -> n > 50}
                      .find{n -> n == 84}
                      .isSome,
        "filter", {{}})}
      .do{ Assert!(ListIter#l1
                      .filter{n -> n > 50}
                      .count == 2u,
        "count", {{}})}
      .do{ Assert!(ListIter#l1
                      .filter{n -> n > 50}
                      .list.len == 2u,
        "toList", {{}})}
      .do{ Assert!(ListIter#l1
                      .filter{n -> n > 50}
                      .llist
                      .len == 2u,
        "to mut LList", {{}})}
      .do{ Assert!(ListIter#l1
                    .flatMap{n -> ListIter#(List#(n, n, n))}
                    .map{n -> n * 10}
                    .str({n -> n.str}, ";") == "350;350;350;520;520;520;840;840;840;140;140;140",
        "flatMap", {{}})}
      .do{ Assert!(Sum.int(ListIter#l1) == 185, "sum int", {{}})}
      .do{ Assert!(Sum.uint(ListIter#l1.map{n -> n.uint}) == 185u, "sum uint", {{}})}
      .do{ Assert!(Sum.float(ListIter#l1.map{n -> n.float}) == 185.0, "sum float", {{}})}
      .return{{}}
      }
    """, Base.mutBaseAliases); }

  @Test void absIntPos() { ok(new Res("", "", 0), "test.Test", """
    package test
    Test:Main{ _ -> Assert!(5 .abs == 5) }
    """, Base.mutBaseAliases); }
  @Test void absIntZero() { ok(new Res("", "", 0), "test.Test", """
    package test
    Test:Main{ _ -> Assert!(0 .abs == 0) }
    """, Base.mutBaseAliases); }
  @Test void absIntNeg() { ok(new Res("", "", 0), "test.Test", """
    package test
    Test:Main{ _ -> Assert!(-5 .abs == 5) }
    """, Base.mutBaseAliases); }

  @Test void absUIntPos() { ok(new Res("", "", 0), "test.Test", """
    package test
    Test:Main{ _ -> Assert!(5u .abs == 5u) }
    """, Base.mutBaseAliases); }
  @Test void absUIntZero() { ok(new Res("", "", 0), "test.Test", """
    package test
    Test:Main{ _ -> Assert!(0u .abs == 0u) }
    """, Base.mutBaseAliases); }

  @Test void isoPod1() { ok(new Res("", "", 0), "test.Test", """
    package test
    Test:Main{ _ -> Do#
      .var[mut IsoPod[MutThingy]] a = { IsoPod#[MutThingy](MutThingy'#(Count.int(0))) }
      .return{ Assert!(Usage#(a*) == 0) }
      }
    Usage:{ #(m: iso MutThingy): Int -> (m.n*) }
    MutThingy:{ mut .n: mut Count[Int] }
    MutThingy':{ #(n: mut Count[Int]): mut MutThingy -> { n }  }
    """, Base.mutBaseAliases); }
  @Test void isoPod2() { ok(new Res("", "", 0), "test.Test", """
    package test
    Test:Main{ _ -> Do#
      .var[mut IsoPod[MutThingy]] a = { IsoPod#[MutThingy](MutThingy'#(Count.int(0))) }
      .do{ a.next(MutThingy'#(Count.int(5))) }
      .return{ Assert!(Usage#(a*) == 5) }
      }
    Usage:{ #(m: iso MutThingy): Int -> (m.n*) }
    MutThingy:{ mut .n: mut Count[Int] }
    MutThingy':{ #(n: mut Count[Int]): mut MutThingy -> { n }  }
    """, Base.mutBaseAliases); }
  @Test void isoPod3() { ok(new Res("", "", 0), "test.Test", """
    package test
    Test:Main{ _ -> Do#
      .var[mut IsoPod[MutThingy]] a = { IsoPod#[MutThingy](MutThingy'#(Count.int(0))) }
      .do{ Yeet#(a.mutate{ mt -> Yeet#(mt.n++) }) }
      .return{ Assert!(Usage#(a*) == 1) }
      }
    Usage:{ #(m: iso MutThingy): Int -> (m.n*) }
    MutThingy:{ mut .n: mut Count[Int] }
    MutThingy':{ #(n: mut Count[Int]): mut MutThingy -> { n }  }
    """, Base.mutBaseAliases); }

  @Test void envFromRootAuth() { okWithArgs(new Res("hi bye", "", 0), "test.Test", List.of("hi", "bye"), """
    package test
    Test:Main{ s -> Do#
      .var io = { FIO#s }
      .var env = { FEnv#s }
      .return{ io.println(LListIter.im(env.launchArgs).str({arg -> arg.str}, " ")) }
      }
    """, Base.mutBaseAliases); }
  @Test void envFromIO() { okWithArgs(new Res("hi bye", "", 0), "test.Test", List.of("hi", "bye"), """
    package test
    Test:Main{ s -> Do#
      .var io = { FIO#s }
      .return{ io.println(LListIter.im(base.caps.FEnv.io(io).launchArgs).str({arg -> arg.str}, " ")) }
      }
    """, Base.mutBaseAliases); }

  @Test void intExp() { ok(new Res("3125", "", 0), "test.Test", """
    package test
    Test:Main{ s -> Do#
      .var io = { FIO#s }
      .return{ io.println(5 ** 5u .str) }
      }
    """, Base.mutBaseAliases); }
  @Test void uintExp() { ok(new Res("3125", "", 0), "test.Test", """
    package test
    Test:Main{ s -> Do#
      .var io = { FIO#s }
      .return{ io.println(5u ** 5u .str) }
      }
    """, Base.mutBaseAliases); }

  @Test void negativeNums() { ok(new Res("", "", 0), "test.Test", """
    package test
    Test:Main{ _ -> Do#
      .do{ Assert!(-5 == -5, "id", {{}}) }
      .do{ Assert!((-5 - -5) == 0, "subtraction 1", {{}}) }
      .do{ Assert!((-5 - 5) == -10, "subtraction 2", {{}}) }
      .do{ Assert!((-5 + 3) == -2, "addition 1", {{}}) }
      .do{ Assert!((-5 + 7) == 2, "addition 2", {{}}) }
      .do{ Assert!((5 + -7) == -2, "addition 3", {{}}) }
      .return{{}}
      }
    """, Base.mutBaseAliases); }

  @Test void floats() { ok(new Res("", "", 0), "test.Test", """
    package test
    Test:Main{ _ -> Do#
      .do{ Assert!(-5.0 == -5.0, "id (neg)", {{}}) }
      .do{ Assert!((-5.0 - -5.0) == 0.0, "subtraction 1", {{}}) }
      .do{ Assert!((-5.0 - 5.0) == -10.0, "subtraction 2", {{}}) }
      .do{ Assert!((-5.0 + 3.0) == -2.0, "addition 1", {{}}) }
      .do{ Assert!((-5.0 + 7.0) == 2.0, "addition 2", {{}}) }
      .do{ Assert!((5.0 + -7.0) == -2.0, "addition 3", {{}}) }
      // pos
      .do{ Assert!(1.0 == 1.0, "id", {{}}) }
      .do{ Assert!(1.0 + 3.5 == 4.5, "addition 1 (pos)", {{}}) }
      .do{ Assert!((1.0).str == "1.0", "str", {{}}) }
      .do{ Assert!((5.0 / 2.0) == 2.5, (5.0 / 2.0).str, {{}}) }
      .return{{}}
      }
    """, Base.mutBaseAliases); }

  @Test void shouldPeekIntoIsoPod() { ok(new Res("""
    peek: help, i'm alive
    consume: help, i'm alive
    """.strip(), "", 0), "test.Test", """
    package test
    Test:Main{ s -> Do#
      .var io = { FIO#s }
      .var s1 = { IsoPod#[iso Str](iso "help, i'm alive") }
      .do{ PrintMsg#(io, s1) }
      .return{ io.println("consume: " + (s1.consume)) }
      }
    PrintMsg:{
      #(io: mut IO, msg: read IsoPod[iso Str]): Void -> msg.peek{
        .some(str) -> io.println("peek: " + str),
        .empty -> Void
        }
      }
    """, Base.mutBaseAliases); }
  @Test void shouldPeekIntoIsoPodHyg() { ok(new Res("""
    peek: help, i'm alive
    consume: help, i'm alive
    """.strip(), "", 0), "test.Test", """
    package test
    Test:Main{ s -> Do#
      .var[mut IO] io = { FIO#s }
      .var s1 = { IsoPod#[Str]iso "help, i'm alive" }
      .do{ s1.peekHyg{
        .some(str) -> io.println("peek: " + str),
        .empty -> Void
        }}
      .return{ io.println("consume: " + (s1.consume)) }
      }
    """, Base.mutBaseAliases); }

  @Test void automatonPure() {
    ok(new Res("", "", 0), "test.Test", """
      package test
      alias base.iter.Automaton as Auto,
      Test:Main{ _ -> Do#
        .var[Auto[FB, FB]] pB = { Auto.pure(F[FB,FB]{ _ -> Bar }) }
        .var[Auto[FB, FB]] pId = { Auto.pure(F[FB,FB]{ a -> a }) }
        .do{ Assert!(pB.step(Foo)!.result.str == "Bar") }
        .do{ Assert!(pB.step(Foo)!.next.step(Foo)!.result.str == "Bar") }
        .do{ Assert!(pId.step(Foo)!.result.str == "Foo") }
        .do{ Assert!(pId.step(Foo)!.next.step(Bar)!.result.str == "Bar") }
        .return{{}}
        }
      
      FB:Stringable{}
      Foo:FB{ .str -> "Foo" }
      Bar:FB{ .str -> "Bar" }
      """, Base.mutBaseAliases);
  }
  @Test void automatonConst() {
    ok(new Res("", "", 0), "test.Test", """
      package test
      alias base.iter.Automaton as Auto,
      Test:Main{ _ -> Do#
        .var[mut Auto[FB, mut FB]] pB = { mut Auto.const[FB, mut FB](mut Bar) }
        .do{ Assert!(pB.step(Foo)!.result.str == "Bar") }
        .do{ Assert!(pB.step(Foo)!.next.step(Foo)!.result.str == "Bar") }
        .return{{}}
        }
      FB:Stringable{}
      Foo:FB{ .str -> "Foo" }
      Bar:FB{ .str -> "Bar" }
      """, Base.mutBaseAliases);
  }
  @Test void automatonConstMutCapture() {
    ok(new Res("", "", 0), "test.Test", """
    package test
    alias base.iter.Automaton as Auto,
    Test:Main{ _ -> Do#
      .var[mut Bar] bar = { mut Bar }
      .var[mut Auto[FB, mut FB]] pB = { mut Auto.const[FB, mut FB](bar) }
      .do{ Assert!(pB.step(Foo)!.result.str == "Bar") }
      .do{ Assert!(pB.step(Foo)!.next.step(Foo)!.result.str == "Bar") }
      .return{{}}
      }
    FB:Stringable{}
    Foo:FB{ .str -> "Foo" }
    Bar:FB{ .str -> "Bar" }
    """, Base.mutBaseAliases);
  }
  @Test void automatonId() {
    ok(new Res("", "", 0), "test.Test", """
      package test
      alias base.iter.Automaton as Auto,
      Test:Main{ _ -> Do#
        .var[mut Auto[mut FB, mut FB]] pB = { mut Auto.id[mut FB] }
        .do{ Assert!(pB.step(mut Foo)!.result.str == "Foo") }
        .do{ Assert!(pB.step(mut Foo)!.next.step(mut Bar)!.result.str == "Bar") }
        .return{{}}
        }
      FB:Stringable{}
      Foo:FB{ .str -> "Foo" }
      Bar:FB{ .str -> "Bar" }
      """, Base.mutBaseAliases);
  }
  @Test void automatonComposition() {
    ok(new Res("", "", 0), "test.Test", """
      package test
      alias base.iter.Automaton as Auto,
      Test:Main{ _ -> Do#
        .var[Auto[Int,Int]] a = { Auto.const[Int,Int] 5 }
        .var[Auto[Int,Int]] b = { Auto.pure(F[Int,Int]{n -> n * 10}) }
        .do{ Assert!(a.step(0)!.result == 5) }
        .do{ Assert!(b.step(6)!.result == 60) }
        .var[Auto[Int,Int]] c = { a |> b }
        .do{ Assert!(c.step(0)!.result == 50) }
        .return{{}}
        }
      """, Base.mutBaseAliases);
  }
  @Test void automatonCompositionBackwards() {
    ok(new Res("", "", 0), "test.Test", """
      package test
      alias base.iter.Automaton as Auto,
      Test:Main{ _ -> Do#
        .var[Auto[Int,Int]] a = { Auto.const[Int,Int] 5 }
        .var[Auto[Int,Int]] b = { Auto.pure(F[Int,Int]{n -> n * 10}) }
        .do{ Assert!(a.step(0)!.result == 5) }
        .do{ Assert!(b.step(6)!.result == 60) }
        .var[Auto[Int,Int]] c = { b <| a }
        .do{ Assert!(c.step(0)!.result == 50) }
        .return{{}}
        }
      """, Base.mutBaseAliases);
  }
  @Test void automatonList1() {
    ok(new Res("", "", 0), "test.Test", """
      package test
      alias base.iter.Automaton as Auto,
      Test:Main{ _ -> Do#
        .var[LList[Int]] l = { LList[Int] + 12 + 3 + 6 + 7 }
        .var[mut Auto[Void, Int]] a = { mut Auto.llist(l) }
        .var[mut Auto[Int,Int]] x10 = { mut Auto.pure(F[Int,Int]{ n -> n * 10 }) }
        .var[mut Auto[Void, Int]] ax10 = { a |> x10 }
        .do{ Assert!(a.step{}!.result == 12) }
        .do{ Assert!(a.step{}!.result == 12) }
        .do{ Assert!(a.step{}!.next.step(Void)!.result == 3) }
        .do{ Assert!(ax10.step{}!.result == 120) }
        .return{{}}
        }
      """, Base.mutBaseAliases);
  }
  @Test void automatonList2() {
    ok(new Res("", "", 0), "test.Test", """
      package test
      alias base.iter.Automaton as Auto,
      Test:Main{ _ -> Do#
        .var[LList[Int]] l = { LList[Int] + 12 + 3 + 6 + 7 }
        .var[Auto[Void,Int]] a = { Auto.llist(l) }
        .var[Auto[Int,Int]] x10 = { Auto.pure(F[Int,Int]{ n -> n * 10 }) }
        .var[Auto[Void, Int]] ax10 = { a |> x10 }
        .do{ Assert!(a.step(Void)!.result == 12) }
        .do{ Assert!(a.step(Void)!.result == 12) }
        .do{ Assert!(a.step(Void)!.next.step(Void)!.result == 3) }
        .do{ Assert!(ax10.step(Void)!.result == 120) }
        .return{{}}
        }
      """, Base.mutBaseAliases);
  }
  @Test void automatonListRunnerMut() {
    ok(new Res("", "", 0), "test.Test", """
      package test
      alias base.iter.Automaton as Auto,
      Test:Main{ _ -> Do#
        .var[LList[Int]] l = { LList[Int] + 12 + 3 + 6 + 7 }
        .var[mut Auto[Int,Int]] x10 = { mut Auto.pure(F[Int,Int]{ n -> n * 10 }) }
        .var[Int] lx10 = { l.run(x10)! }
        .assert{ lx10 == 70 }
        .return{{}}
        }
      """, Base.mutBaseAliases);
  }
  @Test void automatonListRunner() {
    ok(new Res("", "", 0), "test.Test", """
      package test
      alias base.iter.Automaton as Auto,
      Test:Main{ _ -> Do#
        .var[LList[Int]] l = { LList[Int] + 12 + 3 + 6 + 7 }
        .var[Auto[Int,Int]] x10 = { Auto.pure(F[Int,Int]{ n -> n * 10 }) }
        .var[Int] lx10 = { l.run(x10)! }
        .assert{ lx10 == 70 }
        .return{{}}
        }
      """, Base.mutBaseAliases);
  }
  @Test void automatonAllMatch1() {
    ok(new Res("", "", 0), "test.Test", """
      package test
      alias base.iter.Automaton as Auto,
      alias base.iter.Predicate as P,
      Test:Main{ _ -> Do#
        .var[LList[Int]] l = { LList[Int] + 12 + 3 + 6 + 7 }
        .assert{ l.run(Auto.allMatch P[Int]{n -> n > 1 })! }
        .assert{ l.run(Auto.allMatch P[Int]{n -> n > 4 })!.not }
        .assert{ l.run[Bool]((Auto.allMatch P[Int]{n -> n > 4 }) |> (Auto.pure F[Bool,Bool]{b -> b.not}))! }
        .assert{ l.run[Bool]((Auto.pure F[Bool,Bool]{b -> b.not}) <| (Auto.allMatch P[Int]{n -> n > 4 }))! }
        .return{{}}
        }
      """, Base.mutBaseAliases);
  }
  @Test void automatonEarlyExit() {
    ok(new Res("", "", 0), "test.Test", """
      package test
      alias base.iter.Automaton as Auto,
      alias base.iter.MapFn as MF, alias base.iter.Predicate as P, alias base.iter.DoFn as DF,
      Test:Main{ s -> Do#
        .var[LList[Int]] l = { LList[Int] + 5 + 3 + 6 + 7 }
        .var[mut Auto[Int,Int]] x10 = { mut Auto.pure(F[Int,Int]{ n -> n * 10 }) }
        .assert{ l.run(mut Auto.map(mut MF[Int,Int]{n -> n * 10})
                               .map(mut MF[Int,Int]{n -> Assert!(n < 70, "not early exiting", {n})})
                               .allMatch(mut P[Int]{n -> n >= 50}))!.not }
        .return{{}}
        }
      """, Base.mutBaseAliases);
  }
  @Test void automatonEarlyExitFilter() {
    ok(new Res("", "", 0), "test.Test", """
      package test
      alias base.iter.Automaton as Auto,
      alias base.iter.MapFn as MF, alias base.iter.Predicate as P, alias base.iter.DoFn as DF,
      Test:Main{ s -> Do#
        .var[LList[Int]] l = { LList[Int] + 1 + 2 + 3 }
        .assert{ l.run(mut Auto.map(mut MF[Int,Int]{n -> n * 10})
                               .filter(mut P[Int]{n -> (n == 20).not})
                               .allMatch(mut P[Int]{n -> (n == 20).not}))! }
        .return{{}}
        }
      """, Base.mutBaseAliases);
  }
  @Test void automatonRead() {
    ok(new Res("Bob", "", 0), "test.Test", """
      package test
      alias base.iter.Automaton as Auto,
      alias base.iter.MapFn as MF, alias base.iter.Predicate as P, alias base.iter.DoFn as DF,
      Person:{ read .name: Str } FPerson:F[Str,mut Person]{ name -> { name } }
      Test:Main{ s -> Do#
        .var[mut LList[mut Person]] l = { mut LList#[mut Person] (FPerson#"Alice") + (FPerson#"Bob") + (FPerson#"Charles") }
        .do{ FIO#s.println(ReadAuto#l) }
        .return{{}}
        }
      ReadAuto:{
        #(l: read LList[mut Person]): Str -> l.run[Str](read Auto.map(read MF[read Person,Str]{p -> p.name})
                                                                 .filter({name -> (name == "Charles").not }))!,
        }
      """, Base.mutBaseAliases);
  }
  @Test void automatonAllMatch2() {
    ok(new Res("", "", 0), "test.Test", """
      package test
      alias base.iter.Automaton as Auto,
      alias base.iter.Predicate as P,
      alias base.iter.MapFn as MF,
      Test:Main{ _ -> Do#
        .var[LList[Int]] l = { LList[Int] + 12 + 3 + 6 + 7 }
        .assert{ l.run(Auto.map(MF[Int,Int]{n -> n * 10})
                           .map(MF[Int,Int]{n -> n * 1000})
                           .allMatch(P[Int]{n -> n >= 30000}))! }
//        .assert{ Auto.map(MF[Int,Int]{n -> n * 10})
//                     .map(MF[Int,Int]{n -> n * 1000})
//                     .allMatch(P[Int]{n -> n >= 30000})
//                     .run(base.iter.RunAutomaton[Int,Bool,Opt[Bool]]{auto -> l.run(auto)})
//          }
        .return{{}}
        }
      """, Base.mutBaseAliases);
  }

  @Test void optionalMapImm() {
    ok(new Res("", "", 0), "test.Test", """
      package test
      Test:Main{ _ -> Do#
        .var[Opt[Int]] i = { Opt#[Int]16 }
        .var[Opt[Int]] ix10 = { i.map{n -> n * 10} }
        .return{{}}
        }
      """, Base.mutBaseAliases);
  }

  @Test void equality() {
    ok(new Res("", "", 0), "test.Test", """
      package test
      Test:Main{ _ -> Do#
        .var[Shape] s1 = {{ .x -> 5, .y -> 6 }}
        .var[Shape] s1' = {{ .x -> 5, .y -> 6 }}
        .assert({ s1 == s1 }, "shape eq same id")
        .assert({ s1 == s1' }, "shape same structure")
        .var[Shape] s2 = {{ .x -> 7, .y -> 6 }}
        .assert({ s1 != s2 }, "shape neq")
        .return{{}}
        }
      
      Shape:{
        .x: Int, .y: Int,
        ==(other: Shape): Bool -> (this.x == (other.x)) && (this.y == (other.y)),
        !=(other: Shape): Bool -> this == other .not,
        }
      """, Base.mutBaseAliases);
  }
  @Test void equalitySubtyping() {
    ok(new Res("", "", 0), "test.Test", """
      package test
      Test:Main{ _ -> Do#
        .var[Shape] s1 = {{ .x -> 5, .y -> 6 }}
        .var[Square] sq1 = {{ .x -> 5, .y -> 6, .size -> 12 }}
        .var[Square] sq1' = {{ .x -> 5, .y -> 6, .size -> 12 }}
        .assert({ sq1 == sq1' }, "square eq same structure")
        .assert({ s1 == sq1 }, "shape eq")
        .return{{}}
        }
      
      Shape:{
        .x: Int, .y: Int,
        ==(other: Shape): Bool -> (this.x == (other.x)) && (this.y == (other.y)),
        !=(other: Shape): Bool -> this == other .not,
        }
      Square:Shape{
        read .size: Int,
        .eqSq(other: Square): Bool -> this == other && (this.size == (other.size)),
        }
      """, Base.mutBaseAliases);
  }

  @Test void callingMultiSigAmbiguousDiffRet() { ok(new Res("", "", 0), "test.Test", """
    package test
    alias base.Void as Void, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    A:{
      read .m1: read B -> {},
      mut .m1: mut A -> Assert!(False, {{}}),
      }
    B:{}
    Test:base.Main{
      #(s) -> ToVoid#(this.aRead(mut A)),
      read .aRead(a: mut A): read B -> a.m1[](),
      }
    ToVoid:{ #[I](x: mdf I): Void -> {} }
    """); }
  @Test void callingMultiSigAmbiguousDiffRetMut() { ok(new Res("", "", 0), "test.Test", """
    package test
    alias base.Void as Void, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    A:{
      read .m1: read B -> Assert!(False, {{}}),
      mut .m1: mut A -> this,
      }
    B:{}
    Test:base.Main{
      #(s) -> ToVoid#(this.aRead(mut A)),
      read .aRead(a: mut A): mut A -> a.m1[](),
      }
    ToVoid:{ #[I](x: mdf I): Void -> {} }
    """); }
  @Test void callingMultiSigAmbiguousSameRet() { ok(new Res("", "", 0), "test.Test", """
    package test
    alias base.Void as Void, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    A:{
      read .m1: mut A -> Assert!(False, { mut A }),
      mut .m1: mut A -> {},
      }
    B:{}
    Test:base.Main{
      #(s) -> ToVoid#(this.aRead(mut A)),
      read .aRead(a: mut A): mut A -> a.m1[](),
      }
    ToVoid:{ #[I](x: mdf I): Void -> {} }
    """); }
  @Test void optionals1() { ok(new Res("", "", 0), "test.Test", """
    package test
    alias base.Void as Void,
    A:{}
    Test:base.Main{
      #(s) -> (base.Opt#Void).match[base.Void](mut base.OptMatch[Void,Void]{ .some(x) -> x, .empty -> {} }),
      }
    """); }
  @Test void findClosestIntMultiMdf() { ok(new Res("", "", 0), "test.Test", """
    package test
    Test:Main{ _ -> Do#
      .var[Int] closest = { Closest#(LList[Int] + 35 + 52 + 84 + 14, 49) }
      .return{ Assert!(closest == 52, closest.str, {{}}) }
      }
    Closest:{
      #(ns: LList[Int], target: Int): Int -> Do#
        .do{ Assert!(ns.isEmpty.not, "empty list :-(", {{}}) }
        .var[mut Ref[Int]] closest = { Ref#(ns.head!) }
        .do{ mut Closest'{ 'self
          h, t -> h.match[Void] mut OptMatch[Int,Void]{
            .empty -> {},
            .some(n) -> (target - n).abs < ((target - (closest*)).abs) ? {
              .then -> closest := n,
              .else -> self#(t.head, t.tail)
              }
            }
          }#(ns.head, ns.tail) }
        .return{ closest* }
      }
    Closest':{ mut #(h: Opt[Int], t: LList[Int]): Void }
    """, Base.mutBaseAliases); }
  @Test void canCreateMutLList() { ok(new Res("", "", 0), "test.Test", """
    package test
    Test:base.Main{ _ -> {} }
    MutLList:{ #: mut base.LList[base.Int] -> mut base.LList[base.Int] +[] 35 +[] 52 +[] 84 +[] 14 }
    """); }
}
