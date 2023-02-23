package utils;

import java.util.stream.Collectors;

public interface Base {
  static ast.Program ignoreBase(ast.Program p) {
    return new ast.Program(
      p.ds().entrySet().stream()
        .filter(kv->!kv.getKey().name().startsWith("base."))
        .collect(Collectors.toMap(kv->kv.getKey(), kv->kv.getValue()))
    );
  }

  String minimalBase = """
  package base
  Main[R]:{ #(s: lent System[R]): mdf R }
  System[R]:{}
  """;

  String immBaseLib = immBaseLib("base");
  static String immBaseLib(String pkg) {
    // TODO: calling mut meths from lent seems broken
    return "package " + pkg + """

      Void:{}
      NoMutHyg[X]:{}
      Sealed:{}
      Main[R]:{ #(s: lent System[R]): mdf R }
      LentReturnStmt[R]:{ lent #: mdf R }
      System[R]:Sealed{
        lent .use[C](c: CapFactory[lent _RootCap, lent C], cont: lent UseCapCont[C, mdf R]): mdf R ->
          cont#(c#_RootCap, this), // TODO: use a block here to call c.close afterwards
        // mut .clone(): iso System[mdf R] -> {},
        lent .return(ret: lent LentReturnStmt[mdf R]): mdf R -> ret#
        }
      _RootCap:Sealed,IO{
        .print(msg) -> this.print(msg),
        .println(msg) -> this.println(msg),
        }
      UseCapCont[C, R]:{ lent #(cap: lent C, self: lent System[mdf R]): mdf R }
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
      
      Let:{ #[V,R](l:mut Let[mdf V, mdf R]): mdf R -> l.in(l.var) }
      Let[V,R]:{ mut .var: mdf V, mut .in(v: mdf V): mdf R }
          
      Bool:Sealed,Stringable{
        .and(b: Bool): Bool,
        &&(b: Bool): Bool -> this.and(b),
        .or(b: Bool): Bool,
        ||(b: Bool): Bool -> this.or(b),
        .not: Bool,
        ?[R](f: mut ThenElse[mdf R]): mdf R, // ?  because `bool ? { .then->aa, .else->bb }` is kinda like a ternary
        }
      True:Bool{ .and(b) -> b, .or(b) -> this, .not -> False, ?(f) -> f.then(), .str -> "True" }
      False:Bool{ .and(b) -> this, .or(b) -> b, .not -> True, ?(f) -> f.else(), .str -> "False" }
      ThenElse[R]:{ mut .then: mdf R, mut .else: mdf R, }
      
      Assert:Sealed{
        #[R](assertion: Bool, cont: mut AssertCont[mdf R]): mdf R -> assertion ? {
          .then -> cont#,
          .else -> this._fail[mdf R]()
          },
        #[R](assertion: Bool, msg: Str, cont: mut AssertCont[mdf R]): mdf R -> assertion ? {
          .then -> cont#,
          .else -> this._fail[mdf R](msg)
          },
        ._fail[R]: mdf R -> this._fail,
        ._fail[R](msg: Str): mdf R -> this._fail(msg),
        }
      AssertCont[R]:{ mut #: mdf R }
          
      Opt[T]:NoMutHyg[T]{
        .match[R](m: OptMatch[T, R]): R -> m.none,
        .map[R](f: OptMap[T,R]): Opt[R] -> this.match(f),
        .do(f: OptDo[T]):Opt[T] -> this.match(f),
        .flatMap[R](f: OptFlatMap[T, R]): Opt[R] ->this.match(f),
        }
      OptMatch[T,R]:{ .some(x:T): R, .none: R }
      OptFlatMap[T,R]:OptMatch[T,Opt[R]]{ .none->{} }
      OptMap[T,R]:OptMatch[T,Opt[R]]{ #(t:T):R, .some(x) -> Opt#(this#x), .none->{} }
      OptDo[T]:OptMatch[T,Opt[T]]{
        #(t:T):Void,   //#[R](t:T):R,
        .some(x) -> Opt#this._doRes(this#x, x),
        .none->{},
        ._doRes(y:Void,x:T):T -> x
        }
      Opt:{ #[T](x: T): Opt[T] -> { .match(m)->m.some(x) } } // TODO: this inferred Opt[mdf T] for the lambda
      
      Str:Sealed{
        .len: UInt
      }
      Stringable:{
        .str: Str,
      }
      _StrInstance:Str{
        .len -> this.len
        }
          
      // Ints
      Int:Sealed,MathOps[Int],IntOps[Int],Stringable{
        .uint: UInt,
        .float: Float
        }
      UInt:Sealed,MathOps[UInt],IntOps[UInt],Stringable{
        .int: Int,
        .float: Float
        }
      Float:Sealed,MathOps[Float],Stringable{
        .int: Int,
        .uint: UInt
        }

      MathOps[T]:{
        +(n: T): T,
        -(n: T): T,
        *(n: T): T,
        /(n: T): T,
        %(n: T): T,
        **(n: T): T, // pow
          
        // Comparisons
        >(n: T): Bool,
        <(n: T): Bool,
        >=(n: T): Bool,
        <=(n: T): Bool,
        ==(n: T): Bool,
        }
      IntOps[T]:{
        // bitwise
        >>(n: T): T,
        <<(n: T): T,
        ^(n: T): T,
        &(n: T): T,
        |(n: T): T,
        }
        
      // Fake concrete type for all numbers. The real implementation is generated at code-gen.
      _IntInstance:Int{
        .uint -> this.uint,
        .float -> this.float,
        .str -> this.str,
        +(n) -> this+n,
        -(n) -> this-n,
        *(n) -> this*n,
        /(n) -> this/n,
        %(n) -> this%n,
        **(n) -> this**n,
          
        // bitwise
        >>(n) -> this>>n,
        <<(n) -> this<<n,
        ^(n) -> this^n,
        &(n) -> this&n,
        |(n) -> this|n,
          
        // Comparisons
        >n -> this>n,
        <n -> this<n,
        >=n -> this>=n,
        <=n -> this<=n,
        ==n -> this==n,
        }
      _UIntInstance:UInt{
        .int -> this.int,
        .float -> this.float,
        .str -> this.str,
        +(n) -> this+n,
        -(n) -> this-n,
        *(n) -> this*n,
        /(n) -> this/n,
        %(n) -> this%n,
        **(n) -> this**n,
          
        // bitwise
        >>(n) -> this>>n,
        <<(n) -> this<<n,
        ^(n) -> this^n,
        &(n) -> this&n,
        |(n) -> this|n,
          
        // Comparisons
        >n -> this>n,
        <n -> this<n,
        >=n -> this>=n,
        <=n -> this<=n,
        ==n -> this==n,
        }
      _FloatInstance:Float{
        .int -> this.int,
        .uint -> this.uint,
        .str -> this.str,
        +(n) -> this+n,
        -(n) -> this-n,
        *(n) -> this*n,
        /(n) -> this/n,
        %(n) -> this%n,
        **(n) -> this**n,
        // Comparisons
        >n -> this>n,
        <n -> this<n,
        >=n -> this>=n,
        <=n -> this<=n,
        ==n -> this==n,
        }
        
      Ref:{ #[X](x: mdf X): mut Ref[mdf X] -> this#(x) }
      Ref[X]:NoMutHyg[X],Sealed{
        read * : recMdf X,
        mut .swap(x: mdf X): mdf X,
        mut :=(x: mdf X): Void -> Let#mut Let[mdf X, Void]{ .var -> this.swap(x), .in(_) -> Void },
        mut <-(f: mut UpdateRef[mdf X]): mdf X -> this.swap(f#(this*)),
      }
      UpdateRef[X]:{ mut #(x: mdf X): mdf X }
      """;
  }

  String onlyNums = """
      package base
      Str:{} Stringable:{ .str: Str } Bool:{} Sealed:{}
          
      // Ints
      Int:Sealed,MathOps[Int],IntOps[Int],Stringable{
        .uint: UInt,
        .float: Float
        }
      UInt:Sealed,MathOps[UInt],IntOps[UInt],Stringable{
        .int: Int,
        .float: Float
        }
      Float:Sealed,MathOps[Float],Stringable{
        .int: Int,
        .uint: UInt
        }

      MathOps[T]:{
        +(n: T): T,
        -(n: T): T,
        *(n: T): T,
        /(n: T): T,
        %(n: T): T,
        **(n: T): T, // pow
          
        // Comparisons
        >(n: T): Bool,
        <(n: T): Bool,
        >=(n: T): Bool,
        <=(n: T): Bool,
        ==(n: T): Bool,
        }
      IntOps[T]:{
        // bitwise
        >>(n: T): T,
        <<(n: T): T,
        ^(n: T): T,
        &(n: T): T,
        |(n: T): T,
        }
        
      // Fake concrete type for all numbers. The real implementation is generated at code-gen.
      _IntInstance:Int{
        .uint -> this.uint,
        .float -> this.float,
        .str -> this.str,
        +(n) -> this+n,
        -(n) -> this-n,
        *(n) -> this*n,
        /(n) -> this/n,
        %(n) -> this%n,
        **(n) -> this**n,
          
        // bitwise
        >>(n) -> this>>n,
        <<(n) -> this<<n,
        ^(n) -> this^n,
        &(n) -> this&n,
        |(n) -> this|n,
          
        // Comparisons
        >n -> this>n,
        <n -> this<n,
        >=n -> this>=n,
        <=n -> this<=n,
        ==n -> this==n,
        }
      _UIntInstance:UInt{
        .int -> this.int,
        .float -> this.float,
        .str -> this.str,
        +(n) -> this+n,
        -(n) -> this-n,
        *(n) -> this*n,
        /(n) -> this/n,
        %(n) -> this%n,
        **(n) -> this**n,
          
        // bitwise
        >>(n) -> this>>n,
        <<(n) -> this<<n,
        ^(n) -> this^n,
        &(n) -> this&n,
        |(n) -> this|n,
          
        // Comparisons
        >n -> this>n,
        <n -> this<n,
        >=n -> this>=n,
        <=n -> this<=n,
        ==n -> this==n,
        }
      _FloatInstance:Float{
        .int -> this.int,
        .uint -> this.uint,
        .str -> this.str,
        +(n) -> this+n,
        -(n) -> this-n,
        *(n) -> this*n,
        /(n) -> this/n,
        %(n) -> this%n,
        **(n) -> this**n,
        // Comparisons
        >n -> this>n,
        <n -> this<n,
        >=n -> this>=n,
        <=n -> this<=n,
        ==n -> this==n,
        }
  """;
}
