package program.typesystem;

import net.jqwik.api.Example;
import utils.Base;

import java.util.Arrays;

import static program.typesystem.RunTypeSystem.fail;
import static program.typesystem.RunTypeSystem.ok;

public class TestTypeSystem {
  //  TODO: mut Box[read X] is not valid even after promotion
  // TODO: .m: mut Box[mdf X] must return lent Box[read Person] if mdf X becomes read X (same with lent)
  // TODO: Factory of mutBox and immBox, what types do we get?

  @Example void emptyProgram(){ ok("""
    package test
    """); }

  @Example void simpleProgram(){ ok( """
    package test
    A:{ .m: A -> this }
    """); }

  @Example void simpleTypeError(){ fail("""
    In position [###]/Dummy0.fear:4:2
    [E23 methTypeError]
    Expected the method .fail/0 to return imm test.B[], got imm test.A[].
    """, """
    package test
    A:{ .m: A -> this }
    B:{
      .fail: B -> A.m
    }
    """); }

  @Example void subTypingCall(){ ok( """
    package test
    A:{ .m1(a: A): A -> a }
    B:A{}
    C:{ .m2: A -> A.m1(B) }
    """); }
  @Example void numbersGenericTypes2aWorksThanksTo5b(){ ok("""
    package test
    FortyTwo:{}
    FortyThree:{}
    A[N]:{ .count: N, .sum: N }
    B:A[FortyTwo]{ .count -> FortyTwo, .sum -> FortyThree }
    """); }
  @Example void numbersGenericTypes2aNoMagic(){ fail("""
    In position [###]/Dummy0.fear:6:43
    [E18 uncomposableMethods]
    These methods could not be composed.
    conflicts:
    ([###]/Dummy0.fear:4:13) test.FortyThree[], .get/0
    ([###]/Dummy0.fear:3:11) test.FortyTwo[], .get/0
    """, """
    package test
    Res1:{} Res2:{}
    FortyTwo:{ .get: Res1 -> Res1 }
    FortyThree:{ .get: Res2 -> Res2 }
    A[N]:{ .count: N, .sum: N }
    B:A[FortyTwo]{ .count -> FortyTwo, .sum -> FortyThree }
    """); }

  @Example void noRecMdfWeakening() { fail("""
    In position [###]/Dummy0.fear:4:0
    [E18 uncomposableMethods]
    These methods could not be composed.
    conflicts:
    [###]/Dummy0.fear:3:10) test.List[mut test.Person[]], .get/0
    ([###]/Dummy0.fear:4:26) test.Family2[], .get/0
    """, """
    package test
    Person:{}
    List[X]:{ read .get(): recMdf X }
    Family2:List[mut Person]{ read .get(): mut Person }
    """); }

  @Example void ref1() { fail("""
    In position [###]/Dummy0.fear:10:42
    [E30 badCapture]
    'mut this' cannot be captured by an imm method in an imm lambda.
    """, """
    package base
    NoMutHyg[X]:{}
    Sealed:{} Void:{}
    Let:{ #[V,R](l:Let[mdf V,mdf R]):mdf R -> l.in(l.var) }
    Let[V,R]:{ .var:mdf V, .in(v:mdf V):mdf R }
    Ref:{ #[X](x: mdf X): mut Ref[mdf X] -> this#(x) }
    Ref[X]:NoMutHyg[X],Sealed{
      read * : recMdf X,
      mut .swap(x: mdf X): mdf X,
      mut :=(x: mdf X): Void -> Let#{ .var -> this.swap(x), .in(_)->Void },
      mut <-(f: mut UpdateRef[mut X]): mdf X -> this.swap(f#(this*)),
    }
    UpdateRef[X]:{ mut #(x: mdf X): mdf X }
    """); }

  @Example void simpleThis() { ok("""
    package test
    A:{
      .a: C -> B{ this.c }.c,
      .c: C -> {}
      }
    B:{ .c: C }
    C:{ }
    """); }

  @Example void lambdaCapturesThis() { ok("""
    package test
    Let:{ #[V,R](l: mut Let[V, R]): R -> l.in(l.var) }
    Let[V,R]:{ mut .var: V, mut .in(v: V): R }
    Void:{}
    Ref[X]:{
        mut .swap(x: X): X,
        mut :=(x: X): Void -> Let#mut Let[X,Void]{ .var -> this.swap(x), .in(_) -> Void },
      }
    """); }

  @Example void callMutFromLent() { ok("""
    package test
    A:{
      .b: lent B -> {},
      .doThing: Void -> this.b.foo.ret
      }
    B:{
      mut .foo(): mut B -> this,
      mut .ret(): Void -> {},
      }
    Void:{}
    """); }
  @Example void callMutFromIso() { ok("""
    package test
    A:{
      .b: lent B -> {},
      .doThing: Void -> this.b.foo.ret
      }
    B:{
      mut .foo(): mut B -> this,
      mut .ret(): Void -> {},
      }
    Void:{}
    """); }
  @Example void noCallMutFromImm() { fail("""
    In position [###]/Dummy0.fear:4:26
    [E33 callTypeError]
    Type error: None of the following candidates for this method call:
    this .b/0[]([]) .foo/0[]([])
    were valid:
    (imm test.B[]) <: TsT[ts=[mut test.B[]], t=mut test.B[]]
    (imm test.B[]) <: TsT[ts=[iso test.B[]], t=iso test.B[]]
    (imm test.B[]) <: TsT[ts=[iso test.B[]], t=iso test.B[]]
    (imm test.B[]) <: TsT[ts=[iso test.B[]], t=mut test.B[]]
    """, """
    package test
    A:{
      .b: imm B -> {},
      .doThing: Void -> this.b.foo.ret
      }
    B:{
      mut .foo(): mut B -> this,
      mut .ret(): Void -> {},
      }
    Void:{}
    """); }
  @Example void noCallMutFromRead() { fail("""
    In position [###]/Dummy0.fear:4:26
    [E33 callTypeError]
    Type error: None of the following candidates for this method call:
    this .b/0[]([]) .foo/0[]([])
    were valid:
    (read test.B[]) <: TsT[ts=[mut test.B[]], t=mut test.B[]]
    (read test.B[]) <: TsT[ts=[iso test.B[]], t=iso test.B[]]
    (read test.B[]) <: TsT[ts=[iso test.B[]], t=iso test.B[]]
    (read test.B[]) <: TsT[ts=[iso test.B[]], t=mut test.B[]]
    """, """
    package test
    A:{
      .b: read B -> {},
      .doThing: Void -> this.b.foo.ret
      }
    B:{
      mut .foo(): mut B -> this,
      mut .ret(): Void -> {},
      }
    Void:{}
    """); }
  @Example void noCallMutFromRecMdfImm() { fail("""
    In position [###]/Dummy0.fear:4:26
    [E33 callTypeError]
    Type error: None of the following candidates for this method call:
    this .b/0[]([]) .foo/0[]([])
    were valid:
    (imm test.B[]) <: TsT[ts=[mut test.B[]], t=mut test.B[]]
    (imm test.B[]) <: TsT[ts=[iso test.B[]], t=iso test.B[]]
    (imm test.B[]) <: TsT[ts=[iso test.B[]], t=iso test.B[]]
    (imm test.B[]) <: TsT[ts=[iso test.B[]], t=mut test.B[]]
    """, """
    package test
    A:{
      read .b: recMdf B -> {},
      .doThing: Void -> this.b.foo.ret
      }
    B:{
      mut .foo(): mut B -> this,
      mut .ret(): Void -> {},
      }
    Void:{}
    """); }
  @Example void noCallMutFromRecMdfRead() { fail("""
    In position [###]/Dummy0.fear:4:31
    [E33 callTypeError]
    Type error: None of the following candidates for this method call:
    this .b/0[]([]) .foo/0[]([])
    were valid:
    (recMdf test.B[]) <: TsT[ts=[mut test.B[]], t=mut test.B[]]
    (recMdf test.B[]) <: TsT[ts=[iso test.B[]], t=iso test.B[]]
    (recMdf test.B[]) <: TsT[ts=[iso test.B[]], t=iso test.B[]]
    (recMdf test.B[]) <: TsT[ts=[iso test.B[]], t=mut test.B[]]
    """, """
    package test
    A:{
      read .b: recMdf B -> {},
      read .doThing: Void -> this.b.foo.ret
      }
    B:{
      mut .foo(): mut B -> this,
      mut .ret(): Void -> {},
      }
    Void:{}
    """); }
  @Example void CallMutFromRecMdfLent() { ok("""
    package test
    A:{
      lent .b: recMdf B -> {},
      lent .doThing: Void -> this.b.foo.ret
      }
    B:{
      mut .foo(): mut B -> this,
      mut .ret(): Void -> {},
      }
    Void:{}
    """); }
  @Example void CallMutFromRecMdfMut() { ok("""
    package test
    A:{
      lent .b: recMdf B -> {},
      mut .doThing: Void -> this.b.foo.ret
      }
    B:{
      mut .foo(): mut B -> this,
      mut .ret(): Void -> {},
      }
    Void:{}
    """); }
  @Example void recMdfToMut() { ok("""
    package test
    A:{
      read .b(a: recMdf A): recMdf B -> {},
      mut .break: mut B -> this.b(this),
      }
    B:{}
    """); }
  @Example void captureRecMdfAsMut() { ok("""
    package test
    A:{
      read .b(a: recMdf A): recMdf B -> {},
      mut .break: read B -> LetMut#[mut B, read B]{ .var -> this.b(this), .in(b) -> b.foo },
      }
    B:{
      read .foo(): read B
      }
    Void:{}
    LetMut:{ #[V,R](l:mut LetMut[mdf V, mdf R]): mdf R -> l.in(l.var) }
    LetMut[V,R]:base.NoMutHyg[V]{ mut .var: mdf V, mut .in(v: mdf V): mdf R }
    """, """
    package base
    NoMutHyg[X]:{}
    """); }
  // TODO: the recMdf here needs to become mut in inference or something
  @Example void inferCaptureRecMdfAsMut() { ok("""
    package test
    A:{
      read .b(a: recMdf A): recMdf B -> {'b .foo -> b },
      mut .break: read B -> LetMut#{ .var -> this.b(this), .in(b) -> b.foo },
      }
    B:{
      read .foo(): read B
      }
    Void:{}
    LetMut:{ #[V,R](l:mut LetMut[mdf V, mdf R]): mdf R -> l.in(l.var) }
    LetMut[V,R]:{ mut .var: mdf V, mut .in(v: mdf V): mdf R }
    """); }

  @Example void recMdfInSubHyg() { ok("""
    package test
    A[X]:{ .foo(x: mut X): mut X -> mut B[mut X]{ x }.argh }
    B[X]:{ read .argh: recMdf X }
    C:{ #: mut C -> A[C].foo({}) }
    """); }

  @Example void breakingEarlyFancyRename() { fail("""
    In position [###]/Dummy0.fear:3:2
    [E23 methTypeError]
    Expected the method .foo/2 to return recMdf test.A[], got read test.A[].
    """, """
    package test
    A:{
      read .foo(a:recMdf A, b:read A):recMdf A -> b
      }
    B:{
      .foo(mutR: mut A, readR: read A): mut A -> mutR.foo(mutR, readR)
      }
    """); }

  @Example void recMdfCallsRecMdf() { ok("""
    package test
    A:{
      read .inner: recMdf  A -> this,
      read .outer: recMdf A -> this.inner,
      }
    """); }
  @Example void recMdfCallsRecMdfa() { ok("""
    package test
    A:{
      read .inner: recMdf A -> this
      }
    """); }
  @Example void noCaptureReadInMut() { fail("""
    In position [###]/Dummy0.fear:4:26
    [E30 badCapture]
    'recMdf this' cannot be captured by a mut method in a mut lambda.
    """, """
    package test
    A:{ mut .prison: read B }
    B:{
      read .break: mut A -> { this }
      }
    """); }
  @Example void noCaptureMdfInMut() { fail("""
    In position [###]/Dummy0.fear:4:29
    [E30 badCapture]
    'recMdf this' cannot be captured by a mut method in a mut lambda.
    """, """
    package test
    A[X]:{ mut .prison: mdf X }
    B:{
      read .break: mut A[B] -> { this }
      }
    """); }
  @Example void noCaptureMdfInMut2() { fail("""
    In position [###]/Dummy0.fear:4:34
    [E30 badCapture]
    'recMdf this' cannot be captured by a mut method in a mut lambda.
    """, """
    package test
    A[X]:{ mut .prison: mdf X }
    B:{
      read .break: mut A[read B] -> { this } // this capture was being allowed because this:mdf B was adapted with read to become this:recMdf B (which can be captured by mut)
      }
    """); }

  @Example void noCaptureMdfInMut3() { fail("""
    In position [###]/Dummy0.fear:4:38
    [E30 badCapture]
    'mdf x' cannot be captured by a mut method in a mut lambda.
    """, """
    package test
    A[X]:{ mut .prison: mdf X }
    B[X]:{
      .break(x: mdf X): mut A[mdf X] -> { x }
      }
    """); }

  @Example void recMdfFluent() { ok("""
    package test
    Let:{
      read #[V,R](l: recMdf Let[mdf V, mdf R]): recMdf Let[mdf V, mdf R],
      read .run[V,R](l: recMdf LetMut[mdf V, mdf R]): mdf R -> l.in(l.var)
      }
    Let[V,R]:{ recMdf .var: mdf V, recMdf .in(v: mdf V): mdf R }
    """); }
  // TODO: write a test that shows that the error message for this code makes sense:
  /*
      // (Void is the wrong R and this returns Opt[Opt[T]] instead of Opt[T] or the written Void.
        OptDo[T]:OptMatch[T,Void]{
        #(t:T):Void,   //#[R](t:T):R,
        .some(x) -> Opt#this._doRes(this#x, x),
        .none->{},
        ._doRes(y:Void,x:T):T -> Opt#x
        }
   */

  // These are okay because recMdf X where MDF X = imm X becomes imm X.
  // this method always returns imm X in this case.
  @Example void noCaptureImmAsRecMdf() { ok("""
    package test
    B:{}
    L[X]:{ read .absMeth: recMdf X }
    A:{ read .m(par: imm B) : lent L[imm B] -> lent L[imm B]{.absMeth->par} }
    """); }
  @Example void noCaptureImmAsRecMdfExample() { ok("""
    package test
    B:{}
    L[X]:{ read .absMeth: recMdf X }
    A:{ read .m(par: imm B) : lent L[imm B] -> lent L[imm B]{.absMeth->par} }
    C:{ #: imm B -> (A.m(B)).absMeth }
    """); }
  @Example void noCaptureImmAsRecMdfCounterEx() { fail("""
    In position [###]/Dummy0.fear:5:25
    [E32 noCandidateMeths]
    When attempting to type check the method call: [-imm-][test.A[]]{'fear1$ } .m/1[]([[-imm-][test.B[]]{'fear2$ }]) .absMeth/0[]([]), no candidates for .absMeth/0 returned the expected type lent test.B[]. The candidates were:
    TsT[ts=[read test.L[imm test.B[]]], t=imm test.B[]]
    TsT[ts=[read test.L[imm test.B[]]], t=imm test.B[]]
    TsT[ts=[imm test.L[imm test.B[]]], t=imm test.B[]]
    """, """
    package test
    B:{}
    L[X]:{ read .absMeth: recMdf X }
    A:{ read .m(par: imm B) : lent L[imm B] -> lent L[imm B]{.absMeth->par} }
    C:{ #: lent B -> (A.m(B)).absMeth }
    """); }
  @Example void noCaptureImmAsRecMdfTopLvl1() { ok("""
    package test
    B:{}
    L[X]:{ read .absMeth: recMdf X }
    L'[X]:L[imm X]{ read .absMeth: imm X }
    A:{ read .m(par: imm B) : lent L[imm B] -> lent L'[imm B]{.absMeth->par} }
    """); }
  @Example void noCaptureImmAsRecMdfTopLvl2() { fail("""
    In position [###]/Dummy0.fear:4:0
    [E18 uncomposableMethods]
    These methods could not be composed.
    conflicts:
    ([###]/Dummy0.fear:3:7) test.L[mdf X], .absMeth/0
    ([###]/Dummy0.fear:4:16) test.L'[mdf X], .absMeth/0
    """, """
    package test
    B:{}
    L[X]:{ read .absMeth: recMdf X }
    L'[X]:L[mdf X]{ read .absMeth: imm X }
    A:{ read .m(par: imm B) : lent L[imm B] -> lent L'[imm B]{.absMeth->par} }
    """); }

  @Example void recMdfInheritance() { ok("""
    package test
    Foo:{}
    A[X]:{ read .m: recMdf X -> this.m }
    B:A[imm Foo]
    C:B
    CanPass0:{ read .m(par: mut A[imm Foo]) : imm Foo -> par.m  }
    CanPass1:{ read .m(par: mut B) : imm Foo -> par.m  }
    CanPass2:{ read .m(par: mut C) : imm Foo -> par.m  }
    //NoCanPass:{ read .m(par: mut B) : mut Foo -> par.m  }
    """); }

  @Example void recMdfInheritanceFail() { fail("""
    In position [###]/Dummy0.fear:7:48
    [E32 noCandidateMeths]
    When attempting to type check the method call: par .m/0[]([]), no candidates for .m/0 returned the expected type mut test.Foo[]. The candidates were:
    TsT[ts=[read test.B[]], t=imm test.Foo[]]
    TsT[ts=[read test.B[]], t=imm test.Foo[]]
    TsT[ts=[imm test.B[]], t=imm test.Foo[]]
    """, """
    package test
    Foo:{}
    A[X]:{ read .m: recMdf X -> this.m }
    B:A[imm Foo]{}
    CanPass0:{ read .m(par: mut A[imm Foo]) : imm Foo -> par.m  }
    CanPass1:{ read .m(par: mut B) : imm Foo -> par.m  }
    NoCanPass:{ read .m(par: mut B) : mut Foo -> par.m  }
    """); }

  @Example void immToReadCapture() { ok("""
    package test
    B:{}
    L[X]:{ imm .absMeth: read X }
    A:{ read .m[T](par: imm T) : read L[imm T] -> read L[imm T]{.absMeth->par} }
    """); }

  @Example void immCapture() { ok("""
    package test
    B:{}
    L[X]:{ imm .absMeth: imm X }
    A:{ read .m[T](par: mut T) : mut L[mut T] -> mut L[mut T]{.absMeth->par} }
    """); }

  @Example void readMethOnImmLambdaCannotCaptureRead() { fail("""
    In position [###]/Dummy0.fear:4:69
    [E30 badCapture]
    'read par' cannot be captured by a read method in an imm lambda.
    """, """
    package test
    B:{}
    L[X]:{ read .absMeth: read X }
    A:{ read .m[T](par: read T) : imm L[imm T] -> imm L[imm T]{.absMeth->par} }
    """);}

  @Example void immReturnsReadAsLent() { fail("""
    In position [###]/Dummy0.fear:4:61
    [E23 methTypeError]
    Expected the method .absMeth/0 to return lent T, got imm T.
    """, """
    package test
    B:{}
    L[X]:{ imm .absMeth: lent X }
    A:{ read .m[T](par: read T) : lent L[imm T] -> lent L[imm T]{.absMeth->par} }
    """); }

  @Example void mdfParamAsLent() { ok("""
    package test
    B:{}
    L[X]:{ mut .absMeth: lent X }
    A:{ read .m[T](par: mdf T) : lent L[mut T] -> lent L[mut T]{.absMeth->par} }
    C:{ #: lent L[mut B] -> A{}.m[read B](B) }
    """); }

  @Example void noMutHygRenamedGX() { ok("""
    package test
    alias base.NoMutHyg as NoMH,
    Person:{}
    
    Foo[X]:NoMH[mdf X]{ read .stuff: recMdf X }
    FooP0[Y]:Foo[mdf Y]{}
    FooP1:{ #(p: read Person): mut Foo[read Person] -> { p } }
    FooP2:{ #(p: read Person): mut FooP0[read Person] -> { p } }
    
    Test:{
      .t1(t: read Person): mut Foo[read Person] -> FooP1#t,
      .t2(t: read Person): mut FooP0[read Person] -> FooP2#t,
      .t2a(t: read Person): mut Foo[read Person] -> FooP2#t,
      }
    
    //Foo[X]:NoMH[X]{stuff[X]}
    //FooP0[Y]:Foo[Y]
    //FooP1:Foo[Person]
    //FooP2:{stuff[Person]}
    //m(x)->FooP1{ x }
    //m(x)->FooP2{ x }
    """,  """
    package base
    NoMutHyg[X]:{}
    """); }

  @Example void numbersNoBase(){ ok( """
    package test
    A:{ .m(a: 42): 42 -> 42 }
    """, """
    package base
    Sealed:{} Stringable:{ .str: Str } Str:{} Bool:{}
    """, Base.load("nums.fear")); }

  @Example void incompatibleITsDeep() { fail("""
    In position [###]/Dummy0.fear:5:16
    [E18 uncomposableMethods]
    These methods could not be composed.
    conflicts:
    ([###]/Dummy1.fear:23:2) base.caps.IO'[], #/1
    ([###]/Dummy1.fear:15:2) base.caps.CapFactory[lent base.caps.NotTheRootCap[], lent base.caps.IO[]], #/1
    """, """
    package test
    alias base.Main as Main, alias base.Void as Void,
    alias base.caps.IO as IO, alias base.caps.IO' as IO',
    Test:Main[Void]{ #(_, s) -> s
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
    """, Base.load("lang.fear"), Base.load("bools.fear"), Base.load("nums.fear"), Base.load("strings.fear"), Base.load("optionals.fear"), Base.load("lists.fear")); }
  @Example void incompatibleGens() { fail("""
    In position [###]/Dummy1.fear:7:12
    [E34 bothTExpectedGens]
    Type error: the generic type lent C cannot be a super-type of any concrete type, like Fear[###]/0.
    """, """
    package test
    alias base.Main as Main, alias base.Void as Void,
    alias base.caps.IO as IO, alias base.caps.IO' as IO',
    Test:Main[Void]{ #(_, s) -> s
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
    """, Base.load("lang.fear"), Base.load("bools.fear"), Base.load("nums.fear"), Base.load("strings.fear"), Base.load("optionals.fear"), Base.load("lists.fear")); }
  @Example void incompatibleITs() { fail("""
    In position [###]/Dummy1.fear:7:8
    [E33 callTypeError]
    Type error: None of the following candidates for this method call:
    cont #/2[]([c #/1[]([[-lent-][base.caps._RootCap[], base.caps.NotTheRootCap[]]{'fear[###]$ }]), this])
    were valid:
    (mut base.caps.UseCapCont[imm C, mdf R], ?c #/1[]([[-lent-][base.caps._RootCap[], base.caps.NotTheRootCap[]]{'fear[###]$ }])?, lent base.caps.System[mdf R]) <: TsT[ts=[mut base.caps.UseCapCont[imm C, mdf R], lent C, lent base.caps.System[mdf R]], t=mdf R]
    (mut base.caps.UseCapCont[imm C, mdf R], ?c #/1[]([[-lent-][base.caps._RootCap[], base.caps.NotTheRootCap[]]{'fear[###]$ }])?, lent base.caps.System[mdf R]) <: TsT[ts=[iso base.caps.UseCapCont[imm C, mdf R], lent C, lent base.caps.System[mdf R]], t=mdf R]
    (mut base.caps.UseCapCont[imm C, mdf R], ?c #/1[]([[-lent-][base.caps._RootCap[], base.caps.NotTheRootCap[]]{'fear[###]$ }])?, lent base.caps.System[mdf R]) <: TsT[ts=[iso base.caps.UseCapCont[imm C, mdf R], iso C, iso base.caps.System[mdf R]], t=mdf R]
    (mut base.caps.UseCapCont[imm C, mdf R], ?c #/1[]([[-lent-][base.caps._RootCap[], base.caps.NotTheRootCap[]]{'fear[###]$ }])?, lent base.caps.System[mdf R]) <: TsT[ts=[iso base.caps.UseCapCont[imm C, mdf R], mut C, lent base.caps.System[mdf R]], t=mdf R]
    (mut base.caps.UseCapCont[imm C, mdf R], ?c #/1[]([[-lent-][base.caps._RootCap[], base.caps.NotTheRootCap[]]{'fear[###]$ }])?, lent base.caps.System[mdf R]) <: TsT[ts=[iso base.caps.UseCapCont[imm C, mdf R], lent C, mut base.caps.System[mdf R]], t=mdf R]
    """, """
    package test
    alias base.Main as Main, alias base.Void as Void,
    alias base.caps.IO as IO, alias base.caps.IO' as IO',
    Test:Main[Void]{ #(_, s) -> s
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
    """, Base.load("lang.fear"), Base.load("bools.fear"), Base.load("nums.fear"), Base.load("strings.fear"), Base.load("optionals.fear"), Base.load("lists.fear")); }
  @Example void recMdfCannotBeSubtypeOfMdf() { fail("""
    In position [###]/Dummy0.fear:2:7
    [E23 methTypeError]
    Expected the method #/1 to return mdf A, got recMdf A.
    """, """
    package test
    F[A]:{ read #(a:recMdf A):mdf A->a }
    M:{ mut .mutMe: mut M -> this.mutMe } // if this method can be called from M it is broken
    Break:{
      .myF: imm F[mut M] -> {},
      .b1(m: imm M): mut M -> this.myF#m,
      .b2(m: imm M): mut M -> (this.myF#m).mutMe,
      }
    """); }
}
