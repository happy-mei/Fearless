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
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

public final class ResolveResource {
  static private final Path root;
  static private FileSystem virtualFs;
  static{
    var url= ResolveResource.class.getResource("/base");
    if(url==null) {
      String workingDir = System.getProperty("user.dir");
      root = Path.of(workingDir).resolve("resources");
      assert Files.exists(root):root;
    } else {
      URI uri; try { uri= url.toURI();}
      catch (URISyntaxException e) { throw Bug.of(e); }
      var inBundle = uri.getScheme().equals("jar") || uri.getScheme().equals("resource");
      if (inBundle) {
        try {
          virtualFs = FileSystems.newFileSystem(uri, Map.of());
          root = virtualFs.getPath("/");
        } catch (IOException e) {
          throw new UncheckedIOException(e);
        }
      } else {
        root=Path.of(uri).getParent();
      }
    }
  }
  static public Path of(String relativePath) {
    assert relativePath.startsWith("/");
    if (virtualFs != null) {
      return virtualFs.getPath(relativePath);
    }
    URI absolutePath= root.resolve(relativePath.substring(1)).toUri();
    return Path.of(absolutePath);
  }

  static public String getAndRead(String path) {
    return read(of(path));
  }

  static public String read(Path path) {
    return IoErr.of(()->Files.readString(path, StandardCharsets.UTF_8));
  }
  static public Path freshTmpPath(){
    /*var res= Paths.get(
      System.getProperty("java.io.tmpdir"),
      "fearOut"+UUID.randomUUID());*/
    var res=ResolveResource.of("/tempFiles")
      .resolve("fearOut"+UUID.randomUUID());
    //NOTE: may not work in the Jar, but we should not
    //  use this in the Jar anyway
    if(tempToKill.isEmpty()){
      Runtime.getRuntime().addShutdownHook(new Thread(() -> {
        for(var f:tempToKill){ deleteOldFiles(f); }
      }));}
    tempToKill.add(res);
    return res;
  }
  static public void deleteOldFiles(Path output){ 
    IoErr.of(()->_deleteOldFiles(output));
  }
  static private void _deleteOldFiles(Path output) throws IOException{
    if (!Files.exists(output)) { return; }
    try (Stream<Path> walk = Files.walk(output)) {
      Iterable<Path> ps=walk.sorted(Comparator.reverseOrder())::iterator;
      for(Path p:ps){
        if(p.equals(output)){ continue; }
        Files.deleteIfExists(p); 
        }
    }
    Files.deleteIfExists(output);
  }
  static private final List<Path> tempToKill=new ArrayList<>();
}
