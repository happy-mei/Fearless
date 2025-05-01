package stdlib;

import org.junit.jupiter.api.Test;
import utils.Base;
import utils.RunOutput;

import static codegen.java.RunJavaProgramTests.ok;

public class TestBool {
  @Test void shouldShortCircuitTrueOr() {
    ok(new RunOutput.Res("", "", 0), """
    package test
    Test: Main{sys -> Block#(
      True || {Block#(sys.io.println "bad", True)}
      )}
    """, Base.mutBaseAliases);
  }
  @Test void shouldShortCircuitFalseAnd() {
    ok(new RunOutput.Res("", "", 0), """
    package test
    Test: Main{sys -> Block#(
      False && {Block#(sys.io.println "bad", True)}
      )}
    """, Base.mutBaseAliases);
  }

  @Test void shouldNotShortCircuitTrueAnd() {
    ok(new RunOutput.Res("good", "", 0), """
    package test
    Test: Main{sys -> Block#(
      True && {Block#(sys.io.println "good", True)}
      )}
    """, Base.mutBaseAliases);
  }
  @Test void shouldNotShortCircuitFalseOr() {
    ok(new RunOutput.Res("good", "", 0), """
    package test
    Test: Main{sys -> Block#(
      False || {Block#(sys.io.println "good", True)}
      )}
    """, Base.mutBaseAliases);
  }
}
