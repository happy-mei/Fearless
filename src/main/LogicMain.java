package main;

import astFull.Package;
import failure.CompileError;
import parser.Parser;
import program.TypeSystemFeatures;
import program.inference.InferBodies;
import program.typesystem.EMethTypeSystem;
import utils.Bug;
import wellFormedness.WellFormednessFullShortCircuitVisitor;
import wellFormedness.WellFormednessShortCircuitVisitor;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static utils.ResolveResource.read;

public interface LogicMain<Exe> {
  Path base();
  CompilerFrontEnd.Verbosity verbosity();
  Map<String,List<Package>> parseApp();
  default astFull.Program parse(Map<String,List<Package>> app) {
    Map<String,List<Package>> base = load(loadFiles(this.base()));
    return generateProgram(base, app);
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
    onStart(process);
  }
  
  default List<Parser> loadFiles(Path root) {
    try (var fs = Files.walk(root)) {
      return fs
        .filter(Files::isRegularFile)
        .map(p->new Parser(p,read(p)))
        .toList();
    }
    catch(IOException io) { throw new UncheckedIOException(io); }
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
    var err = Collections.disjoint(base.keySet(), app.keySet());
    if (err) { throw Bug.todo(); }
    var packages = new HashMap<>(base);
    packages.putAll(app);
    return Parser.parseAll(packages, new TypeSystemFeatures());
  }
}