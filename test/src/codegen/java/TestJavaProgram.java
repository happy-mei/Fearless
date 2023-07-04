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
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

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
    var inferredSigs = p.inferSignaturesToCore();
    var inferred = new InferBodies(inferredSigs).inferAll(p);
    new WellFormednessShortCircuitVisitor(inferred).visitProgram(inferred).ifPresent(err->{
      throw err;
    });
    inferred.typeCheck();
    var mir = new MIRInjectionVisitor(inferred).visitProgram();
    var java = new JavaCodegen(inferred).visitProgram(mir.pkgs(), new Id.DecId(entry, 0));
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
    var inferredSigs = p.inferSignaturesToCore();
    var inferred = new InferBodies(inferredSigs).inferAll(p);
    new WellFormednessShortCircuitVisitor(inferred).visitProgram(inferred);
    inferred.typeCheck();
    var mir = new MIRInjectionVisitor(inferred).visitProgram();
    try {
      var java = new JavaCodegen(inferred).visitProgram(mir.pkgs(), new Id.DecId(entry, 0));
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
    Test:Main{ _, _ -> {} }
    """);}

  @Test void assertTrue() { ok(new Res("", "", 0), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _, _ -> Assert#(True, { Void }) }
    """);}
  @Test void assertFalse() { ok(new Res("", "Assertion failed :(", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _, _ -> Assert#(False, { Void }) }
    """);}
  @Test void assertFalseMsg() { ok(new Res("", "power level less than 9000", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _, _ -> Assert#(False, "power level less than 9000", { Void }) }
    """);}

  @Test void falseToStr() { ok(new Res("", "False", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _, _ -> Assert#(False, Foo.bs(False), { Void }) }
    Foo:{ .bs(b: base.Bool): base.Str -> b.str }
    """);}
  @Test void trueToStr() { ok(new Res("", "True", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _, _ -> Assert#(False, Foo.bs(True), { Void }) }
    Foo:{ .bs(s: base.Stringable): base.Str -> s.str }
    """);}

  @Test void binaryAnd1() { ok(new Res("", "True", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _, _ -> Assert#(False, (True && True) .str, { Void }) }
    """);}
  @Test void binaryAnd2() { ok(new Res("", "False", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _, _ -> Assert#(False, (True && False) .str, { Void }) }
    """);}
  @Test void binaryAnd3() { ok(new Res("", "False", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _, _ -> Assert#(False, (False && False) .str, { Void }) }
    """);}
  @Test void binaryOr1() { ok(new Res("", "True", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _, _ -> Assert#(False, (True || True) .str, { Void }) }
    """);}
  @Test void binaryOr2() { ok(new Res("", "True", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _, _ -> Assert#(False, (True || False) .str, { Void }) }
    """);}
  @Test void binaryOr3() { ok(new Res("", "True", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _, _ -> Assert#(False, (False || True) .str, { Void }) }
    """);}
  @Test void binaryOr4() { ok(new Res("", "False", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _, _ -> Assert#(False, (False || False) .str, { Void }) }
    """);}

  @Test void conditionals1() { ok(new Res("", "Assertion failed :(", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _, _ -> Assert#(420 > 9000, { Void }) }
    """);}
  @Test void conditionals2() { ok(new Res("", "Assertion failed :(", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _, _ -> Assert#("hi".len() > 9000u, { Void }) }
    """);}

  @Test void longToStr() { ok(new Res("", "123456789", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _, _ -> Assert#(False, 123456789 .str, { Void }) }
    """);}
  @Test void longLongToStr() { ok(new Res("", "9223372036854775807", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _, _ -> Assert#(False, 9223372036854775807 .str, { Void }) }
    """);}

  @Test void veryLongLongToStr() { ok(new Res("", "9223372036854775808", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _, _ -> Assert#(False, 9223372036854775808u .str, { Void }) }
    """);}
  @Test void veryLongLongIntFail() { fail("""
    [E31 invalidNum]
    The number 9223372036854775808 is not a valid Int
    """, "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _, _ -> Assert#(False, 9223372036854775808 .str, { Void }) }
    """);}
  @Test void veryLongLongUIntFail() { fail("""
    [E31 invalidNum]
    The number 10000000000000000000000u is not a valid UInt
    """, "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _, _ -> Assert#(False, 10000000000000000000000u .str, { Void }) }
    """);}
  @Test void negativeToStr() { ok(new Res("", "-123456789", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _, _ -> Assert#(False, -123456789 .str, { Void }) }
    """);}

  @Test void addition() { ok(new Res("", "7", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _, _ -> Assert#(False, (5 + 2) .str, { Void }) }
    """);}
  @Test void subtraction() { ok(new Res("", "3", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _, _ -> Assert#(False, (5 - 2) .str, { Void }) }
    """);}
  @Test void subtractionNeg() { ok(new Res("", "-2", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _, _ -> Assert#(False, (0 - 2) .str, { Void }) }
    """);}
  @Test void subtractionUnderflow() { ok(new Res("", "9223372036854775807", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _, _ -> Assert#(False, ((0 - 2) - 9223372036854775807) .str, { Void }) }
    """);}

  // TODO: using brackets around (io, s') breaks antlr, fix the grammar
  @Test void println() { ok(new Res("Hello, World!", "", 0), "test.Test", """
    package test
    alias base.Main as Main, alias base.Void as Void,
    Test:Main{ _, s -> s
      .use[base.caps.IO](base.caps.IO', { io, s' -> s'.return{ io.println "Hello, World!" } })
      }
    """);}
  @Disabled
  @Test void printlnInferUse() { ok(new Res("Hello, World!", "", 0), "test.Test", """
    package test
    alias base.Main as Main, alias base.Void as Void,
    Test:Main{ _, s -> s
      .use(base.caps.IO', { io, s' -> s'.return{ io.println "Hello, World!" } })
      }
    """);}
  @Test void printlnSugar() { ok(new Res("Hello, World!", "", 0), "test.Test", """
    package test
    alias base.Main as Main, alias base.Void as Void,
    alias base.caps.IO as IO, alias base.caps.IO' as IO',
    Test:Main{ _, s -> s
      .use[IO] io = IO'
      .return{ io.println("Hello, World!") }
      }
    """); }

  @Disabled
  @Test void printlnSugarInferUse() { ok(new Res("Hello, World!", "", 0), "test.Test", """
    package test
    alias base.Main as Main, alias base.Void as Void,
    alias base.caps.IO' as IO',
    Test:Main{ _, s -> s
      .use io = IO'
      .return{ io.println("Hello, World!") }
      }
    """); }

  @Test void nestedPkgs() { ok(new Res("", "", 0), "test.Test", """
    package test
    Test:base.Main{ _, _ -> {} }
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
    Test:Main{ _, _ -> Assert#((GetRef#5)* == 5, { Void }) }
    GetRef:{ #(n: Int): mut Ref[Int] -> Ref#n }
    """); }
  @Test void ref2() { ok(new Res("", "", 0), "test.Test", """
    package test
    alias base.Main as Main, alias base.Void as Void, alias base.Assert as Assert,
    alias base.Ref as Ref, alias base.Int as Int,
    Test:Main{ _, _ -> Assert#((GetRef#5).swap(6) == 5, { Void }) }
    GetRef:{ #(n: Int): mut Ref[Int] -> Ref#n }
    """); }
  // TODO: loops if we give a broken value like `.var[mut Ref[Int]](n = Ref#5)` (not a ReturnStmt)
  @Test void ref3() { ok(new Res("", "", 0), "test.Test", """
    package test
    alias base.Main as Main, alias base.Void as Void, alias base.Assert as Assert, alias base.Block as Block,
    alias base.Ref as Ref, alias base.Int as Int, alias base.ReturnStmt as ReturnStmt,
    Test:Main{ _, _ -> mut Block[Void]
      .var(n = { Ref#[Int]5 })
      .do{ Assert#(n.swap(6) == 5) }
      .do{ Assert#(n* == 6) }
      .return{{}}
      }
    """); }

  static String cliArgsOrElseGet = """
    package test
    MyApp:Main{ args, s -> s
      .use[IO] io = IO'
      .return{ io.println(ImmMain#args) }
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
    MyApp:Main{ args, s -> s
      .use[IO] io = IO'
      .return{ io.println(ImmMain#args) }
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
    Test:Main{ _,_ -> Do#
      .var[Int] closest = { Closest#(LList[Int] + 35 + 52 + 84 + 14, 49) }
      .return{ Assert#(closest == 52, closest.str, {{}}) }
      }
    Closest:{
      #(ns: LList[Int], target: Int): Int -> Do#
        .do{ Assert#(ns.isEmpty.not, "empty list :-(", {{}}) }
        .var[mut Ref[Int]] closest = { Ref#(ns.head!) }
        .do{ mut Closest'{ 'self
          h, t -> h.match{
            .none -> {},
            .some(n) -> (target - n).abs < (target - closest*).abs ? {
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
    Test:Main{ _,_ -> Do#
      .var[Int] closest = { Closest#(LListMut#[Int]35 + 52 + 84 + 14, 49) }
      .return{ Assert#(closest == 52, closest.str, {{}}) }
      }
    Closest:{
      #(ns: LListMut[Int], target: Int): Int -> Do#
        .do{ Assert#(ns.isEmpty.not, "empty list :-(", {{}}) }
        .var[Int] closest' = { (ns.look(0u))! }
        .var[mut Ref[Int]] closest = { Ref#(closest') }
        .do{ mut Closest'{ 'self
          h, t -> h.match{
            .none -> {},
            .some(n) -> (target - n).abs < (target - closest*).abs ? {
              .then -> closest := n,
              .else -> self#(t.head, t.tail)
              }
            }
          }#(ns.head, ns.tail) }
        .return{ closest* }
      }
    Closest':{ mut #(h: Opt[Int], t: LListMut[Int]): Void }
    """, Base.mutBaseAliases); }
  @Test void findClosestIntMut2() { ok(new Res("", "", 0), "test.Test", """
    package test
    Test:Main{ _,_ -> Do#
      .var[Int] closest = { Closest#(LListMut#[Int]35 + 52 + 84 + 14, 49) }
      .return{ Assert#(closest == 52, closest.str, {{}}) }
      }
    Closest:{
      #(ns: LListMut[Int], target: Int): Int -> Do#
        .do{ Assert#(ns.isEmpty.not, "empty list :-(", {{}}) }
        .var[mut Ref[Int]] closest = { Ref#(ns.head!) }
        .do{ mut Closest'{ 'self
          h, t -> h.match{
            .none -> {},
            .some(n) -> (target - n).abs < (target - closest*).abs ? {
              .then -> closest := n,
              .else -> self#(t.head, t.tail)
              }
            }
          }#(ns.head, ns.tail) }
        .return{ closest* }
      }
    Closest':{ mut #(h: Opt[Int], t: LListMut[Int]): Void }
    """, Base.mutBaseAliases); }
  @Test void findClosestIntMut3() { ok(new Res("", "", 0), "test.Test", """
    package test
    Test:Main{ _,_ -> Do#
      .var[Int] closest = { Closest#(LListMut#[Int]35 + 52 + 84 + 14, 49) }
      .return{ Assert#(closest == 52, closest.str, {{}}) }
      }
    Closest:{
      #(ns: LListMut[Int], target: Int): Int -> Do#
        .do{ Assert#(ns.isEmpty.not, "empty list :-(", {{}}) }
        .var[mut Ref[Int]] closest = { Ref#((ns.getImm(0u))!) }
        .do{ mut Closest'{ 'self
          h, t -> h.match{
            .none -> {},
            .some(n) -> (target - n).abs < (target - closest*).abs ? {
              .then -> closest := n,
              .else -> self#(t.head, t.tail)
              }
            }
          }#(ns.head, ns.tail) }
        .return{ closest* }
      }
    Closest':{ mut #(h: Opt[Int], t: LListMut[Int]): Void }
    """, Base.mutBaseAliases); }
  @Test void findClosestIntMutWithMutLList() { ok(new Res("", "", 0), "test.Test", """
    package test
    Test:Main{ _,_ -> Do#
      .var[Int] closest = { Closest#(LListMut#[Int]35 + 52 + 84 + 14, 49) }
      .return{ Assert#(closest == 52, closest.str, {{}}) }
      }
    Closest:{
      #(ns: mut LListMut[Int], target: Int): Int -> Do#
        .do{ Assert#(ns.isEmpty.not, "empty list :-(", {{}}) }
        .var[mut Ref[Int]] closest = { Ref#((ns.get(0u))!) }
        .do{ mut Closest'{ 'self
          h, t -> h.match{
            .none -> {},
            .some(n) -> (target - n).abs < (target - closest*).abs ? {
              .then -> closest := n,
              .else -> self#(t.head, t.tail)
              }
            }
          }#(ns.tail.head, ns.tail.tail) }
        .return{ closest* }
      }
    Closest':{ mut #(h: Opt[Int], t: LListMut[Int]): Void }
    """, Base.mutBaseAliases); }
  @Test void findClosestIntMutWithMutList() { ok(new Res("", "", 0), "test.Test", """
    package test
    Test:Main{ _,_ -> Do#
      .var[Int] closest = { Closest#((LListMut#[Int]35 + 52 + 84 + 14).list, 49) }
      .return{ Assert#(closest == 52, closest.str, {{}}) }
      }
    Closest:{
      #(ns: mut List[Int], target: Int): Int -> Do#
        .do{ Assert#(ns.isEmpty.not, "empty list :-(", {{}}) }
        .var[mut Ref[Int]] closest = { Ref#((ns.get(0u))!) }
        .do{ mut Closest'{ 'self
          i -> ns.get(i).match{
            .none -> {},
            .some(n) -> (target - n).abs < (target - closest*).abs ? {
              .then -> closest := n,
              .else -> self#(i + 1u)
              }
            }
          }#(1u) }
        .return{ closest* }
      }
    Closest':{ mut #(i: UInt): Void }
    """, Base.mutBaseAliases); }

  @Test void llistIters() { ok(new Res("", "", 0), "test.Test", """
    package test
    Test:Main{ _,_ -> Do#
      .var[LList[Int]] l1 = { LList#[Int]35 + 52 + 84 + 14 }
      .do{ Assert#(l1.head! == l1.iter.next!, "sanity", {{}}) }
      .do{ Assert#((l1.iter.find{n -> n > 60})! == 84, "find some", {{}}) }
      .do{ Assert#((l1.iter.find{n -> n > 100}).isNone, "find empty", {{}}) }
      .do{ Assert#((l1.iter
                      .map{n -> n * 10}
                      .find{n -> n == 140})
                      .isSome,
        "map", {{}})}
      .return{{}}
      }
    """, Base.mutBaseAliases); }
  @Test void llistMutItersIterRead() { ok(new Res("", "", 0), "test.Test", """
    package test
    Test:Main{ _,_ -> Do#
      .var[mut LListMut[Int]] l1 = { LListMut#[Int]35 + 52 + 84 + 14 }
      .do{ Assert#(l1.head! == l1.iter.next!, "sanity", {{}}) }
      .do{ Assert#((l1.iter.find{n -> n > 60})! == 84, "find some", {{}}) }
      .do{ Assert#((l1.iter.find{n -> n > 100}).isNone, "find empty", {{}}) }
      .do{ Assert#((l1.iter
                      .map{n -> n * 10}
                      .find{n -> n == 140})
                      .isSome,
        "map", {{}})}
      .return{{}}
      }
    """, Base.mutBaseAliases); }
  @Test void llistMutItersIterMut() { ok(new Res("", "", 0), "test.Test", """
    package test
    Test:Main{ _,_ -> Do#
      .var[mut LListMut[Int]] l1 = { LListMut#[Int]35 + 52 + 84 + 14 }
      .do{ Assert#(l1.head! == l1.iterMut.next!, "sanity", {{}}) }
      .do{ Assert#((l1.iterMut.find{n -> n > 60})! == 84, "find some", {{}}) }
      .do{ Assert#((l1.iterMut.find{n -> n > 100}).isNone, "find empty", {{}}) }
      .do{ Assert#((l1.iterMut
                      .map{n -> n * 10}
                      .find{n -> n == 140})
                      .isSome,
        "map", {{}})}
      .return{{}}
      }
    """, Base.mutBaseAliases); }
  @Test void listIterRead() { ok(new Res("", "", 0), "test.Test", """
    package test
    Test:Main{ _,_ -> Do.hyg
      .var[read List[Int]] l1 = { (LListMut#[Int]35 + 52 + 84 + 14).list }
      .do{ Assert#(l1.look(0u)! == (l1.iter.next!), "sanity", {{}}) }
      .do{ Assert#((l1.iter.find{n -> n > 60})! == 84, "find some", {{}}) }
      .do{ Assert#((l1.iter.find{n -> n > 100}).isNone, "find empty", {{}}) }
      .do{ Assert#(l1.iter
                      .map{n -> n * 10}
                      .find{n -> n == 140}
                      .isSome,
        "map", {{}})}
      .do{ Assert#(l1.iter
                      .filter{n -> n > 50}
                      .find{n -> n == 84}
                      .isSome,
        "filter", {{}})}
      .do{ Assert#(l1.iter
                      .filter{n -> n > 50}
                      .count == 2u,
        "count", {{}})}
      .do{ Assert#(l1.iter
                      .filter{n -> n > 50}
                      .list
                      .len == 2u,
        "toList", {{}})}
      .do{ Assert#(l1.iter
                      .filter{n -> n > 50}
                      .llistMut
                      .len == 2u,
        "toLListMut", {{}})}
      .do{ Assert#(l1.iter
                    .flatMap{n -> (List#(n, n, n)).iter}
                    .map{n -> n * 10}
                    .str({n -> n.str}, ";") == "350;350;350;520;520;520;840;840;840;140;140;140",
        "flatMap", {{}})}
      .do{ Assert#(Sum.int(l1.iter.map{n -> n+0}) == 185, "sum int", {{}})}
      .do{ Assert#(Sum.uint(l1.iter.map{n -> n.uint}) == 185u, "sum uint", {{}})}
      .return{{}}
      }
    """, Base.mutBaseAliases); }
  @Test void listIterMut() { ok(new Res("", "", 0), "test.Test", """
    package test
    Test:Main{ _,_ -> Do#
      .var[mut List[Int]] l1 = { (LListMut#[Int]35 + 52 + 84 + 14).list }
      .do{ Assert#((l1.get(0u))! == l1.iterMut.next!, "sanity", {{}}) }
      .do{ Assert#((l1.iterMut.find{n -> n > 60})! == 84, "find some", {{}}) }
      .do{ Assert#((l1.iterMut.find{n -> n > 100}).isNone, "find empty", {{}}) }
      .do{ Assert#((l1.iterMut
                      .map{n -> n * 10}
                      .find{n -> n == 140})
                      .isSome,
        "map", {{}})}
      .do{ Assert#((l1.iterMut
                      .filter{n -> n > 50}
                      .find{n -> n == 84})
                      .isSome,
        "filter", {{}})}
      .do{ Assert#(((l1.iterMut
                      .filter{n -> n > 50})
                      .count) == 2u,
        "count", {{}})}
      .do{ Assert#(((l1.iterMut
                      .filter{n -> n > 50})
                      .list).len == 2u,
        "toList", {{}})}
      .do{ Assert#(((l1.iterMut
                      .filter{n -> n > 50})
                      .llistMut).len == 2u,
        "toLListMut", {{}})}
      .do{ Assert#(((l1.iterMut
                    .flatMap{n -> (List#(n, n, n)).iterMut}
                    .map{n -> n * 10})
                    .str({n -> n.str}, ";")) == "350;350;350;520;520;520;840;840;840;140;140;140",
        "flatMap", {{}})}
      .do{ Assert#(Sum.int(l1.iterMut) == 185, "sum int", {{}})}
      .do{ Assert#(Sum.uint(l1.iterMut.map{n -> n.uint}) == 185u, "sum uint", {{}})}
      .return{{}}
      }
    """, Base.mutBaseAliases); }

  @Test void absIntPos() { ok(new Res("", "", 0), "test.Test", """
    package test
    Test:Main{ _,_ -> Assert#(5 .abs == 5) }
    """, Base.mutBaseAliases); }
  @Test void absIntZero() { ok(new Res("", "", 0), "test.Test", """
    package test
    Test:Main{ _,_ -> Assert#(0 .abs == 0) }
    """, Base.mutBaseAliases); }
  @Test void absIntNeg() { ok(new Res("", "", 0), "test.Test", """
    package test
    Test:Main{ _,_ -> Assert#(-5 .abs == 5) }
    """, Base.mutBaseAliases); }

  @Test void absUIntPos() { ok(new Res("", "", 0), "test.Test", """
    package test
    Test:Main{ _,_ -> Assert#(5u .abs == 5u) }
    """, Base.mutBaseAliases); }
  @Test void absUIntZero() { ok(new Res("", "", 0), "test.Test", """
    package test
    Test:Main{ _,_ -> Assert#(0u .abs == 0u) }
    """, Base.mutBaseAliases); }

  @Test void isoPod1() { ok(new Res("", "", 0), "test.Test", """
    package test
    Test:Main{ _,_ -> Do#
      .var[mut IsoPod[MutThingy]] a = { IsoPod#[MutThingy](MutThingy'#(Count.int(0))) }
      .return{ Assert#(Usage#(a*) == 0) }
      }
    Usage:{ #(m: iso MutThingy): Int -> (m.n*) }
    MutThingy:{ mut .n: mut Count[Int] }
    MutThingy':{ #(n: mut Count[Int]): mut MutThingy -> { n }  }
    """, Base.mutBaseAliases); }
  @Test void isoPod2() { ok(new Res("", "", 0), "test.Test", """
    package test
    Test:Main{ _,_ -> Do#
      .var[mut IsoPod[MutThingy]] a = { IsoPod#[MutThingy](MutThingy'#(Count.int(0))) }
      .do{ a.next(MutThingy'#(Count.int(5))) }
      .return{ Assert#(Usage#(a*) == 5) }
      }
    Usage:{ #(m: iso MutThingy): Int -> (m.n*) }
    MutThingy:{ mut .n: mut Count[Int] }
    MutThingy':{ #(n: mut Count[Int]): mut MutThingy -> { n }  }
    """, Base.mutBaseAliases); }
  @Test void isoPod3() { ok(new Res("", "", 0), "test.Test", """
    package test
    Test:Main{ _,_ -> Do#
      .var[mut IsoPod[MutThingy]] a = { IsoPod#[MutThingy](MutThingy'#(Count.int(0))) }
      .do{ Yeet#(a.mutate{ mt -> Yeet#(mt.n++) }) }
      .return{ Assert#(Usage#(a*) == 1) }
      }
    Usage:{ #(m: iso MutThingy): Int -> (m.n*) }
    MutThingy:{ mut .n: mut Count[Int] }
    MutThingy':{ #(n: mut Count[Int]): mut MutThingy -> { n }  }
    """, Base.mutBaseAliases); }

//  @Test void ref1() { ok(new Res("", "", 0), "test.Test", """
//    package test
//    alias base.Main as Main, alias base.Void as Void, alias base.Assert as Assert,
//    alias base.Ref as Ref, alias base.Int as Int,
//    Test:Main{ _, _ -> Assert#((Ref#[Int]5)* == 5, { Void }) }
//    """); }
}
