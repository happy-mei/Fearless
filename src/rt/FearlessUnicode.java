package rt;

public final class FearlessUnicode {
  static {
    var path = ResolveResource.of("/rt/libunicode_rt.so");
    System.load(path.toFile().getPath());
  }

  public static native byte[][] parse(String input);
}
