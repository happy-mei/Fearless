package utils;

import main.Main;
import parser.Parser;
import wellFormedness.WellFormednessFullShortCircuitVisitor;
import program.TypeSystemFeatures;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public interface FromContent {
  static astFull.Program of(String[] content){
    Main.resetAll();
    AtomicInteger pi = new AtomicInteger();
    var ps = Arrays.stream(content)
      .map(code -> new Parser(Path.of("Dummy"+pi.getAndIncrement()+".fear"), code))
      .toList();
    var p = Parser.parseAll(ps, TypeSystemFeatures.of());
    new WellFormednessFullShortCircuitVisitor()
      .visitProgram(p)
      .ifPresent(err->{ throw err; });
    return p;
  }
  static astFull.Program withTsf(TypeSystemFeatures tsf, String[] content){
    Main.resetAll();
    AtomicInteger pi = new AtomicInteger();
    var ps = Arrays.stream(content)
      .map(code -> new Parser(Path.of("Dummy"+pi.getAndIncrement()+".fear"), code))
      .toList();
    var p = Parser.parseAll(ps, tsf);
    new WellFormednessFullShortCircuitVisitor()
      .visitProgram(p)
      .ifPresent(err->{ throw err; });
    return p;
  }
}
