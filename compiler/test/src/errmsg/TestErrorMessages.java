package errmsg;

import failure.CompileError;
import main.Main;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import parser.Parser;
import program.TypeSystemFeatures;
import utils.Err;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class TestErrorMessages {
  void ok(String expected, String... content){
    Main.resetAll();
    AtomicInteger pi = new AtomicInteger();
    var ps = Arrays.stream(content)
      .map(code -> new Parser(Path.of("Dummy"+pi.getAndIncrement()+".fear"), code))
      .toList();
    String res = Parser.parseAll(ps, TypeSystemFeatures.of()).toString();
    Err.strCmpFormat(expected,res);
  }

  void fail(String expectedErr, String... content){
    Main.resetAll();
    AtomicInteger pi = new AtomicInteger();
    var ps = Arrays.stream(content)
      .map(code -> new Parser(Path.of("Dummy"+pi.getAndIncrement()+".fear"), code))
      .toList();
    try {
      var res = Parser.parseAll(ps, TypeSystemFeatures.of());
      Assertions.fail("Parsing did not fail. Got: "+res);
    }
    catch (CompileError e) {
      Err.strCmp(expectedErr, e.toString());
    }
  }

  @Test void failBasicCurly() { fail(
    """
    [###]Dummy0.fear:2:5
    [E59 syntaxError]
    Error: mismatched closing parenthesis ')' at 2:5
    2: Bar:{)
           ^^ mismatched close, is it meant to be '}'?
           |
           unclosed open
    """,
    """
    package pkg1
    Bar:{)
    """
  );}

  @Test void failMultiLineCurly() { fail(
  """
    [###]Dummy0.fear:4:0
    [E59 syntaxError]
    Error: mismatched closing parenthesis ')' at 4:0
    2  : Bar:{
             ^ unclosed open
    3  :   Hello:{}
    4  : )
         ^ mismatched close, is it meant to be '}'?
    """,
  """
    package pkg1
    Bar:{
      Hello:{}
    )
    """
  );}

  @Test void failLongMultiLineCurly() { fail(
    """
    [###]Dummy0.fear:9:0
    [E59 syntaxError]
    Error: mismatched closing parenthesis ')' at 9:0
    2  : Bar:{
             ^ unclosed open
    3  :   // Show this line
    4-7: ... ... ...
    8  :   Hello:{}  // Show this line
    9  : )
         ^ mismatched close, is it meant to be '}'?
    """,
  """
    package pkg1
    Bar:{
      // Show this line
      // Hide these lines
      //
      //
      //
      Hello:{}  // Show this line
    )
    """
  );}
}
