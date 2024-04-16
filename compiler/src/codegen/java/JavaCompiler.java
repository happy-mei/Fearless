package codegen.java;

import java.nio.file.Files;
import java.util.List;
import javax.tools.Diagnostic;
import javax.tools.ToolProvider;

import main.CompilerFrontEnd.Verbosity;
import main.InputOutput;
import utils.Box;
import utils.Bug;
import utils.IoErr;

public record JavaCompiler(Verbosity verbosity, InputOutput io){
  public void compile(List<JavaFile> files) {
    assert !files.isEmpty();
    var compiler = ToolProvider.getSystemJavaCompiler();
    assert compiler != null
      :"No Java compiler could be found. Please use a JDK >= 10";
    //TODO: are you sure this is the right message? used to be about JDK vs JRE
    IoErr.of(()->Files.createDirectories(io.output()));
    if (verbosity.printCodegen()) {//TODO: what pattern is this???
      System.err.println("Java codegen working dir: " 
        +io.output().toAbsolutePath());
    }//should not this be a method of verbosity?
    
    var options = List.of(
      "-d", io.output().toAbsolutePath().toString(),
      "-cp", io.cachedBase().toAbsolutePath().toString(),
      "-Xdiags:verbose"
      );
    var errors = new Box<Diagnostic<?>>(null);
    boolean success = compiler.getTask(
      null,
      null,
      errors::set,
      options,
      null,
      (Iterable<JavaFile>) files::iterator
      ).call();
  
    if (success){ return; }
    var diagnostic = errors.get();
    if (diagnostic == null) {
      throw Bug.of("Java compilation failed.");
    }
    throw Bug.of("Java compilation failed:\n"+ diagnostic);
  }
}