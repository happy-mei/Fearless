package codegen.java;

import codegen.MIR;
import utils.IoErr;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

public class JavaFilesCodegen{
  final private Path filesRoot;
  final private JavaCompiler c;
  private ArrayList<JavaFile> javaFiles= new ArrayList<>();
  final JavaSingleCodegen gen;
  final MIR.Program program;
  public JavaFilesCodegen(Path filesRoot, MIR.Program program, JavaCompiler c){
    this.program=program;
    this.gen= new JavaSingleCodegen(program);
    this.filesRoot= filesRoot;
    this.c= c;
    }
  public JavaProgram getJavaProgram(List<JavaFile> more){
    var all= Stream.concat(javaFiles.stream(),more.stream()).toList();
    return new JavaProgram(filesRoot,all,c);
  }

  public List<JavaFile> readAllFiles(Path path){
    return IoErr.of(()->_readAllFiles(path));
  }    
  private List<JavaFile> _readAllFiles(Path path) throws IOException{
    assert Files.exists(path);
    var res= new ArrayList<JavaFile>();
    try (Stream<Path> walk = Files.walk(path)) {
      Iterable<Path> ps=walk.filter(p
          ->p.toString().endsWith(".java"))::iterator;
      for(Path p:ps){
        String fileName="base/"+p.getFileName();
        Path pi=filesRoot.resolve(fileName);
        res.add(new JavaFile(pi, Files.readString(p)));
      }
    }
    return Collections.unmodifiableList(res);
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
      javaFiles.add(new JavaFile(p,pkg+content));
      //file locations seems to be ignored
    }
}
