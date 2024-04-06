package rt;

public final class NativeRuntime {
  static {
    // This notably is defined as the architecture the JVM is running as (and not the architecture of the computer).
    // We want this because the code is going to be linked into the JVM so it *must* be the same architecture.
    var arch = System.getProperty("os.arch");
    arch = switch (arch) {
      case "x86_64", "amd64" -> "amd64";
      case "aarch64", "arm64" -> "arm64";
      default -> throw new IllegalStateException("Unsupported architecture: "+System.getProperty(arch));
    };
    var path = ResolveResource.of("/rt/libnative/"+arch+"-"+System.mapLibraryName("native_rt"));
    try {
      System.load(path.toFile().getPath());
    } catch (UnsatisfiedLinkError err) {
      throw new RuntimeException("Internal Fearless runtime error: Could not link to "+path);
    }
  }

  public static class StringEncodingError extends RuntimeException {
    public StringEncodingError(String message) {
      super(message);
    }
  }

  public static native int[] indexString(byte[] utf8Str);
  public static native void print(byte[] utf8Str);
  public static native void println(byte[] utf8Str);
  public static native void printlnErr(byte[] utf8Str);
  public static native void printErr(byte[] utf8Str);
}
