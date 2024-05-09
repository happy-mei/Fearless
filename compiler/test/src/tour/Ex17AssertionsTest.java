package tour;

import org.junit.jupiter.api.Test;
import utils.RunOutput;
import utils.RunOutput.Res;

import static codegen.java.RunJavaProgramTests.ok;

public class Ex17AssertionsTest {
  @Test void strAssertions() { ok(new Res("", "", 0), """
    package test
    alias base.Main as Main,
    alias base.Int as Int, alias base.Nat as Nat, alias base.Float as Float,
    alias base.Str as Str,
    
    Test: Main{_ -> "a".assertEq("a")}
    """);}
  @Test void strAssertionsFail() { ok(new RunOutput.Res("", """
    Expected: a
    Actual: b
    """, 1), """
    package test
    alias base.Main as Main,
    alias base.Int as Int, alias base.Nat as Nat, alias base.Float as Float,
    alias base.Str as Str,
    
    Test: Main{_ -> "a".assertEq("b")}
    """);}
  @Test void strAssertionsFailWithMessage() { ok(new RunOutput.Res("", """
    oh no
    Expected: a
    Actual: b
    """, 1), """
    package test
    alias base.Main as Main,
    alias base.Int as Int, alias base.Nat as Nat, alias base.Float as Float,
    alias base.Str as Str,
    
    Test: Main{_ -> "a".assertEq("oh no", "b")}
    """);}


  @Test void intAssertions() { ok(new RunOutput.Res("", "", 0), """
    package test
    alias base.Main as Main,
    alias base.Int as Int, alias base.Nat as Nat, alias base.Float as Float,
    alias base.Str as Str,
    
    Test: Main{_ -> (+5).assertEq(+5)}
    """);}
  @Test void intAssertionsFail() { ok(new RunOutput.Res("", """
    Expected: 5
    Actual: 10
    """, 1), """
    package test
    alias base.Main as Main,
    alias base.Int as Int, alias base.Nat as Nat, alias base.Float as Float,
    alias base.Str as Str,
    
    Test: Main{_ -> (+5).assertEq(+10)}
    """);}
  @Test void intAssertionsFailWithMessage() { ok(new RunOutput.Res("", """
    oh no
    Expected: 5
    Actual: 10
    """, 1), """
    package test
    alias base.Main as Main,
    alias base.Int as Int, alias base.Nat as Nat, alias base.Float as Float,
    alias base.Str as Str,
    
    Test: Main{_ -> (+5).assertEq("oh no", +10)}
    """);}

  @Test void natAssertions() { ok(new RunOutput.Res("", "", 0), """
    package test
    alias base.Main as Main,
    alias base.Int as Int, alias base.Nat as Nat, alias base.Float as Float,
    alias base.Str as Str,
    
    Test: Main{_ -> (5).assertEq(5)}
    """);}
  @Test void natAssertionsFail() { ok(new RunOutput.Res("", """
    Expected: 5
    Actual: 10
    """, 1), """
    package test
    alias base.Main as Main,
    alias base.Int as Int, alias base.Nat as Nat, alias base.Float as Float,
    alias base.Str as Str,
    
    Test: Main{_ -> (5).assertEq(10)}
    """);}
  @Test void natAssertionsFailWithMessage() { ok(new RunOutput.Res("", """
    oh no
    Expected: 5
    Actual: 10
    """, 1), """
    package test
    alias base.Main as Main,
    alias base.Int as Int, alias base.Nat as Nat, alias base.Float as Float,
    alias base.Str as Str,
    
    Test: Main{_ -> (5).assertEq("oh no", 10)}
    """);}

  @Test void floatAssertions() { ok(new RunOutput.Res("", "", 0), """
    package test
    alias base.Main as Main,
    alias base.Int as Int, alias base.Nat as Nat, alias base.Float as Float,
    alias base.Str as Str,
    
    Test: Main{_ -> (5.23).assertEq(5.23)}
    """);}
  @Test void floatAssertionsFail() { ok(new RunOutput.Res("", """
    Expected: 5.23
    Actual: 5.64
    """, 1), """
    package test
    alias base.Main as Main,
    alias base.Int as Int, alias base.Nat as Nat, alias base.Float as Float,
    alias base.Str as Str,
    
    Test: Main{_ -> (5.23).assertEq(5.64)}
    """);}
  @Test void floatAssertionsFailWithMessage() { ok(new RunOutput.Res("", """
    oh no
    Expected: 5.23
    Actual: 5.64
    """, 1), """
    package test
    alias base.Main as Main,
    alias base.Int as Int, alias base.Nat as Nat, alias base.Float as Float,
    alias base.Str as Str,
    
    Test: Main{_ -> (5.23).assertEq("oh no", 5.64)}
    """);}
}
