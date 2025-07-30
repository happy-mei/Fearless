package codegen.js;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static codegen.js.RunJsProgramTests.ok;
import static utils.RunOutput.Res;

@Disabled
public class TestJsProgram {
  @Test void emptyProgram() { ok(new Res("", "", 0), """
    package test
    alias base.Main as Main,
    Test:Main{ _ -> "" }
    """);}
  @Test void assertTrue() { ok(new Res("", "", 0), """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    TestM:Main{ _ -> Assert!(True, { "" }) }
    """);}
//  @Test void assertFalse() { ok(new Res("", "Assertion failed :(", 1), """
//    package test
//    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
//    alias base.Void as Void,
//    Test:Main{ _ -> Assert!(False, { "" }) }
//    """);}
//  @Test void assertFalseMsg() { ok(new Res("", "power level less than 9000", 1), """
//    package test
//    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
//    alias base.Void as Void,
//    Test:Main{ _ -> Assert!(False, "power level less than 9000", { Void }) }
//    """);}
//  @Test void addition() { ok(new Res("", "7", 1), """
//    package test
//    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
//    alias base.Void as Void,
//    Test:Main{ _ -> Assert!(False, (5 + 2) .str, { "" }) }
//    """);}

//  @Test void arithmeticOrderOfOperations() {
//    ok(new Res("", "", 0), """
//    package test
//    Test:Main{ _ -> Block#
//      .do{ Assert!(1 + 2 * 3 == 9, "order of ops left-associative", {{}}) }
//      .do{ Assert!(1 + (2 * 3) == 7, "order of ops", {{}}) }
//      .do{ Assert!(10 - 3 * 2 == 14, "subtraction left-associative", {{}}) }
//      .do{ Assert!(8 / 2 + 1 == 5, "division and addition", {{}}) }
//      .return{{}}
//    }
//  """, Base.mutBaseAliases);
//  }
}
