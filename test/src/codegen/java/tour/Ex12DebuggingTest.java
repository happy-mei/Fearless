package codegen.java.tour;

import org.junit.jupiter.api.Test;
import utils.Base;
import utils.RunJava;

import static codegen.java.RunJavaProgramTests.ok;

public class Ex12DebuggingTest {
  @Test void debugPrintNumber() { ok(new RunJava.Res("30", "", 0), "test.Test", """
    package test
    alias base.Debug as Debug,
    Test:Main {sys -> Block#(Debug#30)}
    """, Base.mutBaseAliases);}
  @Test void debugPrintStringable() { ok(new RunJava.Res("hi!", "", 0), "test.Test", """
    package test
    alias base.Debug as Debug,
    Test:Main {sys -> Block#(Debug#(Foo))}
    Foo: {read .str: Str -> "hi!"}
    """, Base.mutBaseAliases);}
  @Test void debugPrintNonStringable() { ok(new RunJava.Res("[###]$$Impl$Foo$0$$[]", "", 0), "test.Test", """
    package test
    alias base.Debug as Debug,
    Test:Main {sys -> Block#(Debug#(Foo))}
    Foo: {read .notStr: Str -> "hi!"}
    """, Base.mutBaseAliases);}
}
