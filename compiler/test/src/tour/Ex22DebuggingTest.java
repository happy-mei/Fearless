package tour;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import utils.Base;
import utils.RunOutput.Res;

import static codegen.java.RunJavaProgramTests.ok;

public class Ex22DebuggingTest {
  @Disabled("30/11/2024")@Test void debugPrintNumber() { ok(new Res("30", "", 0), """
    package test
    alias base.Debug as Debug,
    Test:Main {sys -> Block#(Debug#[Nat]30)}
    """, Base.mutBaseAliases);}
  @Disabled("30/11/2024")@Test void debugPrintStringable() { ok(new Res("hi!", "", 0), """
    package test
    alias base.Debug as Debug,
    Test:Main {sys -> Block#(Debug#(Foo))}
    Foo: {read .str: Str -> "hi!"}
    """, Base.mutBaseAliases);}
  @Disabled("30/11/2024")@Test void debugPrintNonStringable() { ok(new Res("test.Foo/0", "", 0), """
    package test
    alias base.Debug as Debug,
    Test:Main {sys -> Block#(Debug#(Foo))}
    Foo: {read .notStr: Str -> "hi!"}
    """, Base.mutBaseAliases);}
}
