package program.typesystem;

import failure.CompileError;
import main.Main;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
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

import static program.typesystem.RunTypeSystem.fail;

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
    new WellFormednessShortCircuitVisitor(inferred).visitProgram(inferred).ifPresent(err->{ throw err; });
    inferred.typeCheck();
  }
  void fail(String expectedErr, String... content){
    Main.resetAll();
    AtomicInteger pi = new AtomicInteger();
    String[] baseLibs = Base.baseLib;
    var ps = Stream.concat(Arrays.stream(content), Arrays.stream(baseLibs))
      .map(code -> new Parser(Path.of("Dummy"+pi.getAndIncrement()+".fear"), code))
      .toList();
    try {
      var p = Parser.parseAll(ps);
      new WellFormednessFullShortCircuitVisitor().visitProgram(p).ifPresent(err->{ throw err; });
      var inferredSigs = p.inferSignaturesToCore();
      var inferred = new InferBodies(inferredSigs).inferAll(p);
      new WellFormednessShortCircuitVisitor(inferred).visitProgram(inferred).ifPresent(err->{ throw err; });
      inferred.typeCheck();
      Assertions.fail("Did not fail!\n");
    } catch (CompileError e) {
      Err.strCmp(expectedErr, e.toString());
    }
  }

  @Test void simpleProgram(){ ok( """
    package test
    A:{ .m: A -> this }
    """); }

  @Test void baseLib(){ ok(); }

  @Test void subTypingCall(){ ok( """
    package test
    A:{ .m1(a: A): A -> a }
    B:A{}
    C:{ .m2: A -> A.m1(B) }
    """); }

  @Test void numbers1(){ ok( """
    package test
    A:{ .m(a: 42): 42 -> 42 }
    """); }

  @Test void numbersSubTyping1(){ ok("""
    package test
    alias base.Int as Int,
    A:{ .m(a: 42): Int -> a }
    """); }
  @Test void numbersSubTyping2(){ fail("""
    In position [###]/Dummy0.fear:3:4
    [E23 methTypeError]
    Expected the method .m/1 to return imm 42[], got imm base.Int[].
    """, """
    package test
    alias base.Int as Int,
    A:{ .m(a: Int): 42 -> a }
    """); }
  @Test void numbersSubTyping3(){ ok("""
    package test
    alias base.Int as Int,
    A:{ .a: Int }
    B:A{ .a -> 42 }
    C:A{ .a -> 420 }
    """); }
  @Test void numbersSubTyping4(){ ok("""
    package test
    alias base.Int as Int,
    A:{ .a: Int }
    B:A{ .a -> 42 }
    C:A{ .a -> 420 }
    D:B{ .b: Int -> this.a }
    """); }
  @Test void numbersGenericTypes1(){ ok("""
    package test
    alias base.Int as Int,
    A[N]:{ .count: N }
    B:A[42]{ 42 }
    C:A[Int]{ 42 }
    """); }
  @Test void numbersGenericTypes2(){ ok("""
    package test
    alias base.Int as Int,
    A[N]:{ .count: N, .sum: N }
    B:A[42]{ .count -> 42, .sum -> 42 }
    C:A[Int]{ .count -> 56, .sum -> 3001 }
    """); }
  @Test void numbersGenericTypes2a(){ fail("""
    In position [###]/Dummy0.fear:4:23
    [E23 methTypeError]
    Expected the method .sum/0 to return imm 42[], got imm 43[].
    """, """
    package test
    alias base.Int as Int,
    A[N]:{ .count: N, .sum: N }
    B:A[42]{ .count -> 42, .sum -> 43 }
    """); }
  @Test void numbersSubTyping5a(){ fail("""
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
  @Test void twoInts(){ ok("""
    package test
    alias base.Int as Int,
    A:{ .m(a: 56, b: 12): Int -> b+a }
    """); }

  @Test void boolIntRet() { ok("""
    package test
    alias base.Main as Main, alias base.Int as Int, alias base.False as False, alias base.True as True,
    Test:{
      #: Int -> False.or(True)?{.then->42,.else->0}
    }
    """); }
  @Test void boolSameRet() { ok("""
    package test
    alias base.Main as Main, alias base.Int as Int, alias base.False as False, alias base.True as True,
    Foo:{}
    Test:{
      #: Foo -> False.or(True)?{.then->Foo,.else->Foo}
    }
    """); }

  @Test void numImpls1() { ok("""
    package test
    alias base.Int as Int,
    Foo:{ .bar: 5 -> 5 }
    Bar:{
      .nm(n: Int): Int -> n,
      .check: Int -> this.nm(Foo.bar)
      }
    """);}

  @Test void numImpls2() { ok("""
    package test
    alias base.Int as Int,
    Bar:{
      .nm(n: Int): Int -> n,
      .check: Int -> this.nm(5)
      }
    """);}

  @Test void numImpls3() { fail("""
    In position [###]/Dummy0.fear:5:21
    [E33 callTypeError]
    Type error: None of the following candidates (returning the expected type "imm base.Int[]") for this method call:
    this .nm/1[]([[-imm-][5[]]{'fear[###]$ }])
    were valid:
    (imm test.Bar[], imm 5[]) <: (imm test.Bar[], imm base.Float[]): imm base.Int[]
    """, """
    package test
    alias base.Int as Int, alias base.Float as Float,
    Bar:{
      .nm(n: Float): Int -> 12,
      .check: Int -> this.nm(5)
      }
    """);}

  @Test void numImpl4() { fail("""
    In position [###]/Dummy0.fear:5:21
    [E33 callTypeError]
    Type error: None of the following candidates (returning the expected type "imm base.Int[]") for this method call:
    this .nm/1[]([[-imm-][5[]]{'fear[###]$ }])
    were valid:
    (imm test.Bar[], imm 5[]) <: (imm test.Bar[], imm 6[]): imm base.Int[]
    """, """
    package test
    alias base.Int as Int,
    Bar:{
      .nm(n: 6): Int -> 12,
      .check: Int -> this.nm(5)
      }
    """);}

  @Test void shouldPromoteList() { ok("""
    package test
    Foo:{
      .toMut: mut List[Int] -> (mut LList#[Int]35 + 52 + 84 + 14).list,
      .toIso: iso List[Int] -> (mut LList#[Int]35 + 52 + 84 + 14).list,
      .toImm: List[Int] -> (mut LList#[Int]35 + 52 + 84 + 14).list
      }
    """, Base.mutBaseAliases);}

  @Test void cannotCreateRootCapInCode1() { fail("""
    In position [###]/Dummy0.fear:2:39
    [E35 sealedCreation]
    The sealed trait base.caps.System/0 cannot be created in a different package (test).
    """, """
    package test
    Evil:{ .break: mut base.caps.System -> { this.break } }
    """); }

  @Test void mutateInPlace() { ok("""
    package test
    Person:{ mut .name: mut Ref[Str], mut .friends: mut List[Person] }
    Person':{
      #(name: Str): mut Person -> this.new(Ref#name, List#),
      .new(name: mut Ref[Str], friends: mut List[Person]): mut Person ->
        { .name -> name, .friends -> friends },
      }
    
    MyApp:{
      #: Void -> Do#
        .var[mut List[mut Person]] ps = { List#(Person'#"Alice", Person'#"Bob", Person'#"Nick") }
        .do{ ListIter#ps.for{ p -> p.name := "new name" } }
        .return{{}}
      }
    """, Base.mutBaseAliases); }
  @Test void mutateHyg() { ok("""
    package test
    Person:{ mut .name: mut Ref[Str], mut .friends: mut List[Person] }
    Person':{
      #(name: Str): mut Person -> this.new(Ref#name, List#),
      .new(name: mut Ref[Str], friends: mut List[Person]): mut Person ->
        { .name -> name, .friends -> friends },
      }
    Usage:{
      .mutate(p: lent Person): Void -> p.name := "bob",
      }
    """, Base.mutBaseAliases); }
  @Test void mutateHyg2() { fail("""
    In position [###]/Dummy0.fear:9:48
    [E33 callTypeError]
    Type error: None of the following candidates (returning the expected type "mut base.Ref[imm base.Str[]]") for this method call:
    p .name/0[]([])
    were valid:
    (lent test.Person[]) <: (mut test.Person[]): mut base.Ref[imm base.Str[]]
    (lent test.Person[]) <: (iso test.Person[]): iso base.Ref[imm base.Str[]]
    """, """
    package test
    Person:{ mut .name: mut Ref[Str], mut .friends: mut List[Person] }
    Person':{
      #(name: Str): mut Person -> this.new(Ref#name, List#),
      .new(name: mut Ref[Str], friends: mut List[Person]): mut Person ->
        { .name -> name, .friends -> friends },
      }
    Usage:{
      .mutate(p: lent Person): iso Ref[Str] -> p.name,
//      .break: Void -> Do#
//        .var[mut Person] p = { Person'#"Alice" }
//        .var[imm Ref[Str]] illegal = { this.mutate(p) }
//        .do{ p.name := "Charles" }
//        .return
      }
    """, Base.mutBaseAliases); }
  @Test void unsoundHygienicList() { fail("""
    [###]'lent p' cannot be captured by a lent method in a mut lambda.[###]
    """, """
    package test
    Person:{ read .age: UInt, mut .age(n: UInt): Void }
    FPerson:F[UInt,mut Person]{ age -> Do#
      .var[mut Count[UInt]] age' = { Count.uint(age) }
      .return{{ .age -> age'*, .age(n) -> age' := n }}
      }
    Test:Main{ s -> Do#
      .var[mut Person] p = { FPerson#24u }
      .var[imm List[read Person]] unsound = { A#(iso List#[read Person], p) }
      .var[imm Person] uhOh = { unsound.get(0u)! }
      .do{ p.age(25u) }
      .assert({ uhOh.age == 24u }, uhOh.age.str)
      .return{{}}
      }
    A:{
      #(l: mut List[read Person], p: read Person): mut List[read Person] -> Yeet.with(l.add(p), l),
      }
    """, Base.mutBaseAliases); }
  @Test void unsoundHygienicLList() { fail("""
    [###]'lent p' cannot be captured by a lent method in a mut lambda.[###]
    """, """
    package test
    Person:{ read .age: UInt, mut .age(n: UInt): Void }
    FPerson:F[UInt,mut Person]{ age -> Do#
      .var[mut Count[UInt]] age' = { Count.uint(age) }
      .return{{ .age -> age'*, .age(n) -> age' := n }}
      }
    Test:Main{ s -> Do#
      .var[mut Person] p = { FPerson#24u }
      .var[imm LList[read Person]] unsound = { A#(iso LList[read Person]{}, p) }
      .var[imm Person] uhOh = { unsound.get(0u)! }
      .do{ p.age(25u) }
      .assert({ uhOh.age == 24u }, uhOh.age.str)
      .return{{}}
      }
    A:{
      #(l: mut LList[read Person], p: read Person): mut LList[read Person] -> l + p,
      }
    """, Base.mutBaseAliases); }
  @Test void unsoundHygienicLListMethOkPromotion() { fail("""
    In position [###]/Dummy0.fear:16:76
    [E33 callTypeError]
    Type error: None of the following candidates (returning the expected type "mut base.LList[read test.Person[]]") for this method call:
    l +/1[]([p])
    were valid:
    (lent base.LList[read test.Person[]], read test.Person[]) <: (iso base.LList[read test.Person[]], imm test.Person[]): iso base.LList[read test.Person[]]
    """, """
    package test
    Person:{ read .age: UInt, mut .age(n: UInt): Void }
    FPerson:F[UInt,mut Person]{ age -> Do#
      .var[mut Count[UInt]] age' = { Count.uint(age) }
      .return{{ .age -> age'*, .age(n) -> age' := n }}
      }
    Test:Main{ s -> Do#
      .var[mut Person] p = { FPerson#24u }
      .var[imm LList[read Person]] unsound = { A#(iso LList[read Person]{}, p) }
      .var[imm Person] uhOh = { unsound.get(0u)! }
      .do{ p.age(25u) }
      .assert({ uhOh.age == 24u }, uhOh.age.str)
      .return{{}}
      }
    A:{
      #(l: mut LList[read Person], p: read Person): iso LList[read Person] -> l + p,
      }
    """, Base.mutBaseAliases); }
}
