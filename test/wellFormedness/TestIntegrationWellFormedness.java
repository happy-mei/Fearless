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
    var p = Parser.parseAll(ps);
    var res = new WellFormednessVisitor().visitProgram(p);
    var isWellFormed = res.isEmpty();
    assertTrue(isWellFormed, res.map(Object::toString).orElse(""));
  }
  void fail(String expectedErr, String... content){
    Main.resetAll();
    AtomicInteger pi = new AtomicInteger();
    var ps = Arrays.stream(content)
      .map(code -> new Parser(Path.of("Dummy"+pi.getAndIncrement()+".fear"), code))
      .toList();

    try {
      var p = Parser.parseAll(ps);
      var error = new WellFormednessVisitor().visitProgram(p);
      Err.strCmp(expectedErr, error.map(Object::toString).orElse(""));
    } catch (CompileError e) {
      Err.strCmp(expectedErr, e.toString());
    }
  }
  @Test void noIsoParamsLambdaOk() { ok("""
    package pkg1
    Opt[T]:{}
    A:Opt[A]
    """); }
  @Test void noIsoParamsLambda1() { fail("""
    In position [###]/Dummy0.fear:3:2
    isoInTypeArgs:5
    The iso reference capability may not be used in type modifiers:
    iso pkg1.A[]
    """, """
    package pkg1
    Opt[T]:{}
    A:Opt[iso A]
    """); }
  @Test void noIsoParamsLambda2() { fail("""
    In position [###]/Dummy0.fear:3:2
    isoInTypeArgs:5
    The iso reference capability may not be used in type modifiers:
    iso pkg1.A[]
    """, """
    package pkg1
    Opt[T]:{}
    A:{ #: Opt[iso A] -> Opt[iso A] }
    """); }
  @Test void noIsoParamsLambdaNested1() { fail("""
    In position [###]/Dummy0.fear:3:2
    isoInTypeArgs:5
    The iso reference capability may not be used in type modifiers:
    iso pkg1.A[]
    """, """
    package pkg1
    Opt[T]:{}
    A:Opt[mut Opt[iso A]]
    """); }
  @Test void noIsoParamsLambdaNested2() { fail("""
    In position [###]/Dummy0.fear:4:2
    isoInTypeArgs:5
    The iso reference capability may not be used in type modifiers:
    iso pkg1.A[]
    """, """
    package pkg1
    Opt[T]:{}
    B[C,D]:{}
    A:B[Opt[A], Opt[Opt[iso A]]]
    """); }

  @Test void noIsoParamsAliasOk() { ok("""
    package pkg1
    Opt[T]:{}
    Opt[T]:{}
    alias Opt[pkg1.A] as OptA,
    A:{}
    """); }
  @Test void noIsoParamsAlias1() { fail("""
    In position [###]/Dummy0.fear:3:0
    isoInTypeArgs:5
    The iso reference capability may not be used in type modifiers:
    iso pkg1.A[]
    """, """
    package pkg1
    Opt[T]:{}
    Opt[T]:{}
    alias Opt[iso pkg1.A] as OptA,
    A:{}
    """); }
  @Test void noIsoParamsAliasNested1() { fail("""
    In position [###]/Dummy0.fear:3:0
    isoInTypeArgs:5
    The iso reference capability may not be used in type modifiers:
    iso pkg1.A[]
    """, """
    package pkg1
    Opt[T]:{}
    Opt[T]:{}
    alias Opt[Opt[iso pkg1.A]] as OptA,
    A:{}
    """); }

  @Test void noIsoParamsMethRet() { fail("""
    In position [###]/Dummy0.fear:3:2
    isoInTypeArgs:5
    The iso reference capability may not be used in type modifiers:
    iso pkg1.A[]
    """, """
    package pkg1
    Opt[T]:{}
    A:{ #: Opt[iso A] -> {} }
    """); }
  @Test void isoParamsMethParamsOk() { ok("""
    package pkg1
    Opt[T]:{}
    A:{ #(x: iso A): A -> {} }
    """); }
  @Test void isoParamsMethParams() { fail("""
    In position [###]/Dummy0.fear:3:2
    isoInTypeArgs:5
    The iso reference capability may not be used in type modifiers:
    iso pkg1.A[]
    """, """
    package pkg1
    Opt[T]:{}
    A:{ #(x: A[iso A]): A -> {} }
    """); }
  @Test void isoParamsMethParamsGens() { fail("""
    In position [###]/Dummy0.fear:3:2
    isoInTypeArgs:5
    The iso reference capability may not be used in type modifiers:
    iso GX[name=T]
    """, """
    package pkg1
    Opt[T]:{}
    A:{ #[T](x: A[iso T]): A -> {} }
    """); }
  @Test void isoParamsMethCall() { fail("""
    In position [###]/Dummy0.fear:3:2
    isoInTypeArgs:5
    The iso reference capability may not be used in type modifiers:
    iso pkg1.A[]
    """, """
    package pkg1
    Opt[T]:{}
    A:{
      #[T](x: A[mdf T]): A -> {},
      .foo(): A -> this#[iso A]A
      }
    """); }
  @Test void paramsMethCallOk() { ok("""
    package pkg1
    Opt[T]:{}
    A:{
      #[T](x: A[mdf T]): A -> {},
      .foo(): A -> this#[read A]A
      }
    """); }
}
