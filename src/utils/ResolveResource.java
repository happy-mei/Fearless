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

public final class ResolveResource {
  static private final Path root;
  static{
    ResolveResource.class.getResource("ResolveResource.class");
    var url= ResolveResource.class.getResource("/base");
    if(url==null) {
      String workingDir = System.getProperty("user.dir");
      root=Path.of(workingDir).resolve("resources");
      assert root!=null;
      assert Files.exists(root):root;
    }
    else {
      URI uri; try {uri= url.toURI();}
      catch (URISyntaxException e) { throw Bug.of(e); }
      root=Path.of(uri);
    }
  }
  static public <R> R of(String relativePath, Function<Path, R> f) throws IOException {
    assert relativePath.startsWith("/");
    URI absolutePath= root.resolve(relativePath.substring(1)).toUri();
    if (!absolutePath.getScheme().equals("jar") && !absolutePath.getScheme().equals("resource")) {
      return f.apply(Path.of(absolutePath));
    }
    try(var fs = FileSystems.newFileSystem(root.toUri(), Map.of())) {
      return f.apply(fs.getPath(relativePath));
    }
  }

  static public String read(String path) {
    try {
      return of(path, ResolveResource::readLive);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  /**
   * Read the contents of the file at a path in to a string. This should only ever be called on
   * paths that are known to exist. That means that it should not be called on a path that is within a JAR
   * unless you know that the virtual filesystem of the JAR is alive.
   */
  static public String readLive(Path p) {
    try(var br = Files.newBufferedReader(p, StandardCharsets.UTF_8)) {
      return br.lines().collect(Collectors.joining("\n"));
    } catch (IOException err) {
      throw new UncheckedIOException(err);
    }
  }
}
