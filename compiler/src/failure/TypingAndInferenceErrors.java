package failure;


import ast.Program;
import id.Id;
import id.Mdf;
import program.typesystem.XBs;
import utils.Bug;

import java.net.URI;

public record TypingAndInferenceErrors(Program p, URI fileName) {
  public static CompileError fromInference(Program p, CompileError error) {
    if (error.code() == 0) { return error; }
    var errorProcessor = new TypingAndInferenceErrors(p, error.posOrUnknown().fileName());
    return switch (ErrorCode.fromCode(error.code())) {
      // TODO: match on error types you want to improve
      case undefinedMethod -> new PlainError(errorProcessor.undefinedMeth(error));
      default -> error;
    };
  }

  public static CompileError fromMethodError(Program p, CompileError error) {
    if (error.code() == 0) { return error; }
    var errorProcessor = new TypingAndInferenceErrors(p, error.posOrUnknown().fileName());
    return switch (ErrorCode.fromCode(error.code())) {
      // TODO: match on error types you want to improve
      case undefinedMethod -> new PlainError(errorProcessor.undefinedMeth(error));
      default -> error;
    };
  }

  public String undefinedMeth(CompileError rawError) {
    // TODO: improve this error in some way
    ast.T recvT = switch (rawError.attributes.get("recvT")) {
      case astFull.T t -> t.toAstT();
      case ast.T t -> t;
      default -> throw Bug.unreachable();
    };
    var name = (Id.MethName) rawError.attributes.get("name");
    var ms = p().meths(XBs.empty(), Mdf.recMdf, recvT.itOrThrow(), 0);

    return rawError+"\nextra info for experts:\n"+ms;
  }
}
