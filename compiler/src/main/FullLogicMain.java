package main;

import codegen.MIR;
import program.typesystem.TsT;

import java.util.concurrent.ConcurrentHashMap;

public interface FullLogicMain<Exe> extends LogicMain {
  CompilerFrontEnd.Verbosity verbosity();

  MIR.Program lower(ast.Program program, ConcurrentHashMap<Long, TsT> resolvedCalls);
  Exe codeGeneration(MIR.Program program);
  ProcessBuilder execution(MIR.Program program, Exe exe, ConcurrentHashMap<Long, TsT> resolvedCalls);
  default ProcessBuilder run(){
    var fullProgram= parse();
    wellFormednessFull(fullProgram);
    var program = inference(fullProgram);
    wellFormednessCore(program);
    var resolvedCalls = typeSystem(program);
    var mir = lower(program,resolvedCalls);
    var code = codeGeneration(mir);
    var process = execution(mir,code,resolvedCalls);
    cachePackageTypes(program);
    return process;
  }
}