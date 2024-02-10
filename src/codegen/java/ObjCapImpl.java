package codegen.java;

import codegen.MIR;
import id.Id;

import java.util.List;

public interface ObjCapImpl {
  String call(MagicImpls ctx, Id.MethName m, List<MIR.E> args);
}
