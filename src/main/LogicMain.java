package main;

import ast.Program;
import astFull.Package;
import codegen.MIRInjectionVisitor;
import codegen.java.ImmJavaCodegen;
import codegen.java.JavaCodegen;
import codegen.java.JavaProgram;
import failure.CompileError;
import failure.Fail;
import id.Id;
import id.Mdf;
import magic.Magic;
import parser.Parser;
import program.TypeSystemFeatures;
import program.inference.InferBodies;
import program.typesystem.EMethTypeSystem;
import program.typesystem.XBs;
import utils.Box;
import utils.Bug;
import utils.ResolveResource;
import utils.ThrowingFunction;
import wellFormedness.WellFormednessFullShortCircuitVisitor;
import wellFormedness.WellFormednessShortCircuitVisitor;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static utils.ResolveResource.read;

public interface LogicMain {
  void parse();
  void wellFormednessFull();
  void inference();
  void wellFormednessCore();
  void typeSystem();
  void codeGeneration();
  void mainCodeGeneration();
  void execution();
  Path base();
  Path userApp();
  String entry();
  List<String> commandLineArguments();
  CompilerFrontEnd.Verbosity verbosity();
  default void logicMain(){
    parse();
    wellFormednessFull();
    inference();
    wellFormednessCore();
    typeSystem();
    codeGeneration();
    mainCodeGeneration();
    execution();
  }
  static Map<String, List<Package>> load(Path root) {
    try (var fs = Files.walk(root)) {
      return fs
        .filter(Files::isRegularFile)
        .map(ThrowingFunction.of(path->new Parser(path, read(path)).parseFile(CompileError::err)))
        .collect(Collectors.groupingBy(Package::name));
    }
    catch (IOException ie) { throw new UncheckedIOException(ie); }
  }
  static astFull.Program generateProgram(Path stLib, Path appRoot) {
    var base = load(stLib);
    var app = load(appRoot);
    var err = Collections.disjoint(base.keySet(), app.keySet());
    if (err) { throw Bug.todo(); }
    var packages = new HashMap<>(base);
    packages.putAll(app);
    return Parser.parseAll(packages, new TypeSystemFeatures());
  }
}
class MutTestLogicMain implements LogicMain{
  astFull.Program fullProgram;
  ast.Program program;
  JavaProgram javaProgram;
  ConcurrentHashMap<Long, EMethTypeSystem.TsT> resolvedCalls;
  String[] makeJavaCommand(Path pathToMain) {
    var jrePath= Path.of(System.getProperty("java.home"), "bin", "java").toAbsolutePath();
    String entryPoint= "userCode." + pathToMain.getFileName().toString().split("\\.class")[0];
    var baseCommand = Stream.of(jrePath.toString(),entryPoint);
    return Stream.concat(baseCommand, commandLineArguments().stream())
      .toArray(String[]::new);
  }
  public void parse() {
    fullProgram = LogicMain.generateProgram(base(), userApp());
    //ResolveResource.of("/base")
    }
  public void wellFormednessFull(){
    new WellFormednessFullShortCircuitVisitor()
      .visitProgram(fullProgram)
      .ifPresent(err->{ throw err; });
    }
  public void inference(){
    program= InferBodies.inferAll(fullProgram);
  }
  public void wellFormednessCore(){
    new WellFormednessShortCircuitVisitor(program)
      .visitProgram(program)
      .ifPresent(err->{ throw err; });
  }
  public void typeSystem(){
    program.typeCheck(resolvedCalls);
  }
  public void codeGeneration(){
    var mir = new MIRInjectionVisitor(program, resolvedCalls).visitProgram();
    var codegen= new JavaCodegen(mir);
    var main = program.of(Magic.Main).toIT();
    var entry= new Id.DecId(entry(),0);
    var isEntryValid = program
      .isSubType(XBs.empty(), new ast.T(Mdf.mdf, program.of(entry).toIT()), new ast.T(Mdf.mdf,main));
    if (!isEntryValid) { throw Fail.invalidEntryPoint(entry, main); }
    var src = codegen.visitProgram(entry);
    javaProgram= new JavaProgram(src);
  }
  public void mainCodeGeneration(){}
  public void execution(){
    Path pathToMain= JavaProgram.compile(verbosity(), javaProgram);
    var pb = new ProcessBuilder(makeJavaCommand(pathToMain));
    pb.directory(pathToMain.getParent().toFile());
    pb.inheritIO();
    Process proc; try { proc = pb.start();}
    catch (IOException e) { throw Bug.of(e); }
    proc.onExit().join();
    System.exit(proc.exitValue());
  }

  @Override public Path base() {
    return null;
  }

  @Override public Path userApp() {
    return null;
  }

  @Override public String entry() {
    return null;
  }

  @Override public List<String> commandLineArguments() {
    return null;
  }

  @Override public CompilerFrontEnd.Verbosity verbosity() {
    return null;
  }
}