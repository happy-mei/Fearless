package main;

import astFull.E;
import com.github.bogdanovmn.cmdline.CmdLineAppBuilder;
import failure.CompileError;
import id.Id;
import utils.Box;
import utils.Bug;

public class Main {
  private static CompilerFrontEnd frontEnd = null;
  public static boolean isUserInvoked() {
    return frontEnd != null;
  }

  public static void resetAll(){
    E.X.reset();
    Id.GX.reset();
  }

  public static void main(String[] args) {
    args = args.length > 0 ? args : new String[]{"--help"};
    var verbosity = new Box<>(new CompilerFrontEnd.Verbosity(false, false));
    var cli = new CmdLineAppBuilder(args)
      .withJarName("fearless")
      .withDescription("The compiler for the Fearless programming language. See https://fearlang.org for more information.")
      .withArg("new", "Create a new package")
      .withArg("check", "c", "Check that the given fearless program is valid")
      .withArg("run", "r", "Compile and run the given fearless program")
        .withArg("entry-point", "The qualified name for the entry-trait that implements base.Main")
        .withDependencies("run", "entry-point")
      .withFlag("regenerate-aliases", "ra", "Print the default alias file for a new package to standard output")
      .withFlag("imm-base", "Use a pure version of the Fearless standard library")
      .withFlag("show-internal-stack-traces", "Show stack traces within the compiler on errors for debugging purposes")
      .withFlag("print-codegen", "pc", "Print the output of the codegen stage to standard output")
      .withAtLeastOneRequiredOption("help", "new", "check", "run", "regenerate-aliases")
      .withEntryPoint(res->{
        var bv = res.hasOption("imm-base") ? CompilerFrontEnd.BaseVariant.Imm : CompilerFrontEnd.BaseVariant.Std;

        verbosity.set(new CompilerFrontEnd.Verbosity(
          res.hasOption("show-internal-stack-traces"),
          res.hasOption("print-codegen")
        ));
        frontEnd = new CompilerFrontEnd(bv, verbosity.get());

        if (res.hasOption("new")) {
          frontEnd.newPkg(res.getOptionValue("new"));
          return;
        }
        if (res.hasOption("check")) {
          frontEnd.check(res.getOptionValues("check"));
          return;
        }
        if (res.hasOption("run")) {
          frontEnd.run(res.getOptionValue("entry-point"), res.getOptionValues("run"), res.getArgList());
          return;
        }
        if (res.hasOption("regenerate-aliases")) {
          System.out.println(frontEnd.regenerateAliases());
          return;
        }
        throw Bug.unreachable();
      });


    try {
      cli.build().run();
    } catch (CompileError | IllegalStateException e) {
      if (verbosity.get().showInternalStackTraces()) { throw e; }
      System.err.println(e);
      System.exit(1);
    } catch (Exception e) {
      throw Bug.of(e);
    }
  }

//  private static Options buildCli() {
//    var res = new Options();
//    res.addOption(Option.builder()
//      .desc("Create a new Fearless package")
//      .longOpt("new")
//      .hasArg().numberOfArgs(1)
//      .argName("package name")
//      .optionalArg(false)
//      .build()
//    );
//    res.addOption(Option.builder()
//      .desc("Generate aliases for the standard library")
//      .longOpt("gen-aliases")
//      .optionalArg(true)
//      .argName("output path")
//      .build()
//    );
//    res.addOption(Option.builder()
//      .desc("Compile and run the provided fearless files")
//      .hasArgs()
//      .longOpt("run")
//    )
//
//    return res;
//  }
}
