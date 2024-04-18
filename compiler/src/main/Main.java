package main;

import astFull.E;
import com.github.bogdanovmn.cmdline.CmdLineAppBuilder;
import failure.CompileError;
import id.Id;
import main.java.LogicMainJava;
import utils.Box;
import utils.Bug;

import java.io.UncheckedIOException;
import java.nio.file.Path;

public class Main {
  public static void resetAll(){
    E.X.reset();
    Id.GX.reset();
  }

  public static void main(String[] args) {
    args = args.length > 0 ? args : new String[]{"--help"};
    var verbosity = new Box<>(new CompilerFrontEnd.Verbosity(false, false, CompilerFrontEnd.ProgressVerbosity.None));
//This commented code was terrible anyway, please, divide in more methods and concepts....
     var cli = new CmdLineAppBuilder(args)
      .withJarName("fearless")
      .withDescription("The compiler for the Fearless programming language. See https://fearlang.org for more information.")
      .withFlag("new", "Create a new package")
      .withFlag("check", "c", "Check that the given Fearless program is valid")
      .withFlag("run", "r", "Compile and run the given Fearless program")
        .withArg("entry-point", "The qualified name for the entry-trait that implements base.Main")
        .withDependencies("run", "entry-point")
      .withFlag("regenerate-aliases", "ra", "Print the default alias file for a new package to standard output")
      .withFlag("generate-docs", "d", "Generate documentation for the given Fearless program")
      .withFlag("imm-base", "Use a pure version of the Fearless standard library")
      .withFlag("show-internal-stack-traces", "di", "Show stack traces within the compiler on errors for debugging purposes")
      .withFlag("print-codegen", "pc", "Print the output of the codegen stage to standard output")
      .withFlag("show-tasks", "sct", "Print progress messages showing the current task the compiler is performing.")
      .withFlag("show-full-progress", "sfp", "Print progress messages showing the current task and all sub-tasks the compiler is performing.")
      .withAtLeastOneRequiredOption("help", "new", "check", "run", "regenerate-aliases", "generate-docs")
      .withEntryPoint(res->{
        CompilerFrontEnd.ProgressVerbosity pv = CompilerFrontEnd.ProgressVerbosity.None;
        if (res.hasOption("show-tasks")) { pv = CompilerFrontEnd.ProgressVerbosity.Tasks; }
        if (res.hasOption("show-full-progress")) { pv = CompilerFrontEnd.ProgressVerbosity.Full; }
        verbosity.set(new CompilerFrontEnd.Verbosity(
          res.hasOption("show-internal-stack-traces"),
          res.hasOption("print-codegen"),
          pv
        ));

        if (res.hasOption("regenerate-aliases")) {
          var trashIO = InputOutput.trash(res.hasOption("imm-base"));
          new GenerateAliases(trashIO).printAliases();
          return;
        }

        if (res.getArgList().isEmpty()) {
          throw Bug.todo("good error about no project path existing");
        }
        var projectPath = Path.of(res.getArgList().getFirst());
        var extraArgs = res.getArgList().subList(1, res.getArgList().size());
        var io = res.hasOption("imm-base")
          ? InputOutput.userFolderImm(res.getOptionValue("entry-point"), extraArgs, projectPath)
          : InputOutput.userFolder(res.getOptionValue("entry-point"), extraArgs, projectPath);
        var main = LogicMainJava.of(io, verbosity.get());

        if (res.hasOption("new")) {
          throw Bug.todo();
        }
        if (res.hasOption("check")) {
          CheckMain.of(main);
          return;
        }
        if (res.hasOption("run")) {
          var p = main.run().inheritIO().start().onExit().join();
          System.exit(p.exitValue());
          return;
        }

//        frontEnd = new CompilerFrontEnd(bv, verbosity.get(), new TypeSystemFeatures());
//        if (res.hasOption("new")) {
//          frontEnd.newPkg(res.getOptionValue("new"));
//          return;
//        }
//        if (res.hasOption("generate-docs")) {
//          frontEnd.generateDocs(res.getOptionValues("generate-docs"));
//          return;
//        }
        throw Bug.unreachable();
      });


    try {
      cli.build().run();
    } catch (RuntimeException e) {
      if (verbosity.get().showInternalStackTraces()) {
        throw e;
      }
      System.err.println(e.getMessage());
      System.exit(1);
    } catch (Exception e) {
      throw Bug.of(e);
    }
  }
}
