package codegen.md;

import main.Main;
import org.junit.jupiter.api.Test;
import parser.Parser;
import program.inference.InferBodies;
import utils.Base;
import utils.Err;
import wellFormedness.WellFormednessFullShortCircuitVisitor;
import wellFormedness.WellFormednessShortCircuitVisitor;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class TestMarkdownDocgen {
  void ok(String expected, String entry, boolean loadBase, String... content) {
    assert content.length > 0;
    Main.resetAll();
    AtomicInteger pi = new AtomicInteger();
    String[] baseLibs = loadBase ? Base.baseLib : new String[0];
    var ps = Stream.concat(Arrays.stream(content), Arrays.stream(baseLibs))
      .map(code->new Parser(Path.of("Dummy" + pi.getAndIncrement() + ".fear"), code))
      .toList();
    var p = Parser.parseAll(ps);
    new WellFormednessFullShortCircuitVisitor().visitProgram(p).ifPresent(err->{
      throw err;
    });
    var inferredSigs = p.inferSignaturesToCore();
    var inferred = new InferBodies(inferredSigs).inferAll(p);
    new WellFormednessShortCircuitVisitor(inferred).visitProgram(inferred);
    inferred.typeCheck();
    var md = new MarkdownDocgen(inferred).visitProgram();
    Err.strCmp(expected, md.toString());
  }

  @Test void emptyProgram() { ok("""
    [TraitDoc[fileName=base.html, markdown=<h1><a href="base.html"><code>base</code></a></h1>
    <h2><a href="base.html#base.System"><code>base.System/1</code></a></h2>
    **Implements**: [`base.Sealed[]`](base.html#base.Sealed)
    **Type parameters**:
    - `R`
        
    <h2><a href="base.html#base.Sealed"><code>base.Sealed/0</code></a></h2>
        
    <h2><a href="base.html#base.NoMutHyg"><code>base.NoMutHyg/1</code></a></h2>
        
    **Type parameters**:
    - `X`
        
    <h2><a href="base.html#base.Void"><code>base.Void/0</code></a></h2>
        
    <h2><a href="base.html#base.Main"><code>base.Main/0</code></a></h2>
        
        
    <h3><em><a href="base.html#Fear14%24%2F0_%23%2F1"><code>#/1</code></em></a></h3>
    *Abstract*
        
    **Returns**: [`base.Void[]`](base.html#base.Void)
        
    **Parameters**:
    - [`s: base.System[imm base.Void[]]`](base.html#base.System)]]
    """, "fake.Fake", false, """
    package test
    """, Base.minimalBase);
  }
}
