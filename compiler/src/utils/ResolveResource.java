package utils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

public final class ResolveResource {
  static private final Path assetRoot;
  static private final Path artefactRoot;
  static private FileSystem virtualFs;
  static{
    var url= ResolveResource.class.getResource("/base");
    if(url==null) {
      var root = Path.of("").toAbsolutePath().getParent();
      assetRoot = root.resolve("assets");
      artefactRoot = root.resolve("artefacts");
      assert Files.exists(assetRoot):assetRoot;
      assert Files.exists(artefactRoot):artefactRoot;
    } else {
      URI uri; try { uri= url.toURI();}
      catch (URISyntaxException e) { throw Bug.of(e); }
      var inBundle = uri.getScheme().equals("jar") || uri.getScheme().equals("resource");
      if (inBundle) {
        try {
          virtualFs = FileSystems.newFileSystem(uri, Map.of());
          assetRoot = virtualFs.getPath("/");
          artefactRoot = assetRoot;
        } catch (IOException e) {
          throw new UncheckedIOException(e);
        }
      } else {
        assetRoot = Path.of(uri).getParent();
        artefactRoot = assetRoot;
      }
    }
  }

  static public Path asset(String relativePath) {
    return of(assetRoot, relativePath);
  }
  static public Path artefact(String relativePath) {
    return of(artefactRoot, relativePath);
  }
  static private Path of(Path root, String relativePath) {
    assert relativePath.startsWith("/");
    if (virtualFs != null) {
      return virtualFs.getPath(relativePath);
    }
    URI absolutePath = root.resolve(relativePath.substring(1)).toUri();
    return Path.of(absolutePath);
  }

  static public String getAndReadAsset(String path) {
    return read(asset(path));
  }

  static public String read(Path path) {
    return IoErr.of(()->Files.readString(path, StandardCharsets.UTF_8));
  }
  static public Path freshTmpPath(){
    /*var res= Paths.get(
      System.getProperty("java.io.tmpdir"),
      "fearOut"+UUID.randomUUID());*/
    var res = ResolveResource.artefact("/.tmp")
      .resolve("fearOut"+UUID.randomUUID());
    IoErr.of(()->Files.createDirectories(res));
    DeleteOnExit.of(res);
    return res;
  }
}
