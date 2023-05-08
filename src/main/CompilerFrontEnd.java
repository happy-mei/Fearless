package main;

import astFull.Package;
import codegen.MIRInjectionVisitor;
import codegen.java.JavaCodegen;
import codegen.java.JavaProgram;
import ast.Program;
import astFull.T;
import id.Id;
import parser.Parser;
import program.inference.InferBodies;
import utils.Box;
import utils.Bug;
import wellFormedness.WellFormednessFullShortCircuitVisitor;
import wellFormedness.WellFormednessShortCircuitVisitor;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public record CompilerFrontEnd(BaseVariant bv) {
  enum BaseVariant { Std, Imm }
  static Box<Map<String, List<Package>>> immBaseLib = new Box<>(null);

  void newPkg(String name) {
    // TODO: check valid package name
    try {
      var dir = Path.of(name);
      Files.createDirectory(dir);
      Files.writeString(dir.resolve("pkg.fear"), "package "+name+"\n"+regenerateAliases()+"\n");
      Files.writeString(dir.resolve("lib.fear"), "package "+name+"\nExampleTrait:Main{ \"Hello, World!\" }\n");
    } catch (IOException err) {
      System.err.println("Error creating package structure: "+ err);
      System.exit(1);
    }
  }

  void run(String entryPoint, String[] files) {
    var entry = new Id.DecId(entryPoint, 0);
    var p = compile(files);
    var java = toJava(entry, p);
    var classFile = java.compile();

    var pb = new ProcessBuilder("java", classFile.getFileName().toString().split("\\.class")[0]);
    pb.directory(classFile.getParent().toFile());
    Process proc; try { proc = pb.start();
      proc.getInputStream().transferTo(System.out);
      proc.getErrorStream().transferTo(System.err);
    } catch (IOException e) {
      throw Bug.of(e);
    }

    proc.onExit().join();
    System.exit(proc.exitValue());
  }

  Program compile(String[] files) {
    var p = Parser.parseAll(parseBase());
    new WellFormednessFullShortCircuitVisitor().visitProgram(p).ifPresent(err->{ throw err; });
    var inferredSigs = p.inferSignaturesToCore();
    var inferred = new InferBodies(inferredSigs).inferAll(p);
    new WellFormednessShortCircuitVisitor(inferred).visitProgram(inferred);
    inferred.typeCheck();
    // TODO: compile user code
    return inferred;
  }
  private JavaProgram toJava(Id.DecId entry, Program p) {
    var mir = new MIRInjectionVisitor(p).visitProgram();
    var src = new JavaCodegen(p).visitProgram(mir.pkgs(), entry);
    return new JavaProgram(src);
  }

  String regenerateAliases() {
    return parseBase().values().stream()
      .flatMap(Collection::stream)
      .flatMap(pkg->pkg.shallowParse().stream())
      .map(T.Dec::name)
      .filter(dec->!dec.shortName().startsWith("_"))
      .map(dec->"alias "+dec.name()+" as "+dec.shortName()+",")
      .distinct()
      .collect(Collectors.joining("\n"));
  }

  Map<String, List<Package>> parseBase() {
    Map<String, List<Package>> ps; try { ps =
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
  static Map<String, List<Package>> load(String root) throws URISyntaxException, IOException {
    var top = Paths.get(requireNonNull(CompilerFrontEnd.class.getResource(root)).toURI());
    try(var fs = Files.walk(top)) {
      return fs
        .filter(p->p.toFile().isFile())
        .map(path->new Parser(path, read(path)).parseFile(Bug::err))
        .collect(Collectors.groupingBy(Package::name));
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
