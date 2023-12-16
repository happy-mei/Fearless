package codegen.java.tour;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import utils.Base;

import static utils.RunJava.Res;
import static codegen.java.RunJavaProgramTests.*;

public class Ex02CollectionsTest {
  @Disabled // TODO: No flows yet
  @Test void listNums() { ok(new Res("YAY!", "", 0), "test.Test", """
    package test
    Test:Main {sys -> FIO#sys.println(
      // List#[Int](5, 10, 15).str, how do we print these elements?
      List#[Int](5, 10, 15).flow
        .map{n -> n.str}
        #(Flow.join ", ")
      )}
    """, Base.mutBaseAliases);}
  @Test void addAndGetFromList() { ok(new Res("YAY!", "", 0), "test.Test", """
    package test
    Test:Main {sys -> Block#
      .var list = {List#[Str]}
      .do {list.add("YAY!")}
      .var yayOpt = {list.get(0u)}
//      .var[Str] yay = {yayOpt!}
      .return {FIO#sys.println(yayOpt!)}
      }
    """, Base.mutBaseAliases);}
  @Test void listSize() { ok(new Res("", "", 0), "test.Test", """
    package test
    Test:Main {sys -> Block#
      .var list = {List#[Int](5, 10, 15)}
      .do {list.add(20)}
      .assert({list.size == 4u})
      .return{Void}
      }
    """, Base.mutBaseAliases);}
}
