package codegen.go;

import utils.Bug;
import utils.ResolveResource;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public record GoCompiler(List<? extends Unit> units) {

  public sealed interface Unit permits Unit.Runtime, PackageCodegen.GoPackage {
    record Runtime(String name) implements Unit {}
  }

  public Path compile() {
    assert !units.isEmpty();
    try {
      var res = ResolveResource.of("/go-compilers/go-linux-amd64/go/bin/go", compiler->{
        String[] command = Stream.of(compiler.toString()).toArray(String[]::new);
        var pb = new ProcessBuilder(command);
        Process proc; try { proc = pb.inheritIO().start(); }
        catch (IOException e) { throw Bug.of(e); }
        return null;
      });
    } catch (IOException | URISyntaxException e) {
      throw Bug.of(e);
    }

    throw Bug.todo();
  }
}
