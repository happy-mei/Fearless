package main;

import astFull.Package;
import failure.CompileError;
import failure.Fail;
import parser.Parser;
import program.TypeSystemFeatures;
import program.inference.InferBodies;
import program.typesystem.EMethTypeSystem;
import utils.IoErr;
import utils.ResolveResource;
import wellFormedness.WellFormednessFullShortCircuitVisitor;
import wellFormedness.WellFormednessShortCircuitVisitor;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public interface LogicMain<Exe> {
  Path baseDir();
  Path rtDir();
  Path output();
  Path cachedBase();
  List<String>cachedPkg();
  CompilerFrontEnd.Verbosity verbosity();
  Map<String,List<Package>> parseApp();
  
  private List<Parser> loadCachedFiles(Path root) {
    var res= IoErr.of(()->{try(var fs = Files.walk(root)) {
      return fs
        .filter(Files::isRegularFile)
        .filter(p->p.endsWith("pkgInfo.txt"))
        .map(p->new Parser(p, ResolveResource.read(p)))
        .toList();
      }});
    cachedPkg().addAll(extractCachedPkg(res));
    return res;
  }
  private List<String> extractCachedPkg(List<Parser> ps) {
    return ps.stream()
      .map(p->pkgName(p.content()))
      .toList();
  }
  private String pkgName(String content){
    assert content.startsWith("package ");
    int nl=content.indexOf("\n");
    assert nl!=-1;
    return content.substring("package ".length(),nl).trim();
  }
  default astFull.Program parse(Map<String,List<Package>> app) {
    var cached= Files.exists(cachedBase().resolve("base"));
    List<Parser> base=cached
      ?loadCachedFiles(cachedBase().resolve("base"))
      :loadFiles(this.baseDir());
    return generateProgram(load(base), app);
    }
  default void wellFormednessFull(astFull.Program fullProgram){
    new WellFormednessFullShortCircuitVisitor()
      .visitProgram(fullProgram)
      .ifPresent(err->{ throw err; });
    }
  default ast.Program inference(astFull.Program fullProgram){
    return InferBodies.inferAll(fullProgram);
  }
  default void wellFormednessCore(ast.Program program){
    new WellFormednessShortCircuitVisitor(program)
      .visitProgram(program)
      .ifPresent(err->{ throw err; });
  }
  default ConcurrentHashMap<Long, EMethTypeSystem.TsT> typeSystem(ast.Program program){
    var acc= new ConcurrentHashMap<Long, EMethTypeSystem.TsT>();
    program.typeCheck(acc);    
    return acc;
  }
  void cachePackageTypes(ast.Program program);

  Exe codeGeneration(
      ast.Program program,
      ConcurrentHashMap<Long, EMethTypeSystem.TsT> resolvedCalls 
      );
  Exe mainCodeGeneration(
      ast.Program program,
      Exe exe,
      ConcurrentHashMap<Long, EMethTypeSystem.TsT> resolvedCalls 
      );
  Process execution(
      ast.Program program,
      Exe exe,
      Exe mainExe,
      ConcurrentHashMap<Long, EMethTypeSystem.TsT> resolvedCalls 
      );
  void preStart(ProcessBuilder pb);
  void onStart(Process process);
  default void logicMain(){
    var v= verbosity();
    var app=parseApp();
    var fullProgram= parse(app);
    wellFormednessFull(fullProgram);
    var program= inference(fullProgram);
    wellFormednessCore(program);
    var resolvedCalls= typeSystem(program);
    var code= codeGeneration(program,resolvedCalls);
    var mainCode= mainCodeGeneration(program,code,resolvedCalls);
    var process= execution(program,code,mainCode,resolvedCalls);
    cachePackageTypes(program);
    onStart(process);
  }
  
  default List<Parser> loadFiles(Path root) {
    return IoErr.of(()->{try(var fs = Files.walk(root)) {
      return fs
        .filter(Files::isRegularFile)
        .map(p->new Parser(p, ResolveResource.read(p)))
        .toList();
      }});
  }
  default Map<String, List<Package>> load(List<Parser> files) {
    return files.stream()
        .map(p->p.parseFile(CompileError::err))
        .collect(Collectors.groupingBy(Package::name));
  }
  default astFull.Program generateProgram(
      Map<String,List<Package>> base,
      Map<String,List<Package>> app
      ) {
    var standardLibOverriden = !Collections.disjoint(base.keySet(), app.keySet());
    if (standardLibOverriden) {
      throw Fail.specialPackageConflict(base.keySet(), app.keySet());
    }
    var packages = new HashMap<>(base);
    packages.putAll(app);
    return Parser.parseAll(packages, new TypeSystemFeatures());
  }
}