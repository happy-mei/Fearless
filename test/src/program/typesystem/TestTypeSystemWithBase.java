package program.typesystem;

import failure.CompileError;
import main.Main;
import net.jqwik.api.Example;
import org.junit.jupiter.api.Assertions;
import parser.Parser;
import program.inference.InferBodies;
import utils.Base;
import utils.Err;
import wellFormedness.WellFormednessFullShortCircuitVisitor;
import wellFormedness.WellFormednessShortCircuitVisitor;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class TestTypeSystemWithBase {
  void ok(String... content){
    Main.resetAll();
    AtomicInteger pi = new AtomicInteger();
    String[] baseLibs = Base.baseLib;
    var ps = Stream.concat(Arrays.stream(content), Arrays.stream(baseLibs))
      .map(code -> new Parser(Path.of("Dummy"+pi.getAndIncrement()+".fear"), code))
      .toList();
    var p = Parser.parseAll(ps);
    new WellFormednessFullShortCircuitVisitor().visitProgram(p).ifPresent(err->{ throw err; });
    var inferredSigs = p.inferSignaturesToCore();
    var inferred = new InferBodies(inferredSigs).inferAll(p);
    new WellFormednessShortCircuitVisitor(inferred).visitProgram(inferred);
    inferred.typeCheck();
  }
  void fail(String expectedErr, String... content){
    Main.resetAll();
    AtomicInteger pi = new AtomicInteger();
    String[] baseLibs = Base.baseLib;
    var ps = Stream.concat(Arrays.stream(content), Arrays.stream(baseLibs))
      .map(code -> new Parser(Path.of("Dummy"+pi.getAndIncrement()+".fear"), code))
      .toList();
    var p = Parser.parseAll(ps);
    new WellFormednessFullShortCircuitVisitor().visitProgram(p).ifPresent(err->{ throw err; });
    var inferredSigs = p.inferSignaturesToCore();
    var inferred = new InferBodies(inferredSigs).inferAll(p);
    try {
      inferred.typeCheck();
      Assertions.fail("Did not fail!\n");
    } catch (CompileError e) {
      Err.strCmp(expectedErr, e.toString());
    }
  }

  @Example void simpleProgram(){ ok( """
    package test
    A:{ .m: A -> this }
    """); }

  @Example void baseLib(){ ok(); }

  @Example void subTypingCall(){ ok( """
    package test
    A:{ .m1(a: A): A -> a }
    B:A{}
    C:{ .m2: A -> A.m1(B) }
    """); }

  @Example void numbers1(){ ok( """
    package test
    A:{ .m(a: 42): 42 -> 42 }
    """); }

  @Example void numbersNoBase(){ ok( """
    package test
    A:{ .m(a: 42): 42 -> 42 }
    """, """
    package base
    Sealed:{} Stringable:{ .str: Str } Str:{} Bool:{}
    """, Base.load("nums.fear")); }

  @Example void numbersSubTyping1(){ ok("""
    package test
    alias base.Int as Int,
    A:{ .m(a: 42): Int -> a }
    """); }
  @Example void numbersSubTyping2(){ fail("""
    In position [###]/Dummy0.fear:3:4
    [E23 methTypeError]
    Expected the method .m/1 to return imm 42[], got imm base.Int[].
    """, """
    package test
    alias base.Int as Int,
    A:{ .m(a: Int): 42 -> a }
    """); }
  @Example void numbersSubTyping3(){ ok("""
    package test
    alias base.Int as Int,
    A:{ .a: Int }
    B:A{ .a -> 42 }
    C:A{ .a -> 420 }
    """); }
  @Example void numbersSubTyping4(){ ok("""
    package test
    alias base.Int as Int,
    A:{ .a: Int }
    B:A{ .a -> 42 }
    C:A{ .a -> 420 }
    D:B{ .b: Int -> this.a }
    """); }
  @Example void numbersGenericTypes1(){ ok("""
    package test
    alias base.Int as Int,
    A[N]:{ .count: N }
    B:A[42]{ 42 }
    C:A[Int]{ 42 }
    """); }
  @Example void numbersGenericTypes2(){ ok("""
    package test
    alias base.Int as Int,
    A[N]:{ .count: N, .sum: N }
    B:A[42]{ .count -> 42, .sum -> 42 }
    C:A[Int]{ .count -> 56, .sum -> 3001 }
    """); }
  @Example void numbersGenericTypes2a(){ fail("""
    In position [###]/Dummy0.fear:4:31
    [E18 uncomposableMethods]
    These methods could not be composed.
    conflicts:
    ([###]/Dummy4.fear:43:2) 43[], .float/0
    ([###]/Dummy4.fear:43:2) 42[], .float/0
    """, """
    package test
    alias base.Int as Int,
    A[N]:{ .count: N, .sum: N }
    B:A[42]{ .count -> 42, .sum -> 43 }
    """); }
  @Example void numbersSubTyping5a(){ fail("""
    In position [###]/Dummy0.fear:6:5
    [E23 methTypeError]
    Expected the method .b/0 to return imm 42[], got imm base.Int[].
    """, """
    package test
    alias base.Int as Int,
    A:{ .a: Int }
    B:A{ .a -> 42 }
    C:A{ .a -> 420 }
    D:B{ .b: 42 -> this.a }
    """); }
  @Example void twoInts(){ ok("""
    package test
    alias base.Int as Int,
    A:{ .m(a: 56, b: 12): Int -> b+a }
    """); }

  @Example void boolIntRet() { ok("""
    package test
    alias base.Main as Main, alias base.Int as Int, alias base.False as False, alias base.True as True,
    Test:Main[Int]{
      _->False.or(True)?{.then->42,.else->0}
    }
    """); }
  @Example void boolSameRet() { ok("""
    package test
    alias base.Main as Main, alias base.Int as Int, alias base.False as False, alias base.True as True,
    Foo:{}
    Test:Main[Foo]{
      _->False.or(True)?{.then->Foo,.else->Foo}
    }
    """); }

  @Example void numImpls1() { ok("""
    package test
    alias base.Int as Int,
    Foo:{ .bar: 5 -> 5 }
    Bar:{
      .nm(n: Int): Int -> n,
      .check: Int -> this.nm(Foo.bar)
      }
    """);}

  @Example void numImpls2() { ok("""
    package test
    alias base.Int as Int,
    Bar:{
      .nm(n: Int): Int -> n,
      .check: Int -> this.nm(5)
      }
    """);}

  @Example void numImpls3() { fail("""
    In position [###]/Dummy0.fear:5:25
    [E18 uncomposableMethods]
    These methods could not be composed.
    conflicts:
    ([###]/Dummy4.fear:63:2) 5[], <=/1
    ([###]/Dummy4.fear:28:2) base.MathOps[imm base.Float[]], <=/1
    """, """
    package test
    alias base.Int as Int, alias base.Float as Float,
    Bar:{
      .nm(n: Float): Int -> 12,
      .check: Int -> this.nm(5)
      }
    """);}

  @Example void numImpl4() { fail("""
    In position [###]/Dummy0.fear:5:25
    [E18 uncomposableMethods]
    These methods could not be composed.
    conflicts:
    ([###]/Dummy4.fear:43:2) 5[], .float/0
    ([###]/Dummy4.fear:43:2) 6[], .float/0
    """, """
    package test
    alias base.Int as Int,
    Bar:{
      .nm(n: 6): Int -> 12,
      .check: Int -> this.nm(5)
      }
    """);}

  @Example void incompatibleGens() { fail("""
    In position [###]/Dummy1.fear:7:12
    [E34 bothTExpectedGens]
    Type error: the generic type lent C cannot be a super-type of any concrete type, like Fear71$/0.
    """, """
    package test
    alias base.Main as Main, alias base.Void as Void,
    alias base.caps.IO as IO, alias base.caps.IO' as IO',
    Test:Main[Void]{ s -> s
      .use[IO] io = IO'
      .return{ io.println("Hello, World!") }
      }
    """, """
    package base.caps
    alias base.Sealed as Sealed, alias base.Void as Void, alias base.Str as Str,
    // bad version of caps.fear
    LentReturnStmt[R]:{ lent #: mdf R }
    System[R]:{
      lent .use[C](c: CapFactory[lent C, lent C], cont: mut UseCapCont[C, mdf R]): mdf R ->
        cont#(c#NotTheRootCap, this), // should fail here because NotTheRootCap is not a sub-type of C
      lent .return(ret: lent LentReturnStmt[mdf R]): mdf R -> ret#
      }
        
    NotTheRootCap:{}
    _RootCap:IO{ .println(msg) -> this.println(msg), }
    UseCapCont[C, R]:{ mut #(cap: lent C, self: lent System[mdf R]): mdf R }
    CapFactory[C,R]:{
      #(s: lent C): lent R,
      .close(c: lent R): Void,
      }
    IO:{
      lent .print(msg: Str): Void,
      lent .println(msg: Str): Void,
      }
    IO':CapFactory[lent IO, lent IO]{
      #(auth: lent IO): lent IO -> auth,
      .close(c: lent IO): Void -> {},
      }
    """); }
  @Example void incompatibleITs() { fail("""
    In position [###]/Dummy1.fear:7:8
    [E33 callTypeError]
    Type error: None of the following candidates for this method call:
    cont #/2[]([c #/1[]([[-lent-][base.caps._RootCap[], base.caps.NotTheRootCap[]]{'fear1$ }]), this])
    were valid:
    (mut base.caps.UseCapCont[imm C, mdf R], ?c #/1[]([[-lent-][base.caps._RootCap[], base.caps.NotTheRootCap[]]{'fear1$ }])?, lent base.caps.System[mdf R]) <: TsT[ts=[mut base.caps.UseCapCont[imm C, mdf R], lent C, lent base.caps.System[mdf R]], t=mdf R]
    (mut base.caps.UseCapCont[imm C, mdf R], ?c #/1[]([[-lent-][base.caps._RootCap[], base.caps.NotTheRootCap[]]{'fear1$ }])?, lent base.caps.System[mdf R]) <: TsT[ts=[iso base.caps.UseCapCont[imm C, mdf R], lent C, lent base.caps.System[mdf R]], t=mdf R]
    (mut base.caps.UseCapCont[imm C, mdf R], ?c #/1[]([[-lent-][base.caps._RootCap[], base.caps.NotTheRootCap[]]{'fear1$ }])?, lent base.caps.System[mdf R]) <: TsT[ts=[iso base.caps.UseCapCont[imm C, mdf R], iso C, iso base.caps.System[mdf R]], t=mdf R]
    (mut base.caps.UseCapCont[imm C, mdf R], ?c #/1[]([[-lent-][base.caps._RootCap[], base.caps.NotTheRootCap[]]{'fear1$ }])?, lent base.caps.System[mdf R]) <: TsT[ts=[iso base.caps.UseCapCont[imm C, mdf R], mut C, lent base.caps.System[mdf R]], t=mdf R]
    (mut base.caps.UseCapCont[imm C, mdf R], ?c #/1[]([[-lent-][base.caps._RootCap[], base.caps.NotTheRootCap[]]{'fear1$ }])?, lent base.caps.System[mdf R]) <: TsT[ts=[iso base.caps.UseCapCont[imm C, mdf R], lent C, mut base.caps.System[mdf R]], t=mdf R]
    """, """
    package test
    alias base.Main as Main, alias base.Void as Void,
    alias base.caps.IO as IO, alias base.caps.IO' as IO',
    Test:Main[Void]{ s -> s
      .use[IO] io = IO'
      .return{ io.println("Hello, World!") }
      }
    """, """
    package base.caps
    alias base.Sealed as Sealed, alias base.Void as Void, alias base.Str as Str,
    // bad version of caps.fear
    LentReturnStmt[R]:{ lent #: mdf R }
    System[R]:{
      lent .use[C](c: CapFactory[lent _RootCap, lent C], cont: mut UseCapCont[C, mdf R]): mdf R ->
        cont#(c#NotTheRootCap, this), // should fail here because NotTheRootCap is not a sub-type of C
      lent .return(ret: lent LentReturnStmt[mdf R]): mdf R -> ret#
      }
        
    NotTheRootCap:{}
    _RootCap:IO{ .println(msg) -> this.println(msg), }
    UseCapCont[C, R]:{ mut #(cap: lent C, self: lent System[mdf R]): mdf R }
    CapFactory[C,R]:{
      #(s: lent C): lent R,
      .close(c: lent R): Void,
      }
    IO:{
      lent .print(msg: Str): Void,
      lent .println(msg: Str): Void,
      }
    IO':CapFactory[lent _RootCap, lent IO]{
      #(auth: lent _RootCap): lent IO -> auth,
      .close(c: lent IO): Void -> {},
      }
    """); }
  @Example void incompatibleITsDeep() { fail("""
    In position [###]/Dummy0.fear:5:2
    [E33 callTypeError]
    Type error: None of the following candidates for this method call:
    s .use/2[imm base.caps.IO[]]([[-imm-][base.caps.IO'[]]{'fear7$ }, [-mut-][base.caps.UseCapCont[imm base.caps.IO[], imm base.Void[]]]{'fear8$ #/2([io, fear0$]): Sig[mdf=mut,gens=[],ts=[lent base.caps.IO[], lent base.caps.System[imm base.Void[]]],ret=imm base.Void[]] -> fear0$ .return/1[]([[-lent-][base.caps.LentReturnStmt[imm base.Void[]]]{'fear9$ #/0([]): Sig[mdf=lent,gens=[],ts=[],ret=imm base.Void[]] -> io .println/1[]([[-imm-]["Hello, World!"[]]{'fear10$ }])}])}])
    were valid:
    (lent base.caps.System[imm base.Void[]], imm base.caps.IO'[], mut base.caps.UseCapCont[imm base.caps.IO[], imm base.Void[]]) <: TsT[ts=[lent base.caps.System[imm base.Void[]], imm base.caps.CapFactory[lent base.caps.NotTheRootCap[], imm base.caps.IO[]], mut base.caps.UseCapCont[imm base.caps.IO[], imm base.Void[]]], t=imm base.Void[]]
    (lent base.caps.System[imm base.Void[]], imm base.caps.IO'[], mut base.caps.UseCapCont[imm base.caps.IO[], imm base.Void[]]) <: TsT[ts=[lent base.caps.System[imm base.Void[]], imm base.caps.CapFactory[lent base.caps.NotTheRootCap[], imm base.caps.IO[]], iso base.caps.UseCapCont[imm base.caps.IO[], imm base.Void[]]], t=imm base.Void[]]
    (lent base.caps.System[imm base.Void[]], imm base.caps.IO'[], mut base.caps.UseCapCont[imm base.caps.IO[], imm base.Void[]]) <: TsT[ts=[iso base.caps.System[imm base.Void[]], imm base.caps.CapFactory[lent base.caps.NotTheRootCap[], imm base.caps.IO[]], iso base.caps.UseCapCont[imm base.caps.IO[], imm base.Void[]]], t=imm base.Void[]]
    (lent base.caps.System[imm base.Void[]], imm base.caps.IO'[], mut base.caps.UseCapCont[imm base.caps.IO[], imm base.Void[]]) <: TsT[ts=[mut base.caps.System[imm base.Void[]], imm base.caps.CapFactory[lent base.caps.NotTheRootCap[], imm base.caps.IO[]], iso base.caps.UseCapCont[imm base.caps.IO[], imm base.Void[]]], t=imm base.Void[]]
    """, """
    package test
    alias base.Main as Main, alias base.Void as Void,
    alias base.caps.IO as IO, alias base.caps.IO' as IO',
    Test:Main[Void]{ s -> s
      .use[IO] io = IO'
      .return{ io.println("Hello, World!") }
      }
    """, """
    package base.caps
    alias base.Sealed as Sealed, alias base.Void as Void, alias base.Str as Str,
    // bad version of caps.fear
    LentReturnStmt[R]:{ lent #: mdf R }
    System[R]:{
      lent .use[C](c: CapFactory[lent NotTheRootCap, lent C], cont: mut UseCapCont[C, mdf R]): mdf R ->
        cont#(c#NotTheRootCap, this), // should fail here because NotTheRootCap is not a sub-type of C
      lent .return(ret: lent LentReturnStmt[mdf R]): mdf R -> ret#
      }
        
    NotTheRootCap:{}
    _RootCap:IO{ .println(msg) -> this.println(msg), }
    UseCapCont[C, R]:{ mut #(cap: lent C, self: lent System[mdf R]): mdf R }
    CapFactory[C,R]:{
      #(s: lent C): lent R,
      .close(c: lent R): Void,
      }
    IO:{
      lent .print(msg: Str): Void,
      lent .println(msg: Str): Void,
      }
    IO':CapFactory[lent IO, lent IO]{
      #(auth: lent IO): lent IO -> auth,
      .close(c: lent IO): Void -> {},
      }
    """); }
}
