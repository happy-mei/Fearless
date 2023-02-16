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
    var mir = new MIRInjectionVisitor(inferred).visitProgram();
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
      return ((base.Void_0)new base.True_0(){
      }.or$(new test.Fear46$36_0(){
      }).$63$(new base.ThenElse_1(){
      public base.Void_0 then$(){
      var fear13$$ = this;
      return ((base.Void_0)new base.Void_0(){
      });
      }
      public base.Void_0 else$(){
      var fear13$$ = this;
      return ((base.Void_0)new base.Void_0(){
      });
      }}));
      }}
      interface Fear46$36_0 extends base.Bool_0,base.False_0{
      }
      }
      interface base{interface OptDo_1 extends base.OptMatch_2{
      default Object $95doRes$(base.Void_0 y$,Object x$){
      var f$thiz = this;
      return ((Object)x$);
      }
      default base.Opt_1 some$(Object x$){
      var f$thiz = this;
      return ((base.Opt_1)new base.Opt_0(){
      }.$35$(f$thiz.$95doRes$(f$thiz.$35$(x$),x$)));
      }
      default base.Opt_1 none$(){
      var f$thiz = this;
      return ((base.Opt_1)new base.Opt_1(){
      });
      }
      base.Void_0 $35$(Object t$);}
      interface Sealed_0{
      }
      interface Bool_0 extends base.Sealed_0{
      base.Bool_0 not$();
      Object $63$(base.ThenElse_1 f$);
      base.Bool_0 or$(base.Bool_0 b$);
      base.Bool_0 and$(base.Bool_0 b$);}
      interface Stringable_0{
      base.Str_0 str$();}
      interface False_0 extends base.Bool_0{
      default base.Bool_0 not$(){
      var f$thiz = this;
      return ((base.Bool_0)new base.Fear44$36_0(){
      });
      }
      default Object $63$(base.ThenElse_1 f$){
      var f$thiz = this;
      return ((Object)f$.else$());
      }
      default base.Bool_0 or$(base.Bool_0 b$){
      var f$thiz = this;
      return ((base.Bool_0)b$);
      }
      default base.Bool_0 and$(base.Bool_0 b$){
      var f$thiz = this;
      return ((base.Bool_0)f$thiz);
      }}
      interface UInt_0 extends base.Sealed_0,base.MathOps_1,base.Stringable_0{
      base.Bool_0 $60$61$(Object n$);
      Object $42$42$(Object n$);
      Object $62$62$(Object n$);
      Object $60$60$(Object n$);
      base.Bool_0 $61$61$(Object n$);
      base.Bool_0 $62$61$(Object n$);
      base.Str_0 str$();
      Object $94$(Object n$);
      base.Bool_0 $62$(Object n$);
      Object $124$(Object n$);
      base.Bool_0 $60$(Object n$);
      Object $47$(Object n$);
      base.Num_0 num$();
      Object $45$(Object n$);
      Object $43$(Object n$);
      Object $42$(Object n$);
      Object $38$(Object n$);
      Object $37$(Object n$);}
      interface Let_2{
      Object in$(Object v$);
      Object var$();}
      interface Ref_1 extends base.NoMutHyg_1,base.Sealed_0{
      default Object $60$45$(base.UpdateRef_1 f$){
      var f$thiz = this;
      return ((Object)f$thiz.swap$(f$.$35$(f$thiz.$42$())));
      }
      Object $42$();
      Object swap$(Object x$);
      default base.Void_0 $58$61$(Object x$){
      var f$thiz = this;
      return ((base.Void_0)new base.Let_0(){
      }.$35$(new base.Let_2(){
      public Object var$(){
      var fear4$$ = this;
      return ((Object)f$thiz.swap$(x$));
      }
      public base.Void_0 in$(Object _$){
      var fear4$$ = this;
      return ((base.Void_0)new base.Void_0(){
      });
      }}));
      }}
      interface Ref_0{
      default base.Ref_1 $35$(Object x$){
      var f$thiz = this;
      return ((base.Ref_1)f$thiz.$35$(x$));
      }}
      interface Let_0{
      default Object $35$(base.Let_2 l$){
      var f$thiz = this;
      return ((Object)l$.in$(l$.var$()));
      }}
      interface NoMutHyg_1{
      }
      interface MathOps_1{
      base.Bool_0 $60$61$(Object n$);
      base.Bool_0 $61$61$(Object n$);
      Object $60$60$(Object n$);
      Object $62$62$(Object n$);
      Object $42$42$(Object n$);
      base.Bool_0 $62$61$(Object n$);
      base.Bool_0 $62$(Object n$);
      Object $94$(Object n$);
      base.Bool_0 $60$(Object n$);
      Object $124$(Object n$);
      Object $47$(Object n$);
      Object $45$(Object n$);
      Object $43$(Object n$);
      Object $42$(Object n$);
      Object $38$(Object n$);
      Object $37$(Object n$);}
      interface Void_0{
      }
      interface Main_1{
      Object $35$(base.System_0 s$);}
      interface OptMatch_2{
      Object some$(Object x$);
      Object none$();}
      interface OptFlatMap_2 extends base.OptMatch_2{
      Object some$(Object x$);
      default base.Opt_1 none$(){
      var f$thiz = this;
      return ((base.Opt_1)new base.Opt_1(){
      });
      }}
      interface $95NumInstance_0 extends base.Num_0{
      default base.Bool_0 $60$61$(base.Num_0 n$){
      var f$thiz = this;
      return ((base.Bool_0)f$thiz.$60$61$(n$));
      }
      default base.Bool_0 $61$61$(base.Num_0 n$){
      var f$thiz = this;
      return ((base.Bool_0)f$thiz.$61$61$(n$));
      }
      default base.Num_0 $60$60$(base.Num_0 n$){
      var f$thiz = this;
      return ((base.Num_0)f$thiz.$60$60$(n$));
      }
      default base.Num_0 $62$62$(base.Num_0 n$){
      var f$thiz = this;
      return ((base.Num_0)f$thiz.$62$62$(n$));
      }
      default base.Num_0 $42$42$(base.Num_0 n$){
      var f$thiz = this;
      return ((base.Num_0)f$thiz.$42$42$(n$));
      }
      default base.Bool_0 $62$61$(base.Num_0 n$){
      var f$thiz = this;
      return ((base.Bool_0)f$thiz.$62$61$(n$));
      }
      default base.UInt_0 unum$(){
      var f$thiz = this;
      return ((base.UInt_0)f$thiz.uInt$());
      }
      default base.Bool_0 $62$(base.Num_0 n$){
      var f$thiz = this;
      return ((base.Bool_0)f$thiz.$62$(n$));
      }
      default base.Num_0 $94$(base.Num_0 n$){
      var f$thiz = this;
      return ((base.Num_0)f$thiz.$94$(n$));
      }
      default base.Str_0 str$(){
      var f$thiz = this;
      return ((base.Str_0)f$thiz.str$());
      }
      default base.Bool_0 $60$(base.Num_0 n$){
      var f$thiz = this;
      return ((base.Bool_0)f$thiz.$60$(n$));
      }
      default base.Num_0 $124$(base.Num_0 n$){
      var f$thiz = this;
      return ((base.Num_0)f$thiz.$124$(n$));
      }
      default base.Num_0 $47$(base.Num_0 n$){
      var f$thiz = this;
      return ((base.Num_0)f$thiz.$47$(n$));
      }
      default base.Num_0 $45$(base.Num_0 n$){
      var f$thiz = this;
      return ((base.Num_0)f$thiz.$45$(n$));
      }
      default base.Num_0 $43$(base.Num_0 n$){
      var f$thiz = this;
      return ((base.Num_0)f$thiz.$43$(n$));
      }
      default base.Num_0 $42$(base.Num_0 n$){
      var f$thiz = this;
      return ((base.Num_0)f$thiz.$42$(n$));
      }
      default base.Num_0 $38$(base.Num_0 n$){
      var f$thiz = this;
      return ((base.Num_0)f$thiz.$38$(n$));
      }
      default base.Num_0 $37$(base.Num_0 n$){
      var f$thiz = this;
      return ((base.Num_0)f$thiz.$37$(n$));
      }}
      interface $95UIntInstance_0 extends base.UInt_0{
      default base.Bool_0 $60$61$(base.UInt_0 n$){
      var f$thiz = this;
      return ((base.Bool_0)f$thiz.$60$61$(n$));
      }
      default base.Bool_0 $61$61$(base.UInt_0 n$){
      var f$thiz = this;
      return ((base.Bool_0)f$thiz.$61$61$(n$));
      }
      default base.UInt_0 $60$60$(base.UInt_0 n$){
      var f$thiz = this;
      return ((base.UInt_0)f$thiz.$60$60$(n$));
      }
      default base.UInt_0 $62$62$(base.UInt_0 n$){
      var f$thiz = this;
      return ((base.UInt_0)f$thiz.$62$62$(n$));
      }
      default base.UInt_0 $42$42$(base.UInt_0 n$){
      var f$thiz = this;
      return ((base.UInt_0)f$thiz.$42$42$(n$));
      }
      default base.Bool_0 $62$61$(base.UInt_0 n$){
      var f$thiz = this;
      return ((base.Bool_0)f$thiz.$62$61$(n$));
      }
      default base.Bool_0 $62$(base.UInt_0 n$){
      var f$thiz = this;
      return ((base.Bool_0)f$thiz.$62$(n$));
      }
      default base.UInt_0 $94$(base.UInt_0 n$){
      var f$thiz = this;
      return ((base.UInt_0)f$thiz.$94$(n$));
      }
      default base.Str_0 str$(){
      var f$thiz = this;
      return ((base.Str_0)f$thiz.str$());
      }
      default base.Bool_0 $60$(base.UInt_0 n$){
      var f$thiz = this;
      return ((base.Bool_0)f$thiz.$60$(n$));
      }
      default base.UInt_0 $124$(base.UInt_0 n$){
      var f$thiz = this;
      return ((base.UInt_0)f$thiz.$124$(n$));
      }
      default base.UInt_0 $47$(base.UInt_0 n$){
      var f$thiz = this;
      return ((base.UInt_0)f$thiz.$47$(n$));
      }
      default base.Num_0 num$(){
      var f$thiz = this;
      return ((base.Num_0)f$thiz.num$());
      }
      default base.UInt_0 $45$(base.UInt_0 n$){
      var f$thiz = this;
      return ((base.UInt_0)f$thiz.$45$(n$));
      }
      default base.UInt_0 $43$(base.UInt_0 n$){
      var f$thiz = this;
      return ((base.UInt_0)f$thiz.$43$(n$));
      }
      default base.UInt_0 $42$(base.UInt_0 n$){
      var f$thiz = this;
      return ((base.UInt_0)f$thiz.$42$(n$));
      }
      default base.UInt_0 $38$(base.UInt_0 n$){
      var f$thiz = this;
      return ((base.UInt_0)f$thiz.$38$(n$));
      }
      default base.UInt_0 $37$(base.UInt_0 n$){
      var f$thiz = this;
      return ((base.UInt_0)f$thiz.$37$(n$));
      }}
      interface True_0 extends base.Bool_0{
      default base.Bool_0 not$(){
      var f$thiz = this;
      return ((base.Bool_0)new base.Fear45$36_0(){
      });
      }
      default Object $63$(base.ThenElse_1 f$){
      var f$thiz = this;
      return ((Object)f$.then$());
      }
      default base.Bool_0 or$(base.Bool_0 b$){
      var f$thiz = this;
      return ((base.Bool_0)f$thiz);
      }
      default base.Bool_0 and$(base.Bool_0 b$){
      var f$thiz = this;
      return ((base.Bool_0)b$);
      }}
      interface UpdateRef_1{
      Object $35$(Object x$);}
      interface Num_0 extends base.Sealed_0,base.MathOps_1,base.Stringable_0{
      base.Bool_0 $60$61$(Object n$);
      Object $42$42$(Object n$);
      Object $62$62$(Object n$);
      Object $60$60$(Object n$);
      base.Bool_0 $61$61$(Object n$);
      base.Bool_0 $62$61$(Object n$);
      base.UInt_0 unum$();
      base.Str_0 str$();
      Object $94$(Object n$);
      base.Bool_0 $62$(Object n$);
      Object $124$(Object n$);
      base.Bool_0 $60$(Object n$);
      Object $47$(Object n$);
      Object $45$(Object n$);
      Object $43$(Object n$);
      Object $42$(Object n$);
      Object $38$(Object n$);
      Object $37$(Object n$);}
      interface ThenElse_1{
      Object then$();
      Object else$();}
      interface OptMap_2 extends base.OptMatch_2{
      default base.Opt_1 some$(Object x$){
      var f$thiz = this;
      return ((base.Opt_1)new base.Opt_0(){
      }.$35$(f$thiz.$35$(x$)));
      }
      default base.Opt_1 none$(){
      var f$thiz = this;
      return ((base.Opt_1)new base.Opt_1(){
      });
      }
      Object $35$(Object t$);}
      interface Opt_0{
      default base.Opt_1 $35$(Object x$){
      var f$thiz = this;
      return ((base.Opt_1)new base.Opt_1(){
      public Object match$(base.OptMatch_2 m$){
      var fear10$$ = this;
      return ((Object)m$.some$(x$));
      }});
      }}
      interface System_0{
      }
      interface Opt_1 extends base.NoMutHyg_1{
      default Object match$(base.OptMatch_2 m$){
      var f$thiz = this;
      return ((Object)m$.none$());
      }
      default base.Opt_1 map$(base.OptMap_2 f$){
      var f$thiz = this;
      return ((base.Opt_1)f$thiz.match$(f$));
      }
      default base.Opt_1 flatMap$(base.OptFlatMap_2 f$){
      var f$thiz = this;
      return ((base.Opt_1)f$thiz.match$(f$));
      }
      default base.Opt_1 do$(base.OptDo_1 f$){
      var f$thiz = this;
      return ((base.Opt_1)f$thiz.match$(f$));
      }}
      interface Str_0{
      base.UInt_0 len$();}
      interface Fear44$36_0 extends base.Bool_0,base.True_0{
      }
      interface Fear45$36_0 extends base.Bool_0,base.False_0{
      }
      }
      static void main(String[] args){ base.Main_1 entry = new test.Test_0(){}; entry.$35$(new base.System_0(){}); }
      }
      """, "test.Test", """
      package test
      alias base.Main as Main, alias base.Void as Void, alias base.True as True, alias base.False as False,
      Test:Main[Void]{ _ -> True.or(False)?{ .then -> Void, .else -> Void } }
      """, Base.immBaseLib);
  }

  @Test void magicNumInlining() { ok("""
    """, "test.Test", """
    package test
    alias base.Main as Main, alias base.Void as Void, alias base.True as True, alias base.False as False, alias base.Bool as Bool,
    Test:Main[Bool]{ _ -> 5>6 }
    """, Base.immBaseLib); }
}
