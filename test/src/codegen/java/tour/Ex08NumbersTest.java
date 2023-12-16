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
}
