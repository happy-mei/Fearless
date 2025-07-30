package codegen.js;

import codegen.MIRInjectionVisitor;
import id.Id;
import main.Main;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import parser.Parser;
import program.TypeSystemFeatures;
import program.inference.InferBodies;
import program.typesystem.TsT;
import utils.Base;
import wellFormedness.WellFormednessFullShortCircuitVisitor;
import wellFormedness.WellFormednessShortCircuitVisitor;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static utils.RunOutput.Res;

@Disabled
public class RunJsProgramTests {

  public static void ok(Res expected, String... content) {
    okWithArgs(expected, List.of(), content);
  }

  public static void okWithArgs(Res expected, List<String> args, String... content) {
    assert content.length > 0;
    Main.resetAll();
    AtomicInteger pi = new AtomicInteger();

    // Parse source + base
    var parsers = Stream.concat(Arrays.stream(content), Arrays.stream(Base.immBaseLib))
      .map(code -> new Parser(Path.of("Dummy" + pi.getAndIncrement() + ".fear"), code))
      .toList();

    var parsed = Parser.parseAll(parsers, new TypeSystemFeatures());

    new WellFormednessFullShortCircuitVisitor().visitProgram(parsed).ifPresent(err -> {
      throw new RuntimeException(err);
    });

    var inferred = InferBodies.inferAll(parsed);
    new WellFormednessShortCircuitVisitor(inferred).visitProgram(inferred);

    ConcurrentHashMap<Long, TsT> resolvedCalls = new ConcurrentHashMap<>();
    inferred.typeCheck(resolvedCalls);

    var mir = new MIRInjectionVisitor(List.of(), inferred, resolvedCalls).visitProgram();
    var jsCodegen = new JsCodegen(mir);

    String js = jsCodegen.visitProgram(new Id.DecId("test.Test", 0));

    try {
      Path jsFile = Paths.get("test-output.js");
      Files.writeString(jsFile, js);

      Process process = new ProcessBuilder("node", jsFile.toAbsolutePath().toString())
        .redirectErrorStream(true)
        .start();

      String output = new String(process.getInputStream().readAllBytes()).trim();
      int exitCode = process.waitFor();

      if (exitCode != expected.exitCode()) {
        System.err.println("Expected exit code: " + expected.exitCode());
        System.err.println("Actual exit code: " + exitCode);
        System.err.println("Output:\n" + output);
        Assertions.fail("Exit code mismatch");
      }

      // Compare output
      String expectedOutput = expected.stdOut();
      if (exitCode != 0) {
        // For assertion failures, match the error message
        expectedOutput = expected.stdErr();
      }
      Assertions.assertEquals(expectedOutput, output, "Output mismatch");

    } catch (Exception e) {
      throw new RuntimeException("Failed to execute JS: " + e.getMessage(), e);
    }
  }

  public static void fail(String expectedErr, String... content) {
    failWithArgs(expectedErr, List.of(), content);
  }

  public static void failWithArgs(String expectedErr, List<String> args, String... content) {
    assert content.length > 0;
    Main.resetAll();
    AtomicInteger pi = new AtomicInteger();

    var parsers = Stream.concat(Arrays.stream(content), Arrays.stream(Base.immBaseLib))
      .map(code -> new Parser(Path.of("Dummy" + pi.getAndIncrement() + ".fear"), code))
      .toList();

    var parsed = Parser.parseAll(parsers, new TypeSystemFeatures());

    new WellFormednessFullShortCircuitVisitor().visitProgram(parsed).ifPresent(err -> {
      throw new RuntimeException(err);
    });

    var inferred = InferBodies.inferAll(parsed);
    new WellFormednessShortCircuitVisitor(inferred).visitProgram(inferred);

    ConcurrentHashMap<Long, TsT> resolvedCalls = new ConcurrentHashMap<>();
    inferred.typeCheck(resolvedCalls);

    try {
      var mir = new MIRInjectionVisitor(List.of(), inferred, resolvedCalls).visitProgram();
      var jsCodegen = new JsCodegen(mir);
      jsCodegen.visitProgram(new Id.DecId("test.Test", 0));
      Assertions.fail("Expected failure, but JS codegen succeeded");
    } catch (RuntimeException e) {
      Assertions.assertTrue(e.getMessage().contains(expectedErr), "Error message does not contain expected substring");
    }
  }
}
