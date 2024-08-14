package tour;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import utils.Base;
import utils.RunOutput.Res;

import static tour.TourHelper.run;
import static codegen.java.RunJavaProgramTests.*;

public class Ex23ActionsTest {
  @Test void lazyAction() {ok(new Res("hi\nyes hi", "", 0), """
    package test
    alias base.Action as Action,
    
    Test: Main{sys -> Block#
      .do {sys.io.println("hi")}
      .do {Block#(Actions.lazy{sys.io.println("no hi")})}
      .do {Actions.lazy{sys.io.println("yes hi")}!}
      .return {{}}
      }
    """, Base.mutBaseAliases);}

  /* Method `Str.int` can be used to convert the string into an integer.
It returns a `mut Action[Int]`.
Actions are used to enforce that we check for an error condition.
Action is declared as follows in the standard library:
-------------------------*/@Disabled @Test void action() { run("""
  //sholuld a mut Action cache the result?
  MF[A,R]:{ mut #(a:A):R }
  Actions:{
    .some[E](e: E): mut Action[E]   -> {m-> m.some(e)},
    .info[E](i: Info): mut Action[E]-> {m-> m.info(i)},
    .info[E](kind: Str, msg: Str): mut Action[E]-> {m-> m.info(Info#(kind,msg))},//How to make info {kind:"..", msg:".."}
    }
  ActionMatch[E,R]:{ .some(e: E): R, .info(i: Info): R, }
  Action[E]:{
    mut .run[R] (m: mut ActionMatch[E,R]): R,
    mut .map[R'](f: mut MF[R,R']): Action[R']->this.run{//WRONG, eager!
      .some(e)->Actions.some(f#e),
      .info(i)->this,
      }
    mut .map[R'](f: mut MF[R,R']): Action[R']->{m->this.run{//RIGHT, lazy!
      .some(e)->m.some(f#e),
      .info(i)->m.info(i),
      }}
    mut !: E-> this.match{
      .some(e)-> e,
      .info(i)-> Error#(i),
      }
    }
    //showing how Str.int can be implemented
    _StrPrivate:{
      .intInfoStart(s: Str): Action[Int]->
        Action.info("Invalid Int",
          "Nees to start with +/-"
          + " but it starts with "
          + this.substring(0,1),
      .intInfoNoDigit(char: Num): Action[Int]->
        Action.info("Invalid Int",
          "Non digit character ".addChar(char)
          + " found.",
      .intOverflow(s: Str): Action[Int]->
        Action.info("Invalid Int", 
          (s.size>20?{
            .then-> "String of lenght " + s.size + " would encode"
            .else-> "String "+s+" encodes"
            })
           +" a number overflowing the Int representation",
      .int(s:Str): Action[Int] ->Block#//this works like an Either
        .if {s.size>20} .return {this.intOverflow(s)}
        .let pos = {s.startsWith("+")}
        .let neg = {s.startsWith("-")}
        .if {pos .or neg !} .return {this.intInfoStart(s)}
        .let start = neg ? {.then-> +0, .else-> -0} // Nick: this does not make sense
        .let s0 = {s.substring(1,s.size)}
        .let digits[List[Nat]] = {"0123456789".flow.list}
        .let err = {s0.flow.findFirst{c->digits.indexOf({e->e == c).isSome}}
        .if {err.isPresent} .return {this.intInfoNoDigit(err!)}
        .let res= {s0.flow
          .flatMap{c->digits.indexOf({e->e == c}).flow}//{e->e==c} converts ashii to int
          .fold(start,{acc,curr-> acc * +10 + curr.int})
          }
         .if {(res >= 0 .and neg) .or (res < 0 . and pos)}
           .return {this.intOverflow(s)}
         .return{res}
      }
    Str:{//..other code of string
      .int: Action[Int] ->{m->
        _StrPrivate.int(this).run{
          .some(e)-> m.some(e),
          .info(i)-> m.info(i), 
          }
        }
      }
    """); }/*--------------------------------------------
As you can see, Actions can be used both as a conventional Either
or as a lazy operation that can produce an informative message instead of failing.
Actions are our answer to Java checked exceptions: when something is not necessarily an observed bug, we can turn the operation into an Action.
Actions can be combined with `Action.map`.
Actions are intrinsically imperative, in the sense that they can capture mutable state and cause side effects when they `.run`.

This code also shows that our matcher methods are much more then
ADT matchers.*/
}
