package program.typesystem;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static program.typesystem.RunTypeSystem.fail;
import static program.typesystem.RunTypeSystem.ok;

public class TestTypeSystemOldRecMdf {
  @Test void recMdfCannotBeSubtypeOfMdf1() { fail("""
    In position [###]/Dummy0.fear:2:7
    [E23 methTypeError]
    Expected the method #/1 to return A, got recA.
    Try writing the signature for #/1 explicitly if it needs to return a rectype.
    """, """
    package test
    F[A]:{ rec#(a:recA):A->a }
    M:{ mut .mutMe: mut M -> this.mutMe } // if this method can be called from M it is broken
    Break:{
      .myF: imm F[mut M] -> {},
      .b1(m: imm M): mut M -> this.myF#m,
      .b2(m: imm M): mut M -> (this.myF#m).mutMe,
      }
    """); }
  @Test void recMdfCannotBeSubtypeOfMdf2() { fail("""
    In position [###]/Dummy0.fear:2:7
    [E30 badCapture]
    'mut test.M[]' cannot be captured by an imm method in an imm lambda.
    """, """
    package test
    F[A]:{ rec#(a:recA):recA->a }
    M:{ mut .mutMe: mut M -> this.mutMe } // if this method can be called from M it is broken
    Break:{
      .myF: imm F[mut M] -> {},
      .b1(m: imm M): mut M -> this.myF#m,
      .b2(m: imm M): mut M -> (this.myF#m).mutMe,
      }
    """); }

  @Test void unsoundHygRecMdf() { fail("""
    In position [###]/Dummy0.fear:5:8
    [E30 badCapture]
    'readOnly x: X1/0$' cannot be captured by a mut method in a mut lambda.
    """, """
    package test
    Foo:{}
    Box:{ recMdf #[T](x: recMdf T): recMdf Box[recMdf T] -> {x} }
    Box[T]:{ recMdf .get: recMdf T }
    Break:{ #(foo: readOnly Foo): mut Box[readOnly Foo] -> mut Box#foo }
    """); }
  @Test void soundHygRecMdf() { ok("""
    package test
    Foo:{}
    Box:{ recMdf #[T](x: recMdf T): recMdf Box[recMdf T] -> {x} }
    Box[T]:{ recMdf .get: recMdf T }
    Break:{ #(foo: readOnly Foo): readOnly Box[readOnly Foo] -> readOnly Box#foo }
    """); }
  @Test void unsoundHygRecMdfCapture() { fail("""
    In position [###]/Dummy0.fear:3:10
    [E30 badCapture]
    'readOnly test.Foo[]' cannot be captured by a mut method in a mut lambda.
    """, """
    package test
    Foo:{}
    FBox[T]:{ recMdf #(x: recMdf T): recMdf Box[recMdf T] -> {x} }
    Box[T]:{ recMdf .get: recMdf T }
    Break:{ #(foo: readOnly Foo): mut Box[readOnly Foo] -> mut FBox[readOnly Foo]#foo }
    """); }
  @Test void soundHygRecMdfCapture() { ok("""
    package test
    Foo:{}
    FBox[T]:{ recMdf #(x: recMdf T): recMdf Box[recMdf T] -> {x} }
    Box[T]:{ recMdf .get: recMdf T }
    Break:{ #(foo: readOnly Foo): readOnly Box[readOnly Foo] -> readOnly FBox[readOnly Foo]#foo }
    """); }
  @Test void unsoundHygRecMdfIndirect() { fail("""
    In position [###]/Dummy0.fear:8:14
    [E30 badCapture]
    'x: X1/0$' cannot be captured by a mut method in a mut lambda.
    """, """
    package test
    Foo:{}
    Box:{ recMdf #[T](x: recMdf T): recMdf Box[recMdf T] -> {x} }
    Box[T]:{
      recMdf .get: recMdf T,
      recMdf .clone(c: mut BoxClone[recMdf T]): mut Box[recMdf T] -> c.task(this.get),
      }
    BoxClone[T]:{ mut .task(x: T): mut Box[T] -> mut Box#x }
    Break:{ #(foo: readOnly Foo): mut Box[readOnly Foo] -> (readOnly Box#foo).clone(mut BoxClone[readOnly Foo]) }
    """); }
  @Test void soundHygRecMdfIndirect() { ok("""
    package test
    Foo:{}
    Box:{ recMdf #[T](x: recMdf T): recMdf Box[recMdf T] -> {x} }
    Box[T]:{
      recMdf .get: recMdf T,
      recMdf .clone(c: mut BoxClone[recMdf T]): mut Box[recMdf T] -> c.task(this.get),
      }
    BoxClone[T:imm,mut]:{ mut .task(x: T): mut Box[T] -> mut Box#x }
//    Break:{ #(foo: readOnly Foo): mut Box[readOnly Foo] -> (readOnly Box#foo).clone(mut BoxClone[mut Foo]) }
    """); }

  @Test void readToReadOnlyPromotion3Fail() { fail("""
    In position [###]/Dummy0.fear:2:48
    [E33 callTypeError]
    Type error: None of the following candidates (returning the expected type "read test.Foo[]") for this method call:
    r .rb/0[]([]) .get/0[]([])
    were valid:
    (read test.Box[imm test.Foo[]]) <= (mut test.Box[imm test.Foo[]]): imm test.Foo[]
      The following errors were found when checking this sub-typing:
        In position [###]/Dummy0.fear:2:45
        [E32 noCandidateMeths]
        When attempting to type check the method call: r .rb/0[]([]), no candidates for .rb/0 returned the expected type mut test.Box[imm test.Foo[]]. The candidates were:
        (read test.MutyBox[]): read test.Box[imm test.Foo[]]
        (imm test.MutyBox[]): imm test.Box[imm test.Foo[]]
        (readOnly test.MutyBox[]): readOnly test.Box[imm test.Foo[]]
        
    (read test.Box[imm test.Foo[]]) <= (iso test.Box[imm test.Foo[]]): imm test.Foo[]
      The following errors were found when checking this sub-typing:
        In position [###]/Dummy0.fear:2:45
        [E32 noCandidateMeths]
        When attempting to type check the method call: r .rb/0[]([]), no candidates for .rb/0 returned the expected type iso test.Box[imm test.Foo[]]. The candidates were:
        (read test.MutyBox[]): read test.Box[imm test.Foo[]]
        (imm test.MutyBox[]): imm test.Box[imm test.Foo[]]
        (readOnly test.MutyBox[]): readOnly test.Box[imm test.Foo[]]
        
    (read test.Box[imm test.Foo[]]) <= (lent test.Box[imm test.Foo[]]): imm test.Foo[]
      The following errors were found when checking this sub-typing:
        In position [###]/Dummy0.fear:2:45
        [E32 noCandidateMeths]
        When attempting to type check the method call: r .rb/0[]([]), no candidates for .rb/0 returned the expected type lent test.Box[imm test.Foo[]]. The candidates were:
        (read test.MutyBox[]): read test.Box[imm test.Foo[]]
        (imm test.MutyBox[]): imm test.Box[imm test.Foo[]]
        (readOnly test.MutyBox[]): readOnly test.Box[imm test.Foo[]]
        
    (read test.Box[imm test.Foo[]]) <= (read test.Box[imm test.Foo[]]): read test.Foo[]
      The following errors were found when checking this sub-typing:
        In position [###]/Dummy0.fear:2:45
        [E33 callTypeError]
        Type error: None of the following candidates (returning the expected type "read test.Box[imm test.Foo[]]") for this method call:
        r .rb/0[]([])
        were valid:
        (readOnly test.MutyBox[]) <= (read test.MutyBox[]): read test.Box[imm test.Foo[]]
          The following errors were found when checking this sub-typing:
            In position [###]/Dummy0.fear:2:44
            [E53 xTypeError]
            Expected r to be read test.MutyBox[], got readOnly test.MutyBox[].
       \s
        (readOnly test.MutyBox[]) <= (imm test.MutyBox[]): imm test.Box[imm test.Foo[]]
          The following errors were found when checking this sub-typing:
            In position [###]/Dummy0.fear:2:44
            [E53 xTypeError]
            Expected r to be imm test.MutyBox[], got readOnly test.MutyBox[].
        
    (read test.Box[imm test.Foo[]]) <= (imm test.Box[imm test.Foo[]]): imm test.Foo[]
      The following errors were found when checking this sub-typing:
        In position [###]/Dummy0.fear:2:45
        [E33 callTypeError]
        Type error: None of the following candidates (returning the expected type "imm test.Box[imm test.Foo[]]") for this method call:
        r .rb/0[]([])
        were valid:
        (readOnly test.MutyBox[]) <= (imm test.MutyBox[]): imm test.Box[imm test.Foo[]]
          The following errors were found when checking this sub-typing:
            In position [###]/Dummy0.fear:2:44
            [E53 xTypeError]
            Expected r to be imm test.MutyBox[], got readOnly test.MutyBox[].
    """, """
    package test
    Test1:{ #(r: readOnly MutyBox): read Foo -> r.rb.get }
    MutyBox:{ mut .mb: mut Box[Foo], read .rb: read Box[Foo] }
    """, """
    package test
    Foo:{}
    Box[X]:{
      mut  .get: X,
      read .get: read X,
      }
    """); }

  // These are okay because recX where X = imm X becomes imm X.
  // this method always returns imm X in this case.
  @Test void noCaptureImmAsRecMdf() { ok("""
    package test
    B:{}
    L[X]:{ recMdf .absMeth: recMdf X }
    A:{ readOnly .m(par: imm B) : lent L[imm B] -> lent L[imm B]{.absMeth->par} }
    """); }
  @Test void noCaptureImmAsRecMdfExample() { ok("""
    package test
    B:{}
    L[X]:{ recMdf .absMeth: recMdf X }
    A:{ readOnly .m(par: imm B) : lent L[imm B] -> lent L[imm B]{.absMeth->par} }
    C:{ #: imm B -> (A.m(B)).absMeth }
    """); }

  @Test void recMdfInheritance() { ok("""
    package test
    Foo:{}
    A[X]:{ recMdf .m: recX -> this.m }
    B:A[imm Foo]
    C:B
    CanPass0:{ readOnly .m(par: mut A[imm Foo]) : imm Foo -> par.m  }
    CanPass1:{ readOnly .m(par: mut B) : imm Foo -> par.m  }
    CanPass2:{ readOnly .m(par: mut C) : imm Foo -> par.m  }
    //NoCanPass:{ readOnly .m(par: mut B) : mut Foo -> par.m  }
    """); }

  @Test void recMdfInheritanceFail() { fail("""
    In position [###]/Dummy0.fear:7:48
    [E32 noCandidateMeths]
    When attempting to type check the method call: par .m/0[]([]), no candidates for .m/0 returned the expected type mut test.Foo[]. The candidates were:
    (mut test.B[]): imm test.Foo[]
    (iso test.B[]): imm test.Foo[]
    (lent test.B[]): imm test.Foo[]
    """, """
    package test
    Foo:{}
    A[X]:{ rec.m: recX -> this.m }
    B:A[imm Foo]{}
    CanPass0:{ readOnly .m(par: mut A[imm Foo]) : imm Foo -> par.m  }
    CanPass1:{ readOnly .m(par: mut B) : imm Foo -> par.m  }
    NoCanPass:{ readOnly .m(par: mut B) : mut Foo -> par.m  }
    """); }

  @Test void invalidIsoPromotionWithRecMdf1() { ok("""
    package test
    A:{
      recMdf .m():recMdf A->this,
      //mut .break1:iso A->this,//bad and fails obviusly
      //mut .break2:iso A->this.m(),//bad
      recMdf .noBreak:recMdf A->this.m(),//should pass
      }
    """); }
  @Test void invalidIsoPromotionWithRecMdf2() { fail("""
    In position [###]/Dummy0.fear:4:2
    [E23 methTypeError]
    Expected the method .break1/0 to return mut test.A[], got lent test.A[].
    """, """
    package test
    A:{
      recMdf .m():recMdf A->this,
      mut .break1:iso A->this,//bad and fails obviusly
      //mut .break2:iso A->this.m(),//bad
      recMdf .noBreak:recMdf A->this.m(),//should pass
      }
    """); }
  @Test void invalidIsoPromotionWithRecMdf3() { fail("""
    In position [###]/Dummy0.fear:5:25
    [E33 callTypeError]
    Type error: None of the following candidates (returning the expected type "mut test.A[]") for this method call:
    this .m/0[]([])
    were valid:
    (lent test.A[]) <= (iso test.A[]): iso test.A[]
    """, """
    package test
    A:{
      recMdf .m():recMdf A->this,
      //mut .break1:iso A->this,//bad and fails obviusly
      mut .break2:iso A->this.m(),//bad
      recMdf .noBreak:recMdf A->this.m(),//should pass
      }
    """); }

  // TODO: interesting tests that look into what should stay as recMdf in propagateMdf
  @Disabled
  @Test void recMdfRenameOnITReturnType() { fail("""
    In position [###]/Dummy0.fear:6:2
    [E23 methTypeError]
    Expected the method .first/0 to return recMdf test.Person[], got imm test.Person[].
    """, """
    package test
    Person:{}
    //List[X]:{ recMdf .first: recMdf X }
    Foo:{ recMdf .first: recMdf Person }
    Bar:{ .bar(bob: imm Person): imm Foo -> imm Foo{
      recMdf .first: recMdf Person -> bob,
      }}
    """); }
  @Disabled
  @Test void recMdfRenameOnITReturnType2() { fail("""
    In position [###]/Dummy0.fear:6:2
    [E23 methTypeError]
    Expected the method .first/0 to return recMdf test.Person[], got imm test.Person[].
    """, """
    package test
    Person:{}
    //List[X]:{ recMdf .first: recMdf X }
    Foo:{ recMdf .first: recMdf Person }
    Bar:{ .bar(bob: imm Person): imm Foo -> { bob } }
    """); }
  @Disabled
  @Test void recMdfRenameOnITReturnTypeMGens() { fail("""
    In position [###]/Dummy0.fear:6:2
    [E23 methTypeError]
    Expected the method .first/0 to return recMdf Y, got imm test.Person[].
    """, """
    package test
    Person:{}
    //List[X]:{ recMdf .first: recMdf X }
    Foo[X]:{ recMdf .first: recMdf X }
    Bar:{ .bar[Y](bob: imm Person): imm Foo[Y] -> imm Foo[Y]{
      recMdf .first: recMdf Y -> bob,
      }}
    """); }
  @Disabled
  @Test void recMdfRenameOnGXReturnType() { ok("""
    package test
    Person:{}
    List[X]:{ recMdf .first: recMdf X }
    //Foo:{ recMdf .first: recMdf Person }
    Bar:{ .bar(bob: imm Person): imm List[Person] -> imm List[imm Person]{
      recMdf .first: imm Person -> bob,
      }}
    """); }
  @Disabled
  @Test void recMdfRenameOnGXReturnTypeInfer() { ok("""
    package test
    Person:{}
    List[X]:{ recMdf .first: recMdf X }
    Bar:{ .bar[Y](bob: Y): imm List[Y] -> { bob } }
    """); }

  @Test void okCaptureImmAsRecMdfTopLvl1() { ok("""
    package test
    B:{}
    L[X]:{ rec.absMeth: recMdf X }
    L'[X]:L[imm X]{ recMdf .absMeth: imm X }
    A:{ readOnly .m(par: imm B) : lent L[imm B] -> lent L'[imm B]{.absMeth->par} }
    """); }
  @Test void noCaptureImmAsRecMdfTopLvl2() { fail("""
    In position [###]/Dummy0.fear:4:0
    [E18 uncomposableMethods]
    These methods could not be composed.
    conflicts:
    ([###]/Dummy0.fear:3:7) test.L[X], .absMeth/0[](): recMdf X
    ([###]/Dummy0.fear:4:12) test.L'[X], .absMeth/0[](): imm X
    """, """
    package test
    B:{}
    L[X]:{ recMdf .absMeth: recMdf X }
    L'[X]:L[X]{ recMdf .absMeth: imm X }
    A:{ readOnly .m(par: imm B) : lent L[imm B] -> lent L'[imm B]{.absMeth->par} }
    """); }

  @Test void recMdfCallsRecMdf() { ok("""
    package test
    A:{
      recMdf .inner: recMdf A -> {},
      recMdf .outer: recMdf A -> recMdf A.inner,
      }
    """); }
  @Test void recMdfCallsRecMdfA() { ok("""
    package test
    A:{
      recMdf .asRecMdf: recMdf A -> recMdf A{'inner
        recMdf .inner: recMdf A -> inner
        },
      recMdf .inner: recMdf A,
      }
    B:{ #(a: mut A): mut A -> a.inner }
    """); }

  @Test void noCaptureMdfInMut3() { fail("""
    In position [###]/Dummy0.fear:4:40
    [E30 badCapture]
    'recMdf this' cannot be captured by a mut method in a mut lambda.
    """, """
    package test
    A[X:mut,imm,read,lent,readOnly]:{ mut .prison: X }
    B:{
      recMdf .break: mut A[readOnly B] -> { this } // this capture was being allowed because this:B was adapted with readOnly to become this:recB (which can be captured by mut)
      }
    """); }


  @Test void inferCaptureRecMdfAsMut1() { ok("""
    package test
    A:{
      recMdf .b(a: recMdf A): recMdf B -> {'b .foo -> b },
      mut .break: readOnly B -> LetMut#[mut B, readOnly B]{ .var -> this.b(this), .in(b) -> b.foo },
      }
    B:{
      readOnly .foo(): readOnly B
      }
    Void:{}
    LetMut:{ #[V:mut,imm,read,lent,readOnly,R:mut,imm,read,lent,readOnly](l:mut LetMut[V, R]): R -> l.in(l.var) }
    LetMut[V:mut,imm,read,lent,readOnly,R:mut,imm,read,lent,readOnly]:{ mut .var: V, mut .in(v: V): R }
    """); }
  @Test void inferCaptureRecMdfAsMut2() { ok("""
    package test
    A:{
      recMdf .b(a: mut A): recMdf B -> {'b .foo -> b },
      mut .break: readOnly B -> LetMut#[mut B, readOnly B]{ .var -> this.b(this), .in(b) -> b.foo },
      }
    B:{
      readOnly .foo(): readOnly B
      }
    Void:{}
    LetMut:{ #[V:mut,imm,read,lent,readOnly,R:mut,imm,read,lent,readOnly](l:mut LetMut[V, R]): R -> l.in(l.var) }
    LetMut[V:mut,imm,read,lent,readOnly,R:mut,imm,read,lent,readOnly]:{ mut .var: V, mut .in(v: V): R }
    """); }

  @Test void noCallMutFromRecMdfImm() { fail("""
    In position [###]/Dummy0.fear:4:35
    [E36 undefinedMethod]
    .foo/0 does not exist in readOnly test.B[].
    """, """
    package test
    A:{
      recMdf .b: recMdf B -> {},
      readOnly .doThing: Void -> this.b.foo.ret
      }
    B:{
      mut .foo(): mut B -> this,
      mut .ret(): Void -> {},
      }
    Void:{}
    """); }
  @Test void lentFromRecMdfLent() { ok("""
    package test
    A:{
      recMdf .b: recMdf B -> {},
      lent .doThing: lent B -> this.b
      }
    B:{}
    """); }
  @Test void mutFromrecMdfLent() { fail("""
In position [###]/Dummy0.fear:5:30
[E33 callTypeError]
Type error: None of the following candidates (returning the expected type "mut test.B[]") for this method call:
this .promote/1[]([this .b/0[]([])])
were valid:
(lent test.A[], recMdf test.B[]) <= (readOnly test.A[], mut test.B[]): mut test.B[]
  The following errors were found when checking this sub-typing:
    In position [###]/Dummy0.fear:5:43
    [E33 callTypeError]
    Type error: None of the following candidates (returning the expected type "mut test.B[]") for this method call:
    this .b/0[]([])
    were valid:
    (lent test.A[]) <= (iso test.A[]): iso test.B[]
      The following errors were found when checking this sub-typing:
        In position [###]/Dummy0.fear:5:39
        [E53 xTypeError]
        Expected 'this' to be iso test.A[], got lent test.A[].

(lent test.A[], recMdf test.B[]) <= (readOnly test.A[], iso test.B[]): iso test.B[]
  The following errors were found when checking this sub-typing:
    In position [###]/Dummy0.fear:5:43
    [E33 callTypeError]
    Type error: None of the following candidates (returning the expected type "iso test.B[]") for this method call:
    this .b/0[]([])
    were valid:
    (lent test.A[]) <= (iso test.A[]): iso test.B[]
      The following errors were found when checking this sub-typing:
        In position [###]/Dummy0.fear:5:39
        [E53 xTypeError]
        Expected 'this' to be iso test.A[], got lent test.A[].

(lent test.A[], recMdf test.B[]) <= (imm test.A[], iso test.B[]): iso test.B[]
  The following errors were found when checking this sub-typing:
    In position [###]/Dummy0.fear:5:26
    [E53 xTypeError]
    Expected 'this' to be imm test.A[], got lent test.A[].
    """, """
    package test
    A:{
      recMdf .b: recMdf B -> {},
      readOnly .promote(b: mut B): mut B -> b,
      lent .doThing: mut B -> this.promote(this.b)
      }
    B:{}
    """); }
  @Test void CallMutFromRecMdfMut() { ok("""
    package test
    A:{
      recMdf .b: recMdf B -> {},
      mut .doThing: Void -> this.b.foo.ret
      }
    B:{
      mut .foo(): mut B -> this,
      mut .ret(): Void -> {},
      }
    Void:{}
    """); }
  @Test void recMdfToMut() { ok("""
    package test
    A:{
      recMdf .b(a: recMdf A): recMdf B -> {},
      mut .break: mut B -> this.b(this),
      }
    B:{}
    """); }
  @Test void recMdfThisIsRecMdf() { ok("""
    package test
    A:{
      recMdf .self: recMdf A -> this,
      }
    """); }
  @Test void readThisIsNotRecMdf() { fail("""
    In position [###]/Dummy0.fear:3:2
    [E26 recMdfInNonRecMdf]
    Invalid modifier for recMdf test.A[].
    recMdf may only be used in recMdf methods. The method .self/0 has the readOnly modifier.
    """, """
    package test
    A:{
      readOnly .self: recMdf A -> this,
      }
    """); }

  @Test void nestedRecMdfExplicitMdf() { ok("""
    package test
    A[X:mut,imm,read,lent,readOnly]:{
      recMdf .m1(a: recMdf X, b: imm F[recMdf X]): recMdf X -> b#a,
      }
    F[X:mut,imm,read,lent,readOnly]:{ imm #(x: X): X -> x, }
    B[Y:mut,imm,read,lent,readOnly]:{
      recMdf #(a: mut A[mut B[recMdf Y]]): mut B[recMdf Y] -> a.m1(mut B[recMdf Y], F[mut B[recMdf Y]]),
      }
    C:{
      #(b: mut B[mut C]): mut B[mut C] -> b#({}),
      .i(b: mut B[imm C]): mut B[imm C] -> b#(mut A[mut B[imm C]]),
      }
    """); }

  @Test void captureReadAsRecMdfLent() { fail("""
    In position [###]/Dummy0.fear:7:45
    [E32 noCandidateMeths]
    When attempting to type check the method call: [-imm-][test.A[]]{'fear0$ } .m/1[readOnly test.B[]]([[-read-][test.B[]]{'fear1$ }]) .absMeth/0[]([]), no candidates for .absMeth/0 returned the expected type mut test.B[]. The candidates were:
    (lent test.L[readOnly test.B[]]): readOnly test.B[]
    (iso test.L[readOnly test.B[]]): imm test.B[]
    """, """
    package test
    alias base.NoMutHyg as NoMutHyg,
    B:{}
    L[X]:NoMutHyg[X]{ rec.absMeth: recX }
    A:{ readOnly .m[T](par: readOnly T): lent L[readOnly T] -> lent L[readOnly T]{.absMeth->par} }
    
    Break:{ #(rb: readOnly B): mut B -> (A.m(readOnly B)).absMeth }
    """, """
    package base
    NoMutHyg[X]:{}
    """); }
  @Test void captureReadAsRecMdfRead() { fail("""
    In position [###]/Dummy0.fear:7:45
    [E32 noCandidateMeths]
    When attempting to type check the method call: [-imm-][test.A[]]{'fear0$ } .m/1[readOnly test.B[]]([[-read-][test.B[]]{'fear1$ }]) .absMeth/0[]([]), no candidates for .absMeth/0 returned the expected type mut test.B[]. The candidates were:
    (readOnly test.L[readOnly test.B[]]): readOnly test.B[]
    (imm test.L[readOnly test.B[]]): imm test.B[]
    """, """
    package test
    alias base.NoMutHyg as NoMutHyg,
    B:{}
    L[X]:NoMutHyg[X]{ rec.absMeth: recX }
    A:{ readOnly .m[T](par: readOnly T): readOnly L[readOnly T] -> readOnly L[readOnly T]{.absMeth->par} }
    
    Break:{ #(rb: readOnly B): mut B -> (A.m(readOnly B)).absMeth }
    """, """
    package base
    NoMutHyg[X]:{}
    """); }

  // TODO: Can we use this to break anything? I think not because .get could not be implemented to do anything bad
  // because it can't capture anything muty if I made an imm Family2 or something.
  @Disabled @Test void recMdfWeakening() { ok("""
    package test
    Person:{}
    List[X]:{ recMdf .get(): recMdf X }
    Family2:List[mut Person]{ recMdf .get(): mut Person }
    // Family2:List[mut Person]{ recMdf .get(): recMdf Person } // works
    """); }
}
