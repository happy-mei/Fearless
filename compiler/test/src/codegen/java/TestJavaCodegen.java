package codegen.java;

import codegen.MIRInjectionVisitor;
import id.Id;
import main.Main;
import org.junit.jupiter.api.Disabled;
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
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@Disabled
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
    ConcurrentHashMap<Long, EMethTypeSystem.TsT> resolvedCalls = new ConcurrentHashMap<>();
    inferred.typeCheck(resolvedCalls);
    var mir = new MIRInjectionVisitor(List.of(),inferred, resolvedCalls).visitProgram();
    var java = new JavaCodegen(mir).visitProgram(new Id.DecId(entry, 0));
    Err.strCmp(expected, java);
  }

  @Test void emptyProgram() { ok("""
    package userCode;
    class FearlessError extends RuntimeException {
      public base.Info_0 info;
      public FearlessError(base.Info_0 info) {
        super();
        this.info = info;
      }
      public String getMessage() { return this.info.str$imm(); }
    }
    class FAux { static base.LList_1 LAUNCH_ARGS; }
        
    public interface FProgram{
    interface base{interface Sealed_0{
    Sealed_0 $self = new base$Sealed_0Impl();
    }
    interface System_0 extends base.Sealed_0{
    System_0 $self = new base$System_0Impl();
    }
    interface Void_0{
    Void_0 $self = new base$Void_0Impl();
    }
    interface Main_0{
    base.Void_0 $hash$imm(base.System_0 s$);}
    record base$Sealed_0Impl() implements base.Sealed_0 {
     \s
     \s
    }
        
    record base$System_0Impl() implements base.System_0 {
     \s
     \s
    }
        
    record base$Void_0Impl() implements base.Void_0 {
     \s
     \s
    }
        
    }  static void main(String[] args){
        FAux.LAUNCH_ARGS = base.LList_1.$self;
    for (String arg : args) { FAux.LAUNCH_ARGS = FAux.LAUNCH_ARGS.$43$mut(arg); }
        
        base.Main_0 entry = fake.Fake_0.$self;
        try {
          entry.$hash$imm(base.caps._System_0.$self);
        } catch (StackOverflowError e) {
          System.err.println("Program crashed with: Stack overflowed");
          System.exit(1);
        } catch (Throwable t) {
          System.err.println("Program crashed with: "+t.getLocalizedMessage());
          System.exit(1);
        }
      }
    }
    """, "fake.Fake", false, """
    package test
    """, Base.minimalBase);}

  @Test void capturing() { ok("""
    package userCode;
    class FearlessError extends RuntimeException {
      public base.Info_0 info;
      public FearlessError(base.Info_0 info) {
        super();
        this.info = info;
      }
      public String getMessage() { return this.info.str$imm(); }
    }
    class FAux { static base.LList_1 LAUNCH_ARGS; }
        
    public interface FProgram{
    interface test{interface Num_0{
    Num_0 $self = new test$Num_0Impl();
    }
    interface FortyTwo_0 extends test.Num_0{
    FortyTwo_0 $self = new test$FortyTwo_0Impl();
    }
    interface Usage_0{
    Usage_0 $self = new test$Usage_0Impl();
    test.Num_0 $hash$imm();static test.Num_0 test$Usage_0$$hash$imm$noSelfCap() {
      return test.FPerson_0.$self.$hash$imm(test.FortyTwo_0.$self).age$read();
    }
    }
    interface Person_0{
    test.Num_0 age$read();static test.Num_0 test$Person_0$age$read$noSelfCap(test.Num_0 age$) {
      return age$;
    }
    }
    interface FPerson_0{
    FPerson_0 $self = new test$FPerson_0Impl();
    test.Person_0 $hash$imm(test.Num_0 age$);static test.Person_0 test$FPerson_0$$hash$imm$noSelfCap(test.Num_0 age$) {
      return new test$Person_0Impl(age$);
    }
    }
    record test$Num_0Impl() implements test.Num_0 {
     \s
     \s
    }
        
    record test$FortyTwo_0Impl() implements test.FortyTwo_0 {
     \s
     \s
    }
        
    record test$Usage_0Impl() implements test.Usage_0 {
      public test.Num_0 $hash$imm() {
      return  test.Usage_0.test$Usage_0$$hash$imm$noSelfCap();
    }
        
     \s
    }
        
    record test$Person_0Impl(test.Num_0 age$) implements test.Person_0 {
      public test.Num_0 age$read() {
      return  test.Person_0.test$Person_0$age$read$noSelfCap(this.age$);
    }
        
     \s
    }
        
    record test$FPerson_0Impl() implements test.FPerson_0 {
      public test.Person_0 $hash$imm(test.Num_0 age$) {
      return  test.FPerson_0.test$FPerson_0$$hash$immnoSelfCap(age$);
    }
        
     \s
    }
        
    }
    interface base{interface Sealed_0{
    Sealed_0 $self = new base$Sealed_0Impl();
    }
    interface System_0 extends base.Sealed_0{
    System_0 $self = new base$System_0Impl();
    }
    interface Void_0{
    Void_0 $self = new base$Void_0Impl();
    }
    interface Main_0{
    base.Void_0 $hash$imm(base.System_0 s$);}
    record base$Sealed_0Impl() implements base.Sealed_0 {
     \s
     \s
    }
        
    record base$System_0Impl() implements base.System_0 {
     \s
     \s
    }
        
    record base$Void_0Impl() implements base.Void_0 {
     \s
     \s
    }
        
    }  static void main(String[] args){
        FAux.LAUNCH_ARGS = base.LList_1.$self;
    for (String arg : args) { FAux.LAUNCH_ARGS = FAux.LAUNCH_ARGS.$43$mut(arg); }
        
        base.Main_0 entry = fake.Fake_0.$self;
        try {
          entry.$hash$imm(base.caps._System_0.$self);
        } catch (StackOverflowError e) {
          System.err.println("Program crashed with: Stack overflowed");
          System.exit(1);
        } catch (Throwable t) {
          System.err.println("Program crashed with: "+t.getLocalizedMessage());
          System.exit(1);
        }
      }
    }
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
    package userCode;
    class FearlessError extends RuntimeException {
      public base.Info_0 info;
      public FearlessError(base.Info_0 info) {
        super();
        this.info = info;
      }
      public String getMessage() { return this.info.str$imm(); }
    }
    class FAux { static base.LList_1 LAUNCH_ARGS; }
        
    public interface FProgram{
    interface test{interface Num_0{
    test.Num_0 plus1$imm();}
    interface FortyThree_0 extends test.Num_0{
    FortyThree_0 $self = new test$FortyThree_0Impl();
    test.Num_0 plus1$imm();static test.Num_0 test$FortyThree_0$plus1$imm$noSelfCap() {
      return test.FortyFour_0.$self;
    }
    }
    interface FortyTwo_0 extends test.Num_0{
    FortyTwo_0 $self = new test$FortyTwo_0Impl();
    test.Num_0 plus1$imm();static test.Num_0 test$FortyTwo_0$plus1$imm$noSelfCap() {
      return test.FortyThree_0.$self;
    }
    }
    interface Usage_0{
    Usage_0 $self = new test$Usage_0Impl();
    test.Num_0 $hash$imm();static test.Num_0 test$Usage_0$$hash$imm$noSelfCap() {
      return test.FPerson_0.$self.$hash$imm(test.FortyTwo_0.$self).wrap$mut().age$read();
    }
    }
    interface Person_0{
    test.Num_0 age$read();
    test.Person_0 wrap$mut();static test.Person_0 test$Person_0$wrap$mut$selfCap(test.Person_0 f$thiz) {
      return new test$Fear1$36_0Impl(f$thiz);
    }
    }
    interface Fear0$36_0 extends test.Person_0{
    test.Num_0 age$read();
    test.Person_0 wrap$mut();static test.Num_0 test$Fear0$36_0$age$read$noSelfCap(test.Fear1$36_0 self$) {
      return self$.age$read().plus1$imm();
    }
    }
    interface Fear1$36_0 extends test.Person_0{
    test.Num_0 age$read();
    test.Person_0 wrap$mut();static test.Num_0 test$Fear1$36_0$age$read$noSelfCap(test.Person_0 f$thiz) {
      return f$thiz.age$read().plus1$imm();
    }
        
    static test.Person_0 test$Fear1$36_0$wrap$mut$selfCap(test.Fear1$36_0 self$) {
      return new test$Fear0$36_0Impl(self$);
    }
    }
    interface FortyFour_0 extends test.Num_0{
    FortyFour_0 $self = new test$FortyFour_0Impl();
    test.Num_0 plus1$imm();static test.Num_0 test$FortyFour_0$plus1$imm$selfCap(test.FortyFour_0 f$thiz) {
      return f$thiz.plus1$imm();
    }
    }
    interface Fear2$36_0 extends test.Person_0{
    test.Num_0 age$read();
    test.Person_0 wrap$mut();static test.Num_0 test$Fear2$36_0$age$read$noSelfCap(test.Num_0 age$) {
      return age$;
    }
    }
    interface FPerson_0{
    FPerson_0 $self = new test$FPerson_0Impl();
    test.Person_0 $hash$imm(test.Num_0 age$);static test.Person_0 test$FPerson_0$$hash$imm$noSelfCap(test.Num_0 age$) {
      return new test$Fear2$36_0Impl(age$);
    }
    }
    record test$FortyThree_0Impl() implements test.FortyThree_0 {
      public test.Num_0 plus1$imm() {
      return  test.FortyThree_0.test$FortyThree_0$plus1$imm$noSelfCap();
    }
        
     \s
    }
        
    record test$FortyTwo_0Impl() implements test.FortyTwo_0 {
      public test.Num_0 plus1$imm() {
      return  test.FortyTwo_0.test$FortyTwo_0$plus1$imm$noSelfCap();
    }
        
     \s
    }
        
    record test$Usage_0Impl() implements test.Usage_0 {
      public test.Num_0 $hash$imm() {
      return  test.Usage_0.test$Usage_0$$hash$imm$noSelfCap();
    }
        
     \s
    }
        
    record test$Fear0$36_0Impl(test.Fear1$36_0 self$) implements test.Fear0$36_0 {
      public test.Num_0 age$read() {
      return  test.Fear0$36_0.test$Fear0$36_0$age$read$noSelfCap(this.self$);
    }
        
    public test.Person_0 wrap$mut() {
      return  test.Person_0.test$Person_0$wrap$mut$selfCap(this);
    }
        
     \s
    }
        
    record test$Fear1$36_0Impl(test.Person_0 f$thiz) implements test.Fear1$36_0 {
      public test.Num_0 age$read() {
      return  test.Fear1$36_0.test$Fear1$36_0$age$read$noSelfCap(this.f$thiz);
    }
        
    public test.Person_0 wrap$mut() {
      return  test.Fear1$36_0.test$Fear1$36_0$wrap$mut$selfCap(this);
    }
        
     \s
    }
        
    record test$FortyFour_0Impl() implements test.FortyFour_0 {
      public test.Num_0 plus1$imm() {
      return  test.FortyFour_0.test$FortyFour_0$plus1$imm$selfCap(this);
    }
        
     \s
    }
        
    record test$Fear2$36_0Impl(test.Num_0 age$) implements test.Fear2$36_0 {
      public test.Num_0 age$read() {
      return  test.Fear2$36_0.test$Fear2$36_0$age$read$noSelfCap(this.age$);
    }
        
    public test.Person_0 wrap$mut() {
      return  test.Person_0.test$Person_0$wrap$mut$selfCap(this);
    }
        
     \s
    }
        
    record test$FPerson_0Impl() implements test.FPerson_0 {
      public test.Person_0 $hash$imm(test.Num_0 age$) {
      return  test.FPerson_0.test$FPerson_0$$hash$imm$noSelfCap(age$);
    }
        
     \s
    }
        
    }
    interface base{interface Sealed_0{
    Sealed_0 $self = new base$Sealed_0Impl();
    }
    interface System_0 extends base.Sealed_0{
    System_0 $self = new base$System_0Impl();
    }
    interface Void_0{
    Void_0 $self = new base$Void_0Impl();
    }
    interface Main_0{
    base.Void_0 $hash$imm(base.System_0 s$);}
    record base$Sealed_0Impl() implements base.Sealed_0 {
     \s
     \s
    }
        
    record base$System_0Impl() implements base.System_0 {
     \s
     \s
    }
        
    record base$Void_0Impl() implements base.Void_0 {
     \s
     \s
    }
        
    }  static void main(String[] args){
        FAux.LAUNCH_ARGS = base.LList_1.$self;
    for (String arg : args) { FAux.LAUNCH_ARGS = FAux.LAUNCH_ARGS.$43$mut(arg); }
        
        base.Main_0 entry = fake.Fake_0.$self;
        try {
          entry.$hash$imm(base.caps._System_0.$self);
        } catch (StackOverflowError e) {
          System.err.println("Program crashed with: Stack overflowed");
          System.exit(1);
        } catch (Throwable t) {
          System.err.println("Program crashed with: "+t.getLocalizedMessage());
          System.exit(1);
        }
      }
    }
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
    package userCode;
    class FearlessError extends RuntimeException {
      public base.Info_0 info;
      public FearlessError(base.Info_0 info) {
        super();
        this.info = info;
      }
      public String getMessage() { return this.info.str$imm(); }
    }
    class FAux { static base.LList_1 LAUNCH_ARGS; }
        
    public interface FProgram{
    interface test{interface Bar_0 extends test.Baz_1{
    Bar_0 $self = new test$Bar_0Impl();
    test.Baz_1 loop$imm();
    test.Foo_0 $hash$imm();static test.Foo_0 test$Bar_0$$hash$imm$noSelfCap() {
      return test.Foo_0.$self;
    }
        
    static test.Baz_1 test$Bar_0$loop$imm$selfCap(test.Bar_0 f$thiz) {
      return f$thiz.loop$imm();
    }
    }
    interface Foo_0{
    Foo_0 $self = new test$Foo_0Impl();
    }
    interface Fear1$36_0 extends test.Ok_0{
    Fear1$36_0 $self = new test$Fear1$36_0Impl();
    test.Ok_0 $hash$imm();static test.Ok_0 test$Fear1$36_0$$hash$imm$selfCap(test.Fear1$36_0 ok$) {
      return ok$.$hash$imm();
    }
    }
    interface Ok_0{
    test.Ok_0 $hash$imm();}
    interface Baz_1{
    Object $hash$imm();}
    interface Yo_0{
    Yo_0 $self = new test$Yo_0Impl();
    test.Ok_0 lm$imm();static test.Ok_0 test$Yo_0$lm$imm$noSelfCap() {
      return test.Fear1$36_0.$self;
    }
    }
    record test$Bar_0Impl() implements test.Bar_0 {
      public test.Baz_1 loop$imm() {
      return  test.Bar_0.test$Bar_0$loop$imm$selfCap(this);
    }
        
    public test.Foo_0 $hash$imm() {
      return  test.Bar_0.test$Bar_0$$hash$imm$noSelfCap();
    }
        
     \s
    }
        
    record test$Foo_0Impl() implements test.Foo_0 {
     \s
     \s
    }
        
    record test$Fear1$36_0Impl() implements test.Fear1$36_0 {
      public test.Ok_0 $hash$imm() {
      return  test.Fear1$36_0.test$Fear1$36_0$$hash$imm$selfCap(this);
    }
        
     \s
    }
        
    record test$Yo_0Impl() implements test.Yo_0 {
      public test.Ok_0 lm$imm() {
      return  test.Yo_0.test$Yo_0$lm$imm$noSelfCap();
    }
        
     \s
    }
        
    }
    interface base{interface Sealed_0{
    Sealed_0 $self = new base$Sealed_0Impl();
    }
    interface System_0 extends base.Sealed_0{
    System_0 $self = new base$System_0Impl();
    }
    interface Void_0{
    Void_0 $self = new base$Void_0Impl();
    }
    interface Main_0{
    base.Void_0 $hash$imm(base.System_0 s$);}
    record base$Sealed_0Impl() implements base.Sealed_0 {
     \s
     \s
    }
        
    record base$System_0Impl() implements base.System_0 {
     \s
     \s
    }
        
    record base$Void_0Impl() implements base.Void_0 {
     \s
     \s
    }
        
    }  static void main(String[] args){
        FAux.LAUNCH_ARGS = base.LList_1.$self;
    for (String arg : args) { FAux.LAUNCH_ARGS = FAux.LAUNCH_ARGS.$43$mut(arg); }
        
        base.Main_0 entry = fake.Fake_0.$self;
        try {
          entry.$hash$imm(base.caps._System_0.$self);
        } catch (StackOverflowError e) {
          System.err.println("Program crashed with: Stack overflowed");
          System.exit(1);
        } catch (Throwable t) {
          System.err.println("Program crashed with: "+t.getLocalizedMessage());
          System.exit(1);
        }
      }
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
    package userCode;
    class FearlessError extends RuntimeException {
      public base.Info_0 info;
      public FearlessError(base.Info_0 info) {
        super();
        this.info = info;
      }
      public String getMessage() { return this.info.str$imm(); }
    }
    class FAux { static FProgram.base.LList_1 LAUNCH_ARGS; }
        
    public interface FProgram{
    interface test{interface True_0 extends test.Bool_0{
    True_0 $self = new test$True_0Impl();
    Object $63$imm(test.ThenElse_1 f$);
    test.Bool_0 not$imm();
    test.Bool_0 or$imm(test.Bool_0 b$);
    test.Bool_0 and$imm(test.Bool_0 b$);static test.Bool_0 test$True_0$and$imm$noSelfCap(test.Bool_0 b$) {
      return b$;
    }
        
    static test.Bool_0 test$True_0$or$imm$selfCap(test.Bool_0 b$, test.True_0 f$thiz) {
      return f$thiz;
    }
        
    static test.Bool_0 test$True_0$not$imm$noSelfCap() {
      return test.False_0.$self;
    }
        
    static Object test$True_0$$63$imm$noSelfCap(test.ThenElse_1 f$) {
      return f$.then$mut();
    }
    }
    interface Fear1_0{
    Fear1_0 $self = new test$Fear1_0Impl();
    }
    interface False_0 extends test.Bool_0{
    False_0 $self = new test$False_0Impl();
    Object $63$imm(test.ThenElse_1 f$);
    test.Bool_0 not$imm();
    test.Bool_0 or$imm(test.Bool_0 b$);
    test.Bool_0 and$imm(test.Bool_0 b$);static test.Bool_0 test$False_0$and$imm$selfCap(test.Bool_0 b$, test.False_0 f$thiz) {
      return f$thiz;
    }
        
    static test.Bool_0 test$False_0$or$imm$noSelfCap(test.Bool_0 b$) {
      return b$;
    }
        
    static test.Bool_0 test$False_0$not$imm$noSelfCap() {
      return test.True_0.$self;
    }
        
    static Object test$False_0$$63$imm$noSelfCap(test.ThenElse_1 f$) {
      return f$.else$mut();
    }
    }
    interface ThenElse_1{
    Object then$mut();
    Object else$mut();}
    interface Bool_0 extends test.Sealed_0{
    Object $63$imm(test.ThenElse_1 f$);
    test.Bool_0 not$imm();
    test.Bool_0 or$imm(test.Bool_0 b$);
    test.Bool_0 and$imm(test.Bool_0 b$);}
    interface Sealed_0{
    Sealed_0 $self = new test$Sealed_0Impl();
    }
    record test$True_0Impl() implements test.True_0 {
      public Object $63$imm(test.ThenElse_1 f$) {
      return  test.True_0.test$True_0$$63$imm$noSelfCap(f$);
    }
        
    public test.Bool_0 not$imm() {
      return  test.True_0.test$True_0$not$imm$noSelfCap();
    }
        
    public test.Bool_0 or$imm(test.Bool_0 b$) {
      return  test.True_0.test$True_0$or$imm$selfCap(b$,this);
    }
        
    public test.Bool_0 and$imm(test.Bool_0 b$) {
      return  test.True_0.test$True_0$and$imm$noSelfCap(b$);
    }
        
     \s
    }
        
    record test$Fear1_0Impl() implements test.Fear1_0 {
     \s
     \s
    }
        
    record test$False_0Impl() implements test.False_0 {
      public Object $63$imm(test.ThenElse_1 f$) {
      return  test.False_0.test$False_0$$63$imm$noSelfCap(f$);
    }
        
    public test.Bool_0 not$imm() {
      return  test.False_0.test$False_0$not$imm$noSelfCap();
    }
        
    public test.Bool_0 or$imm(test.Bool_0 b$) {
      return  test.False_0.test$False_0$or$imm$noSelfCap(b$);
    }
        
    public test.Bool_0 and$imm(test.Bool_0 b$) {
      return  test.False_0.test$False_0$and$imm$selfCap(b$,this);
    }
        
     \s
    }
        
    record test$Sealed_0Impl() implements test.Sealed_0 {
     \s
     \s
    }
        
    }
    interface base{interface Sealed_0{
    Sealed_0 $self = new base$Sealed_0Impl();
    }
    interface System_0 extends base.Sealed_0{
    System_0 $self = new base$System_0Impl();
    }
    interface Void_0{
    Void_0 $self = new base$Void_0Impl();
    }
    interface Main_0{
    base.Void_0 $hash$imm(base.System_0 s$);}
    record base$Sealed_0Impl() implements base.Sealed_0 {
     \s
     \s
    }
        
    record base$System_0Impl() implements base.System_0 {
     \s
     \s
    }
        
    record base$Void_0Impl() implements base.Void_0 {
     \s
     \s
    }
        
    }  static void main(String[] args){
        FAux.LAUNCH_ARGS = base.LList_1.$self;
    for (String arg : args) { FAux.LAUNCH_ARGS = FAux.LAUNCH_ARGS.$43$mut(arg); }
        
        base.Main_0 entry = fake.Fake_0.$self;
        try {
          entry.$hash$imm(base.caps._System_0.$self);
        } catch (StackOverflowError e) {
          System.err.println("Program crashed with: Stack overflowed");
          System.exit(1);
        } catch (Throwable t) {
          System.err.println("Program crashed with: "+t.getLocalizedMessage());
          System.exit(1);
        }
      }
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
    Fear1:{}
    """, Base.minimalBase);}
  @Test void multiPackage() { ok("""
    package userCode;
    class FearlessError extends RuntimeException {
      public base.Info_0 info;
      public FearlessError(base.Info_0 info) {
        super();
        this.info = info;
      }
      public String getMessage() { return this.info.str$imm(); }
    }
    class FAux { static base.LList_1 LAUNCH_ARGS; }
        
    public interface FProgram{
    interface test{interface HelloWorld_0 extends base.Main_0{
    HelloWorld_0 $self = new test$HelloWorld_0Impl();
    base.Void_0 $hash$imm(base.System_0 s$);static base.Void_0 test$HelloWorld_0$$hash$imm$noSelfCap(base.System_0 s$) {
      return base.Void_0.$self;
    }
    }
    record test$HelloWorld_0Impl() implements test.HelloWorld_0 {
      public base.Void_0 $hash$imm(base.System_0 s$) {
      return  test.HelloWorld_0.test$HelloWorld_0$$hash$imm$noSelfCap(s$);
    }
        
     \s
    }
        
    }
    interface base{interface Sealed_0{
    Sealed_0 $self = new base$Sealed_0Impl();
    }
    interface System_0 extends base.Sealed_0{
    System_0 $self = new base$System_0Impl();
    }
    interface Void_0{
    Void_0 $self = new base$Void_0Impl();
    }
    interface Main_0{
    base.Void_0 $hash$imm(base.System_0 s$);}
    record base$Sealed_0Impl() implements base.Sealed_0 {
     \s
     \s
    }
        
    record base$System_0Impl() implements base.System_0 {
     \s
     \s
    }
        
    record base$Void_0Impl() implements base.Void_0 {
     \s
     \s
    }
        
    }  static void main(String[] args){
        FAux.LAUNCH_ARGS = base.LList_1.$self;
    for (String arg : args) { FAux.LAUNCH_ARGS = FAux.LAUNCH_ARGS.$43$mut(arg); }
        
        base.Main_0 entry = test.HelloWorld_0.$self;
        try {
          entry.$hash$imm(base.caps._System_0.$self);
        } catch (StackOverflowError e) {
          System.err.println("Program crashed with: Stack overflowed");
          System.exit(1);
        } catch (Throwable t) {
          System.err.println("Program crashed with: "+t.getLocalizedMessage());
          System.exit(1);
        }
      }
    }
    """, "test.HelloWorld", false, """
    package test
    alias base.Main as Main,
    HelloWorld:Main{
      #s -> base.Void
    }
    """, Base.minimalBase); }

  @Test void nestedPkgs() { ok("""
    package userCode;
    class FearlessError extends RuntimeException {
      public base.Info_0 info;
      public FearlessError(base.Info_0 info) {
        super();
        this.info = info;
      }
      public String getMessage() { return this.info.str$imm(); }
    }
    class FAux { static base.LList_1 LAUNCH_ARGS; }
        
    public interface FProgram{
    interface test{interface Foo_0{
    test.Foo_0 a$imm();}
    interface Fear2$36_0 extends test$46foo.Bar_0{
    Fear2$36_0 $self = new test$Fear2$36_0Impl();
    test$46foo.Bar_0 a$imm();static test$46foo.Bar_0 test$Fear2$36_0$a$imm$noSelfCap() {
      return test$46foo.Bar_0.$self;
    }
    }
    interface A_0{
    A_0 $self = new test$A_0Impl();
    test$46foo.Bar_0 $hash$imm();static test$46foo.Bar_0 test$A_0$$hash$imm$noSelfCap() {
      return test.Fear2$36_0.$self;
    }
    }
    interface Test_0 extends base.Main_0{
    Test_0 $self = new test$Test_0Impl();
    base.Void_0 $hash$imm(base.System_0 fear0$$);static base.Void_0 test$Test_0$$hash$imm$noSelfCap(base.System_0 fear0$$) {
      return base.Void_0.$self;
    }
    }
    record test$Fear2$36_0Impl() implements test.Fear2$36_0 {
      public test$46foo.Bar_0 a$imm() {
      return  test.Fear2$36_0.test$Fear2$36_0$a$imm$noSelfCap();
    }
        
     \s
    }
        
    record test$A_0Impl() implements test.A_0 {
      public test$46foo.Bar_0 $hash$imm() {
      return  test.A_0.test$A_0$$hash$imm$noSelfCap();
    }
        
     \s
    }
        
    record test$Test_0Impl() implements test.Test_0 {
      public base.Void_0 $hash$imm(base.System_0 fear0$$) {
      return  test.Test_0.test$Test_0$$hash$imm$noSelfCap(fear0$$);
    }
        
     \s
    }
        
    }
    interface test$46foo{interface Bar_0 extends test.Foo_0{
    Bar_0 $self = new test$46foo$Bar_0Impl();
    test.Foo_0 a$imm();static test.Foo_0 test$46foo$Bar_0$a$imm$selfCap(test$46foo.Bar_0 f$thiz) {
      return f$thiz;
    }
    }
    record test$46foo$Bar_0Impl() implements test$46foo.Bar_0 {
      public test.Foo_0 a$imm() {
      return  test$46foo.Bar_0.test$46foo$Bar_0$a$imm$selfCap(this);
    }
        
     \s
    }
        
    }
    interface base{interface Sealed_0{
    Sealed_0 $self = new base$Sealed_0Impl();
    }
    interface System_0 extends base.Sealed_0{
    System_0 $self = new base$System_0Impl();
    }
    interface Void_0{
    Void_0 $self = new base$Void_0Impl();
    }
    interface Main_0{
    base.Void_0 $hash$imm(base.System_0 s$);}
    record base$Sealed_0Impl() implements base.Sealed_0 {
     \s
     \s
    }
        
    record base$System_0Impl() implements base.System_0 {
     \s
     \s
    }
        
    record base$Void_0Impl() implements base.Void_0 {
     \s
     \s
    }
        
    }  static void main(String[] args){
        FAux.LAUNCH_ARGS = base.LList_1.$self;
    for (String arg : args) { FAux.LAUNCH_ARGS = FAux.LAUNCH_ARGS.$43$mut(arg); }
        
        base.Main_0 entry = test.Test_0.$self;
        try {
          entry.$hash$imm(base.caps._System_0.$self);
        } catch (StackOverflowError e) {
          System.err.println("Program crashed with: Stack overflowed");
          System.exit(1);
        } catch (Throwable t) {
          System.err.println("Program crashed with: "+t.getLocalizedMessage());
          System.exit(1);
        }
      }
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
