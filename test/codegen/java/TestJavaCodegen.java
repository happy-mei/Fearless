package codegen.java;

import codegen.MIRInjectionVisitor;
import id.Id;
import main.Main;
import org.junit.jupiter.api.Test;
import parser.Parser;
import program.inference.InferBodies;
import utils.Base;
import utils.Err;
import wellFormedness.WellFormednessFullShortCircuitVisitor;
import wellFormedness.WellFormednessShortCircuitVisitor;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class TestJavaCodegen {
  void ok(String expected, String entry, boolean loadBase, String... content) {
    assert content.length > 0;
    Main.resetAll();
    AtomicInteger pi = new AtomicInteger();
    String[] baseLibs = loadBase ? Base.baseLib : new String[0];
    var ps = Stream.concat(Arrays.stream(content), Arrays.stream(baseLibs))
      .map(code->new Parser(Path.of("Dummy" + pi.getAndIncrement() + ".fear"), code))
      .toList();
    var p = Parser.parseAll(ps);
    new WellFormednessFullShortCircuitVisitor().visitProgram(p).ifPresent(err->{
      throw err;
    });
    var inferredSigs = p.inferSignaturesToCore();
    var inferred = new InferBodies(inferredSigs).inferAll(p);
    new WellFormednessShortCircuitVisitor(inferred).visitProgram(inferred);
    inferred.typeCheck();
    var mir = new MIRInjectionVisitor(inferred).visitProgram();
    var java = new JavaCodegen(inferred).visitProgram(mir.pkgs(), new Id.DecId(entry, 0));
    Err.strCmp(expected, java);
  }

  @Test void emptyProgram() { ok("""
    interface FProgram{interface base{interface System_1 extends base.Sealed_0{
    }
    interface Sealed_0{
    }
    interface NoMutHyg_1{
    }
    interface Void_0{
    }
    interface Main_1{
    Object $35$(Object s$);}
    }
    static void main(String[] args){ base.Main_1 entry = new fake.Fake_0(){}; entry.$35$(new base$46caps.System_1(){}); }
    }
    """, "fake.Fake", false, """
    package test
    """, Base.minimalBase);}

  @Test void simpleProgram() { ok("""
interface FProgram{interface test{interface Bar_0 extends test.Baz_1{
default test.Baz_1 loop$(){
var f$thiz = this;
return ((test.Baz_1)(((test.Bar_0)(f$thiz)).loop$()));
}
default test.Foo_0 $35$(){
var f$thiz = this;
return ((test.Foo_0)(new test.Foo_0(){
}));
}}
interface Foo_0{
}
interface Ok_0{
test.Ok_0 $35$();}
interface Baz_1{
Object $35$();}
interface Yo_0{
default test.Ok_0 lm$(){
var f$thiz = this;
return ((test.Ok_0)(new test.Ok_0(){
public test.Ok_0 $35$(){
var ok$ = this;
return ((test.Ok_0)(((test.Ok_0)(ok$)).$35$()));
}}));
}}
}
interface base{interface System_1 extends base.Sealed_0{
}
interface Sealed_0{
}
interface NoMutHyg_1{
}
interface Void_0{
}
interface Main_1{
Object $35$(Object s$);}
}
static void main(String[] args){ base.Main_1 entry = new fake.Fake_0(){}; entry.$35$(new base$46caps.System_1(){}); }
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
interface FProgram{interface test{interface True_0 extends test.Bool_0{
default test.Bool_0 not$(){
var f$thiz = this;
return ((test.Bool_0)(new test.False_0(){
}));
}
default Object $63$(Object f$){
var f$thiz = this;
return ((Object)(((test.ThenElse_1)(f$)).then$()));
}
default test.Bool_0 or$(Object b$){
var f$thiz = this;
return ((test.Bool_0)(((test.True_0)(f$thiz))));
}
default test.Bool_0 and$(Object b$){
var f$thiz = this;
return ((test.Bool_0)(((test.Bool_0)(b$))));
}}
interface False_0 extends test.Bool_0{
default test.Bool_0 not$(){
var f$thiz = this;
return ((test.Bool_0)(new test.True_0(){
}));
}
default Object $63$(Object f$){
var f$thiz = this;
return ((Object)(((test.ThenElse_1)(f$)).else$()));
}
default test.Bool_0 or$(Object b$){
var f$thiz = this;
return ((test.Bool_0)(((test.Bool_0)(b$))));
}
default test.Bool_0 and$(Object b$){
var f$thiz = this;
return ((test.Bool_0)(((test.False_0)(f$thiz))));
}}
interface ThenElse_1{
Object then$();
Object else$();}
interface Bool_0 extends test.Sealed_0{
test.Bool_0 not$();
Object $63$(Object f$);
test.Bool_0 or$(Object b$);
test.Bool_0 and$(Object b$);}
interface Sealed_0{
}
}
interface base{interface System_1 extends base.Sealed_0{
}
interface Sealed_0{
}
interface NoMutHyg_1{
}
interface Void_0{
}
interface Main_1{
Object $35$(Object s$);}
}
static void main(String[] args){ base.Main_1 entry = new fake.Fake_0(){}; entry.$35$(new base$46caps.System_1(){}); }
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
interface FProgram{interface test{interface Foo_0{
}
interface HelloWorld_0 extends base.Main_1{
default test.Foo_0 $35$(Object s$){
var f$thiz = this;
return ((test.Foo_0)(new test.Foo_0(){
}));
}}
}
interface base{interface System_1 extends base.Sealed_0{
}
interface Sealed_0{
}
interface NoMutHyg_1{
}
interface Void_0{
}
interface Main_1{
Object $35$(Object s$);}
}
static void main(String[] args){ base.Main_1 entry = new test.HelloWorld_0(){}; entry.$35$(new base$46caps.System_1(){}); }
}
    """, "test.HelloWorld", false, """
    package test
    alias base.Main as Main,
    HelloWorld:Main[Foo]{
      #s -> Foo
    }
    Foo:{}
    """, Base.minimalBase); }

  @Test void nestedPkgs() { ok("""
interface FProgram{interface test{interface Foo_0{
test.Foo_0 a$();}
interface Test_0 extends base.Main_1{
default test$46foo.Bar_0 $35$(Object fear0$$){
var f$thiz = this;
return ((test$46foo.Bar_0)(new test$46foo.Bar_0(){
public test$46foo.Bar_0 a$(){
var fear1$$ = this;
return ((test$46foo.Bar_0)(new test$46foo.Bar_0(){
}));
}}));
}}
}
interface test$46foo{interface Bar_0 extends test.Foo_0{
default test.Foo_0 a$(){
var f$thiz = this;
return ((test.Foo_0)(((test$46foo.Bar_0)(f$thiz))));
}}
}
interface base{interface System_1 extends base.Sealed_0{
}
interface Sealed_0{
}
interface NoMutHyg_1{
}
interface Void_0{
}
interface Main_1{
Object $35$(Object s$);}
}
static void main(String[] args){ base.Main_1 entry = new test.Test_0(){}; entry.$35$(new base$46caps.System_1(){}); }
}
    """, "test.Test", false, """
    package test
    Test:base.Main[test.foo.Bar]{ _ -> { .a -> test.foo.Bar } }
    Foo:{ .a: Foo }
    """, """
    package test.foo
    Bar:test.Foo{ .a -> this }
    """, Base.minimalBase);}
}
