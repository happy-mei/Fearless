package main;

import utils.Bug;

import java.util.HashSet;

public record GenerateAliases(InputOutput io) implements LogicMain {
  public void printAliases() {
    System.out.println(generateAliases());
  }

  @Override public HashSet<String> cachedPkg() { throw Bug.unreachable(); }
}
