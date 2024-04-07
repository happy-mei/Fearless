package codegen.java;

import codegen.MIR;
import codegen.MethExprKind;
import codegen.ParentWalker;
import codegen.optimisations.OptimisationBuilder;
import id.Id;
import id.Mdf;
import magic.Magic;
import parser.Parser;
import rt.FProgram;
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

/*
 JavaCompiler need instance compile
 Files:
   String typeDefContent = gen.visitTypeDef(pkg.name(), def, pkg.funs());
   Is this all the 'class body'? what this looks like?
   makes sense to make a file for each of those?
   
 package: one or many?
 class name mangling: does contain package name or not?
 so I can generate fully qualified names, mangling does not include packages
 reserved package names?  'rt' can not be a package name 
 rt is a package that contains core magic stuff that we have
 to include in the classpath to run/compile
 Or, we can copy those source file as 'base' classes 
 what really is "FProgram"
 interface Top
   interface pk1
     interface Person
 
*/
public class JavaFilesCodegen{
  JavaProgram javaProgram= new JavaProgram(new ArrayList<>());
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
    //ResolveResource.of("/testFiles/test1");
    Path.of("/Users/sonta/Desktop/Java22/wk/GeneratedFearless/src");
  
  private void _deleteOldFiles() throws IOException{
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
        content= content.replace("FProgram.base.", "base.");
        var dest= filesRoot.resolve("rt",p.getFileName().toString());
        Files.writeString(dest, content);
      }
    }
  }
  public void generateFiles() {
     /*String fearlessErrorContent = userCodePkg+"""
       class FearlessError extends RuntimeException {
         // ...
       }
       """;
     String fearlessAuxContent = userCodePkg+"""
       class FAux { static FProgram.base.LList_1 LAUNCH_ARGS; }
       """;*/
      //addFile(userCodeDir, "FearlessError.java", fearlessErrorContent);
      //addFile(userCodeDir, "FAux.java", fearlessAuxContent);

      for (MIR.Package pkg : program.pkgs()) {
        //if(pkg.name().startsWith("base")){ continue; }
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
      System.out.println(pkgName+" "+fileName);
      Path p=filesRoot.resolve(fileName);
      javaProgram.files().add(new JavaFile(p,pkg+content));
    }

}
