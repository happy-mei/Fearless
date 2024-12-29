package flows;

import org.junit.jupiter.api.Test;
import utils.Base;

import static codegen.java.RunJavaProgramTests.ok;
import static utils.RunOutput.Res;

public class ThesisExamplesTest {
  @Test void flowSemanticD1() {ok(new Res("a,b,c", "", 0), """
    package test
    alias base.Debug as D,
    Only3: {#: Str -> LList[Str] + "a" + "b" + "c" + "d"
      .flow
      .limit(3)
      .peek{e -> e == "d" ? {
        .then -> Error.msg "flow did not stop",
        .else -> {}
      }}
      .join ","
    }
    Test: Main{sys -> sys.io.println(Only3#)}
    """, Base.mutBaseAliases);}
  @Test void flowSemanticD1List() {ok(new Res("a,b,c", "", 0), """
    package test
    alias base.Debug as D,
    Only3: {#(list: read List[Str]): Str -> list
      .flow
      .limit(3)
      .peek{e -> e == "d" ? {
        .then -> Error.msg "flow did not stop",
        .else -> {}
      }}
      .join ","
    }
    Test: Main{sys -> Block#
      .let[mut List[Str]] list = {List#}
      .do {list.add "a"}
      .do {list.add "b"}
      .do {list.add "c"}
      .do {list.add "d"}
      .return {sys.io.println(Only3#list)}
      }
    """, Base.mutBaseAliases);}

  @Test void flowSemanticD2() {ok(new Res("c", "", 0), """
    package test
    alias base.Debug as D,
    Only3: {#: Str -> LList[Str] + "a" + "b" + "c" + "d"
      .flow
      .peek{e -> e == "d" ? {
        .then -> Error.msg "flow did not stop",
        .else -> {}
      }}
      .first{e -> e == "c"}!
    }
    Test: Main{sys -> sys.io.println(Only3#)}
    """, Base.mutBaseAliases);}
}
