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

  static String immBaseLib = immBaseLib("base");
  static String immBaseLib(String pkg) {
    return "package " + pkg + """
      
      Void:{}
      NoMutHyg[X]:{}
      Sealed:{}
      Main:{ #[R](s: lent System): R }
      System:{} // Root capability
          
      Num:{}
      UNum:{}
          
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
      OptDo[T]:OptMatch[T,Void]{
        #(t:T):Void,   //#[R](t:T):R,
        .some(x) -> Opt#(this._doRes(this#x, x)),
        .none->{},
        ._doRes(y:Void,x:T):Opt[T]->Opt#x
        }
      Opt:{ #[T](x: T): Opt[T] -> { .match(m)->m.some(x)} }
      
      Stringable:{
        .str: Str,
      }
          
      // Nums
      Num:Sealed,MathOpts[Num],Stringable{
        .str: Str,
        //.unum: UNum
        }
          
      UNum:MathOps[UNum],Sealed,Stringable{
        .str: Str,
        .num: Num
        }
          
      MathOps[T]:{
        +(n: mdf T): mdf T,
        -(n: mdf T): mdf T,
        *(n: mdf T): mdf T,
        /(n: mdf T): mdf T,
        %(n: mdf T): mdf T
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
        
        _NumInstance:Num:{
          
          }
      """;
  }
}
