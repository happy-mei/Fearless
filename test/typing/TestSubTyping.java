package typing;

import failure.CompileError;
import id.Mdf;
import net.jqwik.api.Example;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import parser.Parser;
import program.Program;
import utils.Err;
import utils.FromContent;

public class TestSubTyping {
  void ok(String t1, String t2, boolean res, String ...code){
    var ty1 = new Parser(Parser.dummy, t1).parseFullT();
    var ty2 = new Parser(Parser.dummy, t2).parseFullT();
    Program p = FromContent.of(code);
    Assertions.assertEquals(res,p.isSubType(ty1, ty2), String.format("t1: %s\nt2: %s", ty1, ty2));
  }

  void fail(String expected, String t1, String t2, String ...code){
    var ty1 = new Parser(Parser.dummy, t1).parseFullT();
    var ty2 = new Parser(Parser.dummy, t2).parseFullT();
    Program p = FromContent.of(code);
    try {
      p.isSubType(ty1, ty2);
      Assertions.fail("Expected failure");
    } catch (CompileError e) {
      Err.strCmp(expected, e.toString());
    }
  }

  @Property public void mdfIsCommonSupertype(@ForAll Mdf mdf) {;
    ok(mdf+" a.A", "read a.A", true, "package a\nA:{}");
  }

  @Property public void isoIsCommonSubtype(@ForAll Mdf mdf) {
    ok("iso a.A", mdf+" a.A", true, "package a\nA:{}");
  }

  @Example void reflSub() { ok("a.A","a.A",true,"package a\nA:{}"); }
  @Example void noDSub() { ok("a.A","a.B",false,"package a\nA:{} B:{}"); }
  @Example void directSub() { ok("a.A","a.B",true,"package a\nA:a.B{} B:{}"); }
  @Example void inverseDirectSub() { ok("a.B","a.A",false,"package a\nA:a.B{} B:{}"); }
  @Example void reflXSub() { ok("X","X",true,"package a\nA:a.B{} B:{}"); }

  @Example void directSubMdf() { ok("a.A","read a.A",true,"package a\n A:{}"); }
  @Example void inverseDirectSubMdf() { ok("read a.A","a.A",false,"package a\n A:{}"); }
  @Example void noSubMdf() { ok(
    "mut a.A","imm a.A",false,
    """
    package a
    A:{}
    """); }
  @Example void inverseTransitiveSub() { ok(
    "a.A","a.C",false,
    """
    package a
    A:{}
    B:A{}
    C:B{}
    """); }
  @Example void transitiveSub() { ok(
    "a.C","a.A",true,
    """
    package a
    A:{} B:A C:B
    """); }
  @Example void transitiveManyStepsSub() { ok(
    "a.E","a.A",true,
    """
    package a
    A:{} B:A C:F,B,G{} D:C E:D F:{} G:{}
    """); }
  @Example void inverseTransitiveManyStepsSub() { ok(
    "a.A","a.E",false,
    """
    package a
    A:{} B:A C:B D:C E:D
    """); }
  @Example void loopingAdapt() { fail("""
    [E25 circularSubType]
    There is a cyclical sub-typing relationship between imm a.Break[imm A] and imm a.Break[imm B].
    """, "a.Break[A]", "a.Break[B]", """
    package a
    A:B{ .m: Break[A] }
    B:{ .m: Break[B] }
    Break[X]:{ .b: Break[X] }
    // Break[A]:{ .self: Break[B], .b: Break[A] -> this.self.b } // loop
    """); }

  final String pointEx = """
    package a
    List[T]:{
      read .get: recMdf T
    }
    SortedList[T]:List[mdf T]
    Int:{}
    Point:{ .x: Int, .y: Int }
    ColouredPoint:Point{ .colour: Int }
    """;
  @Example void sortedListOfTExtendsListTOfT() { ok("a.SortedList[a.Int]","a.List[a.Int]",true,pointEx); }
  @Example void sortedListOfTExtendsListTOfTMdfFail() { ok("a.SortedList[a.Int]","a.List[mut a.Int]",false,pointEx); }
  @Example void sortedListOfTExtendsListTOfTMdf() { ok("a.SortedList[a.Int]","a.List[read a.Int]",true,pointEx); }
  @Example void sortedListOfTExtendsListTOfTMdfReflexive() { ok("a.SortedList[read a.Int]","a.List[a.Int]",true,pointEx); }
  @Example void sortedListOfTExtendsListTOfX() { ok("a.SortedList[X]","a.List[X]",true,pointEx); }
  @Example void sortedListOfTExtendsListTOfNot1() { ok("a.SortedList[a.Int]","a.List[X]",false,pointEx); }
  @Example void sortedListOfTExtendsListTOfNot2() { ok("a.SortedList[X]","a.List[a.Int]",false,pointEx); }
  @Example void sortedListOfTExtendsListTOfNot3() { ok("a.SortedList[X]","a.List[a.List[a.Int]]",false,pointEx); }
  @Example void sortedListMixedGens() { ok("a.SortedList[a.ColouredPoint]","a.SortedList[a.Point]",true,pointEx); }
  @Example void inverseSortedListMixedGens() { ok("a.SortedList[a.Point]","a.SortedList[a.ColouredPoint]",false,pointEx); }
}
