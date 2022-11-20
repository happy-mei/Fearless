package wellFormedness;

import main.Main;
import org.junit.jupiter.api.Test;
import parser.Parser;
import utils.Err;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestIntegrationWellFormedness {
  void ok(String expected, String... content){
    Main.resetAll();
    AtomicInteger pi = new AtomicInteger();
    var ps = Arrays.stream(content)
      .map(code -> new Parser(Path.of("Dummy"+pi.getAndIncrement()+".fear"), code))
      .toList();
    var decTable = Parser.parseAll(ps);
    var isWellFormed = decTable.values().stream()
      .map(d->d.accept(new WellFormednessVisitor()))
      .allMatch(Optional::isEmpty);
    assertTrue(isWellFormed);
  }
  void fail(String expectedErr, String... content){
    Main.resetAll();
    AtomicInteger pi = new AtomicInteger();
    var ps = Arrays.stream(content)
      .map(code -> new Parser(Path.of("Dummy"+pi.getAndIncrement()+".fear"), code))
      .toList();

    var decTable = Parser.parseAll(ps);
    var errors = decTable.values().stream()
      .map(d->d.accept(new WellFormednessVisitor()))
      .filter(Optional::isPresent)
      .map(Optional::get)
      .toList();
    Err.strCmp(expectedErr, errors.toString());
  }
  @Test void noIsoParamsLambda1() { fail("""
    [In position [###]/Dummy0.fear:2:2
    isoInTypeArgs:5
    The iso reference capability may not be used in type modifiers:
    iso pkg1.A[]]
    """, """
    package pkg1
    A:base.Opt[iso A]
    """); }
  @Test void noIsoParamsLambdaNested1() { fail("""
    [In position [###]/Dummy0.fear:2:2
    isoInTypeArgs:5
    The iso reference capability may not be used in type modifiers:
    iso pkg1.A[]]
    """, """
    package pkg1
    A:base.Opt[mut base.Opt[iso A]]
    """); }
  @Test void noIsoParamsLambdaNested2() { fail("""
    [In position [###]/Dummy0.fear:2:2
    isoInTypeArgs:5
    The iso reference capability may not be used in type modifiers:
    iso pkg1.A[]]
    """, """
    package pkg1
    A:base.Opt[base.Opt[A], base.Opt[base.Opt[iso A]]]
    """); }
}
