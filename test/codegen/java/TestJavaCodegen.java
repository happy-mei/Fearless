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

public class TestJavaCodegen {
  void ok(String expected, String entry, String... content) {
    assert content.length > 0;
    Main.resetAll();
    AtomicInteger pi = new AtomicInteger();
    var ps = Arrays.stream(content)
      .map(code->new Parser(Path.of("Dummy" + pi.getAndIncrement() + ".fear"), code))
      .toList();
    var p = Parser.parseAll(ps);
    new WellFormednessFullShortCircuitVisitor().visitProgram(p).ifPresent(err->{
      throw err;
    });
    var inferredSigs = p.inferSignaturesToCore();
    var inferred = new InferBodies(inferredSigs).inferAll(p);
    new WellFormednessShortCircuitVisitor().visitProgram(inferred);
    inferred.typeCheck();
    var mir = new MIRInjectionVisitor().visitProgram(inferred);
    var java = new JavaCodegen().visitProgram(mir.pkgs(), new Id.DecId(entry, 0));
    Err.strCmp(expected, java);
  }

  @Test void emptyProgram() { ok("""
    interface FProgram{interface base{interface System_0{
    }
    interface Main_1<R>{
    R $35$(base.System_0 s$);}
    }
    public static void main(String[] args){ base.Main_1 entry = new fake.Fake_0(){}; entry.$35$(new base.System_0(){}); }
    }
    """, "fake.Fake", """
    package test
    """, Base.minimalBase);}

  @Test void simpleProgram() { ok("""
interface FProgram{interface test{interface Bar_0 extends test.Baz_1{
default test.Baz_1<test.Bar_0> loop$(){
var f$thiz = this;
return f$thiz.loop$();
}
default test.Foo_0 $35$(){
var f$thiz = this;
return new test.Fear9$36_0(){
};
}}
interface Foo_0{
}
interface Ok_0{
test.Ok_0 $35$();}
interface Baz_1<X>{
X $35$();}
interface Yo_0{
default test.Ok_0 lm$(){
var f$thiz = this;
return new test.Fear10$36_0(){
public test.Ok_0 $35$(){
var ok$ = this;
return ok$.$35$();
}};
}}
interface Fear9$36_0 extends test.Foo_0{
}
interface Fear10$36_0 extends test.Ok_0{
}
}
interface base{interface System_0{
}
interface Main_1<R>{
R $35$(base.System_0 s$);}
}
public static void main(String[] args){ base.Main_1 entry = new fake.Fake_0(){}; entry.$35$(new base.System_0(){}); }
}
    """, "fake.Fake", """
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
return new test.Fear9$36_0(){
};
}
default <X0$470$36> X0$470$36 $63$(test.ThenElse_1<X0$470$36> f$){
var f$thiz = this;
return f$.then$();
}
default test.Bool_0 or$(test.Bool_0 b$){
var f$thiz = this;
return f$thiz;
}
default test.Bool_0 and$(test.Bool_0 b$){
var f$thiz = this;
return b$;
}}
interface False_0 extends test.Bool_0{
default test.Bool_0 not$(){
var f$thiz = this;
return new test.Fear10$36_0(){
};
}
default <X0$470$36> X0$470$36 $63$(test.ThenElse_1<X0$470$36> f$){
var f$thiz = this;
return f$.else$();
}
default test.Bool_0 or$(test.Bool_0 b$){
var f$thiz = this;
return b$;
}
default test.Bool_0 and$(test.Bool_0 b$){
var f$thiz = this;
return f$thiz;
}}
interface ThenElse_1<R>{
R then$();
R else$();}
interface Bool_0 extends test.Sealed_0{
test.Bool_0 not$();
<R> R $63$(test.ThenElse_1<R> f$);
test.Bool_0 or$(test.Bool_0 b$);
test.Bool_0 and$(test.Bool_0 b$);}
interface Sealed_0{
}
interface Fear9$36_0 extends test.Bool_0,test.False_0{
}
interface Fear10$36_0 extends test.Bool_0,test.True_0{
}
}
interface base{interface System_0{
}
interface Main_1<R>{
R $35$(base.System_0 s$);}
}
public static void main(String[] args){ base.Main_1 entry = new fake.Fake_0(){}; entry.$35$(new base.System_0(){}); }
}
    """, "fake.Fake", """
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
default test.Foo_0 $35$(base.System_0 s$){
var f$thiz = this;
return new test.Fear5$36_0(){
};
}}
interface Fear5$36_0 extends test.Foo_0{
}
}
interface base{interface System_0{
}
interface Main_1<R>{
R $35$(base.System_0 s$);}
}
public static void main(String[] args){ base.Main_1 entry = new test.HelloWorld_0(){}; entry.$35$(new base.System_0(){}); }
}
    """, "test.HelloWorld", """
    package test
    alias base.Main as Main,
    HelloWorld:Main[Foo]{
      #s -> Foo
    }
    Foo:{}
    """, """
    package base
    Main[R]:{ #(s: lent System): mdf R }
    System:{}
    """); }
}
