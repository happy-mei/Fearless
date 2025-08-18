package main.js;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

import ast.Program;
import codegen.MIR;
import codegen.MIRInjectionVisitor;
import codegen.js.JsMagicImpls;
import codegen.js.JsProgram;
import codegen.optimisations.OptimisationBuilder;
import main.CompilerFrontEnd.Verbosity;
import main.InputOutput;
import main.FullLogicMain;
import program.typesystem.TsT;
import utils.Bug;

public interface LogicMainJs extends FullLogicMain<JsProgram> {
  @Override
  default void cachePackageTypes(Program program) {
    // No-op for JavaScript (no Java-style caching needed)
  }

  @Override
  default MIR.Program lower(Program program, ConcurrentHashMap<Long, TsT> resolvedCalls) {
    var mir = new MIRInjectionVisitor(cachedPkg(), program, resolvedCalls).visitProgram();
    var magic = new JsMagicImpls(null, mir.p());
    return new OptimisationBuilder(magic)
      .withAsIdFnOptimisation()
      .withBoolIfOptimisation()
      .withBlockOptimisation()
      .run(mir);
  }

  @Override
  default JsProgram codeGeneration(MIR.Program mir) {
    var res = new JsProgram(this, mir);

    if (verbosity().printCodegen()) {
      // Write Js files to the output directory for testing purposes
      try {
        Path outputDir = Path.of("test/output-js");
        // before writing, clean the output directory
        if (Files.exists(outputDir)) {
          Files.walk(outputDir)
            .sorted((a, b) -> b.compareTo(a)) // reverse order to delete files before directories
            .forEach(path -> {
              try {Files.delete(path);} catch (IOException e) { /* ignore */ }
            });
        }
        Files.createDirectories(outputDir);
        res.writeJsFiles(outputDir);
        System.out.println("Js files saved to " + outputDir.toAbsolutePath());
      } catch (IOException e) {
        System.err.println("Failed to write java files: " + e.getMessage());
      }
    }
    return res;
  }

  @Override
  default void compileBackEnd(JsProgram src) {
    src.writeJsFiles(io().output()); // Just write JS files
  }

  @Override
  default ProcessBuilder execution(JsProgram exe) {
    throw Bug.todo();
//    return new ProcessBuilder("node", io().output().resolve("main.js").toString());
  }

  static LogicMainJs of(InputOutput io, Verbosity verbosity) {
    var cachedPkg = new HashSet<String>();
    return new LogicMainJs() {
      public InputOutput io() { return io; }
      public HashSet<String> cachedPkg() { return cachedPkg; }
      public Verbosity verbosity() { return verbosity; }
    };
  }
}