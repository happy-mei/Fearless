package main;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import codegen.java.JavaFile;
import parser.Parser;
import utils.IoErr;
import utils.ResolveResource;

public interface InputOutput{
  String entry();
  List<String> commandLineArguments();
  Path baseDir();
  Path magicDir();
  Path output();//where we put temp .class
  Path cachedBase(); //where we save base, can be the same of output
  List<Parser> cachedFiles();
  //here since it can be derived from output+cachedBase
  List<Parser> inputFiles();
  //here instead of 'input'
  
  default List<Parser> baseFiles(){//crucially, this method is lazy
    return InputOutputHelper.loadFiles(baseDir(),".fear");
  }
  default List<JavaFile> magicFiles(){//crucially, this method is lazy
    return InputOutputHelper.readMagicFiles(magicDir());
  }

  record FieldsInputOutput(
      String entry,
      List<String> commandLineArguments,
      Path baseDir, Path magicDir,
      List<Parser> inputFiles, Path output,
      Path cachedBase, List<Parser> cachedFiles
      )
    implements InputOutput{}
  public static InputOutput userFolder(
      String entry, List<String> commandLineArguments, Path userFolder){
    Path output= userFolder.resolve("bin");
    List<Parser> inputFiles= InputOutputHelper.loadInputFiles(userFolder);
    List<Parser> cachedFiles= InputOutputHelper.loadCachedFiles(output);
    return new FieldsInputOutput(
      entry,
      commandLineArguments,
      ResolveResource.of("/base"),
      ResolveResource.of("/rt"),
      inputFiles,
      output,
      output,
      cachedFiles
      );
  }
  public static InputOutput programmatic(
      String entry, List<String> commandLineArguments,
      List<String> files, Path output,Path cachedBase){
    List<Parser> inputFiles= IntStream.range(0,files.size())
        .mapToObj(i->new Parser(Path.of("Dummy"+i+".fear"),files.get(i)))
        .toList();
    List<Parser> cachedFiles= InputOutputHelper.loadCachedFiles(cachedBase);
    return new FieldsInputOutput(
      entry,
      commandLineArguments, 
      ResolveResource.of("/base"),
      ResolveResource.of("/rt"),
      inputFiles,
      output,
      cachedBase,//ResolveResource.of("/cachedBase"),
      cachedFiles
      );
  }
  //This will only work outside of the jar
  public static InputOutput programmaticAuto(List<String> files){
    return programmatic("test.Test", List.of(),files,
      ResolveResource.freshTmpPath(),
      ResolveResource.of("/cachedBase"));
  }
}
class InputOutputHelper{
  static List<Parser> loadInputFiles(Path root) {
    return loadFiles(root,".fear");
  }
  static List<Parser> loadCachedFiles(Path root) {
    return loadFiles(root,"pkgInfo.txt");
  }
  static List<Parser> loadFiles(Path root,String endsWith) {
    return IoErr.of(()->{try(var fs = Files.walk(root)) {
      return fs
        .filter(Files::isRegularFile)
        .filter(p->p.getFileName().toString().endsWith(endsWith))
        //.map(p->{System.out.println(p); return p;})
        .map(p->new Parser(p, ResolveResource.read(p)))
        .toList();
      }});
  }
  public static List<JavaFile> readMagicFiles(Path root){
    List<Parser> files= loadFiles(root,".java");
    return files.stream()
      .map(p->new JavaFile(p.fileName(),p.content()))
      .toList();
  }
}