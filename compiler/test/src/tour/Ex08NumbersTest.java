package tour;

import org.junit.jupiter.api.Test;
import utils.Base;
import utils.RunOutput;

import static codegen.java.RunJavaProgramTests.ok;

public class Ex08NumbersTest {
  @Test void unsignedUnderflowOverFlow() { ok(new RunOutput.Res("45", "", 0), """
    package test
    Test:Main {sys -> FIO#sys.println(((15 - 20) + 50).str)}
    """, Base.mutBaseAliases);}

  @Test void intDivByZero() { ok(new RunOutput.Res("", "Program crashed with: / by zero", 1), """
    package test
    Test:Main {sys -> Block#(+5 / +0) }
    """, Base.mutBaseAliases);}
  @Test void uIntDivByZero() { ok(new RunOutput.Res("", "Program crashed with: / by zero", 1), """
    package test
    Test:Main {sys -> Block#(5 / 0) }
    """, Base.mutBaseAliases);}
  @Test void floatDivByZero() { ok(new RunOutput.Res("", "", 0), """
    package test
    Test:Main {sys -> Assert!((5.0 / 0.0).isInfinite) }
    """, Base.mutBaseAliases);}
}
