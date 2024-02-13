package codegen.go;

import ast.E;
import codegen.MIRInjectionVisitor;
import codegen.java.JavaCodegen;
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

public class TestGoCodegen {
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
    var res = new GoCodegen(mir).visitProgram(new Id.DecId(entry, 0));
    Err.strCmp(expected, res.toString());
  }

  @Test void emptyProgram() { ok("""
package main
type baseΦSystem_1 interface {
}
type baseΦSystem_1Impl struct {}
type baseΦSealed_0 interface {
}
type baseΦSealed_0Impl struct {}
type baseΦNoMutHyg_1 interface {
}
type baseΦNoMutHyg_1Impl struct {}
type baseΦVoid_0 interface {
}
type baseΦVoid_0Impl struct {}
type baseΦMain_0 interface {
Φ35Φ(sΦ baseΦSystem_1) baseΦVoid_0
}
type baseΦMain_0Impl struct {}

func main(){
var entry baseΦMain_0 = fakeΦFake_0Impl{}
entry.Φ35Φ(baseΦ46capsΦΦ95System_0{})
}
    """, "fake.Fake", false, """
    package test
    """, Base.minimalBase);}

  @Test void simpleProgram() { ok("""
    package main
    type testΦBar_0 interface {
    loopΦ() testΦBaz_1
    Φ35Φ() testΦFoo_0
    }
    type testΦBar_0Impl struct {}
    func (this testΦBar_0Impl) loopΦ() testΦBaz_1 {
     return this.loopΦ()
    }
    func (this testΦBar_0Impl) Φ35Φ() testΦFoo_0 {
     return testΦFoo_0Impl{}
    }
    type testΦFoo_0 interface {
    }
    type testΦFoo_0Impl struct {}
    type testΦOk_0 interface {
    Φ35Φ() testΦOk_0
    }
    type testΦOk_0Impl struct {}
    type testΦBaz_1 interface {
    Φ35Φ() interface{}
    }
    type testΦBaz_1Impl struct {}
    type testΦYo_0 interface {
    lmΦ() testΦOk_0
    }
    type testΦYo_0Impl struct {}
    func (this testΦYo_0Impl) lmΦ() testΦOk_0 {
     return testΦOk_0Impl{}
    }
    type baseΦSystem_1 interface {
    }
    type baseΦSystem_1Impl struct {}
    type baseΦSealed_0 interface {
    }
    type baseΦSealed_0Impl struct {}
    type baseΦNoMutHyg_1 interface {
    }
    type baseΦNoMutHyg_1Impl struct {}
    type baseΦVoid_0 interface {
    }
    type baseΦVoid_0Impl struct {}
    type baseΦMain_0 interface {
    Φ35Φ(sΦ baseΦSystem_1) baseΦVoid_0
    }
    type baseΦMain_0Impl struct {}
        
    func main(){
    var entry baseΦMain_0 = fakeΦFake_0Impl{}
    entry.Φ35Φ(baseΦ46capsΦΦ95System_0{})
    }
    """, "fake.Fake", false, """
    package test
    Baz[X]:{ #: X }
    Bar:Baz[Foo]{ # -> Foo, .loop: Baz[Bar] -> this.loop }
    Ok:{ #: Ok }
    Yo:{ .lm: Ok -> {'ok ok# } }
    Foo:{}
    """, Base.minimalBase);}

  @Test void bools() {ok("""
package main
type testΦTrue_0 interface {
notΦ() testΦBool_0
Φ63Φ(fΦ interface{}) interface{}
orΦ(bΦ testΦBool_0) testΦBool_0
andΦ(bΦ testΦBool_0) testΦBool_0
}
type testΦTrue_0Impl struct {}
func (this testΦTrue_0Impl) notΦ() testΦBool_0 {
 return testΦFalse_0Impl{}
}
func (this testΦTrue_0Impl) Φ63Φ(fΦ interface{}) interface{} {
 return fΦ.(testΦThenElse_1).thenΦ()
}
func (this testΦTrue_0Impl) orΦ(bΦ testΦBool_0) testΦBool_0 {
 return this
}
func (this testΦTrue_0Impl) andΦ(bΦ testΦBool_0) testΦBool_0 {
 return bΦ.(testΦBool_0)
}
type testΦFalse_0 interface {
notΦ() testΦBool_0
Φ63Φ(fΦ interface{}) interface{}
orΦ(bΦ testΦBool_0) testΦBool_0
andΦ(bΦ testΦBool_0) testΦBool_0
}
type testΦFalse_0Impl struct {}
func (this testΦFalse_0Impl) notΦ() testΦBool_0 {
 return testΦTrue_0Impl{}
}
func (this testΦFalse_0Impl) Φ63Φ(fΦ interface{}) interface{} {
 return fΦ.(testΦThenElse_1).elseΦ()
}
func (this testΦFalse_0Impl) orΦ(bΦ testΦBool_0) testΦBool_0 {
 return bΦ.(testΦBool_0)
}
func (this testΦFalse_0Impl) andΦ(bΦ testΦBool_0) testΦBool_0 {
 return this
}
type testΦThenElse_1 interface {
thenΦ() interface{}
elseΦ() interface{}
}
type testΦThenElse_1Impl struct {}
type testΦBool_0 interface {
notΦ() testΦBool_0
Φ63Φ(fΦ interface{}) interface{}
orΦ(bΦ testΦBool_0) testΦBool_0
andΦ(bΦ testΦBool_0) testΦBool_0
}
type testΦBool_0Impl struct {}
type testΦSealed_0 interface {
}
type testΦSealed_0Impl struct {}
type baseΦSystem_1 interface {
}
type baseΦSystem_1Impl struct {}
type baseΦSealed_0 interface {
}
type baseΦSealed_0Impl struct {}
type baseΦNoMutHyg_1 interface {
}
type baseΦNoMutHyg_1Impl struct {}
type baseΦVoid_0 interface {
}
type baseΦVoid_0Impl struct {}
type baseΦMain_0 interface {
Φ35Φ(sΦ baseΦSystem_1) baseΦVoid_0
}
type baseΦMain_0Impl struct {}

func main(){
var entry baseΦMain_0 = fakeΦFake_0Impl{}
entry.Φ35Φ(baseΦ46capsΦΦ95System_0{})
}
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
    """, Base.minimalBase);}
  @Test void multiPackage() { ok("""
package main
type testΦHelloWorld_0 interface {
Φ35Φ(sΦ baseΦSystem_1) baseΦVoid_0
}
type testΦHelloWorld_0Impl struct {}
func (this testΦHelloWorld_0Impl) Φ35Φ(sΦ baseΦSystem_1) baseΦVoid_0 {
 return baseΦVoid_0Impl{}
}
type baseΦSystem_1 interface {
}
type baseΦSystem_1Impl struct {}
type baseΦSealed_0 interface {
}
type baseΦSealed_0Impl struct {}
type baseΦNoMutHyg_1 interface {
}
type baseΦNoMutHyg_1Impl struct {}
type baseΦVoid_0 interface {
}
type baseΦVoid_0Impl struct {}
type baseΦMain_0 interface {
Φ35Φ(sΦ baseΦSystem_1) baseΦVoid_0
}
type baseΦMain_0Impl struct {}

func main(){
var entry baseΦMain_0 = testΦHelloWorld_0Impl{}
entry.Φ35Φ(baseΦ46capsΦΦ95System_0{})
}
    """, "test.HelloWorld", false, """
    package test
    alias base.Main as Main,
    HelloWorld:Main{
      #s -> base.Void
    }
    """, Base.minimalBase); }

  @Test void nestedPkgs() { ok("""
package main
type testΦFoo_0 interface {
aΦ() testΦFoo_0
}
type testΦFoo_0Impl struct {}
type testΦTest_0 interface {
Φ35Φ(fear0$Φ baseΦSystem_1) baseΦVoid_0
}
type testΦTest_0Impl struct {}
func (this testΦTest_0Impl) Φ35Φ(fear0$Φ baseΦSystem_1) baseΦVoid_0 {
 return baseΦVoid_0Impl{}
}
type testΦA_0 interface {
Φ35Φ() testΦ46fooΦBar_0
}
type testΦA_0Impl struct {}
func (this testΦA_0Impl) Φ35Φ() testΦ46fooΦBar_0 {
 return testΦ46fooΦBar_0Impl{}
}
type testΦ46fooΦBar_0 interface {
aΦ() testΦFoo_0
}
type testΦ46fooΦBar_0Impl struct {}
func (this testΦ46fooΦBar_0Impl) aΦ() testΦFoo_0 {
 return this
}
type baseΦSystem_1 interface {
}
type baseΦSystem_1Impl struct {}
type baseΦSealed_0 interface {
}
type baseΦSealed_0Impl struct {}
type baseΦNoMutHyg_1 interface {
}
type baseΦNoMutHyg_1Impl struct {}
type baseΦVoid_0 interface {
}
type baseΦVoid_0Impl struct {}
type baseΦMain_0 interface {
Φ35Φ(sΦ baseΦSystem_1) baseΦVoid_0
}
type baseΦMain_0Impl struct {}

func main(){
var entry baseΦMain_0 = testΦTest_0Impl{}
entry.Φ35Φ(baseΦ46capsΦΦ95System_0{})
}
    """, "test.Test", false, """
    package test
    Test:base.Main[]{ _ -> {} }
    A:{ #: test.foo.Bar -> { .a -> test.foo.Bar } }
    Foo:{ .a: Foo }
    """, """
    package test.foo
    Bar:test.Foo{ .a -> this }
    """, Base.minimalBase);}

  @Test void fullBase() { ok("""
    """, "test.Test", true, """
    package test
    alias base.Main as Main,
    alias base.Void as Void,
    Test:Main{ _ -> "hi" }
    """); }
}
