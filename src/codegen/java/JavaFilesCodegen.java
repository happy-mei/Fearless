package codegen.java;

import codegen.MIR;
import codegen.MethExprKind;
import codegen.ParentWalker;
import codegen.optimisations.OptimisationBuilder;
import id.Id;
import id.Mdf;
import magic.Magic;
import parser.Parser;
import utils.Box;
import utils.Bug;
import utils.ResolveResource;
import utils.Streams;
import visitors.MIRVisitor;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static magic.MagicImpls.getLiteral;

public class JavaFilesCodegen{
  //TODO: simplyfy and store arraylist only in here
  private JavaProgram javaProgram= new JavaProgram(new ArrayList<>());
  public JavaProgram getJavaProgram(){
    return new JavaProgram(List.copyOf(javaProgram.files()));
  } 
  JavaSingleCodegen gen;
  MIR.Program program;
  public JavaFilesCodegen(MIR.Program program){
    this.program=program;
    this.gen= new JavaSingleCodegen(program);
    }
  public void writeFiles(){
    try {_writeFiles();}
    catch(IOException io){ throw new UncheckedIOException(io); }
  }
  public static final Path filesRoot=
    Path.of(System.getProperty("user.dir"), "GeneratedFearless", "src");
    //ResolveResource.of("/testFiles/test1");
//    Path.of("/Users/sonta/Desktop/Java22/wk/GeneratedFearless/src");
  
  private void _deleteOldFiles() throws IOException{
    if (!Files.exists(filesRoot)) { return; }
    try (Stream<Path> walk = Files.walk(filesRoot)) {
      Iterable<Path> ps=walk.sorted(Comparator.reverseOrder())::iterator;
      for(Path p:ps){ Files.deleteIfExists(p); }
    }
  }
  private void _writeFiles() throws IOException{
    _deleteOldFiles();
    for(var fi:javaProgram.files()) {
      var pi= Path.of(fi.toUri());
      Files.createDirectories(pi.getParent());
      Files.write(pi,fi.code().getBytes());
    }
    _copyRtFiles();
  }
  private void _copyRtFiles()  throws IOException{
    try(var fs= Files.walk(ResolveResource.of("/rt"))){
      Iterable<Path> ps=fs.filter(p
        ->p.toString().endsWith(".java"))::iterator;
      for(var p:ps) {
        String content= Files.readString(p);
        Files.createDirectories(filesRoot.resolve("rt"));
        var dest= filesRoot.resolve(Path.of("rt",p.getFileName().toString()));
        Files.writeString(dest, content);
      }
    }
  }
  public void generateFiles() {
      for (MIR.Package pkg : program.pkgs()) {
        for (MIR.TypeDef def : pkg.defs().values()) {
          var funs= pkg.funs().stream()
            .filter(f->f.name().d().equals(def.name()))
            .toList();
          String typeDefContent = gen
            .visitTypeDef(pkg.name(), def, funs);
          if(typeDefContent.isEmpty()){ continue; }
          String name   = gen.id.getSimpleName(def.name());
          addFile(pkg.name(), name, typeDefContent);
        }
      }
      for(var e: gen.freshRecords.entrySet()){
        String pkg    = e.getKey().pkg();
        String name   = gen.id.getSimpleName(e.getKey())+"Impl";
        String content= e.getValue();
        addFile(pkg, name, content);
      }
    }

    private void addFile(String pkgName, String name, String content) {
      String pkg= "package "+pkgName+";\n";
      String fileName=pkgName.replace(".","/")+"/"+name+".java";
      Path p=filesRoot.resolve(fileName);
      javaProgram.files().add(new JavaFile(p,pkg+content));
    }

}
