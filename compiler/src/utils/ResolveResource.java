package utils;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

public record ResolveResource(
    Path assetRoot, Path artefactRoot, FileSystem virtualFs
    ){
  static private final ResolveResource instance= 
    ResolveResource.infer();
    //ResolveResource.of(Paths.get("C:/")
    //  .resolve("Users/sonta/Documents/GitHub/Fearless"));
  //Infer works if we put the two data folders in the class path

  public ResolveResource{
    assert Files.exists(assetRoot):assetRoot;
    assert Files.exists(artefactRoot):artefactRoot;
  }
  static ResolveResource of(Path p) {
    return new ResolveResource(p.resolve("assets"),p.resolve("artefacts"),null);
  }
  static ResolveResource infer() {
    var url= ResolveResource.class.getResource("/base");
    if(url==null) { //Here we are in???? TODO:
      var root = Path.of("").toAbsolutePath().getParent();
      return new ResolveResource(
        root.resolve("assets"),root.resolve("artefacts"),null);
    }
    URI uri; try { uri= url.toURI();}
    catch (URISyntaxException e) { throw Bug.of(e); }
    var inBundle = uri.getScheme().equals("jar")
      || uri.getScheme().equals("resource");
    if (!inBundle){ //Here we are in???? TODO:
      Path.of(uri).getParent();
      var both= Path.of(uri).getParent();
      return new ResolveResource(both,both,null);
    }
    return IoErr.of(()->{
      var virtualFs= FileSystems.newFileSystem(uri, Map.of());
      var both= virtualFs.getPath("/");
      return new ResolveResource(both,both,virtualFs);
      //TODO: how can this work? can we write in the Jar?
    });
  }
  static public Path asset(String relativePath) {
    return of(instance.assetRoot, relativePath);
  }
  static public Path artefact(String relativePath) {
    return of(instance.artefactRoot, relativePath);
  }
  static private Path of(Path root, String relativePath) {
    assert relativePath.startsWith("/");
    if (instance.virtualFs != null) {
      return instance.virtualFs.getPath(relativePath);
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
