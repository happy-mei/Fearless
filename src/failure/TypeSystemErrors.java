package failure;


import java.net.URI;

public record TypeSystemErrors(URI fileName) {
  public static CompileError fromMethodError(CompileError error) {
    var errorProcessor = new TypeSystemErrors(error.posOrUnknown().fileName());
    return switch (ErrorCode.fromCode(error.code())) {
      // TODO: match on error types you want to improve
      case noCandidateMeths -> new PlainError(errorProcessor.noCandidateMeths(error));
      default -> error;
    };
  }

  public String noCandidateMeths(CompileError rawError) {
    // TODO: improve this error in some way
    return rawError.toString();
  }
}
