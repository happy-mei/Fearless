package wellFormedness;

import main.CompileError;
import main.Main;
import org.junit.jupiter.api.Test;
import parser.Parser;
import utils.Err;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestIntegrationWellFormedness {
  void ok(String... content){
    Main.resetAll();
    AtomicInteger pi = new AtomicInteger();
    var ps = Arrays.stream(content)
      .map(code -> new Parser(Path.of("Dummy"+pi.getAndIncrement()+".fear"), code))
      .toList();
    var decTable = Parser.parseAll(ps);
    var res = decTable.values().stream()
      .map(d->d.accept(new WellFormednessVisitor()))
      .toList();
    var isWellFormed = res.stream().allMatch(Optional::isEmpty);
    assertTrue(isWellFormed, res.stream().filter(Optional::isPresent).map(Optional::get).toList().toString());
  }
  void fail(String expectedErr, String... content){
    Main.resetAll();
    AtomicInteger pi = new AtomicInteger();
    var ps = Arrays.stream(content)
      .map(code -> new Parser(Path.of("Dummy"+pi.getAndIncrement()+".fear"), code))
      .toList();

    try {
      var decTable = Parser.parseAll(ps);
      var errors = decTable.values().stream()
        .map(d->d.accept(new WellFormednessVisitor()))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .toList();
      Err.strCmp(expectedErr, errors.toString());
    } catch (CompileError e) {
      Err.strCmp(expectedErr, e.toString());
    }
  }
  @Test void noIsoParamsLambdaOk() { ok("""
    package pkg1
    A:base.Opt[A]
    """); }
  @Test void noIsoParamsLambda1() { fail("""
    [In position [###]/Dummy0.fear:2:2
    isoInTypeArgs:5
    The iso reference capability may not be used in type modifiers:
    iso pkg1.A[]]
    """, """
    package pkg1
    A:base.Opt[iso A]
    """); }
  @Test void noIsoParamsLambda2() { fail("""
    [In position [###]/Dummy0.fear:2:2
    isoInTypeArgs:5
    The iso reference capability may not be used in type modifiers:
    iso pkg1.A[]]
    """, """
    package pkg1
    A:{ #: base.Opt[iso A] -> base.Opt[iso A] }
    """); }
  @Test void noIsoParamsLambdaNested1() { fail("""
    [In position [###]/Dummy0.fear:2:2
    isoInTypeArgs:5
    The iso reference capability may not be used in type modifiers:
    iso pkg1.A[]]
    """, """
    package pkg1
    A:base.Opt[mut base.Opt[iso A]]
    """); }
  @Test void noIsoParamsLambdaNested2() { fail("""
    [In position [###]/Dummy0.fear:2:2
    isoInTypeArgs:5
    The iso reference capability may not be used in type modifiers:
    iso pkg1.A[]]
    """, """
    package pkg1
    A:base.Opt[base.Opt[A], base.Opt[base.Opt[iso A]]]
    """); }

  // TODO: Alias generic params cannot use other aliases right now (i.e. pkg1.A vs A)
  @Test void noIsoParamsAliasOk() { ok("""
    package pkg1
    alias base.Opt as Opt,
    alias base.Opt[pkg1.A] as OptA,
    A:{}
    """); }
  @Test void noIsoParamsAlias1() { fail("""
    In position [###]/Dummy0.fear:3:0
    isoInTypeArgs:5
    The iso reference capability may not be used in type modifiers:
    iso pkg1.A[]
    """, """
    package pkg1
    alias base.Opt as Opt,
    alias base.Opt[iso pkg1.A] as OptA,
    A:{}
    """); }
  @Test void noIsoParamsAliasNested1() { fail("""
    In position [###]/Dummy0.fear:3:0
    isoInTypeArgs:5
    The iso reference capability may not be used in type modifiers:
    iso pkg1.A[]
    """, """
    package pkg1
    alias base.Opt as Opt,
    alias base.Opt[base.Opt[iso pkg1.A]] as OptA,
    A:{}
    """); }

  @Test void noIsoParamsMethRet() { fail("""
    [In position [###]/Dummy0.fear:2:2
    isoInTypeArgs:5
    The iso reference capability may not be used in type modifiers:
    iso pkg1.A[]]
    """, """
    package pkg1
    A:{ #: base.Opt[iso A] -> {} }
    """); }
  @Test void isoParamsMethParamsOk() { ok("""
    package pkg1
    A:{ #(x: iso A): A -> {} }
    """); }
  @Test void isoParamsMethParams() { fail("""
    [In position [###]/Dummy0.fear:2:2
    isoInTypeArgs:5
    The iso reference capability may not be used in type modifiers:
    iso pkg1.A[]]
    """, """
    package pkg1
    A:{ #(x: A[iso A]): A -> {} }
    """); }
  @Test void isoParamsMethParamsGens() { fail("""
    [In position [###]/Dummy0.fear:2:2
    isoInTypeArgs:5
    The iso reference capability may not be used in type modifiers:
    iso GX[name=T]]
    """, """
    package pkg1
    A:{ #[T](x: A[iso T]): A -> {} }
    """); }
  @Test void isoParamsMethCall() { fail("""
    [In position [###]/Dummy0.fear:2:2
    isoInTypeArgs:5
    The iso reference capability may not be used in type modifiers:
    iso pkg1.A[]]
    """, """
    package pkg1
    A:{
      #[T](x: A[mdf T]): A -> {},
      .foo(): A -> this#[iso A]A
      }
    """); }
  @Test void paramsMethCallOk() { ok("""
    package pkg1
    A:{
      #[T](x: A[mdf T]): A -> {},
      .foo(): A -> this#[read A]A
      }
    """); }
}
