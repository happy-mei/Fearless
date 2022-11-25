package typing;

import main.CompileError;
import main.Main;
import org.junit.jupiter.api.Assertions;
import parser.Parser;
import program.Program;
import utils.Err;
import wellFormedness.WellFormednessFullShortCircuitVisitor;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class TestSubTyping {
  //TODO: we may want to move it to a more general Utils
  Program fromContent(String[] content){
    Main.resetAll();
    AtomicInteger pi = new AtomicInteger();
    var ps = Arrays.stream(content)
      .map(code -> new Parser(Path.of("Dummy"+pi.getAndIncrement()+".fear"), code))
      .toList();
    var p = Parser.parseAll(ps);
    new WellFormednessFullShortCircuitVisitor()
      .visitProgram(p)
      .ifPresent(err->{ throw err; });
    return p;
  }
  void ok(String expected, String... content){
    var p=fromContent(content);

    Err.strCmpFormat(expected, "TODO");
  }
  void fail(String expectedErr, String... content){
    Main.resetAll();
    AtomicInteger pi = new AtomicInteger();
    var ps = Arrays.stream(content)
      .map(code -> new Parser(Path.of("Dummy"+pi.getAndIncrement()+".fear"), code))
      .toList();
    var p = Parser.parseAll(ps);
    new WellFormednessFullShortCircuitVisitor().visitProgram(p).ifPresent(err->{ throw err; });

    try {
      // TODO: do subtyping
      Assertions.fail("Did not fail, got:\n");
    } catch (CompileError e) {
      Err.strCmp(expectedErr, e.toString());
    }
  }
}
