package main;

import astFull.E;
import id.Id;
import org.apache.commons.cli.*;
import program.Program;

public class Main {
  public static void main(String[] args) {
    CommandLineParser parser = new DefaultParser();
    var formatter = new HelpFormatter();
    var options = buildCli();
    formatter.printHelp("fear", options);
    try {
      var res = parser.parse(options, args);
    } catch (ParseException exp) {
      System.out.println("Invalid arguments: " + exp.getMessage());
      System.exit(1);
    }
  }

  public static void resetAll(){
    E.X.reset();
    Id.GX.reset();
    Program.reset();
  }

  private static Options buildCli() {
    return new Options();
  }
}
