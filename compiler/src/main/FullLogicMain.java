package main;

import codegen.MIR;
import program.typesystem.TsT;

import java.util.concurrent.ConcurrentHashMap;

public interface FullLogicMain<Exe> extends LogicMain {
  CompilerFrontEnd.Verbosity verbosity();

  MIR.Program lower(ast.Program program, ConcurrentHashMap<Long, TsT> resolvedCalls);
  Exe codeGeneration(MIR.Program program);
  void compileBackEnd(Exe exe);
  ProcessBuilder execution(Exe exe);
  default Exe buildAndCache() {
    var fullProgram= parse();
    wellFormednessFull(fullProgram);
    var program = inference(fullProgram);
    wellFormednessCore(program);
    var resolvedCalls = typeSystem(program);
    var mir = lower(program,resolvedCalls);
    var exe = codeGeneration(mir);
    compileBackEnd(exe);
    cachePackageTypes(program);
    return exe;
  }
  default ProcessBuilder run(){
    var executable = buildAndCache();
    return execution(executable);
  }
}