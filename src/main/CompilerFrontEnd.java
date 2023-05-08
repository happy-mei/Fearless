package main;

import astFull.Package;
import astFull.T;
import parser.Parser;
import utils.Box;
import utils.Bug;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public record CompilerFrontEnd(BaseVariant bv) {
  enum BaseVariant { Std, Imm }
  static Box<List<Package>> immBaseLib = new Box<>(null);

  void newPkg(String name) {
    // TODO: check valid package name
    try {
      var dir = Path.of(name);
      Files.createDirectory(dir);
      Files.writeString(dir.resolve("pkg.fear"), "package "+name+"\n"+regenerateAliases()+"\n");
      Files.writeString(dir.resolve("lib.fear"), "package "+name+"\nExampleTrait:Main{ #: Str -> \"Hello, World!\" }\n");
    } catch (IOException err) {
      System.err.println("Error creating package structure: "+ err);
      System.exit(1);
    }
  }

  String regenerateAliases() {
    return parseBase().stream()
      .flatMap(pkg->pkg.shallowParse().stream())
      .map(T.Dec::name)
      .filter(dec->!dec.shortName().startsWith("_"))
      .map(dec->"alias "+dec.name()+" as "+dec.shortName()+",")
      .distinct()
      .collect(Collectors.joining("\n"));
  }

  List<Package> parseBase() {
    List<Package> ps; try { ps =
      switch (bv) {
        case Std -> throw Bug.todo();
        case Imm -> {
          var res = immBaseLib.get();
          if (res == null) { res = load("/immBase"); immBaseLib.set(res); }
          yield res;
        }
      };
    } catch (URISyntaxException | IOException e) {
      throw Bug.of(e);
    }
    return ps;
  }
  static List<Package> load(String root) throws URISyntaxException, IOException {
    var top = Paths.get(requireNonNull(CompilerFrontEnd.class.getResource(root)).toURI());
    try(var fs = Files.walk(top)) {
      return fs
        .filter(p->p.toFile().isFile())
        .map(path->new Parser(path, read(path)).parseFile(Bug::err))
        .toList();
    }
  }
  static String read(Path p) {
    try(var br = Files.newBufferedReader(p, StandardCharsets.UTF_8)) {
      return br.lines().collect(Collectors.joining("\n"));
    } catch (IOException e) {
      throw Bug.of(e);
    }
  }
}
