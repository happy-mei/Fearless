package codegen.mir2;

import ast.E;
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
import codegen.mir2.java.JavaCodegen;

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
    var java = new JavaCodegen(mir).visitProgram(mir, new Id.DecId(entry, 0));
    Err.strCmp(expected, java);
  }

  @Test void emptyProgram() { ok("""
    """, "fake.Fake", false, """
    package test
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
    """, Base.minimalBase);}
  @Test void multiPackage() { ok("""
class FearlessError extends RuntimeException {
  public FProgram.base.Info_0 info;
  public FearlessError(FProgram.base.Info_0 info) {
    super();
    this.info = info;
  }
}

class FAux { static FProgram.base.LList_1 LAUNCH_ARGS; }
interface FProgram{interface test{interface HelloWorld_0 extends base.Main_0{
test.HelloWorld_0 _$self = new test.HelloWorld_0(){};
default base.Void_0 $35$imm$(Object s$){
var f$thiz = this;
return ((base.Void_0)(base.Void_0._$self));
}}
}
interface base{interface System_1 extends base.Sealed_0{
base.System_1 _$self = new base.System_1(){};
}
interface Sealed_0{
base.Sealed_0 _$self = new base.Sealed_0(){};
}
interface NoMutHyg_1{
base.NoMutHyg_1 _$self = new base.NoMutHyg_1(){};
}
interface Void_0{
base.Void_0 _$self = new base.Void_0(){};
}
interface Main_0{
base.Void_0 $35$imm$(Object s$);}
}
static void main(String[] args){ FAux.LAUNCH_ARGS = new base.LList_1(){};
for (String arg : args) { FAux.LAUNCH_ARGS = FAux.LAUNCH_ARGS.$43$mut$(arg); }
 base.Main_0 entry = new test.HelloWorld_0(){}; entry.$35$imm$(new base$46caps.$95System_0(){}); }
}
    """, "test.HelloWorld", false, """
    package test
    alias base.Main as Main,
    HelloWorld:Main{
      #s -> base.Void
    }
    """, Base.minimalBase); }

  @Test void nestedPkgs() { ok("""
class FearlessError extends RuntimeException {
  public FProgram.base.Info_0 info;
  public FearlessError(FProgram.base.Info_0 info) {
    super();
    this.info = info;
  }
}

class FAux { static FProgram.base.LList_1 LAUNCH_ARGS; }
interface FProgram{interface test{interface Foo_0{
test.Foo_0 a$imm$();}
interface A_0{
test.A_0 _$self = new test.A_0(){};
default test$46foo.Bar_0 $35$imm$(){
var f$thiz = this;
return ((test$46foo.Bar_0)(new test$46foo.Bar_0(){
public test$46foo.Bar_0 a$imm$(){
var fear4$$ = this;
return ((test$46foo.Bar_0)(test$46foo.Bar_0._$self));
}}));
}}
interface Test_0 extends base.Main_0{
test.Test_0 _$self = new test.Test_0(){};
default base.Void_0 $35$imm$(Object fear0$$){
var f$thiz = this;
return ((base.Void_0)(base.Void_0._$self));
}}
}
interface test$46foo{interface Bar_0 extends test.Foo_0{
test$46foo.Bar_0 _$self = new test$46foo.Bar_0(){};
default test.Foo_0 a$imm$(){
var f$thiz = this;
return ((test.Foo_0)(((test$46foo.Bar_0)(f$thiz))));
}}
}
interface base{interface System_1 extends base.Sealed_0{
base.System_1 _$self = new base.System_1(){};
}
interface Sealed_0{
base.Sealed_0 _$self = new base.Sealed_0(){};
}
interface NoMutHyg_1{
base.NoMutHyg_1 _$self = new base.NoMutHyg_1(){};
}
interface Void_0{
base.Void_0 _$self = new base.Void_0(){};
}
interface Main_0{
base.Void_0 $35$imm$(Object s$);}
}
static void main(String[] args){ FAux.LAUNCH_ARGS = new base.LList_1(){};
for (String arg : args) { FAux.LAUNCH_ARGS = FAux.LAUNCH_ARGS.$43$mut$(arg); }
 base.Main_0 entry = new test.Test_0(){}; entry.$35$imm$(new base$46caps.$95System_0(){}); }
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
}
