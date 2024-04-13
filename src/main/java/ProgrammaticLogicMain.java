package main.java;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import astFull.Package;
import main.CompilerFrontEnd.Verbosity;
import parser.Parser;
import utils.ResolveResource;

public record ProgrammaticLogicMain(
  List<String> commandLineArguments,
  String entry,
  Verbosity verbosity,
  List<String> files,
  Consumer<Process> res,
  Path output,
  List<String> cachedPkg
  )
implements LogicMainJava{
  public Path baseDir() {return ResolveResource.of("/base"); }
  public Path rtDir() {return ResolveResource.of("/rt"); }
  public Path cachedBase() {return ResolveResource.of("/cachedBase"); }
  public Map<String,List<Package>> parseApp(){
    var ps= IntStream.range(0,files.size())
      .mapToObj(i->new Parser(Path.of("Dummy"+i+".fear"),files.get(i)))
      .toList();
    return load(ps);
  }
  public void onStart(Process proc) { 
    proc.onExit().thenAccept(res).join();
  }
  @Override public void preStart(ProcessBuilder pb) {}
}