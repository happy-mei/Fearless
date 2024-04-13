package main.java;

import astFull.Package;
import main.CompilerFrontEnd.Verbosity;
import utils.ResolveResource;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public record MutLogicMain(
  List<String> commandLineArguments,
  String entry,
  Path userApp,
  Verbosity verbosity,
  Path output,
  List<String> cachedPkg
  )
implements LogicMainJava{
  public Path baseDir() {return ResolveResource.of("/base"); }
  public Path rtDir() {return ResolveResource.of("/rt"); }
  public Path cachedBase() {return ResolveResource.of("/cachedBase"); }
  public Map<String,List<Package>> parseApp(){
    return load(loadFiles(userApp()));
  }
  public void onStart(Process proc) {
    proc.onExit().join();
    System.exit(proc.exitValue());
  }
}