package codegen.java;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import main.CompilerFrontEnd;
import utils.IoErr;

public record JavaProgram(Path output, List<JavaFile> files, Path pathToMain){
  JavaProgram(Path output, List<JavaFile> files,JavaCompiler c){
   this(output,files,c.compile(output, files));
  }
  
  public void writeJavaFiles(){IoErr.of(this::_writeJavaFiles);}
  
  private void _deleteOldFiles() throws IOException{
    if (!Files.exists(output)) { return; }
    try (Stream<Path> walk = Files.walk(output)) {
      Iterable<Path> ps=walk.sorted(Comparator.reverseOrder())::iterator;
      for(Path p:ps){
        if(p.equals(output)){ continue; }
        Files.deleteIfExists(p); 
        }
    }
  }
  private void _writeJavaFiles() throws IOException{
    _deleteOldFiles();
    for(var fi:files) {
      var pi= Path.of(fi.toUri());
      Files.createDirectories(pi.getParent());
      Files.write(pi,fi.code().getBytes());
    }
  }
  //public static final Path filesRoot=
    //Path.of(System.getProperty("user.dir"), "GeneratedFearless", "src");
  //  ResolveResource.of("/testFiles/test1");
    //Path.of("/Users/sonta/Desktop/Java22/wk/GeneratedFearless/src");
}