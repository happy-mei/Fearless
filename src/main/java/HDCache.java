package main.java;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ast.E.Sig;
import ast.T.Dec;
import files.Pos;
import id.Id.MethName;
import utils.IoErr;
import ast.E;

public record HDCache(Path code) {
  public HDCache{ assert Files.exists(code) && Files.isDirectory(code):code; }
  public void cacheBase(Path cachedBase) {
    IoErr.of(()->_cacheBase(cachedBase));
  }
  private void _cacheBase(Path cachedBase) throws IOException {
    assert Files.exists(cachedBase) && Files.isDirectory(cachedBase);    
    Path basePath = code.resolve("base");
    // Check if basePath exists and is a directory
    if (!Files.exists(basePath)){ return; }
    assert Files.isDirectory(basePath);
    Path targetPath = cachedBase.resolve("base");
    if(!Files.exists(targetPath)){
      Files.move(basePath, targetPath);
    }
  }
  public void cacheTypeInfo(String pkgName, List<Dec> decs) {
    var pkg= code.resolve(pkgName.replace(".","/"));
    assert Files.exists(pkg) && Files.isDirectory(pkg):pkg;
    var v= new DecTypeInfo();
    var file=decs.stream().map(v::visitDec).toList();
    String tot="package "+pkgName+"\n"+file.stream()
      .collect(Collectors.joining());
    System.out.println("FILE");
    System.out.println(pkg.resolve("pkgInfo.txt").toAbsolutePath());
    System.out.println(tot);
    IoErr.of(()->Files.writeString(pkg.resolve("pkgInfo.txt"),tot));
  }
}