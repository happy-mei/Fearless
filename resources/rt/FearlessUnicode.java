package rt;

import java.io.IOException;
import java.net.URISyntaxException;

public final class FearlessUnicode {
  static {
    try {
      ResolveResource.of("/rt/libunicode_rt.so", path->{
        System.load(path.toFile().getPath());
        return null;
      });
    } catch (IOException | URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  public static native byte[][] parse(String input);
}
