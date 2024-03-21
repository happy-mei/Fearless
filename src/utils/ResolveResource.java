package utils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

interface ResolveResourceNick {  
  static <R> R of(String root, Function<Path, R> f) throws IOException, URISyntaxException {
    var top= ResolveResourceNick.class.getResource(root);
    assert top!=null :"root was "+root;
    var topURI= top.toURI();
    if (!topURI.getScheme().equals("jar") && !topURI.getScheme().equals("resource")) {
      return f.apply(Path.of(topURI));
    }
    try(var fs = FileSystems.newFileSystem(topURI, Map.of())) {
      return f.apply(fs.getPath(root));
    }
  }

  static String getStringOrThrow(String path) {
    try {
      return of(path, ThrowingFunction.of(ResolveResourceNick::read));
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


public class ResolveResource{
  private static final String srcRootTag= "target";
  private static final String resourcesLoc= "resources";

  /*static public String getString(String path) {
    return read(of(path));
  }*/
  static public Path of(String s){
    var res= root.resolve(resourcesLoc).resolve(s);
    assert Files.exists(res);
    return res;
  }
  static public String read(Path p){
    try(var br = Files.newBufferedReader(p, StandardCharsets.UTF_8)) {
      return br.lines().collect(Collectors.joining("\n"));
    }
    catch(IOException ioe){ throw Bug.of(ioe); }
  }
  public final static URI rootURI= myPlace();
  public final static Path root= myRoot();

  static private URI myPlace(){
    var me= ResolveResource.class;
    String myName= me.getSimpleName();
    var url= me.getResource(myName+".class");
    assert url!=null:"Somehow can not self-locate "+myName;
    try { return url.toURI();}
    catch (URISyntaxException e) { throw Bug.of(e); }
  }
  static private Path jarPath(){
    try(var fs = FileSystems.newFileSystem(rootURI, Map.of())) {
      return fs.getPath(".");
    }
    catch (IOException e) { throw Bug.of(e); }
  }
  static private Path myRoot(){
    var noJar= !rootURI.getScheme().equals("jar");
    //&& !rootURI.getScheme().equals("resource")
    Path p= noJar?Path.of(rootURI):jarPath();
    while (!p.endsWith(Path.of(srcRootTag))){
      p = p.getParent();
    }
    return p.getParent();
  }
}