package tour;

import utils.Base;
import utils.Err;
import utils.IoErr;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;

import main.CompilerFrontEnd;
import main.CompilerFrontEnd.ProgressVerbosity;
import main.InputOutput;

public class TourHelper {
  private static String lastLine(String text) {
    text=text.trim();
    int lastIndex = text.lastIndexOf("\n");
    return lastIndex == -1 ? text : text.substring(lastIndex + 1);
  }

  public static void run(String content){
    var last= lastLine(content);
    var aliases = "package test\n";
    if (!content.startsWith("package") && !content.startsWith("//|")){
      content = "package test\n" + content;
      aliases = Base.mutBaseAliases;
    }
    String expectedPrint= "";
    if (last.startsWith("//prints ")){
      expectedPrint = last.substring("//prints ".length());
    }
    if(!content.contains(":Main") && !content.contains(": Main") && !content.contains(": TestMain")){
      content += "Test:base.Main{s->Void}";
    }
    /*ok(new Res(expectedPrint,"",0), "test.Test",
      content, Base.mutBaseAliases);*/
    runCode(List.of(content, aliases),expectedPrint);
    //TODO: add case for errs?
  }
  static void checker(Process p, String expectedIO){
    String err= p.errorReader().lines().collect(Collectors.joining("\n"));
    String out= p.inputReader().lines().collect(Collectors.joining("\n"));
    if(!out.isEmpty()){ Err.strCmp(expectedIO, out); }
    if(!err.isEmpty()){ Err.strCmp(expectedIO, err); }
    if(!expectedIO.isEmpty()) { Assertions.assertTrue(!out.isEmpty() || !err.isEmpty()); }
    Assertions.assertEquals(0, p.exitValue(), "Expected exit code did not match");
  }
  static void runCode(List<String> files, String expectedIO){
    var v= new CompilerFrontEnd.Verbosity(false,false,
            ProgressVerbosity.None);
    var io= InputOutput.programmaticAuto(files);
    var runner= main.java.LogicMainJava.of(io,v);
    ProcessBuilder proc= runner.run();
    //proc.inheritIO();//not in the tests
    Process running= IoErr.of(proc::start);
    var p = running.onExit().join();
    checker(p,expectedIO);
  }
}
