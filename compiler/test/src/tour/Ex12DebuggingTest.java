package tour;

import org.junit.jupiter.api.Test;
import utils.Base;
import utils.RunOutput;

import static codegen.java.RunJavaProgramTests.ok;

public class Ex12DebuggingTest {
  @Test void debugPrintNumber() { ok(new RunOutput.Res("30", "", 0), """
    package test
    alias base.Debug as Debug,
    Test:Main {sys -> Block#(Debug#[Nat]30)}
    """, Base.mutBaseAliases);}
  @Test void debugPrintStringable() { ok(new RunOutput.Res("hi!", "", 0), """
    package test
    alias base.Debug as Debug,
    Test:Main {sys -> Block#(Debug#(Foo))}
    Foo: {read .str: Str -> "hi!"}
    """, Base.mutBaseAliases);}
  @Test void debugPrintNonStringable() { ok(new RunOutput.Res("Foo_0Impl[]", "", 0), """
    package test
    alias base.Debug as Debug,
    Test:Main {sys -> Block#(Debug#(Foo))}
    Foo: {read .notStr: Str -> "hi!"}
    """, Base.mutBaseAliases);}
}
