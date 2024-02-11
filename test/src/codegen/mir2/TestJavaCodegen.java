package codegen.mir2;

import ast.E;
import codegen.MIRInjectionVisitor;
import id.Id;
import main.Main;
import org.junit.jupiter.api.Test;
import parser.Parser;
import program.TypeSystemFeatures;
import program.inference.InferBodies;
import program.typesystem.EMethTypeSystem;
import utils.Base;
import utils.Err;
import wellFormedness.WellFormednessFullShortCircuitVisitor;
import wellFormedness.WellFormednessShortCircuitVisitor;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import codegen.java.JavaCodegen;

public class TestJavaCodegen {
  void ok(String expected, String entry, boolean loadBase, String... content) {
    assert content.length > 0;
    Main.resetAll();
    AtomicInteger pi = new AtomicInteger();
    String[] baseLibs = loadBase ? Base.baseLib : new String[0];
    var ps = Stream.concat(Arrays.stream(content), Arrays.stream(baseLibs))
      .map(code->new Parser(Path.of("Dummy" + pi.getAndIncrement() + ".fear"), code))
      .toList();
    var p = Parser.parseAll(ps, new TypeSystemFeatures());
    new WellFormednessFullShortCircuitVisitor().visitProgram(p).ifPresent(err->{
      throw err;
    });
    var inferred = InferBodies.inferAll(p);
    new WellFormednessShortCircuitVisitor(inferred).visitProgram(inferred);
    IdentityHashMap<E.MCall, EMethTypeSystem.TsT> resolvedCalls = new IdentityHashMap<>();
    inferred.typeCheck(resolvedCalls);
    var mir = new MIRInjectionVisitor(inferred, resolvedCalls).visitProgram();
    var java = new JavaCodegen(mir).visitProgram(new Id.DecId(entry, 0));
    Err.strCmp(expected, java);
  }

  @Test void emptyProgram() { ok("""
    """, "fake.Fake", false, """
    package test
    """, Base.minimalBase);}

  @Test void capturing() { ok("""
    """, "fake.Fake", false, """
    package test
    FPerson: { #(age: Num): mut Person -> mut Person: {
      read .age: Num -> age,
      }}
    Usage: {
      #: Num -> FPerson#FortyTwo.age,
      }
    Num: {}
    FortyTwo: Num
    """, Base.minimalBase);}

  @Test void capturingDeep() { ok("""
    """, "fake.Fake", false, """
    package test
    Person: {
      read .age: Num,
      mut .wrap: mut Person -> {'self
       .age -> this.age.plus1,
       .wrap -> {'topLevelWrapped
         .age -> self.age.plus1,
         }
       },
      }
    FPerson: { #(age: Num): mut Person -> {'original
      .age -> age,
      }}
    Usage: {
      #: Num -> FPerson#FortyTwo.wrap.age,
      }
    Num: {
      .plus1: Num,
      }
    FortyTwo: Num{ .plus1 -> FortyThree }
    FortyThree: Num{ .plus1 -> FortyFour }
    FortyFour: Num{ .plus1 -> this.plus1 }
    """, Base.minimalBase);}


  @Test void simpleProgram() { ok("""
    """, "fake.Fake", false, """
    package test
    Baz[X]:{ #: X }
    Bar:Baz[Foo]{ # -> Foo, .loop: Baz[Bar] -> this.loop }
    Ok:{ #: Ok }
    Yo:{ .lm: Ok -> {'ok ok# } }
    Foo:{}
    """, Base.minimalBase);}

  @Test void bools() {ok("""
    """, "fake.Fake", false, """
    package test
    Sealed:{}
    Bool:Sealed{
      .and(b: Bool): Bool,
      .or(b: Bool): Bool,
      .not: Bool,
      ?[R](f: mut ThenElse[R]): R, // ?  because `bool ? { .then->aa, .else->bb }` is kinda like a ternary
      }
    True:Bool{ .and(b) -> b, .or(b) -> this, .not -> False, ?(f) -> f.then() }
    False:Bool{ .and(b) -> this, .or(b) -> b, .not -> True, ?(f) -> f.else() }
    ThenElse[R]:{ mut .then: R, mut .else: R, }
    Fear1:{}
    """, Base.minimalBase);}
  @Test void multiPackage() { ok("""
    """, "test.HelloWorld", false, """
    package test
    alias base.Main as Main,
    HelloWorld:Main{
      #s -> base.Void
    }
    """, Base.minimalBase); }

  @Test void nestedPkgs() { ok("""
    """, "test.Test", false, """
    package test
    Test:base.Main[]{ _ -> {} }
    A:{ #: test.foo.Bar -> { .a -> test.foo.Bar } }
    Foo:{ .a: Foo }
    """, """
    package test.foo
    Bar:test.Foo{ .a -> this }
    """, Base.minimalBase);}
}
