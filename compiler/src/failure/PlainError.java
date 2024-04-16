package failure;

/**
 * A simple error that just prints its message on toString.
 */
public class PlainError extends CompileError {
  public PlainError(String message) {
    super(message);
  }

  @Override public String toString() {
    return super.getMessage();
  }
}
