package program.typesystem;

import net.jqwik.api.Example;
import org.junit.jupiter.api.Test;

import static program.typesystem.RunTypeSystem.fail;
import static program.typesystem.RunTypeSystem.ok;

public class TestNoMutHyg {
  @Test void methMakingNoMutHygReturnsLent() { ok("""
    package test
    alias base.NoMutHyg as NoMutHyg,
    Box[X]:NoMutHyg[mdf X]{
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
@Test void methMakingNoMutHygReturnsLent2() { ok("""
    package test
    alias base.NoMutHyg as NoMutHyg,
    Box[X]:NoMutHyg[mdf X]{
      read .get: recMdf X
      }
    Box':{
      .mut2lent[X](x: mdf X): mut Box[mdf X] -> mut Box[mdf X]{ x }
      }
    Test:{
      #(t: read Test): lent Box[read Test] -> Box'.mut2lent(t),
      }
    """, """
    package base
    NoMutHyg[X]:{}
    """); }

  @Test void shouldKeepMdfIfXIsNotHyg() { ok("""
    package test
    alias base.NoMutHyg as NoMutHyg,
    Box[X]:NoMutHyg[mdf X]{
      read .get: recMdf X
      }
    Box':{
      .mut2mut[X](x: mdf X): mut Box[mdf X] -> { x }
      }
    Test:{
      #(t: Test): mut Box[imm Test] -> Box'.mut2mut(t),
      }
    """, """
    package base
    NoMutHyg[X]:{}
    """); }
  // TODO: multiple nomuthyg params (NoMutHyg[X] but not of Y)
  // TODO: interesting note about capturing "read this" in the case of NoMutHyg
}
