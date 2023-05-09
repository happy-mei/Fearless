package utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public interface Base {
  static ast.Program ignoreBase(ast.Program p) {
    return new ast.Program(
      p.ds().entrySet().stream()
        .filter(kv->!kv.getKey().name().startsWith("base."))
        .collect(Collectors.toMap(kv->kv.getKey(), kv->kv.getValue()))
    );
  }

  static String load(String file) {
    var in = Thread.currentThread().getContextClassLoader().getResourceAsStream("base/"+file);
    assert in != null: "base/"+file+" is not present";
    return new BufferedReader(new InputStreamReader(in)).lines().collect(Collectors.joining("\n"));
  }

  String[] baseLib = {
    load("lang.fear"),
    load("caps/caps.fear"),
    load("bools.fear"),
    load("nums.fear"),
    load("strings.fear"),
    load("assertions.fear"),
    load("ref.fear"),
    load("optionals.fear"),
    load("block.fear"),
    load("errors.fear"),
  };

  String minimalBase = """
    package base
    Main[R]:{ #(s: lent System[R]): mdf R }
    NoMutHyg[X]:{}
    Sealed:{}
    Void:{}
    
    System[R]:Sealed{}
    """;
}
