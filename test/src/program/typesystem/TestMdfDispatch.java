package program.typesystem;

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
  @Test void inferringShouldFailWhenMultipleCandidates() { fail("""
    In position [###]/Dummy0.fear:8:2
    [E51 ambiguousMethodName]
    Unable to lookup the signature of the method: .m1/0. Multiple candidates exist with the same name and number of arguments.
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
    [E32 noCandidateMeths]
    When attempting to type check the method call: a .m1/0[]([]), no candidates for .m1/0 returned the expected type mut test.A[]. The candidates were:
    (read test.A[]): read test.B[]
    (imm test.A[]): imm test.B[]
    (readOnly test.A[]): readOnly test.B[]
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
      Opt:{ #[T](x: mdf T): mut Opt[mdf T] -> {
  //      mut .match[R](m: mut OptMatch[mdf T, mdf R]): mdf R -> m.some(x),
  //      read .match[R](m: mut OptMatch[read T, mdf R]): mdf R -> m.some(x),
        }}
      Opt[T]:{
  //      mut  .match[R](m: mut OptMatch[mdf T, mdf R]): mdf R -> m.empty,
        read .match[R](m: mut OptMatch[read T, mdf R]): mdf R -> m.empty,
        imm  .match[R](m: mut OptMatch[T, mdf R]): mdf R -> m.empty,
        read .or(f: mut OptOrElse[read Opt[mdf T]]): read Opt[mdf T] -> this.match[read Opt[mdf T]](mut OptMatch[read T, read Opt[mdf T]]{
          .some(x) -> this,
          .empty -> f#
          }),
        imm .or(f: mut OptOrElse[Opt[mdf T]]): Opt[mdf T] -> this.match[Opt[mdf T]](mut OptMatch[T, Opt[mdf T]]{
            .some(x) -> this,
            .empty -> f#
            }),
          }
        OptMatch[T,R]:{ mut .some(x: mdf T): mdf R, mut .empty: mdf R }
        OptOrElse[R]:{ mut #: mdf R }
        """); }
  @Test void newBase() { ok("""
    package base
    //alias test.caps.System as System,
        
    //Main:{ #(s: mut System): Void }
    Sealed:{}
    Void:Sealed{}

    Abort:Sealed{ ![R:readOnly,lent,read,mut,imm,iso]: mdf R -> this! } // can be optimised to just terminate (goes stuck)
    Magic:Sealed{ ![R:readOnly,lent,read,mut,imm,iso]: mdf R -> this! } // magic'd out to tell us what we forgot to implement
    Debug:Sealed{ #[T](x: T): T -> x } // TODO: magic
    HasIdentity:{ mut .idEq(other: readOnly HasIdentity): Bool -> Magic! } // TODO: magic
        
    Let:{
      #[V,R](l: mut Let[mdf V, mdf R]): mdf R -> l.in(l.var),
      }
    Let[V,R]:{ mut .var: mdf V, mut .in(v: mdf V): mdf R }

    F[R:read,mut,imm,iso]:{ read #: mdf R }
    F[A:read,mut,imm,iso,R:read,mut,imm,iso]:{ read #(a: mdf A): mdf R }
    F[A:read,mut,imm,iso, B:read,mut,imm,iso, R:read,mut,imm,iso]:{ read #(a: mdf A, b: mdf B): mdf R }
    F[A:read,mut,imm,iso, B:read,mut,imm,iso, C:read,mut,imm,iso, R:read,mut,imm,iso]:{ read #(a: mdf A, b: mdf B, c: mdf C): mdf R }
    
    // TODO: could be overloads on Do
    Yeet:{
      #[X:read,mut,imm,iso](x: mdf X): Void -> this.with(x, Void),
      .with[X:read,mut,imm,iso, R:read,mut,imm,iso](_: mdf X, res: mdf R): mdf R -> res,
      }
    """, """
    package base
    Bool:Sealed,Stringable{
      .and(b: Bool): Bool,
      &&(b: Bool): Bool -> this.and(b),
      .or(b: Bool): Bool,
      ||(b: Bool): Bool -> this.or(b),
      .not: Bool,
      .if[R](f: mut ThenElse[mdf R]): mdf R,
      ?[R](f: mut ThenElse[mdf R]): mdf R -> this.if(f),
      }
    True:Bool{ .and(b) -> b, .or(b) -> this, .not -> False, .if(f) -> f.then(), .str -> "True" }
    False:Bool{ .and(b) -> this, .or(b) -> b, .not -> True, .if(f) -> f.else(), .str -> "False" }
    ThenElse[R]:{ mut .then: mdf R, mut .else: mdf R, }
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
        
    Opt:{ #[T](x: mdf T): mut Opt[mdf T] -> {
      // .match(m) -> m.some(x),
      mut .match[R](m: mut OptMatch[mdf T, mdf R]): mdf R -> m.some(x),
      read .match[R](m: mut OptMatch[read T, mdf R]): mdf R -> m.some(x),
      }}
    Opt[T]:{
      mut .match[R](m: mut OptMatch[mdf T, mdf R]): mdf R -> m.none,
      read .match[R](m: mut OptMatch[read T, mdf R]): mdf R -> m.none,
      mut .map[R](f: mut OptMap[mdf T, mdf R]): mut Opt[mdf R] -> this.match[mut Opt[mdf R]](f),
      read .map[R](f: mut OptMap[read T, mdf R]): mut Opt[mdf R] -> this.match[mut Opt[mdf R]](f),
      mut |(alt: mdf T): mdf T -> this.match[mdf T](mut OptMatch[mdf T, mdf T]{
        .some(x) -> x,
        .none -> alt
        }),
      read |(alt: read T): read T -> this.match[read T](mut OptMatch[read T, read T]{
        .some(x) -> x,
        .none -> alt
        }),
      mut ||(alt: mut OptOrElse[mdf T]): mdf T -> this.match[mdf T](mut OptMatch[mdf T, mdf T]{
        .some(x) -> x,
        .none -> alt#
        }),
      read ||(alt: mut OptOrElse[read T]): read T -> this.match[read T](mut OptMatch[read T, read T]{
        .some(x) -> x,
        .none -> alt#
        }),
      mut .or(f: mut OptOrElse[mut Opt[mdf T]]): mut Opt[mdf T] -> this.match[mut Opt[mdf T]](mut OptMatch[mdf T, mut Opt[mdf T]]{
        .some(x) -> this,
        .none -> f#
        }),
      read .or(f: mut OptOrElse[read Opt[read T]]): read Opt[read T] -> this.match[read Opt[read T]](mut OptMatch[read T, read Opt[read T]]{
        .some(x) -> this,
        .none -> f#
        }),
      read .isNone: Bool -> this.match[Bool](mut OptMatch[read T, Bool]{ .none -> True, .some(_) -> False }),
      read .isSome: Bool -> this.match[Bool](mut OptMatch[read T, Bool]{ .none -> False, .some(_) -> True }),
      }
    
    OptMatch[T,R]:{ mut .some(x: mdf T): mdf R, mut .none: mdf R }
    OptOrElse[R]:{ mut #: mdf R }
    OptMap[T,R]:OptMatch[mdf T, mut Opt[mdf R]]{
      mut #(t: mdf T): mdf R,
      .some(x) -> Opt#(this#x),
      .none -> {}
      }
    """); }
}