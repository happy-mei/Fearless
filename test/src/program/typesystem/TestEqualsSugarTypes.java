package program.typesystem;

import org.junit.jupiter.api.Test;

import static program.typesystem.RunTypeSystem.ok;

public class TestEqualsSugarTypes {
  private static final String LET = """
    package test
    Continuation[T,C,R]: {mut #(x: T, self: C): R}
    Let: {#[R]: mut Let[R] -> {}}
    Let[R]: {
      mut .let[T](x: mut MF[T], c: mut Continuation[T,mut Let[R],R]): R
        ->  c#(x#,this),
      mut .in(rv: mut MF[R]): R -> rv#,
      }
    MF[R]: {mut #: R}
    """;
  private static final String NUMS = """
    package test
    Num: {
      .pred: Num,
      +(b: Num): Num,
      *(b: Num): Num,
      .s: Num -> S{this},
      }
    Zero: Num{
      .pred -> Abort!,
      +(b) -> b,
      *(b) -> this,
      }
    S: Num{
      +(b) -> S{this.pred + b},
      *(b) -> b + (b * (this.pred)),
      }
    Abort: { ![R:readOnly,lent,read,mut,imm,iso]: R -> this! }
    """;

  @Test void let() { ok(LET); }
  @Test void letUsageDirectReturn() { ok("""
    package test
    Usage: {#: Num -> Let#
      .let ten = {(Zero.s.s.s.s.s) + (Zero.s.s.s.s.s)}
      .let y = {ten * (Zero.s.s)}
      .in {(y) + (ten)}//returns 30
      }
    """, LET, NUMS); }
}
