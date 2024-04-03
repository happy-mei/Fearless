package rt;

import utils.Bug;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public interface ResolveResource {
  static <R> R of(String root, Function<Path, R> f) throws IOException, URISyntaxException {
    var top = requireNonNull(ResolveResource.class.getResource(root)).toURI();
    if (!top.getScheme().equals("jar") && !top.getScheme().equals("resource")) {
      return f.apply(Path.of(top));
    }
    try(var fs = FileSystems.newFileSystem(top, Map.of())) {
      return f.apply(fs.getPath(root));
    }
  }

  static String getStringOrThrow(String path) {
    try {
      return of(path, ThrowingFunction.of(ResolveResource::read));
    } catch (URISyntaxException | IOException e) {
      throw Bug.of(e);
    }
  }

  static String read(Path p) throws IOException {
    try(var br = Files.newBufferedReader(p, StandardCharsets.UTF_8)) {
      return br.lines().collect(Collectors.joining("\n"));
    }
  }
}
