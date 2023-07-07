package codegen.java;

import ast.T;
import codegen.MIR;
import id.Id;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ObjCapImpl {
  String call(MagicImpls ctx, Id.MethName m, List<MIR> args, Map<MIR, T> gamma);
}
