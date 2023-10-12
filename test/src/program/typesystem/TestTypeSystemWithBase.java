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
import java.util.IdentityHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static program.typesystem.RunTypeSystem.fail;
import static program.typesystem.RunTypeSystem.ok;

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
    var inferred = InferBodies.inferAll(p);
    new WellFormednessShortCircuitVisitor(inferred).visitProgram(inferred).ifPresent(err->{ throw err; });
    inferred.typeCheck(new IdentityHashMap<>());
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
      var inferred = InferBodies.inferAll(p);
      new WellFormednessShortCircuitVisitor(inferred).visitProgram(inferred).ifPresent(err->{ throw err; });
      inferred.typeCheck(new IdentityHashMap<>());
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
    In position [###]/Dummy0.fear:6:19
    [E32 noCandidateMeths]
    When attempting to type check the method call: this .a/0[]([]), no candidates for .a/0 returned the expected type imm 42[]. The candidates were:
    (imm test.D[]): imm base.Int[]
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

  @Test void adaptFails() { fail("""
    """, """
    package test
    Foo:{}
    Box:{ recMdf #[T](x: recMdf T): recMdf Box[recMdf T] -> {x}, }
    No:{ #(f: mut Box[mut Foo]): read Box[read Foo] -> f, }
    No2:{
      #(f: mut Box[mut Foo]): mut Box[read Foo] -> f,
      .break(f: mut Box[read Foo]): Void -> f.update(imm Foo),
      }
    Box[T]:{
      recMdf .get: recMdf T,
      mut .update(x: mdf T): Void -> {},
      }
    Void:{}
    Break:{ #(foo: read Foo): read Box[read Foo] -> read Box#foo }
    """); }

  /*
  ReadBox[T]:{ ==~ having read vs readOnly
  read .get:read T,
  mut .setImm(imm T):Void,
  mut .setMut(mut T):Void,
  }
  ImmBox[T]:ReadBox[T]{ set goes loop }
  MutBox[T]:ReadBox[T]{ set goes loop }
  FReadBox:{#[T](t:mut T):mut ReadBox[T]->
    Ref[ReadBox[T]] state=MutBox[T]{t}
    return { get->state*, setImm->state*.. setMut->state*}
    }
   */
  @Test void readOnlyAsLib() { ok("""
    package test
    alias base.Abort as Abort,
    
    ReadBox[T]:{
      read .get: read T,
      mut .setImm(x: imm T): Void,
      mut .setMut(x: mut T): Void,
      }
    _ImmBox[T]:ReadBox[T]{
      .setImm(x) -> Abort!,
      .setMut(x) -> Abort!,
      }
    _MutBox[T]:ReadBox[T]{
      .setImm(x) -> Abort!,
      .setMut(x) -> Abort!,
      }
    FReadBox:{
      #[T](t: mut T): mut ReadBox[T] -> Do#
        .var[mut Ref[mut ReadBox[T]]] state = { Ref#[mut ReadBox[T]](mut _MutBox[T]{ t }) }
        .return{{
          .get -> state*.get,
          .setImm(x) -> state := mut _ImmBox[T]{ x },
          .setMut(x) -> state := mut _MutBox[T]{ x },
          }}
      }
    """, Base.mutBaseAliases); }

  @Test void canGetImmIntFromImmListOfImmInt() { ok("""
    package test
    MakeList:{ #: LList[Int] -> LList[Int] + 12 }
    Test:{ #: Bool -> (MakeList#).head! == 12 }
    """, Base.mutBaseAliases); }
  @Test void canGetImmIntFromImmListOfImmIntCast() { ok("""
    package test
    MakeList:{ #: LList[Int] -> LList[Int] + 12 }
    Test:{ #: Bool -> As[Int]#((MakeList#).head!) == 12 }
    """, Base.mutBaseAliases); }

  @Test void canGetImmIntFromImmListOfImmIntMatchInferFail() { fail("""
    In position [###]/Dummy0.fear:3:7
    [E33 callTypeError]
    Type error: None of the following candidates (returning the expected type "imm base.Bool[]") for this method call:
    [-imm-][test.MakeList[]]{'fear96$ } #/0[]([]) .head/0[]([]) .match/1[read base.Int[]]([[-mut-][base.OptMatch[read base.Int[], read base.Int[]]]{'fear97$ .some/1([x]): Sig[mdf=mut,gens=[],ts=[read base.Int[]],ret=read base.Int[]] -> x,
    .empty/0([]): Sig[mdf=mut,gens=[],ts=[],ret=imm 0[]] -> [-imm-][0[]]{'fear98$ }}]) ==/1[]([[-imm-][12[]]{'fear99$ }])
    were valid:
    (?[-imm-][test.MakeList[]]{'fear96$ } #/0[]([]) .head/0[]([]) .match/1[read base.Int[]]([[-mut-][base.OptMatch[read base.Int[], read base.Int[]]]{'fear97$ .some/1([x]): Sig[mdf=mut,gens=[],ts=[read base.Int[]],ret=read base.Int[]] -> x,
    .empty/0([]): Sig[mdf=mut,gens=[],ts=[],ret=imm 0[]] -> [-imm-][0[]]{'fear98$ }}])?, imm 12[]) <: (imm base.Int[], imm base.Int[]): imm base.Bool[]
      The following errors were found when checking this sub-typing:
        In position [###]/Dummy0.fear:3:34
        [E33 callTypeError]
        Type error: None of the following candidates (returning the expected type "imm base.Int[]") for this method call:
        [-imm-][test.MakeList[]]{'fear96$ } #/0[]([]) .head/0[]([]) .match/1[read base.Int[]]([[-mut-][base.OptMatch[read base.Int[], read base.Int[]]]{'fear97$ .some/1([x]): Sig[mdf=mut,gens=[],ts=[read base.Int[]],ret=read base.Int[]] -> x,
        .empty/0([]): Sig[mdf=mut,gens=[],ts=[],ret=imm 0[]] -> [-imm-][0[]]{'fear98$ }}])
        were valid:
        (?[-imm-][test.MakeList[]]{'fear96$ } #/0[]([]) .head/0[]([])?, mut base.OptMatch[read base.Int[], read base.Int[]]) <: (iso base.Opt[imm base.Int[]], iso base.OptMatch[imm base.Int[], read base.Int[]]): imm base.Int[]
          The following errors were found when checking this sub-typing:
            In position [###]/Dummy0.fear:3:29
            [E33 callTypeError]
            Type error: None of the following candidates (returning the expected type "iso base.Opt[imm base.Int[]]") for this method call:
            [-imm-][test.MakeList[]]{'fear96$ } #/0[]([]) .head/0[]([])
            were valid:
            (?[-imm-][test.MakeList[]]{'fear96$ } #/0[]([])?) <: (iso base.LList[imm base.Int[]]): iso base.Opt[imm base.Int[]]
              The following errors were found when checking this sub-typing:
                In position [###]/Dummy0.fear:3:27
                [E32 noCandidateMeths]
                When attempting to type check the method call: [-imm-][test.MakeList[]]{'fear96$ } #/0[]([]), no candidates for #/0 returned the expected type iso base.LList[imm base.Int[]]. The candidates were:
                (imm test.MakeList[]): imm base.LList[imm base.Int[]]
       \s
        (iso base.Opt[read base.Int[]], mut base.OptMatch[read base.Int[], read base.Int[]]) <: (imm base.Opt[imm base.Int[]], iso base.OptMatch[imm base.Int[], read base.Int[]]): imm base.Int[]
        (iso base.Opt[read base.Int[]], mut base.OptMatch[read base.Int[], read base.Int[]]) <: (imm base.Opt[imm base.Int[]], iso base.OptMatch[read base.Int[], read base.Int[]]): imm base.Int[]
    """, """
    package test
    MakeList:{ #: LList[Int] -> LList[Int] + 12 }
    Test:{ #: Bool -> (MakeList#).head.match { .some(x) -> x, .empty -> 0 } == 12  }
    """, Base.mutBaseAliases); }
  @Test void canGetImmIntFromImmListOfImmIntMatch() { ok("""
    package test
    MakeList:{ #: LList[Int] -> LList[Int] + 12 }
    Test:{ #: Bool -> (MakeList#).head.match mut OptMatch[Int,Int]{ .some(x) -> x, .empty -> 0 } == 12  }
    """, Base.mutBaseAliases); }
  @Test void canGetImmIntFromImmListOfImmIntMatchExplicitGens() { ok("""
    package test
    MakeList:{ #: LList[Int] -> LList[Int] + 12 }
    Test:{ #: Bool -> (MakeList#).head.match[Int]{ .some(x) -> x, .empty -> 0 } == 12  }
    """, Base.mutBaseAliases); }
  @Test void canGetImmIntFromImmListOfImmIntMatchCast() { ok("""
    package test
    MakeList:{ #: LList[Int] -> LList[Int] + 12 }
    Test1:{ #: Bool -> As[Opt[Int]]#((MakeList#).head).match{ .some(x) -> x, .empty -> 0 } == 12  } // works
    // imm Opt[read Int] fails to become imm Opt[imm Int] because of adapterOk
//    Test2:{ #: Bool -> As[Opt[Int]]#(
//       As[Opt[read Int]]#((MakeList#).head))
//        .match{ .some(x) -> x, .empty -> 0 } == 12  }
    """, Base.mutBaseAliases); }

  @Test void worksWithCast() { ok("""
    package test
    Red[T]:{
      .blue: Blue[mdf T],
      }
    Blue[T]:{
      .red: Red[mdf T],
      }
    Foo:{}
    DoIt:{
      .m1(red: mut Red[read Foo]): mut Red[Foo] -> red,
      .m2(red: mut Red[Foo]): mut Red[read Foo] -> red,
      }
    """, Base.mutBaseAliases); }
  @Test void worksWithCastWithGetter() { ok("""
    package test
    Red[T]:{
      .blue: Blue[mdf T],
      .get: mdf T,
      }
    Blue[T]:{
      .red: Red[mdf T],
      .get: mdf T,
      }
    Foo:{}
    DoIt:{
      .m1(red: Red[read Foo]): Red[Foo] -> red,
      .m2(red: Red[Foo]): Red[read Foo] -> red,
      }
    """, Base.mutBaseAliases); }

  //TODO: test that makes sure we can turn a mut List[mut Person] into a read List[read Person] via adaptorOk
}
