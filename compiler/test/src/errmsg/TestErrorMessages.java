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
    
    """,
    """
    package pkg1
    Bar:{)
    """
  );}

  @Test void failMultiLineCurly() { fail(
  """
    
    """,
  """
    package pkg1
    Bar:{
      Hello:{}
    )
    """
  );}
}
