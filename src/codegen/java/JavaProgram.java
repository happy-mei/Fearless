package codegen.java;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import main.CompilerFrontEnd;
import utils.IoErr;
import utils.ResolveResource;

public record JavaProgram(Path output, List<JavaFile> files, Path pathToMain){
  JavaProgram(Path output, List<JavaFile> files,JavaCompiler c){
   this(output,files,c.compile(output, files));
  }
  
  public void writeJavaFiles(){IoErr.of(this::_writeJavaFiles);}

  private void _writeJavaFiles() throws IOException{
    ResolveResource.deleteOldFiles(output);
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