package flows;

import org.junit.jupiter.api.Test;
import utils.Base;

import static codegen.java.RunJavaProgramTests.ok;
import static utils.RunOutput.Res;

public class ThesisExamplesTest {
  @Test void flowSemanticD1() {ok(new Res("a,b,c", "", 0), """
    package test
    alias base.Debug as D,
    Only3: {#: Str -> LList[Str] + "a" + "b" + "c" + "d"
      .flow
      .limit(3)
      .peek{e -> e == "d" ? {
        .then -> Error.msg "flow did not stop",
        .else -> {}
      }}
      .join ","
    }
    Test: Main{sys -> sys.io.println(Only3#)}
    """, Base.mutBaseAliases);}
  @Test void flowSemanticD1List() {ok(new Res("a,b,c", "", 0), """
    package test
    alias base.Debug as D,
    Only3: {#(list: read List[Str]): Str -> list
      .flow
      .limit(3)
      .peek{e -> e == "d" ? {
        .then -> Error.msg "flow did not stop",
        .else -> {}
      }}
      .join ","
    }
    Test: Main{sys -> Block#
      .let[mut List[Str]] list = {List#}
      .do {list.add "a"}
      .do {list.add "b"}
      .do {list.add "c"}
      .do {list.add "d"}
      .return {sys.io.println(Only3#list)}
      }
    """, Base.mutBaseAliases);}

  @Test void flowSemanticD2() {ok(new Res("c", "", 0), """
    package test
    alias base.Debug as D,
    Only3: {#: Str -> LList[Str] + "a" + "b" + "c" + "d"
      .flow
      .peek{e -> e == "d" ? {
        .then -> Error.msg "flow did not stop",
        .else -> {}
      }}
      .first{e -> e == "c"}!
    }
    Test: Main{sys -> sys.io.println(Only3#)}
    """, Base.mutBaseAliases);}

  @Test void internalIteratorFirstDoubleDigit() {ok(new Res("14", "", 0), """
    package test
    DoubleDigitOrHigher: {.first(nums: mut FlowOp[Nat]): mut Opt[Nat] ->
      Block#
        .var[mut Opt[Nat]] result = {{}}
        .do {nums.for{n -> n > 9 ? {
          .then -> Block#(result.set(Opts#n), False),
          .else -> True,
        }}}
        .return {result.get}
    }
    Test: Main{sys -> Block#
      .let[mut LList[Nat]] list = {mut LList[Nat] + 1 + 2 + 14 + 3 + 32}
      .let res = {DoubleDigitOrHigher.first(list.flowOp)}
      .do {sys.io.println(res!.str)}
      .return {{}}
      }
    """, """
    package test
    LList[E:imm,mut,read]: {
      mut .match[R:iso,imm,mut,mutH,read,readH](m: mut LListMatch[E,R]): R -> m.empty,
      mut .head: mut Opt[E] -> {},
      mut .tail: mut LList[E] -> this,
      read .isEmpty: Bool -> True,
      mut ++(other: mut LList[E]): mut LList[E] -> other,
      mut +(e: E): mut LList[E] -> this ++ (mut LList[E].pushFront(e)),
      mut .pushFront(e: E): mut LList[E] -> {
        .match(m) -> m.elem(this, e),
        .head -> Opts#e,
        .tail -> this,
        ++(other) -> (this ++ other).pushFront(e),
        .isEmpty -> False,
      },
      mut .iter: mut Iter[E] -> Block#
        .var[mut LList[E]] cursor = {this}
        .return {{.next -> cursor.swap(cursor.get.tail).head}},
      mut .flowOp: mut FlowOp[E] -> Block#
        .var[mut LList[E]] cursor = {this}
        .return {{'op
          .step(sink) -> cursor.swap(cursor.get.tail).head.match{
            .some(e) -> sink#(e) ? {
              .then -> {},
              .else -> cursor.set({}),
            },
            .empty -> {},
          },
          .for(sink) -> cursor.get.isEmpty.not ? {
            .then -> Block#(op.step(sink), op.for(sink)),
            .else -> {},
          },
        }},
    }
    LListMatch[T:imm,mut,read, R:iso,imm,mut,mutH,read,readH]: {
      mut .empty: R,
      mut .elem(list: mut LList[T], e: T): R,
    }
    FlowOp[E:imm,mut,read]: {
      mut .for(sink: mut Sink[E]): Void,
      mut .step(sink: mut Sink[E]): Void,
    }
    Sink[E:imm,mut,read]: {
      mut #(e: E): Bool,
    }
    """, """
    package test
    alias base.Opt as Opt,
    alias base.Opts as Opts,
    alias base.iter.Iter as Iter,
    alias base.Nat as Nat,
    alias base.Bool as Bool,
    alias base.True as True, alias base.False as False,
    alias base.Block as Block,
    alias base.Main as Main,
    alias base.Void as Void,
    """);}
}
