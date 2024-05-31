package program.typesystem;

import failure.CompileError;
import main.CompilerFrontEnd;
import main.InputOutput;
import main.Main;
import main.java.LogicMainJava;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import utils.Base;
import utils.Err;

import java.util.Arrays;

public class TestTypeSystemWithBase {
  void ok(String... content){
    Main.resetAll();
    var verbosity = new CompilerFrontEnd.Verbosity(false, false, CompilerFrontEnd.ProgressVerbosity.None);
    var logicMain = LogicMainJava.of(InputOutput.programmaticAuto(Arrays.asList(content)), verbosity);
    logicMain.check();
  }
  void fail(String expectedErr, String... content){
    Main.resetAll();
    var verbosity = new CompilerFrontEnd.Verbosity(false, false, CompilerFrontEnd.ProgressVerbosity.None);
    var logicMain = LogicMainJava.of(InputOutput.programmaticAuto(Arrays.asList(content)), verbosity);
    try {
      logicMain.check();
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
    alias base.Nat as Nat,
    A:{ .m(a: 42): Nat -> a }
    """); }
  @Test void numbersSubTyping2(){ fail("""
    In position [###]/Dummy0.fear:3:22
    [E53 xTypeError]
    Expected 'a' to be imm 42[], got imm base.Nat[].
    """, """
    package test
    alias base.Nat as Nat,
    A:{ .m(a: Nat): 42 -> a }
    """); }
  @Test void numbersSubTyping3(){ ok("""
    package test
    alias base.Nat as Nat,
    A:{ .a: Nat }
    B:A{ .a -> 42 }
    C:A{ .a -> 420 }
    """); }
  @Test void numbersSubTyping4(){ ok("""
    package test
    alias base.Nat as Nat,
    A:{ .a: Nat }
    B:A{ .a -> 42 }
    C:A{ .a -> 420 }
    D:B{ .b: Nat -> this.a }
    """); }
  @Test void numbersGenericTypes1(){ ok("""
    package test
    alias base.Nat as Nat,
    A[N]:{ .count: N }
    B:A[42]{ 42 }
    C:A[Nat]{ 42 }
    """); }
  @Test void numbersGenericTypes2(){ ok("""
    package test
    alias base.Nat as Nat,
    A[N]:{ .count: N, .sum: N }
    B:A[42]{ .count -> 42, .sum -> 42 }
    C:A[Nat]{ .count -> 56, .sum -> 3001 }
    """); }
  @Test void numbersGenericTypes2a(){ fail("""
    In position [###]/Dummy0.fear:4:31
    [E54 lambdaTypeError]
    Expected the lambda here to implement imm 42[].
    """, """
    package test
    alias base.Nat as Nat,
    A[N]:{ .count: N, .sum: N }
    B:A[42]{ .count -> 42, .sum -> 43 }
    """); }
  @Test void numbersSubTyping5a(){ fail("""
    In position [###]/Dummy0.fear:6:19
    [E32 noCandidateMeths]
    When attempting to type check the method call: this .a/0[]([]), no candidates for .a/0 returned the expected type imm 42[]. The candidates were:
    (imm test.D[]): imm base.Nat[]
    """, """
    package test
    alias base.Nat as Nat,
    A:{ .a: Nat }
    B:A{ .a -> 42 }
    C:A{ .a -> 420 }
    D:B{ .b: 42 -> this.a }
    """); }
  @Test void twoNats(){ ok("""
    package test
    alias base.Nat as Nat,
    A:{ .m(a: 56, b: 12): Nat -> b+a }
    """); }

  @Test void boolNatRet() { ok("""
    package test
    alias base.Main as Main, alias base.Nat as Nat, alias base.False as False, alias base.True as True,
    Test:{
      #: Nat -> False.or(True)?{.then->42,.else->0}
    }
    """); }
  @Test void boolSameRet() { ok("""
    package test
    alias base.Main as Main, alias base.Nat as Nat, alias base.False as False, alias base.True as True,
    Foo:{}
    Test:{
      #: Foo -> False.or(True)?{.then->Foo,.else->Foo}
    }
    """); }

  @Test void numImpls1() { ok("""
    package test
    alias base.Nat as Nat,
    Foo:{ .bar: 5 -> 5 }
    Bar:{
      .nm(n: Nat): Nat -> n,
      .check: Nat -> this.nm(Foo.bar)
      }
    """);}

  @Test void numImpls2() { ok("""
    package test
    alias base.Nat as Nat,
    Bar:{
      .nm(n: Nat): Nat -> n,
      .check: Nat -> this.nm(5)
      }
    """);}

  @Test void numImpls3() { fail("""
    In position [###]/Dummy0.fear:5:21
    [E33 callTypeError]
    Type error: None of the following candidates (returning the expected type "imm base.Nat[]") for this method call:
    this .nm/1[]([[-imm-][5[]]{'fear[###]$ }])
    were valid:
    (imm test.Bar[], imm 5[]) <= (imm test.Bar[], imm base.Float[]): imm base.Nat[]
      The following errors were found when checking this sub-typing:
        In position [###]/Dummy0.fear:5:25
        [E54 lambdaTypeError]
        Expected the lambda here to implement imm base.Float[].
    """, """
    package test
    alias base.Nat as Nat, alias base.Float as Float,
    Bar:{
      .nm(n: Float): Nat -> 12,
      .check: Nat -> this.nm(5)
      }
    """);}

  @Test void numImpl4() { fail("""
    In position [###]/Dummy0.fear:5:21
    [E33 callTypeError]
    Type error: None of the following candidates (returning the expected type "imm base.Nat[]") for this method call:
    this .nm/1[]([[-imm-][5[]]{'fear[###]$ }])
    were valid:
    (imm test.Bar[], imm 5[]) <= (imm test.Bar[], imm 6[]): imm base.Nat[]
      The following errors were found when checking this sub-typing:
        In position [###]/Dummy0.fear:5:25
        [E54 lambdaTypeError]
        Expected the lambda here to implement imm 6[].
    """, """
    package test
    alias base.Nat as Nat,
    Bar:{
      .nm(n: 6): Nat -> 12,
      .check: Nat -> this.nm(5)
      }
    """);}

  @Test void shouldPromoteList() { ok("""
    package test
    Foo:{
      .toMut: mut List[Nat] -> (mut LList[Nat] + 35 + 52 + 84 + 14).list,
      .toIso: iso List[Nat] -> (mut LList[Nat] + 35 + 52 + 84 + 14).list,
      .toImm: List[Nat] -> (mut LList[Nat] + 35 + 52 + 84 + 14).list
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
    Person:{ mut .name: mut Var[Str], mut .friends: mut List[Person] }
    Person':{
      #(name: Str): mut Person -> this.new(Var#name, List#),
      .new(name: mut Var[Str], friends: mut List[Person]): mut Person ->
        { .name -> name, .friends -> friends },
      }
    
    MyApp:{
      #: Void -> Block#
        .let[mut List[mut Person]] ps = { List#(Person'#"Alice", Person'#"Bob", Person'#"Nick") }
        .do{ ps.iter.for{ p -> p.name := "new name" } }
        .return{{}}
      }
    """, Base.mutBaseAliases); }
  @Test void mutateHyg() { ok("""
    package test
    Person:{ mut .name: mut Var[Str], mut .friends: mut List[Person] }
    Person':{
      #(name: Str): mut Person -> this.new(Var#name, List#),
      .new(name: mut Var[Str], friends: mut List[Person]): mut Person ->
        { .name -> name, .friends -> friends },
      }
    Usage:{
      .mutate(p: lent Person): Void -> p.name := "bob",
      }
    """, Base.mutBaseAliases); }
  @Test void mutateHyg2() { fail("""
    In position [###]/Dummy0.fear:9:48
    [E33 callTypeError]
    Type error: None of the following candidates (returning the expected type "mut base.Var[imm base.Str[]]") for this method call:
    p .name/0[]([])
    were valid:
    (lent test.Person[]) <= (mut test.Person[]): mut base.Var[imm base.Str[]]
      The following errors were found when checking this sub-typing:
        In position [###]/Dummy0.fear:9:47
        [E53 xTypeError]
        Expected 'p' to be mut test.Person[], got lent test.Person[].
        
    (lent test.Person[]) <= (iso test.Person[]): iso base.Var[imm base.Str[]]
      The following errors were found when checking this sub-typing:
        In position [###]/Dummy0.fear:9:47
        [E53 xTypeError]
        Expected 'p' to be iso test.Person[], got lent test.Person[].
    """, """
    package test
    Person:{ mut .name: mut Var[Str], mut .friends: mut List[Person] }
    Person':{
      #(name: Str): mut Person -> this.new(Var#name, List#),
      .new(name: mut Var[Str], friends: mut List[Person]): mut Person ->
        { .name -> name, .friends -> friends },
      }
    Usage:{
      .mutate(p: lent Person): iso Var[Str] -> p.name,
//      .break: Void -> Block#
//        .let[mut Person] p = { Person'#"Alice" }
//        .let[imm Var[Str]] illegal = { this.mutate(p) }
//        .do{ p.name := "Charles" }
//        .return
      }
    """, Base.mutBaseAliases); }
  @Test void unsoundHygienicList() { fail("""
    [###]Expected 'p' to be imm test.Person[], got mut test.Person[].[###]
    """, """
    package test
    Person:{ read .age: Nat, mut .age(n: Nat): Void }
    FPerson:F[Nat,mut Person]{ age -> Block#
      .let[mut Count[Nat]] age' = { Count.nat(age) }
      .return{{ .age -> age'*, .age(n) -> age' := n }}
      }
    Test:Main{ s -> Block#
      .let[mut Person] p = { FPerson#24 }
      .let[imm List[read Person]] unsound = { A#(iso List#[read Person], p) }
      .let[imm Person] uhOh = { unsound.get(0) }
      .do{ p.age(25) }
      .assert({ uhOh.age == 24 }, uhOh.age.str)
      .return{{}}
      }
    A:{
      #(l: mut List[read Person], p: read Person): mut List[read Person] -> Block#(l.add(p), l),
      }
    """, Base.mutBaseAliases); }
  @Test void unsoundHygienicLList() { fail("""
    [###]Expected 'p' to be imm test.Person[], got mut test.Person[].[###]
    """, """
    package test
    Person:{ readOnly .age: Nat, mut .age(n: Nat): Void }
    FPerson:F[Nat,mut Person]{ age -> Block#
      .let[mut Count[Nat]] age' = { Count.nat(age) }
      .return{{ .age -> age'*, .age(n) -> age' := n }}
      }
    Test:Main{ s -> Block#
      .let[mut Person] p = { FPerson#24 }
      .let[imm LList[read Person]] unsound = { A#(iso LList[read Person]{}, p) }
      .let[imm Person] uhOh = { unsound.get(0) }
      .do{ p.age(25) }
      .assert({ uhOh.age == 24 }, uhOh.age.str)
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
    (lent base.LList[read test.Person[]], readOnly test.Person[]) <= (mut base.LList[read test.Person[]], read test.Person[]): mut base.LList[read test.Person[]]
      The following errors were found when checking this sub-typing:
        In position [###]/Dummy0.fear:16:74
        [E53 xTypeError]
        Expected 'l' to be mut base.LList[read test.Person[]], got lent base.LList[read test.Person[]].
        
    (lent base.LList[read test.Person[]], readOnly test.Person[]) <= (iso base.LList[read test.Person[]], imm test.Person[]): iso base.LList[read test.Person[]]
      The following errors were found when checking this sub-typing:
        In position [###]/Dummy0.fear:16:74
        [E53 xTypeError]
        Expected 'l' to be iso base.LList[read test.Person[]], got lent base.LList[read test.Person[]].
    """, """
    package test
    Person:{ read .age: Nat, mut .age(n: Nat): Void }
    FPerson:F[Nat,mut Person]{ age -> Block#
      .let[mut Count[Nat]] age' = { Count.nat(age) }
      .return{{ .age -> age'*, .age(n) -> age' := n }}
      }
    Test:Main{ s -> Block#
      .let[mut Person] p = { FPerson#24 }
      .let[imm LList[read Person]] unsound = { A#(iso LList[read Person]{}, p) }
      .let[imm Person] uhOh = { unsound.get(0) }
      .do{ p.age(25) }
      .assert({ uhOh.age == 24 }, uhOh.age.str)
      .return{{}}
      }
    A:{
      #(l: mut LList[read Person], p: read Person): iso LList[read Person] -> l + p,
      }
    """, Base.mutBaseAliases); }

  @Disabled // Requires AdapterOK
  @Test void adaptFails() { fail("""
    In position [###]/Dummy0.fear:6:47
    [E53 xTypeError]
    Expected 'f' to be mut test.Box[read test.Foo[]], got mut test.Box[mut test.Foo[]].
        
    In position [###]/Dummy0.fear:4:51
    [E53 xTypeError]
    Expected 'f' to be read test.Box[read test.Foo[]], got mut test.Box[mut test.Foo[]].
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
      mut .update(x: T): Void -> {},
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
    Var[ReadBox[T]] state=MutBox[T]{t}
    return { get->state*, setImm->state*.. setMut->state*}
    }
   */
  @Test void readOnlyAsLib() { ok("""
    package test
    
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
      #[T](t: mut T): mut ReadBox[T] -> Block#
        .let[mut Var[mut ReadBox[T]]] state = { Var#[mut ReadBox[T]](mut _MutBox[T]{ t }) }
        .return{{
          .get -> state*.get,
          .setImm(x) -> state := mut _ImmBox[T]{ x },
          .setMut(x) -> state := mut _MutBox[T]{ x },
          }}
      }
    """, Base.mutBaseAliases); }

  @Test void canGetImmNatFromImmListOfImmNat() { ok("""
    package test
    MakeList:{ #: LList[Nat] -> LList[Nat] + 12 }
    Test:{ #: Bool -> (MakeList#).head! == 12 }
    """, Base.mutBaseAliases); }
  @Test void canGetImmNatFromImmListOfImmNatCast() { ok("""
    package test
    MakeList:{ #: LList[Nat] -> LList[Nat] + 12 }
    Test:{ #: Bool -> As[Nat]#((MakeList#).head!) == 12 }
    """, Base.mutBaseAliases); }

  @Test void canGetImmNatFromImmListOfImmNatTail() { ok("""
    package test
    MakeList:{ #: LList[Nat] -> LList[Nat] + 12 + 24 }
    Test:{ #: Bool -> (MakeList#).tail.head! == 24 }
    """, Base.mutBaseAliases); }
  @Test void canGetImmNatFromImmListOfImmNatTailArg() { ok("""
    package test
    Test:{ #(l: LList[Nat]): Bool -> l.tail.head! == 24 }
    """, Base.mutBaseAliases); }

  @Test void canGetImmNatFromImmListOfImmNatMatchInferFail() { ok("""
    package test
    MakeList:{ #: LList[Nat] -> LList[Nat] + 12 }
    Test:{ #: Bool -> (MakeList#).head.match { .some(x) -> x, .empty -> 0 } == 12  }
    """, Base.mutBaseAliases); }
  @Test void canGetImmNatFromImmListOfImmNatMatch() { ok("""
    package test
    MakeList:{ #: LList[Nat] -> LList[Nat] + 12 }
    Test:{ #: Bool -> (MakeList#).head.match mut base.OptMatch[Nat,Nat]{ .some(x) -> x, .empty -> 0 } == 12  }
    """, Base.mutBaseAliases); }
  @Test void canGetImmNatFromImmListOfImmNatMatchExplicitGens() { ok("""
    package test
    MakeList:{ #: LList[Nat] -> LList[Nat] + 12 }
    Test:{ #: Bool -> (MakeList#).head.match[Nat]{ .some(x) -> x, .empty -> 0 } == 12  }
    """, Base.mutBaseAliases); }
  @Test void canGetImmNatFromImmListOfImmNatMatchCast() { ok("""
    package test
    MakeList:{ #: LList[Nat] -> LList[Nat] + 12 }
    Test1:{ #: Bool -> As[Opt[Nat]]#((MakeList#).head).match{ .some(x) -> x, .empty -> 0 } == 12  } // works
    // imm Opt[read Nat] fails to become imm Opt[imm Nat] because of adapterOk
//    Test2:{ #: Bool -> As[Opt[Nat]]#(
//       As[Opt[read Nat]]#((MakeList#).head))
//        .match{ .some(x) -> x, .empty -> 0 } == 12  }
    """, Base.mutBaseAliases); }

  @Test void worksWithCast() { ok("""
    package test
    Red[T]:{
      .blue: Blue[T],
      }
    Blue[T]:{
      .red: Red[T],
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
      .blue: Blue[T],
      .get: T,
      }
    Blue[T]:{
      .red: Red[T],
      .get: T,
      }
    Foo:{}
    DoIt:{
      .m1(red: Red[read Foo]): Red[Foo] -> red,
      .m2(red: Red[Foo]): Red[read Foo] -> red,
      }
    """, Base.mutBaseAliases); }

  @Test void isoPodNoImmFromPeek() { fail("""
    [###]
    """, """
    package test
    Test:Main{ _ -> Block#
      .let[mut IsoPod[MutThingy]] a = { IsoPod#[MutThingy](MutThingy'#(Count.int(0))) }
      .let[imm Count[Nat]] ok = { a.peek[Count[Nat]]{ .some(m) -> m.rn, .empty -> base.Abort! } }
      .return{Void}
      }
    MutThingy:{ mut .n: mut Count[Nat], read .rn: read Count[Nat] }
    MutThingy':{ #(n: mut Count[Nat]): mut MutThingy -> { .n -> n, .rn -> n }  }
    """, Base.mutBaseAliases); }

  @Test void immFromVar() { ok("""
    package test
    Test:{
      .m1(r: read Var[Nat]): Nat -> r.get,
      .m2: Nat -> this.m1(Var#5),
      }
    """, Base.mutBaseAliases); }
  @Test void updateVarImmRecoverFail() { fail("""
    [###]
    """, """
    package test
    Test:Main{
      #(s) -> UnrestrictedIO#s.println(this.m2.str),
      .m1(r: mut Var[Nat]): Nat -> Block#
        .do{ r := 12 }
        .let[read Var[Nat]] rr = { r }
        .return{ rr.get },
      .m2: Nat -> this.m1(Var.ofImm[Nat]5),
      }
    """, Base.mutBaseAliases); }

  @Test void contravarianceBox() { ok("""
    package test
    Person:{ read .name: Str, read .age: Nat, }
    Student:Person{ read .grades: LList[Nat] }
    Ex:{
      .nums(o: Box[Student]): Box[Person] -> o,
      }
    """, Base.mutBaseAliases); }

  @Disabled // Requires AdapterOK
  @Test void contravarianceOpt() { ok("""
    package test
    Person:{ read .name: Str, read .age: Nat, }
    Student:Person{ read .grades: LList[Nat] }
    Ex:{
      .nums(o: Opt[Student]): Opt[Person] -> o,
      }
    """, Base.mutBaseAliases); }
  @Disabled // Requires AdapterOK
  @Test void covarianceContravariance() { ok("""
    package test
    Person:{ read .name: Str, read .age: Nat, }
    Student:Person{ read .grades: LList[Nat] }
    Ex:{
      .nums(l: LList[Student]): LList[Person] -> l,
//      .addStudent(l: LList[Person], s: Student): LList[Person] -> l + s,
      }
    """, Base.mutBaseAliases); }
  @Disabled // Requires AdapterOK
  @Test void covarianceContravarianceList() { ok("""
    package test
    Person:{ read .name: Str, read .age: Nat, }
    Student:Person{ read .grades: List[Nat] }
    Ex:{
      .nums(l: List[Student]): List[Person] -> l,
//      .addStudent(l: LList[Person], s: Student): LList[Person] -> l + s,
      }
    """, Base.mutBaseAliases); }

  @Test void covarianceContravarianceListMdf() { ok("""
    package test
    Person:{ read .name: Str, read .age: Nat, }
    Ex:{
      .nums(l: mut List[mut Person]): read List[read Person] -> l,
      }
    """, Base.mutBaseAliases); }

  @Test void extensionMethodMdfDispatch() { ok("""
    package test
    Test:Main{ s -> Block#
      .let[Opt[Nat]] res = { Opt[Nat]
        #{opt -> opt.match{.some(_) -> opt, .empty -> Opts#[Nat]9001}}
        }
      .assert{res! == 9001}
      .return {{}}
      }
    """, Base.mutBaseAliases);}

  @Test void inferListWithDifferentNumsExplicit() { ok("""
    package test
    Test: Main{s -> Block#
      .let myList = {List#[Int](+5, +10, -15)}
      .do {UnrestrictedIO#s.println(myList.get(0) .str)}
      .return {Void}
      }
    """, Base.mutBaseAliases); }
  @Test void inferListWithDifferentNums() { ok("""
    package test
    Test: Main{s -> Block#
      .let[List[Int]] myList = {List#(+5, +10, -15)}
      .do {UnrestrictedIO#s.println(myList.get(0) .str)}
      .return {Void}
      }
    """, Base.mutBaseAliases); }
}
