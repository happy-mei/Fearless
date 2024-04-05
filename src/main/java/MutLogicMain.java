package main.java;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import astFull.Package;
import main.CompilerFrontEnd;
import main.CompilerFrontEnd.Verbosity;
import utils.ResolveResource;

public record MutLogicMain(
  List<String> commandLineArguments,
  String entry,
  Path userApp,
  Verbosity verbosity
  )
implements LogicMainJava{
  public Path base() {return ResolveResource.of("/base"); }
  public Map<String,List<Package>> parseApp(){
    return load(loadFiles(userApp()));
  }
  public void onStart(Process proc) {
    proc.onExit().join();
    System.exit(proc.exitValue());
  }

}