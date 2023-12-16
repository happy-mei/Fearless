package codegen.java.tour;

import org.junit.jupiter.api.Test;
import utils.Base;
import utils.RunJava;

import static codegen.java.RunJavaProgramTests.ok;

public class Ex08NumbersTest {
  @Test void unsignedUnderflowOverFlow() { ok(new RunJava.Res("45", "", 0), "test.Test", """
    package test
    Test:Main {sys -> FIO#sys.println(((15u - 20u) + 50u).str)}
    """, Base.mutBaseAliases);}

  @Test void intDivByZero() { ok(new RunJava.Res("", "Program crashed with: / by zero", 1), "test.Test", """
    package test
    Test:Main {sys -> Block#(5 / 0) }
    """, Base.mutBaseAliases);}
  @Test void uIntDivByZero() { ok(new RunJava.Res("", "Program crashed with: / by zero", 1), "test.Test", """
    package test
    Test:Main {sys -> Block#(5u / 0u) }
    """, Base.mutBaseAliases);}
  @Test void floatDivByZero() { ok(new RunJava.Res("", "", 0), "test.Test", """
    package test
    Test:Main {sys -> Assert!((5.0 / 0.0).isInfinite) }
    """, Base.mutBaseAliases);}
}
