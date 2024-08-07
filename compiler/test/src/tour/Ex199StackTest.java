package tour;

import org.junit.jupiter.api.Test;

import static tour.TourHelper.run;

public class Ex199StackTest {
  @Test void simpleStack() {run("""
    Stack[T:imm]: {
      .match[R](m: StackMatch[T,R]): R -> m.empty,
      +(e: T): Stack[T] -> { .match(m) -> m.elem(e, this) },
      }
    StackMatch[T:imm,R]: {
      .empty: R,
      .elem(top: T, tail: Stack[T]): R
      }
    
    Example: {
      .sum(ns: Stack[Nat]): Nat -> ns.match{
        .empty -> 0,
        .elem(top, tail) -> top + ( this.sum(tail) )
        }
      }
    
    Test: Main{sys -> sys.io.println(
      Example
        .sum(Stack[Nat] + 1 + 2 + 3)
        .str
      )}
    //prints 6
    """);}

  @Test void simpleStackOrder() {run("""
    Stack[T:imm]: {
      .match[R](m: StackMatch[T,R]): R -> m.empty,
      +(e: T): Stack[T] -> { .match(m) -> m.elem(e, this) },
      }
    StackMatch[T:imm,R]: {
      .empty: R,
      .elem(top: T, tail: Stack[T]): R
      }
    
    Example: {
      .print(ns: Stack[Nat]): Str -> ns.match{
        .empty -> "",
        .elem(top, tail) -> top.str + "," + ( this.print(tail) )
        }
      }
    
    Test: Main{sys -> sys.io.println(
      Example.print(Stack[Nat] + 1 + 2 + 3)
      )}
    //prints 3,2,1,
    """);}

  @Test void processDishes() {run("""
    Plate: {
      .id: Str,
      .str: Str,
      .clean: Clean -> {this.id},
      }
    Dirty: Plate{.str -> "Dirty " + (this.id)}
    Clean: Plate{.str -> "Clean " + (this.id)}
    
    Stack[T:imm]: {
      .match[R](m: StackMatch[T,R]): R -> m.empty,
      .process[R:imm](f: F[T, R]): Stack[R] -> {}, //empty stack as result of processing an empty stack
      +(e: T): Stack[T] -> {
        .match(m) -> m.elem(e, this),
        .process(f) -> this.process(f) + ( f#(e) ),
        },
      .str(toStr: F[T,Str]): Str -> this.match{
        .empty -> "",
        .elem(top, tail) -> toStr#top + "," + ( tail.str(toStr) )
        },
      }
    StackMatch[T:imm, R]: {
      .empty: R,
      .elem(top:T, tail: Stack[T]): R
    }
    
    // Example: {#(ds: Stack[Dirty]): Stack[Clean] -> ds.process{::clean}}
    Example: {#(ds: Stack[Dirty]): Stack[Clean] -> ds.process{dirty -> dirty.clean}}
    
    Test: Main{sys -> sys.io.println(
      Example#(Stack[Dirty] + Dirty{"a"} + Dirty{"b"} + Dirty{"c"}).str{p -> p.str}
      )}
    // note that the order was not reversed as you would typically expect from a stack traversal
    //prints Clean c,Clean b,Clean a,
    """);}

  @Test void processDishesTailRecursive() {run("""
    Plate: {
      .id: Str,
      .str: Str,
      .clean: Clean -> {this.id},
      }
    Dirty: Plate{.str -> "Dirty " + (this.id)}
    Clean: Plate{.str -> "Clean " + (this.id)}
    
    Stack[T:imm]: {
      .match[R](m: StackMatch[T,R]): R -> m.empty,
      .process[R:imm](f: F[T, R]): Stack[R] -> this.process(f,{}),
      .process[R:imm](f: F[T, R], acc: Stack[R]): Stack[R] -> acc,
      +(e: T): Stack[T] -> {
        .match(m) -> m.elem(e, this),
        .process(f, acc) -> this.process(f, acc + ( f#(e) )),
        },
      .str(toStr: F[T,Str]): Str -> this.match{
        .empty -> "",
        .elem(top, tail) -> toStr#top + "," + ( tail.str(toStr) )
        },
      }
    StackMatch[T:imm, R]: {
      .empty: R,
      .elem(top:T, tail: Stack[T]): R
    }
    
    // Example: {#(ds: Stack[Dirty]): Stack[Clean] -> ds.process{::clean}}
    Example: {#(ds: Stack[Dirty]): Stack[Clean] -> ds.process{dirty -> dirty.clean}}
    
    Test: Main{sys -> sys.io.println(
      Example#(Stack[Dirty] + Dirty{"a"} + Dirty{"b"} + Dirty{"c"}).str{p -> p.str}
      )}
    // note that the order was reversed, as you would expect from processing a stack
    //prints Clean a,Clean b,Clean c,
    """);}

  @Test void processDishesTailComputationObject() {run("""
    Plate: {
      .id: Str,
      .str: Str,
      .clean: Clean -> {this.id},
      }
    Dirty: Plate{.str -> "Dirty " + (this.id)}
    Clean: Plate{.str -> "Clean " + (this.id)}
    
    Stack[T:imm]: {
      .match[R](m: StackMatch[T,R]): R -> m.empty,
      .process[R:imm](f: F[T, R]): Stack[R] -> {'comp
        #(current: Stack[T], acc: Stack[R]): Stack[R] -> current.match{
          .empty -> acc,
          .elem(top, tail) -> comp#(tail, acc + ( f#(top) ))
          },
        }#(this,{}),
      +(e: T): Stack[T] -> {
        .match(m) -> m.elem(e, this),
        },
      .str(toStr: F[T,Str]): Str -> this.match{
        .empty -> "",
        .elem(top, tail) -> toStr#top + "," + ( tail.str(toStr) )
        },
      }
    StackMatch[T:imm, R]: {
      .empty: R,
      .elem(top:T, tail: Stack[T]): R
      }
    
    // Example: {#(ds: Stack[Dirty]): Stack[Clean] -> ds.process{::clean}}
    Example: {#(ds: Stack[Dirty]): Stack[Clean] -> ds.process{dirty -> dirty.clean}}
    
    Test: Main{sys -> sys.io.println(
      Example#(Stack[Dirty] + Dirty{"a"} + Dirty{"b"} + Dirty{"c"}).str{p -> p.str}
      )}
    //note that the order was reversed, as you would expect from processing a stack
    //prints Clean a,Clean b,Clean c,
    """);}

  @Test void processDishesTailComputationObjectExplicit() {run("""
    Plate: {
      .id: Str,
      .str: Str,
      .clean: Clean -> {this.id},
      }
    Dirty: Plate{.str -> "Dirty " + (this.id)}
    Clean: Plate{.str -> "Clean " + (this.id)}
    
    Stack[T:imm]: {
      .match[R](m: StackMatch[T,R]): R -> m.empty,
      .process[R:imm](f: F[T, R]): Stack[R] -> StackProcessComputationObj[T:imm,R:imm]: {'comp
        #(current: Stack[T], acc: Stack[R]): Stack[R] -> current.match{
          .empty -> acc,
          .elem(top, tail) -> comp#(tail, acc + ( f#(top) ))
          },
        }#[](this,{}),
      +(e: T): Stack[T] -> {
        .match(m) -> m.elem(e, this),
        },
      .str(toStr: F[T,Str]): Str -> this.match{
        .empty -> "",
        .elem(top, tail) -> toStr#top + "," + ( tail.str(toStr) )
        },
      }
    StackMatch[T:imm, R]: {
      .empty: R,
      .elem(top:T, tail: Stack[T]): R
      }
    
    // Example: {#(ds: Stack[Dirty]): Stack[Clean] -> ds.process{::clean}}
    Example: {#(ds: Stack[Dirty]): Stack[Clean] -> ds.process{dirty -> dirty.clean}}
    
    Test: Main{sys -> sys.io.println(
      Example#(Stack[Dirty] + Dirty{"a"} + Dirty{"b"} + Dirty{"c"}).str{p -> p.str}
      )}
    //note that the order was reversed, as you would expect from processing a stack
    //prints Clean a,Clean b,Clean c,
    """);}

  @Test void processDishesTailPrivate() {run("""
    Plate: {
      .id: Str,
      .str: Str,
      .clean: Clean -> {this.id},
      }
    Dirty: Plate{.str -> "Dirty " + (this.id)}
    Clean: Plate{.str -> "Clean " + (this.id)}
    
    Stack[T:imm]: {
      .match[R](m: StackMatch[T,R]): R -> m.empty,
      .process[R:imm](f: F[T, R]): Stack[R] -> {}, //empty stack as result of processing an empty stack
      +(e: T): Stack[T] -> {'self
        .match(m) -> m.elem(e, this),
        .process[R:imm](f: F[T, R]): Stack[R] -> self.processAux[R](self,f,Stack[R]), //dynamic dispatch to call a private method
        .processAux[R:imm](current: Stack[T], f: F[T, R], acc: Stack[R]): Stack[R] -> current.match{
          .empty -> acc,
          .elem(top, tail) -> self.processAux[R](tail, f, acc + ( f#(top) ))
          },
        },
      .str(toStr: F[T,Str]): Str -> this.match{
        .empty -> "",
        .elem(top, tail) -> toStr#top + "," + ( tail.str(toStr) )
        },
      }
    StackMatch[T:imm, R]: {
      .empty: R,
      .elem(top:T, tail: Stack[T]): R
    }
    
    // Example: {#(ds: Stack[Dirty]): Stack[Clean] -> ds.process{::clean}}
    Example: {#(ds: Stack[Dirty]): Stack[Clean] -> ds.process{dirty -> dirty.clean}}
    
    Test: Main{sys -> sys.io.println(
      Example#(Stack[Dirty] + Dirty{"a"} + Dirty{"b"} + Dirty{"c"}).str{p -> p.str}
      )}
    // note that the order was reversed, as you would expect from processing a stack
    //prints Clean a,Clean b,Clean c,
    """);}
}
