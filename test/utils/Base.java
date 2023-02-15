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
  Main[R]:{ #(s: lent System): mdf R }
  System:{}
  """;

  String immBaseLib = immBaseLib("base");
  static String immBaseLib(String pkg) {
    return "package " + pkg + """

      Void:{}
      NoMutHyg[X]:{}
      Sealed:{}
      Main[R]:{ #(s: lent System): mdf R }
      System:{} // Root capability
      
      Let:{ #[V,R](l:Let[V,R]):R -> l.in(l.var) }
      Let[V,R]:{ .var:V, .in(v:V):R }
          
      Bool:Sealed{
        .and(b: Bool): Bool,
        .or(b: Bool): Bool,
        .not: Bool,
        ?[R](f: mut ThenElse[R]): R, // ?  because `bool ? { .then->aa, .else->bb }` is kinda like a ternary
        }
      True:Bool{ .and(b) -> b, .or(b) -> this, .not -> False, ?(f) -> f.then() }
      False:Bool{ .and(b) -> this, .or(b) -> b, .not -> True, ?(f) -> f.else() }
      ThenElse[R]:{ mut .then: R, mut .else: R, }
          
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
      
      Str:{
        .len: UNum
      }
      Stringable:{
        .str: Str,
      }
          
      // Nums
      Num:Sealed,MathOps[Num],Stringable{ .unum: UNum }
      UNum:Sealed,MathOps[UNum],Stringable{ .num: Num }

      MathOps[T]:{
        +(n: mdf T): mdf T,
        -(n: mdf T): mdf T,
        *(n: mdf T): mdf T,
        /(n: mdf T): mdf T,
        %(n: mdf T): mdf T,
        **(n: mdf T): mdf T, // pow
          
        // bitwise
        >>(n: mdf T): mdf T,
        <<(n: mdf T): mdf T,
        ^(n: mdf T): mdf T,
        &(n: mdf T): mdf T,
        |(n: mdf T): mdf T,
          
        // Comparisons
        >(n: mdf T): Bool,
        <(n: mdf T): Bool,
        >=(n: mdf T): Bool,
        <=(n: mdf T): Bool,
        ==(n: mdf T): Bool,
        }
        
        // Fake concrete type for all numbers. The real implementation is generated at code-gen.
        _NumInstance:Num{
          .unum -> this.unum,
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
        _UNumInstance:UNum{
          .num -> this.num,
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
        
        Ref:{ #[X](x: mdf X): mut Ref[mdf X] -> this#(x) }
        Ref[X]:NoMutHyg[X],Sealed{
          read * : recMdf X,
          mut .swap(x: mdf X): mdf X,
          mut :=(x: mdf X): Void -> Let#{ .var -> this.swap(x), .in(_)->Void },
          mut <-(f: UpdateRef[mdf X]): mdf X -> this.swap(f#(this*)),
        }
        UpdateRef[X]:{ mut #(x: mdf X): mdf X }
      """;
  }
}
