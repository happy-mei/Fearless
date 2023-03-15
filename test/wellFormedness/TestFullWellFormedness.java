package wellFormedness;

import failure.CompileError;
import main.Main;
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
  @Test void noIsoParamsLambdaOk() { ok("""
    package pkg1
    Opt[T]:{}
    A:Opt[A]
    """); }
  @Test void noIsoParamsLambda1() { fail("""
    In position [###]/Dummy0.fear:3:2
    [E5 isoInTypeArgs]
    The iso reference capability may not be used in type modifiers:
    iso pkg1.A[]
    """, """
    package pkg1
    Opt[T]:{}
    A:Opt[iso A]
    """); }
  @Test void noIsoParamsLambda2() { fail("""
    In position [###]/Dummy0.fear:3:2
    [E5 isoInTypeArgs]
    The iso reference capability may not be used in type modifiers:
    iso pkg1.A[]
    """, """
    package pkg1
    Opt[T]:{}
    A:{ #: Opt[iso A] -> Opt[iso A] }
    """); }
  @Test void noIsoParamsLambdaNested1() { fail("""
    In position [###]/Dummy0.fear:3:2
    [E5 isoInTypeArgs]
    The iso reference capability may not be used in type modifiers:
    iso pkg1.A[]
    """, """
    package pkg1
    Opt[T]:{}
    A:Opt[mut Opt[iso A]]
    """); }
  @Test void noIsoParamsLambdaNested2() { fail("""
    In position [###]/Dummy0.fear:4:2
    [E5 isoInTypeArgs]
    The iso reference capability may not be used in type modifiers:
    iso pkg1.A[]
    """, """
    package pkg1
    Opt[T]:{}
    B[C,D]:{}
    A:B[Opt[A], Opt[Opt[iso A]]]
    """); }

  @Test void noIsoParamsAliasOk() { ok("""
    package pkg1
    alias Opt[pkg1.A] as OptA,
    Opt[T]:{}
    A:{}
    """); }
  @Test void noIsoParamsAlias1() { fail("""
    In position [###]/Dummy0.fear:2:0
    [E5 isoInTypeArgs]
    The iso reference capability may not be used in type modifiers:
    iso pkg1.A[]
    """, """
    package pkg1
    alias pkg1.Opt[iso pkg1.A] as OptA,
    Opt[T]:{}
    A:{}
    """); }
  @Test void noIsoParamsAliasNested1() { fail("""
    In position [###]/Dummy0.fear:2:0
    [E5 isoInTypeArgs]
    The iso reference capability may not be used in type modifiers:
    iso pkg1.A[]
    """, """
    package pkg1
    alias pkg1.Opt[pkg1.Opt[iso pkg1.A]] as OptA,
    Opt[T]:{}
    A:{}
    """); }

  @Test void noIsoParamsMethRet() { fail("""
    In position [###]/Dummy0.fear:3:2
    [E5 isoInTypeArgs]
    The iso reference capability may not be used in type modifiers:
    iso pkg1.A[]
    """, """
    package pkg1
    Opt[T]:{}
    A:{ #: Opt[iso A] -> {} }
    """); }
  @Test void isoParamsMethParamsOk() { ok("""
    package pkg1
    Opt[T]:{}
    A:{ #(x: iso A): A -> {} }
    """); }
  @Test void isoParamsMethParams() { fail("""
    In position [###]/Dummy0.fear:3:2
    [E5 isoInTypeArgs]
    The iso reference capability may not be used in type modifiers:
    iso pkg1.A[]
    """, """
    package pkg1
    Opt[T]:{}
    A:{ #(x: A[iso A]): A -> {} }
    """); }
  @Test void isoParamsMethParamsGens() { fail("""
    In position [###]/Dummy0.fear:3:2
    [E5 isoInTypeArgs]
    The iso reference capability may not be used in type modifiers:
    iso T
    """, """
    package pkg1
    Opt[T]:{}
    A:{ #[T](x: A[iso T]): A -> {} }
    """); }
  @Test void isoParamsMethCall() { fail("""
    In position [###]/Dummy0.fear:3:2
    [E5 isoInTypeArgs]
    The iso reference capability may not be used in type modifiers:
    iso pkg1.A[]
    """, """
    package pkg1
    Opt[T]:{}
    A:{
      #[T](x: A[mdf T]): A -> {},
      .foo(): A -> this#[iso A]A
      }
    """); }
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
    A:{'this}
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

  @Test void noShadowingLambda() { fail("""
    In position [###]/Dummy0.fear:2:2
    [E9 shadowingX]
    'hi' is shadowing another variable in scope.
    """, """
    package base
    A:{'hi .a: A -> A{'hi .a() -> {} } }
    """); }

  @Test void noMutHygOk() { ok("""
    package base
    NoMutHyg[X]:{}
    Sealed:{} Void:{}
    Let:{ #[V,R](l:Let[V,R]):R -> l.in(l.var) }
    Let[V,R]:{ .var:V, .in(v:V):R }
    Ref:{ #[X](x: mdf X): mut Ref[mdf X] -> this#(x) }
    Ref[X]:NoMutHyg[X],Sealed{
      read * : recMdf X,
      mut .swap(x: mdf X): mdf X,
      mut :=(x: mdf X): Void -> Let#{ .var -> this.swap(x), .in(_)->Void },
      mut <-(f: UpdateRef[mdf X]): mdf X -> this.swap(f#(this*)),
    }
    UpdateRef[X]:{ mut #(x: mdf X): mdf X }
    """); }
  @Test void noMutHygConcrete() { fail("""
    In position [###]/Dummy0.fear:7:0
    [E12 concreteInNoMutHyg]
    The type parameters to NoMutHyg must be generic and present in the type parameters of the trait implementing it. A concrete type was found:
    imm base.Ref[]
    """, """
    package base
    NoMutHyg[X]:{}
    Sealed:{} Void:{}
    Let:{ #[V,R](l:Let[V,R]):R -> l.in(l.var) }
    Let[V,R]:{ .var:V, .in(v:V):R }
    Ref:{ #[X](x: mdf X): mut Ref[mdf X] -> this#(x) }
    Ref[X]:NoMutHyg[Ref],Sealed{
      read * : recMdf X,
      mut .swap(x: mdf X): mdf X,
      mut :=(x: mdf X): Void -> Let#{ .var -> this.swap(x), .in(_)->Void },
      mut <-(f: UpdateRef[mdf X]): mdf X -> this.swap(f#(this*)),
    }
    UpdateRef[X]:{ mut #(x: mdf X): mdf X }
    """); }
  @Test void noMutHygNotUsed() { fail("""
    In position [###]/Dummy0.fear:7:0
    [E13 invalidNoMutHyg]
    The type parameters to NoMutHyg must be generic and present in the type parameters of the trait implementing it. This generic type is not a type parameter of the trait:
    imm A
    """, """
    package base
    NoMutHyg[X]:{}
    Sealed:{} Void:{}
    Let:{ #[V,R](l:Let[V,R]):R -> l.in(l.var) }
    Let[V,R]:{ .var:V, .in(v:V):R }
    Ref:{ #[X](x: mdf X): mut Ref[mdf X] -> this#(x) }
    Ref[X]:NoMutHyg[A],Sealed{
      read * : recMdf X,
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
  @Test void recMdfAsMethMdf() { fail("""
    In position [###]/Dummy0.fear:2:2
    [E16 invalidMethMdf]
    recMdf is not a valid modifier for a method (on the method .foo/0).
    """, """
    package base
    A:{ recMdf .foo: A }
    """); }

  @Test void useUndefinedX() { fail("""
    In position [###]/Dummy0.fear:3:2
    [E28 undefinedName]
    The identifier "X" is undefined or cannot be captured.
    """, """
    package test
    A[X]:{ .foo(x: X): X -> B{ x }.argh }
    B:{ read .argh: recMdf X } // should fail because X is not defined here
    """); }
  @Test void useUndefinedIdent() { fail("""
    In position [###]/Dummy0.fear:2:5
    [E28 undefinedName]
    The identifier "X" is undefined or cannot be captured.
    """, """
    package test
    A[X]:{ .foo(x: X): X -> this.foo(b) }
    """); }
}
