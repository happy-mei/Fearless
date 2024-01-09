package codegen.java.tour;

import org.junit.jupiter.api.Test;
import utils.Base;

import static codegen.java.RunJavaProgramTests.ok;
import static utils.RunJava.Res;

public class Ex10ImperativeTest {
  @Test void ifTerminatesTrue() { ok(new Res("hi\nyay", "", 0), "test.Test", """
    package test
    Test:Main {sys -> Block#
      .if {True} .do {FIO#sys.println("hi")}
      .do {FIO#sys.println("yay")}
      .return {Void}
      }
    """, Base.mutBaseAliases); }
  @Test void ifTerminatesFalse() { ok(new Res("yay", "", 0), "test.Test", """
    package test
    Test:Main {sys -> Block#
      .if {False} .do {FIO#sys.println("hi")}
      .do {FIO#sys.println("yay")}
      .return {Void}
      }
    """, Base.mutBaseAliases); }
}
