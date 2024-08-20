package parser;

import failure.CompileError;
import main.Main;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import program.TypeSystemFeatures;
import utils.Err;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

class TestLiterals {
  void ok(String expected, String... content){
    Main.resetAll();
    AtomicInteger pi = new AtomicInteger();
    var ps = Arrays.stream(content)
        .map(code -> new Parser(Path.of("Dummy"+pi.getAndIncrement()+".fear"), code))
        .toList();
    String res = Parser.parseAll(ps, new TypeSystemFeatures()).toString();
    Err.strCmpFormat(expected,res);
  }
  void fail(String expectedErr, String... content){
    Main.resetAll();
    AtomicInteger pi = new AtomicInteger();
    var ps = Arrays.stream(content)
        .map(code -> new Parser(Path.of("Dummy"+pi.getAndIncrement()+".fear"), code))
        .toList();
    try {
      var res = Parser.parseAll(ps, new TypeSystemFeatures());
      Assertions.fail("Parsing did not fail. Got: "+res);
    }
    catch (CompileError e) {
      Err.strCmp(expectedErr, e.toString());
    }
  }

  @Test void uintAsTypeName(){ fail("""
    In position [###]/Dummy0.fear:2:0
    [E59 syntaxError]
    a.5 is not a valid type name.
    """,
    """
    package a
    5: {}
    """); }
  @Test void intAsTypeName(){ fail("""
    In position [###]/Dummy0.fear:2:0
    [E59 syntaxError]
    a.+5 is not a valid type name.
    """,
    """
    package a
    +5: {}
    """); }
  @Test void negativeIntAsTypeName(){ fail("""
    In position [###]/Dummy0.fear:2:0
    [E59 syntaxError]
    a.-5 is not a valid type name.
    """,
    """
    package a
    -5: {}
    """); }
  @Test void floatAsTypeName(){ fail("""
    In position [###]/Dummy0.fear:2:0
    [E59 syntaxError]
    a.5.43 is not a valid type name.
    """,
    """
    package a
    5.43: {}
    """); }
  @Test void negativeFloatAsTypeName(){ fail("""
    In position [###]/Dummy0.fear:2:0
    [E59 syntaxError]
    a.-5.43 is not a valid type name.
    """,
    """
    package a
    -5.43: {}
    """); }
}
