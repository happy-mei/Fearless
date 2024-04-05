package utils;

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
  static <R> R of(String relativePath, Function<Path, R> f) throws IOException, URISyntaxException {
    assert relativePath.startsWith("/");
    var absolutePath = requireNonNull(ResolveResource.class.getResource(relativePath)).toURI();
    if (!absolutePath.getScheme().equals("jar") && !absolutePath.getScheme().equals("resource")) {
      return f.apply(Path.of(absolutePath));
    }
    // yes, this technically fetches /something/foo.file and resolves to / but it's fine for our purposes.
    // We cannot just do  ResolveResource.class.getResource("/") because the resource path we provide must be to a file
    try(var fs = FileSystems.newFileSystem(absolutePath, Map.of())) {
      return f.apply(fs.getPath(relativePath));
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
