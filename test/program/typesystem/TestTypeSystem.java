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

public class TestTypeSystem {
  //  TODO: mut Box[read X] is not valid even after promotion
  // TODO: .m: mut Box[mdf X] must return lent Box[read Person] if mdf X becomes read X (same with lent)
  // TODO: Factory of mutBox and immBox, what types do we get?
  void ok(String... content){
    assert content.length > 0;
    Main.resetAll();
    AtomicInteger pi = new AtomicInteger();
    var ps = Arrays.stream(content)
      .map(code -> new Parser(Path.of("Dummy"+pi.getAndIncrement()+".fear"), code))
      .toList();
    var p = Parser.parseAll(ps);
    new WellFormednessFullShortCircuitVisitor().visitProgram(p).ifPresent(err->{ throw err; });
    var inferredSigs = p.inferSignaturesToCore();
    var inferred = new InferBodies(inferredSigs).inferAll(p);
    new WellFormednessShortCircuitVisitor().visitProgram(inferred);
    inferred.typeCheck();
  }
  void fail(String expectedErr, String... content){
    assert content.length > 0;
    Main.resetAll();
    AtomicInteger pi = new AtomicInteger();
    var ps = Arrays.stream(content)
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

  @Test void emptyProgram(){ ok("""
    package test
    """); }

  @Test void simpleProgram(){ ok( """
    package test
    A:{ .m: A -> this }
    """); }

  @Test void baseLib(){ ok( Base.immBaseLib); }

  // TODO: error message is wrong
  @Test void simpleTypeError(){ fail("""
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

  @Test void subTypingCall(){ ok( """
    package test
    A:{ .m1(a: A): A -> a }
    B:A{}
    C:{ .m2: A -> A.m1(B) }
    """); }

  @Test void numbers1(){ ok( """
    package test
    A:{ .m(a: 42): 42 -> 42 }
    """, Base.immBaseLib); }

  @Test void numbersNoBase(){ ok( """
    package test
    A:{ .m(a: 42): 42 -> 42 }
    """, Base.onlyNums); }

  @Test void numbersSubTyping1(){ ok( """
    package test
    alias base.Int as Int,
    A:{ .m(a: 42): Int -> a }
    """, Base.immBaseLib); }
  @Test void numbersSubTyping2(){ fail("""
    In position [###]/Dummy0.fear:3:4
    [E23 methTypeError]
    Expected the method .m/1 to return imm 42[], got imm base.Int[].
    """, """
    package test
    alias base.Int as Int,
    A:{ .m(a: Int): 42 -> a }
    """, Base.immBaseLib); }
  @Test void numbersSubTyping3(){ ok( """
    package test
    alias base.Int as Int,
    A:{ .a: Int }
    B:A{ .a -> 42 }
    C:A{ .a -> 420 }
    """, Base.immBaseLib); }
  @Test void numbersSubTyping4(){ ok( """
    package test
    alias base.Int as Int,
    A:{ .a: Int }
    B:A{ .a -> 42 }
    C:A{ .a -> 420 }
    D:B{ .b: Int -> this.a }
    """, Base.immBaseLib); }
  @Test void numbersGenericTypes1(){ ok( """
    package test
    alias base.Int as Int,
    A[N]:{ .count: N }
    B:A[42]{ 42 }
    C:A[Int]{ 42 }
    """, Base.immBaseLib); }
  @Test void numbersGenericTypes2(){ ok( """
    package test
    alias base.Int as Int,
    A[N]:{ .count: N, .sum: N }
    B:A[42]{ .count -> 42, .sum -> 42 }
    C:A[Int]{ .count -> 56, .sum -> 3001 }
    """, Base.immBaseLib); }
  @Test void numbersGenericTypes2a(){ fail("""
    In position [###]/Dummy0.fear:4:31
    [E18 uncomposableMethods]
    These methods could not be composed.
    conflicts:
    ([###]/Dummy1.fear:45:6) 43[], .float/0
    ([###]/Dummy1.fear:45:6) 42[], .float/0
    """, """
    package test
    alias base.Int as Int,
    A[N]:{ .count: N, .sum: N }
    B:A[42]{ .count -> 42, .sum -> 43 }
    """, Base.onlyNums); }
  @Test void numbersGenericTypes2aWorksThanksTo5b(){ ok("""
    package test
    FortyTwo:{}
    FortyThree:{}
    A[N]:{ .count: N, .sum: N }
    B:A[FortyTwo]{ .count -> FortyTwo, .sum -> FortyThree }
    """); }
  @Test void numbersGenericTypes2aNoMagic(){ fail("""
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
  @Test void numbersSubTyping4a(){ fail("""
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
    """, Base.onlyNums); }
  @Test void twoInts(){ ok( """
    package test
    alias base.Int as Int,
    A:{ .m(a: 56, b: 12): Int -> b+a }
    """, Base.immBaseLib); }

  @Test void noRecMdfWeakening() { fail("""
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
  @Test void boolIntRet() { ok("""
    package test
    alias base.Main as Main, alias base.Int as Int, alias base.False as False, alias base.True as True,
    Test:Main[Int]{
      _->False.or(True)?{.then->42,.else->0}
    }
    """, Base.immBaseLib); }
  @Test void boolSameRet() { ok("""
    package test
    alias base.Main as Main, alias base.Int as Int, alias base.False as False, alias base.True as True,
    Foo:{}
    Test:Main[Foo]{
      _->False.or(True)?{.then->Foo,.else->Foo}
    }
    """, Base.immBaseLib); }

  @Test void ref1() { ok("""
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

  @Test void numImpls1() { ok("""
    package test
    alias base.Int as Int,
    Foo:{ .bar: 5 -> 5 }
    Bar:{
      .nm(n: Int): Int -> n,
      .check: Int -> this.nm(Foo.bar)
      }
    """, Base.onlyNums);}

  @Test void numImpls2() { ok("""
    package test
    alias base.Int as Int,
    Bar:{
      .nm(n: Int): Int -> n,
      .check: Int -> this.nm(5)
      }
    """, Base.onlyNums);}

  @Test void numImpls3() { fail("""
    In position [###]/Dummy0.fear:5:25
    [E18 uncomposableMethods]
    These methods could not be composed.
    conflicts:
    ([###]/Dummy1.fear:65:6) 5[], <=/1
    ([###]/Dummy1.fear:30:6) base.MathOps[imm base.Float[]], <=/1
    """, """
    package test
    alias base.Int as Int, alias base.Float as Float,
    Bar:{
      .nm(n: Float): Int -> 12,
      .check: Int -> this.nm(5)
      }
    """, Base.onlyNums);}

  @Test void simpleThis() { ok("""
    package test
    A:{
      .a: C -> B{ this.c }.c,
      .c: C -> {}
      }
    B:{ .c: C }
    C:{ }
    """); }

  @Test void lambdaCapturesThis() { ok("""
    package test
    Let:{ #[V,R](l: mut Let[V, R]): R -> l.in(l.var) }
    Let[V,R]:{ mut .var: V, mut .in(v: V): R }
    Void:{}
    Ref[X]:{
        mut .swap(x: X): X,
        mut :=(x: X): Void -> Let#mut Let[X,Void]{ .var -> this.swap(x), .in(_) -> Void },
      }
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
}
