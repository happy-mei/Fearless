package program.typesystem;

import net.jqwik.api.Disabled;
import net.jqwik.api.Example;

import static program.typesystem.RunTypeSystem.fail;
import static program.typesystem.RunTypeSystem.ok;

public class TestNoMutHyg {
  // TODO: there's an inference bug here where we get `Box[recMdf X]{x}` for the body of .mut2lent
//  @Example void methMakingNoMutHygReturnsLent() { ok("""
//    package test
//    alias base.NoMutHyg as NoMutHyg,
//    Box[X]:NoMutHyg[mdf X]{
//      read .get: recMdf X
//      }
//    Box':{
//      .mut2lent[X](x: mdf X): mut Box[mdf X] -> { x }
//      }
//    Test:{
//      #(t: read Test): lent Box[read Test] -> Box'.mut2lent(t),
//      }
//    """, """
//    package base
//    NoMutHyg[X]:{}
//    """); }
@Example void methMakingNoMutHygReturnsLent() { ok("""
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
  @Example void methMakingNoMutHygCannotBeMutIfHyg() { fail("""
    In position [###]/Dummy0.fear:10:45
    [E39 incompatibleMdfs]
    The modifiers for mut test.Box[read test.Test[]] and lent test.Box[read test.Test[]] are not compatible.
    This could be a case of NoMutHyg applying a more restrictive modifier than written.
    """, """
    package test
    alias base.NoMutHyg as NoMutHyg,
    Box[X]:NoMutHyg[mdf X]{
      read .get: recMdf X
      }
    Box':{
      .mut2lent[X](x: mdf X): mut Box[mdf X] -> { x }
      }
    Test:{
      #(t: read Test): mut Box[read Test] -> Box'.mut2lent(t),
      }
    """, """
    package base
    NoMutHyg[X]:{}
    """); }

  @Example void shouldKeepMdfIfXIsNotHyg() { ok("""
    package test
    alias base.NoMutHyg as NoMutHyg,
    Box[X]:NoMutHyg[mdf X]{
      read .get: recMdf X
      }
    Box':{
      .mut2mut[X](x: mdf X): mut Box[mdf X] -> { x }
      }
    Test:{
      #(t: Test): mut Box[Test] -> Box'.mut2mut(t),
      }
    """, """
    package base
    NoMutHyg[X]:{}
    """); }
}
