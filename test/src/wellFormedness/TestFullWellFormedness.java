package wellFormedness;

import failure.CompileError;
import id.Mdf;
import main.Main;
import net.jqwik.api.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import parser.Parser;
import utils.Err;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestFullWellFormedness {
  void ok(String... content){
    Main.resetAll();
    AtomicInteger pi = new AtomicInteger();
    var ps = Arrays.stream(content)
      .map(code -> new Parser(Path.of("Dummy"+pi.getAndIncrement()+".fear"), code))
      .toList();
    var p = Parser.parseAll(ps);
    var res = new WellFormednessFullShortCircuitVisitor().visitProgram(p);
    var isWellFormed = res.isEmpty();
    assertTrue(isWellFormed, res.map(Object::toString).orElse(""));
  }
  void fail(String expectedErr, String... content){
    Main.resetAll();
    AtomicInteger pi = new AtomicInteger();
    var ps = Arrays.stream(content)
      .map(code -> new Parser(Path.of("Dummy"+pi.getAndIncrement()+".fear"), code))
      .toList();

    try {
      var p = Parser.parseAll(ps);
      var error = new WellFormednessFullShortCircuitVisitor().visitProgram(p);
      if (error.isEmpty()) { Assertions.fail("Did not fail"); }
      Err.strCmp(expectedErr, error.map(Object::toString).orElseThrow());
    } catch (CompileError e) {
      Err.strCmp(expectedErr, e.toString());
    }
  }
  @Test void paramsMethCallOk() { ok("""
    package pkg1
    Opt[T]:{}
    A:{
      #[T](x: A[mdf T]): A -> {},
      .foo(): A -> this#[read A]A
      }
    """); }

  @Test void noExplicitThisBlockId() { fail("""
    In position [###]/Dummy0.fear:2:2
    [E6 explicitThis]
    Local variables may not be named 'this'.
    """, """
    package base
    A:{ .m1: A -> {'this} }
    """); }

  @Test void noExplicitThisMethArg() { fail("""
    In position [###]/Dummy0.fear:2:2
    [E6 explicitThis]
    Local variables may not be named 'this'.
    """, """
    package base
    A:{ .foo(this: A): A }
    """); }

  @Test void disjointArgList() { fail("""
    In position [###]/Dummy0.fear:2:2
    [E7 conflictingMethParams]
    Parameters on methods must have different names. The following parameters were conflicting: a
    """, """
    package base
    A:{ .foo(a: A, a: A): A }
    """); }

  @Test void disjointMethGens() { fail("""
    In position [###]/Dummy0.fear:2:2
    [E7 conflictingMethParams]
    Parameters on methods must have different names. The following parameters were conflicting: T
    """, """
    package base
    A:{ .foo[T,T](a: T, b: T): A }
    """); }

  @Test void disjointDecGens() { fail("""
    In position [###]/Dummy0.fear:2:0
    [E7 conflictingMethParams]
    Parameters on methods must have different names. The following parameters were conflicting: T
    """, """
    package base
    A[T,T]:{ .foo(a: T, b: T): A }
    """); }

  @Test void noShadowingMeths() { fail("""
    In position [###]/Dummy0.fear:2:2
    [E17 conflictingMethNames]
    Methods may not have the same name and number of parameters. The following methods were conflicting: .a/0
    """, """
    package base
    A:{ .a: A, .a: A }
    """); }

  @Test void noMutHygOk() { ok("""
    package base
    NoMutHyg[X]:{}
    Sealed:{} Void:{}
    Let:{ #[V,R](l:Let[V,R]):R -> l.in(l.var) }
    Let[V,R]:{ .var:V, .in(v:V):R }
    Ref:{ #[X](x: mdf X): mut Ref[mdf X] -> this#(x) }
    Ref[X]:NoMutHyg[X],Sealed{
      recMdf * : recMdf X,
      mut .swap(x: mdf X): mdf X,
      mut :=(x: mdf X): Void -> Let#{ .var -> this.swap(x), .in(_)->Void },
      mut <-(f: UpdateRef[mdf X]): mdf X -> this.swap(f#(this*)),
    }
    UpdateRef[X]:{ mut #(x: mdf X): mdf X }
    """); }
  @Test void noMutHygOkSplit() { ok("""
    package base
    NoMutHyg[X]:{}
    Sealed:{} Void:{}
    Let:{ #[V,R](l:Let[V,R]):R -> l.in(l.var) }
    Let[V,R]:{ .var:V, .in(v:V):R }
    Ref:{ #[X](x: mdf X): mut Ref[mdf X] -> this#(x) }
    Ref[X,Y]:NoMutHyg[X],NoMutHyg[Y],Sealed{
      recMdf * : recMdf X,
      mut .swap(x: mdf X): mdf X,
      mut :=(x: mdf X): Void -> Let#{ .var -> this.swap(x), .in(_)->Void },
      mut <-(f: UpdateRef[mdf X]): mdf X -> this.swap(f#(this*)),
    }
    UpdateRef[X]:{ mut #(x: mdf X): mdf X }
    """); }

  @Test void mdfAsMethMdf() { fail("""
    In position [###]/Dummy0.fear:2:2
    [E16 invalidMethMdf]
    mdf is not a valid modifier for a method (on the method .foo/0).
    """, """
    package base
    A:{ mdf .foo: A }
    """); }

  @Test void useUndefinedX() { fail("""
    In position [###]/Dummy0.fear:3:2
    [E28 undefinedName]
    The identifier "X" is undefined or cannot be captured.
    """, """
    package test
    A[X]:{ .foo(x: X): X -> B{ x }.argh }
    B:{ recMdf .argh: recMdf X } // should fail because X is not defined here
    """); }
  @Test void useUndefinedIdent() { fail("""
    In position [###]/Dummy0.fear:2:5
    [E28 undefinedName]
    The identifier "b" is undefined or cannot be captured.
    """, """
    package test
    A[X]:{ .foo(x: X): X -> this.foo(b) }
    """); }

  @Test void recMdfAllowedInRecMdf() { ok("""
    package base
    A[X]:{ recMdf .foo(): recMdf X }
    C[X]:{ recMdf .foo(c: recMdf X): recMdf X -> c }
    """); }
  @Test void recMdfAllowedInSubRecMdf() { ok("""
    package base
    A[X]:{ .foo(x: X): X -> B[X]{ x }.argh }
    B[X]:{ recMdf .argh: recMdf X }
    """); }
  @Test void noRecMdfInNonReadRet() { fail("""
    In position [###]/Dummy0.fear:2:5
    [E26 recMdfInNonRecMdf]
    Invalid modifier for recMdf X.
    recMdf may only be used in recMdf methods. The method .foo/0 has the imm modifier.
    """, """
    package base
    A[X]:{ .foo(): recMdf X }
    """); }
  @Test void noRecMdfInNonReadRetNested() { fail("""
    In position [###]/Dummy0.fear:2:5
    [E26 recMdfInNonRecMdf]
    Invalid modifier for recMdf X.
    recMdf may only be used in recMdf methods. The method .foo/0 has the imm modifier.
    """, """
    package base
    A[X]:{ .foo(): A[recMdf X] }
    """); }
  @Test void noRecMdfInNonRecMdfArgs() { fail("""
    In position [###]/Dummy0.fear:3:5
    [E26 recMdfInNonRecMdf]
    Invalid modifier for recMdf base.Foo[].
    recMdf may only be used in recMdf methods. The method .foo/1 has the imm modifier.
    """, """
    package base
    Foo:{}
    A[X]:{ .foo(f: recMdf Foo): Foo -> f }
    """); }
  @Test void noRecMdfInNonReadArgsNested() { fail("""
    In position [###]/Dummy0.fear:3:5
    [E26 recMdfInNonRecMdf]
    Invalid modifier for recMdf X.
    recMdf may only be used in recMdf methods. The method .foo/1 has the imm modifier.
    """, """
    package base
    Foo:{}
    A[X]:{ .foo(f: A[recMdf X]): Foo -> f }
    """); }
  @Test void complexValidRecMdf() { ok("""
    package test
    alias base.NoMutHyg as NoMutHyg,
    Opt:{ #[T](x: mdf T): mut Opt[mdf T] -> { .match(m) -> m.some(x) } }
    Opt[T]:NoMutHyg[mdf T]{
      recMdf .match[R](m: mut OptMatch[recMdf T, mdf R]): mdf R -> m.none,
      recMdf .map[R](f: mut OptMap[recMdf T, mdf R]): mut Opt[mdf R] -> this.match{ .some(x) -> Opt#(f#x), .none -> {} },
      recMdf .flatMap[R](f: mut OptMap[recMdf T, recMdf Opt[mdf R]]): mut Opt[mdf R] -> this.match{
        .some(x) -> f#x,
        .none -> {}
        },
      }
    OptMatch[T,R]:NoMutHyg[mdf R]{ mut .some(x: mdf T): mdf R, mut .none: mdf R }
    OptMap[T,R]:{ mut #(x: mdf T): mdf R }
    """, """
    package base
    NoMutHyg[X]:{}
    """); }

  @Test void explicitMdfLambdaRecMdf1(){ ok("""
    package test
    Foo:{}
    Bar:{ recMdf .a: recMdf Foo -> recMdf Foo }
    """); }
  @Test void explicitMdfLambdaRecMdfONonHyg1(){ fail("""
    In position [###]/Dummy0.fear:3:4
    [E26 recMdfInNonRecMdf]
    Invalid modifier for recMdf test.Foo[].
    recMdf may only be used in recMdf methods. The method .a/0 has the imm modifier.
    """, """
    package test
    Foo:{}
    Bar:{ .a: recMdf Foo -> recMdf Foo }
    """); }
  @Test void recMdfOkayInNonSigs(){ ok("""
    package test
    Foo:{}
    Bar:{ .a: Foo -> recMdf Foo }
    """); }

  @Test void noShadowingSelfName(){ fail("""
    In position [###]/Dummy0.fear:2:4
    [E9 shadowingX]
    'unique' is shadowing another variable in scope.
    """, """
    package test
    Foo:{
      .m1(): Foo -> Foo { 'unique
        .m1 -> {'unique}
        }
      }
    """); }


  @Test void noTopLevelSelfName() { fail("""
    In position [###]/Dummy0.fear:2:2
    [E50 namedTopLevelLambda]
    Trait declarations may not have a self-name other than "this".
    """, """
    package test
    A:{ 'self
      .me: A -> self,
      //.meThis: A -> this
      }
    """); }
  @Test void lambdaSelfNameOk() { ok("""
    package test
    A:{
      .me: A -> {'self },
      }
    """); }

  @Property void recMdfOnlyOnRecMdf(@ForAll("methMdfs") Mdf mdf) {
    var code = String.format("""
    package test
    A:{ %s .foo: recMdf Res }
    Res:{}
    """, mdf);

    if (mdf.isRecMdf()) {
      ok(code);
      return;
    }

    fail(String.format("""
    In position [###]/Dummy0.fear:2:2
    [E26 recMdfInNonRecMdf]
    Invalid modifier for recMdf test.Res[].
    recMdf may only be used in recMdf methods. The method .foo/0 has the %s modifier.
    """, mdf), code);
  }

  @Provide Arbitrary<Mdf> methMdfs() {
    return Arbitraries.of(Arrays.stream(Mdf.values()).filter(mdf->!mdf.isMdf()).toList());
  }
}
