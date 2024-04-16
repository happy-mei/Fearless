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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public interface LogicMain<Exe> {
  InputOutput io();
  HashSet<String> cachedPkg();
  CompilerFrontEnd.Verbosity verbosity();
  default astFull.Program parse() {
    var cache = load(io().cachedFiles());
    cachedPkg().addAll(cache.keySet());
    var app = load(io().inputFiles());
    var standardLibOverriden = app.keySet().stream()
      .filter(s->s.startsWith("base.") || s.equals("base") || s.startsWith("rt.") || s.equals("rt"))
      .toList();
    if (!standardLibOverriden.isEmpty()) {
      throw Fail.specialPackageConflict(standardLibOverriden);
    }
    var packages = new HashMap<>(app);
    if(!cachedPkg().contains("base")){
      packages.putAll(load(io().baseFiles()));
    }
    packages.putAll(cache);//Purposely overriding any app also in cache
    return Parser.parseAll(packages, new TypeSystemFeatures());
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
  ProcessBuilder execution(
          ast.Program program,
          Exe exe,
          ConcurrentHashMap<Long, EMethTypeSystem.TsT> resolvedCalls
  );
  default ProcessBuilder run(){
    var fullProgram= parse();
    wellFormednessFull(fullProgram);
    var program= inference(fullProgram);
    wellFormednessCore(program);
    var resolvedCalls= typeSystem(program);
    var code= codeGeneration(program,resolvedCalls);
    var process= execution(program,code,resolvedCalls);
    cachePackageTypes(program);
    return process;
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
}