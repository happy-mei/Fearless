package codegen.java;

import codegen.MIRInjectionVisitor;
import id.Id;
import main.Main;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import parser.Parser;
import program.inference.InferBodies;
import utils.Base;
import utils.RunJava;
import wellFormedness.WellFormednessFullShortCircuitVisitor;
import wellFormedness.WellFormednessShortCircuitVisitor;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import static utils.RunJava.Res;

public class TestJavaProgram {
  void ok(Res expected, String entry, String... content) {
    assert content.length > 0;
    Main.resetAll();
    AtomicInteger pi = new AtomicInteger();
    var ps = Arrays.stream(content)
      .map(code->new Parser(Path.of("Dummy" + pi.getAndIncrement() + ".fear"), code))
      .toList();
    var p = Parser.parseAll(ps);
    new WellFormednessFullShortCircuitVisitor().visitProgram(p).ifPresent(err->{
      throw err;
    });
    var inferredSigs = p.inferSignaturesToCore();
    var inferred = new InferBodies(inferredSigs).inferAll(p);
    new WellFormednessShortCircuitVisitor().visitProgram(inferred);
    inferred.typeCheck();
    var mir = new MIRInjectionVisitor(inferred).visitProgram();
    var java = new JavaCodegen(inferred).visitProgram(mir.pkgs(), new Id.DecId(entry, 0));
//    System.out.println(java);
    var res = RunJava.of(new JavaProgram(java).compile()).join();
    Assertions.assertEquals(expected, res);
  }

  @Test void emptyProgram() { ok(new Res("", "", 0), "test.Test", """
    package test
    alias base.Main as Main,
    Void:{}
    Test:Main[Void]{ _ -> {} }
    """, Base.minimalBase);}

  @Test void assertTrue() { ok(new Res("", "", 0), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main[Void]{ _ -> Assert#(True, { Void }) }
    """, Base.immBaseLib);}
  @Test void assertFalse() { ok(new Res("", "Assertion failed :(", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main[Void]{ _ -> Assert#(False, { Void }) }
    """, Base.immBaseLib);}
  @Test void assertFalseMsg() { ok(new Res("", "power level less than 9000", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main[Void]{ _ -> Assert#(False, "power level less than 9000", { Void }) }
    """, Base.immBaseLib);}
}
