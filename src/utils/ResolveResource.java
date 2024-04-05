package utils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
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
  static <R> R of(String relativePath, Function<Path, R> f) throws IOException {
    assert relativePath.startsWith("/");
    URI absolutePath; try { absolutePath = requireNonNull(ResolveResource.class.getResource(relativePath)).toURI();
    } catch (URISyntaxException err) { throw Bug.of(err); }
    if (!absolutePath.getScheme().equals("jar") && !absolutePath.getScheme().equals("resource")) {
      return f.apply(Path.of(absolutePath));
    }
    // yes, this technically fetches /something/foo.file and resolves to / but it's fine for our purposes.
    // We cannot just do  ResolveResource.class.getResource("/") because the resource path we provide must be to a file
    try(var fs = FileSystems.newFileSystem(absolutePath, Map.of())) {
      return f.apply(fs.getPath(relativePath));
    }
  }

  static String read(String path) {
    try {
      return of(path, ThrowingFunction.of(ResolveResource::readLive));
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  /**
   * Read the contents of the file at a path in to a string. This should only ever be called on
   * paths that are known to exist. That means that it should not be called on a path that is within a JAR
   * unless you know that the virtual filesystem of the JAR is alive.
   */
  static String readLive(Path p) {
    try(var br = Files.newBufferedReader(p, StandardCharsets.UTF_8)) {
      return br.lines().collect(Collectors.joining("\n"));
    } catch (IOException err) {
      throw new UncheckedIOException(err);
    }
  }
}
