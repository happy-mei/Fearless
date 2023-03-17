package program.typesystem;

import net.jqwik.api.Example;
import static program.typesystem.RunTypeSystem.*;

public class TestNoMutHyg {
  @Example void methMakingNoMutHygReturnsLent() { ok("""
    package test
    alias base.NoMutHyg as NoMutHyg,
    Box[X]:NoMutHyg[X]{
      read .get: recMdf X
      }
    Box':{
      .mut2lent[X](x: mdf X): mut Box[mdf X] -> { x }
      }
    Test:{
      #(t: read Test): lent Box[read Test] -> Box'.mut2lent(t),
      }
    """, """
    package base
    NoMutHyg[X]:{}
    """); }
}
