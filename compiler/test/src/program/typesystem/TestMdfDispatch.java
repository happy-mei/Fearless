package program.typesystem;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import static program.typesystem.RunTypeSystem.fail;
import static program.typesystem.RunTypeSystem.ok;

public class TestMdfDispatch {
  @Test void inferringShouldWorkForASingleCandidate() { ok("""
    package test
    A:{
      .m1: A,
      .m2: A,
      }
    B:A{
      .m1 -> this,
      .m2 -> this.m1,
      }
    """); }

  // This fails because in the promotion that we're asking for (mut -> imm) "mut this" cannot be used in gamma
  @Test void inferringShouldFailWhenMultipleCandidates1() { fail("""
    In position [###]/Dummy0.fear:8:9
    [E53 xTypeError]
    Expected 'this' to be imm test.A[], got mut test.B[].
    """, """
    package test
    A:{
      imm .m1: A,
      mut .m1: A,
      .m2: A,
      }
    B:A{
      .m1 -> this,
      .m2 -> this.m1,
      }
    """); }

  @Disabled
  @Test void inferringShouldFailWhenMultipleCompatibleCandidates() { fail("""
    In position [###]/Dummy0.fear:9:16
    [E52 ambiguousMethod]
    Unable to figure out which method is being referenced here, please write the full signature (including generic type parameters).
    """, """
    package test
    A:{
      imm .m1: A,
      read .m1: A,
      .m2: A,
      }
    B:A{
      imm .m1: A -> this,
      .m2: A -> this.m1,
      }
    """); }

  @Test void inferringShouldWorkWhenMultipleCompatibleCandidates2() { ok("""
    package test
    A:{
      imm .m1: A,
      mut .m1: mut A,
      .m2: A,
      }
    B:A{
      .m1 -> this,
      .m2 -> this.m1,
      }
    """); }

  @Test void inferringShouldWorkWhenMultipleCandidates() { ok("""
    package test
    A:{
      imm .m1: A,
      mut .m1: A,
      .m2: A,
      }
    B:A{
      imm .m1: A -> this,
      .m2: A -> this.m1,
      }
    """); }

  @Test void callingMultiSig() { ok("""
    package test
    A:{
      read .m1: read B -> {},
      mut .m1: mut A -> this,
      }
    B:{}
    Test:{
      read .aRead(a: read A): read B -> a.m1[](),
      mut .aMut(a: mut A): mut A -> a.m1[](),
      }
    """); }
  @Test void callingMultiSigFail() { fail("""
  In position [###]/Dummy0.fear:8:36
  [E33 callTypeError]
  Type error: None of the following candidates (returning the expected type "mut test.A[]") for this method call:
  a .m1/0[]([])
  were valid:
  (read test.A[]) <= (mut test.A[]): mut test.A[]
    The following errors were found when checking this sub-typing:
      In position [###]/Dummy0.fear:8:35
      [E53 xTypeError]
      Expected 'a' to be mut test.A[], got read test.A[].
  
  (read test.A[]) <= (iso test.A[]): iso test.A[]
    The following errors were found when checking this sub-typing:
      In position [###]/Dummy0.fear:8:35
      [E53 xTypeError]
      Expected 'a' to be iso test.A[], got read test.A[].
    """, """
    package test
    A:{
      read .m1: read B -> {},
      mut .m1: mut A -> this,
      }
    B:{}
    Test:{
      read .aRead(a: read A): mut A -> a.m1[](),
      mut .aMut(a: mut A): mut A -> a.m1[](),
      }
    """); }
  @Test void callingMultiSigAmbiguousDiffRet() { ok("""
    package test
    A:{
      read .m1: read B -> {},
      mut .m1: mut A -> this,
      }
    B:{}
    Test:{
      read .aRead(a: mut A): read B -> a.m1[](),
      }
    """); }
  @Test void callingMultiSigAmbiguousSameRet() { ok("""
    package test
    A:{
      read .m1: mut A -> {},
      mut .m1: mut A -> this,
      }
    B:{}
    Test:{
      read .aRead(a: mut A): mut A -> a.m1[](),
      }
    """); }

  @Test void callingMultiSigImmPromotion() { ok("""
    package test
    A:{
      read .m1: mut A -> {},
      mut .m1: mut A -> this,
      }
    B:{}
    Test:{
      read .aRead(a: A): imm A -> a.m1[](),
      }
    """); }
  @Test void callingMultiSigImmDispatch() { ok("""
    package test
    A:{
      read .m1: mut A -> {},
      imm .m1: A -> this,
      mut .m1: mut A -> this,
      }
    B:{}
    Test:{
      read .aRead(a: A): imm A -> a.m1[](),
      }
    """); }

  @Test void optWithImmMatcher() { ok("""
  package base
  Opt:{ #[T](x: T): mut Opt[T] -> {
    mut .match[R](m: mut OptMatch[T, R]): R -> m.some(x),
    read .match[R](m: mut OptMatch[read T, R]): R -> m.some(x),
    }}
  Opt[T]:{
    mut  .match[R](m: mut OptMatch[T, R]): R -> m.empty,
    read .match[R](m: mut OptMatch[read T, R]): R -> m.empty,
    imm  .match[R](m: mut OptMatch[T, R]): R -> m.empty,
    read .or(f: mut OptOrElse[read Opt[T]]): read Opt[T] -> this.match[read Opt[T]](mut OptMatch[read T, read Opt[T]]{
      .some(x) -> this,
      .empty -> f#
      }),
    imm .or(f: mut OptOrElse[Opt[T]]): Opt[T] -> this.match[Opt[T]](mut OptMatch[T, Opt[T]]{
        .some(x) -> this,
        .empty -> f#
        }),
      }
    OptMatch[T,R]:{ mut .some(x: T): R, mut .empty: R }
    OptOrElse[R]:{ mut #: R }
        """); }
  @Test void newBase() { ok("""
    package base
    //alias test.caps.System as System,
        
    //Main:{ #(s: mut System): Void }
    Sealed:{}
    Void:Sealed{}

    Abort:Sealed{ ![R:readOnly,lent,read,mut,imm,iso]: R -> this! } // can be optimised to just terminate (goes stuck)
    Magic:Sealed{ ![R:readOnly,lent,read,mut,imm,iso]: R -> this! } // magic'd out to tell us what we forgot to implement
    Debug:Sealed{ #[T](x: T): T -> x } // TODO: magic
    HasIdentity:{ mut .idEq(other: readOnly HasIdentity): Bool -> Magic! } // TODO: magic
        
    Let:{
      #[V,R](l: mut Let[V, R]): R -> l.in(l.var),
      }
    Let[V,R]:{ mut .var: V, mut .in(v: V): R }

    F[R:read,mut,imm,iso]:{ read #: R }
    F[A:read,mut,imm,iso,R:read,mut,imm,iso]:{ read #(a: A): R }
    F[A:read,mut,imm,iso, B:read,mut,imm,iso, R:read,mut,imm,iso]:{ read #(a: A, b: B): R }
    F[A:read,mut,imm,iso, B:read,mut,imm,iso, C:read,mut,imm,iso, R:read,mut,imm,iso]:{ read #(a: A, b: B, c: C): R }
    
    // TODO: could be overloads on Do
    Yeet:{
      #[X:read,mut,imm,iso](x: X): Void -> this.with(x, Void),
      .with[X:read,mut,imm,iso, R:read,mut,imm,iso](_: X, res: R): R -> res,
      }
    """, """
    package base
    Bool:Sealed,Stringable{
      .and(b: Bool): Bool,
      &&(b: Bool): Bool -> this.and(b),
      .or(b: Bool): Bool,
      ||(b: Bool): Bool -> this.or(b),
      .not: Bool,
      .if[R](f: mut ThenElse[R]): R,
      ?[R](f: mut ThenElse[R]): R -> this.if(f),
      }
    True:Bool{ .and(b) -> b, .or(b) -> this, .not -> False, .if(f) -> f.then(), .str -> "True" }
    False:Bool{ .and(b) -> this, .or(b) -> b, .not -> True, .if(f) -> f.else(), .str -> "False" }
    ThenElse[R]:{ mut .then: R, mut .else: R, }
    """, """
    package base
    Str:Sealed{
      .len: UInt,
      // Str is not Stringable due to limitations of the Java codegen target
      .str: Str,
      +(other: Str): Str,
      ==(other: Str): Bool,
      }
    Stringable:{
      read .str: Str,
      }
    _StrInstance:Str{
      .len -> Magic!,
      .str -> Magic!,
      +(other) -> Magic!,
      ==(other) -> Magic!,
      }
    """, """
    package base
    Int:Sealed,_MathOps[Int],_IntOps[Int]{
      .uint: UInt,
      .float: Float,
      // not Stringable due to limitations of the Java codegen target
      .str: Str,
      }
    UInt:Sealed,_MathOps[UInt],_IntOps[UInt]{
      .int: Int,
      .float: Float,
      // not Stringable due to limitations of the Java codegen target
      .str: Str,
      }
    Float:Sealed,_MathOps[Float]{
      .int: Int,
      .uint: UInt,
      .round: Int,
      .ceil: Int,
      .floor: Int,
      **(n: Float): Float, // pow
      .isNaN: Bool,
      .isInfinite: Bool,
      .isPosInfinity: Bool,
      .isNegInfinity: Bool,
      // not Stringable due to limitations of the Java codegen target
      .str: Str,
      }
        
    _MathOps[T]:Sealed{
      +(n: T): T,
      -(n: T): T,
      *(n: T): T,
      /(n: T): T,
      %(n: T): T,
      .abs: T,
        
      // Comparisons
      >(n: T): Bool,
      <(n: T): Bool,
      >=(n: T): Bool,
      <=(n: T): Bool,
      ==(n: T): Bool,
      }
    _IntOps[T]:Sealed{
      // bitwise
      >>(n: T): T,
      <<(n: T): T,
      ^(n: T): T,
      &(n: T): T,
      |(n: T): T,
        
      **(n: UInt): T, // pow
      }
        
    // Fake concrete type for all numbers. The real implementation is generated at code-gen.
    _IntInstance:Int{
      .uint -> Magic!,
      .float -> Magic!,
      .str -> Magic!,
      +(n) -> Magic!,
      -(n) -> Magic!,
      *(n) -> Magic!,
      /(n) -> Magic!,
      %(n) -> Magic!,
      **(n) -> Magic!,
      .abs -> Magic!,
        
      // bitwise
      >>(n) -> Magic!,
      <<(n) -> Magic!,
      ^(n) -> Magic!,
      &(n) -> Magic!,
      |(n) -> Magic!,
        
      // Comparisons
      >n -> Magic!,
      <n -> Magic!,
      >=n -> Magic!,
      <=n -> Magic!,
      ==n -> Magic!,
      }
    _UIntInstance:UInt{
      .int -> Magic!,
      .float -> Magic!,
      .str -> Magic!,
      +(n) -> Magic!,
      -(n) -> Magic!,
      *(n) -> Magic!,
      /(n) -> Magic!,
      %(n) -> Magic!,
      **(n) -> Magic!,
      .abs -> Magic!,
        
      // bitwise
      >>(n) -> Magic!,
      <<(n) -> Magic!,
      ^(n) -> Magic!,
      &(n) -> Magic!,
      |(n) -> Magic!,
        
      // Comparisons
      >n -> Magic!,
      <n -> Magic!,
      >=n -> Magic!,
      <=n -> Magic!,
      ==n -> Magic!,
      }
    _FloatInstance:Float{
      .int -> Magic!,
      .uint -> Magic!,
      .str -> Magic!,
      .round -> Magic!,
      .ceil -> Magic!,
      .floor -> Magic!,
      .isNaN -> Magic!,
      .isInfinite -> Magic!,
      .isPosInfinity -> Magic!,
      .isNegInfinity -> Magic!,
      +(n) -> Magic!,
      -(n) -> Magic!,
      *(n) -> Magic!,
      /(n) -> Magic!,
      %(n) -> Magic!,
      **(n) -> Magic!,
      .abs -> Magic!,
      // Comparisons
      >n -> Magic!,
      <n -> Magic!,
      >=n -> Magic!,
      <=n -> Magic!,
      ==n -> Magic!,
      }
    """, """
    package base
        
    Opt:{ #[T](x: T): mut Opt[T] -> {
      // .match(m) -> m.some(x),
      mut .match[R](m: mut OptMatch[T, R]): R -> m.some(x),
      read .match[R](m: mut OptMatch[read T, R]): R -> m.some(x),
      }}
    Opt[T]:{
      mut .match[R](m: mut OptMatch[T, R]): R -> m.none,
      read .match[R](m: mut OptMatch[read T, R]): R -> m.none,
      mut .map[R](f: mut OptMap[T, R]): mut Opt[R] -> this.match[mut Opt[R]](f),
      read .map[R](f: mut OptMap[read T, R]): mut Opt[R] -> this.match[mut Opt[R]](f),
      mut |(alt: T): T -> this.match[T](mut OptMatch[T, T]{
        .some(x) -> x,
        .none -> alt
        }),
      read |(alt: read T): read T -> this.match[read T](mut OptMatch[read T, read T]{
        .some(x) -> x,
        .none -> alt
        }),
      mut ||(alt: mut OptOrElse[T]): T -> this.match[T](mut OptMatch[T, T]{
        .some(x) -> x,
        .none -> alt#
        }),
      read ||(alt: mut OptOrElse[read T]): read T -> this.match[read T](mut OptMatch[read T, read T]{
        .some(x) -> x,
        .none -> alt#
        }),
      mut .or(f: mut OptOrElse[mut Opt[T]]): mut Opt[T] -> this.match[mut Opt[T]](mut OptMatch[T, mut Opt[T]]{
        .some(x) -> this,
        .none -> f#
        }),
      read .or(f: mut OptOrElse[read Opt[read T]]): read Opt[read T] -> this.match[read Opt[read T]](mut OptMatch[read T, read Opt[read T]]{
        .some(x) -> this,
        .none -> f#
        }),
      read .isEmpty: Bool -> this.match[Bool](mut OptMatch[read T, Bool]{ .none -> True, .some(_) -> False }),
      read .isSome: Bool -> this.match[Bool](mut OptMatch[read T, Bool]{ .none -> False, .some(_) -> True }),
      }
    
    OptMatch[T,R]:{ mut .some(x: T): R, mut .none: R }
    OptOrElse[R]:{ mut #: R }
    OptMap[T,R]:OptMatch[T, mut Opt[R]]{
      mut #(t: T): R,
      .some(x) -> Opt#(this#x),
      .none -> {}
      }
    """); }
}