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

  @Test void fullBase() {
    ok("""
      interface FProgram{interface test{interface Test_0 extends base.Main_1{
      default base.Void_0 $35$(base.System_0 _$){
      var f$thiz = this;
      return new test.Fear52$36_0(){
      };
      }}
      interface Fear52$36_0 extends base.Void_0{
      }
      }
      interface base{interface OptDo_1<T> extends base.OptMatch_2{
      default T $95doRes$(base.Void_0 y$,T x$){
      var f$thiz = this;
      return x$;
      }
      default base.Opt_1<T> some$(T x$){
      var f$thiz = this;
      return new base.Fear41$36_0(){
      }.$35$(f$thiz.$95doRes$(f$thiz.$35$(x$),x$));
      }
      default base.Opt_1<T> none$(){
      var f$thiz = this;
      return new base.Fear42$36_0(){
      };
      }
      base.Void_0 $35$(T t$);}
      interface Sealed_0{
      }
      interface Bool_0 extends base.Sealed_0{
      base.Bool_0 not$();
      <R> R $63$(base.ThenElse_1<R> f$);
      base.Bool_0 or$(base.Bool_0 b$);
      base.Bool_0 and$(base.Bool_0 b$);}
      interface Stringable_0{
      base.Str_0 str$();}
      interface False_0 extends base.Bool_0{
      default base.Bool_0 not$(){
      var f$thiz = this;
      return new base.Fear43$36_0(){
      };
      }
      default <X0$470$36> X0$470$36 $63$(base.ThenElse_1<X0$470$36> f$){
      var f$thiz = this;
      return f$.else$();
      }
      default base.Bool_0 or$(base.Bool_0 b$){
      var f$thiz = this;
      return b$;
      }
      default base.Bool_0 and$(base.Bool_0 b$){
      var f$thiz = this;
      return f$thiz;
      }}
      interface UNum_0 extends base.Sealed_0,base.MathOps_1,base.Stringable_0{
      base.Bool_0 $60$61$(T n$);
      T $42$42$(T n$);
      T $62$62$(T n$);
      T $60$60$(T n$);
      base.Bool_0 $61$61$(T n$);
      base.Bool_0 $62$61$(T n$);
      base.Str_0 str$();
      T $94$(T n$);
      base.Bool_0 $62$(T n$);
      T $124$(T n$);
      base.Bool_0 $60$(T n$);
      T $47$(T n$);
      base.Num_0 num$();
      T $45$(T n$);
      T $43$(T n$);
      T $42$(T n$);
      T $38$(T n$);
      T $37$(T n$);}
      interface Let_2<V,R>{
      R in$(V v$);
      V var$();}
      interface Ref_1<X> extends base.NoMutHyg_1,base.Sealed_0{
      default X $60$45$(base.UpdateRef_1<X> f$){
      var f$thiz = this;
      return f$thiz.swap$(f$.$35$(f$thiz.$42$()));
      }
      X $42$();
      X swap$(X x$);
      default base.Void_0 $58$61$(X x$){
      var f$thiz = this;
      return new base.Fear44$36_0(){
      }.$35$(new base.Fear45$36_0(){
      public X var$(){
      var fear4$$ = this;
      return f$thiz.swap$(x$);
      }
      public base.Void_0 in$(X _$){
      var fear4$$ = this;
      return new base.Fear46$36_0(){
      };
      }});
      }}
      interface Ref_0{
      default <X> base.Ref_1<X> $35$(X x$){
      var f$thiz = this;
      return f$thiz.$35$(x$);
      }}
      interface Let_0{
      default <V,R> R $35$(base.Let_2<V,R> l$){
      var f$thiz = this;
      return l$.in$(l$.var$());
      }}
      interface NoMutHyg_1<X>{
      }
      interface MathOps_1<T>{
      base.Bool_0 $60$61$(T n$);
      base.Bool_0 $61$61$(T n$);
      T $60$60$(T n$);
      T $62$62$(T n$);
      T $42$42$(T n$);
      base.Bool_0 $62$61$(T n$);
      base.Bool_0 $62$(T n$);
      T $94$(T n$);
      base.Bool_0 $60$(T n$);
      T $124$(T n$);
      T $47$(T n$);
      T $45$(T n$);
      T $43$(T n$);
      T $42$(T n$);
      T $38$(T n$);
      T $37$(T n$);}
      interface Void_0{
      }
      interface Main_1<R>{
      R $35$(base.System_0 s$);}
      interface OptMatch_2<T,R>{
      R some$(T x$);
      R none$();}
      interface OptFlatMap_2<T,R> extends base.OptMatch_2{
      R some$(T x$);
      default base.Opt_1<R> none$(){
      var f$thiz = this;
      return new base.Fear47$36_0(){
      };
      }}
      interface $95NumInstance_0 extends base.Num_0{
      default base.Bool_0 $60$61$(base.Num_0 n$){
      var f$thiz = this;
      return f$thiz.$60$61$(n$);
      }
      default base.Bool_0 $61$61$(base.Num_0 n$){
      var f$thiz = this;
      return f$thiz.$61$61$(n$);
      }
      default base.Num_0 $60$60$(base.Num_0 n$){
      var f$thiz = this;
      return f$thiz.$60$60$(n$);
      }
      default base.Num_0 $62$62$(base.Num_0 n$){
      var f$thiz = this;
      return f$thiz.$62$62$(n$);
      }
      default base.Num_0 $42$42$(base.Num_0 n$){
      var f$thiz = this;
      return f$thiz.$42$42$(n$);
      }
      default base.Bool_0 $62$61$(base.Num_0 n$){
      var f$thiz = this;
      return f$thiz.$62$61$(n$);
      }
      default base.UNum_0 unum$(){
      var f$thiz = this;
      return f$thiz.unum$();
      }
      default base.Bool_0 $62$(base.Num_0 n$){
      var f$thiz = this;
      return f$thiz.$62$(n$);
      }
      default base.Num_0 $94$(base.Num_0 n$){
      var f$thiz = this;
      return f$thiz.$94$(n$);
      }
      default base.Str_0 str$(){
      var f$thiz = this;
      return f$thiz.str$();
      }
      default base.Bool_0 $60$(base.Num_0 n$){
      var f$thiz = this;
      return f$thiz.$60$(n$);
      }
      default base.Num_0 $124$(base.Num_0 n$){
      var f$thiz = this;
      return f$thiz.$124$(n$);
      }
      default base.Num_0 $47$(base.Num_0 n$){
      var f$thiz = this;
      return f$thiz.$47$(n$);
      }
      default base.Num_0 $45$(base.Num_0 n$){
      var f$thiz = this;
      return f$thiz.$45$(n$);
      }
      default base.Num_0 $43$(base.Num_0 n$){
      var f$thiz = this;
      return f$thiz.$43$(n$);
      }
      default base.Num_0 $42$(base.Num_0 n$){
      var f$thiz = this;
      return f$thiz.$42$(n$);
      }
      default base.Num_0 $38$(base.Num_0 n$){
      var f$thiz = this;
      return f$thiz.$38$(n$);
      }
      default base.Num_0 $37$(base.Num_0 n$){
      var f$thiz = this;
      return f$thiz.$37$(n$);
      }}
      interface $95UNumInstance_0 extends base.UNum_0{
      default base.Bool_0 $60$61$(base.UNum_0 n$){
      var f$thiz = this;
      return f$thiz.$60$61$(n$);
      }
      default base.Bool_0 $61$61$(base.UNum_0 n$){
      var f$thiz = this;
      return f$thiz.$61$61$(n$);
      }
      default base.UNum_0 $60$60$(base.UNum_0 n$){
      var f$thiz = this;
      return f$thiz.$60$60$(n$);
      }
      default base.UNum_0 $62$62$(base.UNum_0 n$){
      var f$thiz = this;
      return f$thiz.$62$62$(n$);
      }
      default base.UNum_0 $42$42$(base.UNum_0 n$){
      var f$thiz = this;
      return f$thiz.$42$42$(n$);
      }
      default base.Bool_0 $62$61$(base.UNum_0 n$){
      var f$thiz = this;
      return f$thiz.$62$61$(n$);
      }
      default base.Bool_0 $62$(base.UNum_0 n$){
      var f$thiz = this;
      return f$thiz.$62$(n$);
      }
      default base.UNum_0 $94$(base.UNum_0 n$){
      var f$thiz = this;
      return f$thiz.$94$(n$);
      }
      default base.Str_0 str$(){
      var f$thiz = this;
      return f$thiz.str$();
      }
      default base.Bool_0 $60$(base.UNum_0 n$){
      var f$thiz = this;
      return f$thiz.$60$(n$);
      }
      default base.UNum_0 $124$(base.UNum_0 n$){
      var f$thiz = this;
      return f$thiz.$124$(n$);
      }
      default base.UNum_0 $47$(base.UNum_0 n$){
      var f$thiz = this;
      return f$thiz.$47$(n$);
      }
      default base.Num_0 num$(){
      var f$thiz = this;
      return f$thiz.num$();
      }
      default base.UNum_0 $45$(base.UNum_0 n$){
      var f$thiz = this;
      return f$thiz.$45$(n$);
      }
      default base.UNum_0 $43$(base.UNum_0 n$){
      var f$thiz = this;
      return f$thiz.$43$(n$);
      }
      default base.UNum_0 $42$(base.UNum_0 n$){
      var f$thiz = this;
      return f$thiz.$42$(n$);
      }
      default base.UNum_0 $38$(base.UNum_0 n$){
      var f$thiz = this;
      return f$thiz.$38$(n$);
      }
      default base.UNum_0 $37$(base.UNum_0 n$){
      var f$thiz = this;
      return f$thiz.$37$(n$);
      }}
      interface True_0 extends base.Bool_0{
      default base.Bool_0 not$(){
      var f$thiz = this;
      return new base.Fear48$36_0(){
      };
      }
      default <X0$470$36> X0$470$36 $63$(base.ThenElse_1<X0$470$36> f$){
      var f$thiz = this;
      return f$.then$();
      }
      default base.Bool_0 or$(base.Bool_0 b$){
      var f$thiz = this;
      return f$thiz;
      }
      default base.Bool_0 and$(base.Bool_0 b$){
      var f$thiz = this;
      return b$;
      }}
      interface UpdateRef_1<X>{
      X $35$(X x$);}
      interface Num_0 extends base.Sealed_0,base.MathOps_1,base.Stringable_0{
      base.Bool_0 $60$61$(T n$);
      T $42$42$(T n$);
      T $62$62$(T n$);
      T $60$60$(T n$);
      base.Bool_0 $61$61$(T n$);
      base.Bool_0 $62$61$(T n$);
      base.UNum_0 unum$();
      base.Str_0 str$();
      T $94$(T n$);
      base.Bool_0 $62$(T n$);
      T $124$(T n$);
      base.Bool_0 $60$(T n$);
      T $47$(T n$);
      T $45$(T n$);
      T $43$(T n$);
      T $42$(T n$);
      T $38$(T n$);
      T $37$(T n$);}
      interface ThenElse_1<R>{
      R then$();
      R else$();}
      interface OptMap_2<T,R> extends base.OptMatch_2{
      default base.Opt_1<R> some$(T x$){
      var f$thiz = this;
      return new base.Fear49$36_0(){
      }.$35$(f$thiz.$35$(x$));
      }
      default base.Opt_1<R> none$(){
      var f$thiz = this;
      return new base.Fear50$36_0(){
      };
      }
      R $35$(T t$);}
      interface Opt_0{
      default <T> base.Opt_1<T> $35$(T x$){
      var f$thiz = this;
      return new base.Fear51$36_0(){
      public <X1$470$36> X1$470$36 match$(base.OptMatch_2<T,X1$470$36> m$){
      var fear10$$ = this;
      return m$.some$(x$);
      }};
      }}
      interface System_0{
      }
      interface Opt_1<T> extends base.NoMutHyg_1{
      default <R> R match$(base.OptMatch_2<T,R> m$){
      var f$thiz = this;
      return m$.none$();
      }
      default <R> base.Opt_1<R> map$(base.OptMap_2<T,R> f$){
      var f$thiz = this;
      return f$thiz.match$(f$);
      }
      default <R> base.Opt_1<R> flatMap$(base.OptFlatMap_2<T,R> f$){
      var f$thiz = this;
      return f$thiz.match$(f$);
      }
      default base.Opt_1<T> do$(base.OptDo_1<T> f$){
      var f$thiz = this;
      return f$thiz.match$(f$);
      }}
      interface Str_0{
      base.UNum_0 len$();}
      interface Fear41$36_0 extends base.Opt_0{
      }
      interface Fear42$36_0 extends base.Opt_1{
      }
      interface Fear43$36_0 extends base.Bool_0,base.True_0{
      }
      interface Fear44$36_0 extends base.Let_0{
      }
      interface Fear45$36_0 extends base.Let_2{
      }
      interface Fear46$36_0 extends base.Void_0{
      }
      interface Fear47$36_0 extends base.Opt_1{
      }
      interface Fear48$36_0 extends base.Bool_0,base.False_0{
      }
      interface Fear49$36_0 extends base.Opt_0{
      }
      interface Fear50$36_0 extends base.Opt_1{
      }
      interface Fear51$36_0 extends base.Opt_1{
      }
      }
      public static void main(String[] args){ base.Main_1 entry = new test.Test_0(){}; entry.$35$(new base.System_0(){}); }
      }
      """, "test.Test", """
      package test
      alias base.Main as Main, alias base.Void as Void,
      Test:Main[Void]{ _ -> Void }
      """, Base.immBaseLib);
  }
}
