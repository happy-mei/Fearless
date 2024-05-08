package program.typesystem;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static program.typesystem.RunTypeSystem.fail;
import static program.typesystem.RunTypeSystem.ok;

@Disabled // No more adapterOk
public class TestAdaptSubtyping {
  @Test void contravarianceBoxMatcher() { fail("""
    In position [###]/Dummy0.fear:12:41
    [E53 xTypeError]
    Expected 'o' to be imm test.Box[imm test.Person[]], got imm test.Box[imm test.Student[]].
    """, """
    package test
    Nat:{} Str:{}
    Person:{ read .name: Str, read .age: Nat, }
    Student:Person{ read .grades: Box[Nat] }
    BoxMatcher[T,R]:{ mut #: R }
    Box[T]:{
      .match[R](m: mut BoxMatcher[T, R]): R -> m#,
      .break(x: T): T -> this.match[T]{ x },
    }
    
    Ex:{
      .nums(o: Box[Student]): Box[Person] -> o,
      }
    """); }
  @Test void contravarianceBoxMatcherNoAdapt() { ok("""
    package test
    Nat:{} Str:{}
    Person:{ read .name: Str, read .age: Nat, }
    Student:Person{ read .grades: Nat }
    BoxMatcher[T,R]:{ mut #: R }
    BoxPerson:{
      .match[R](m: mut BoxMatcher[Person, R]): R -> m#,
      .break(x: Person): Person -> this.match[Person]{ x },
    }
    BoxStudent:{
      .match[R](m: mut BoxMatcher[Student, R]): R -> m#,
      .break(x: Student): Student -> this.match[Student]{ x },
    }
    
    Ex:{
      .nums(o: BoxStudent): BoxPerson -> {'adapted
        .match(m) -> o.match(m),
        .break(x) -> o.break(x),
        },
      }
    """); }
  @Test void contravarianceBoxMatcherNoAdaptMdf() { ok("""
    package test
    Nat:{} Str:{}
    Person:{ read .name: Str, read .age: Nat, }
    BoxMatcher[T,R]:{ mut #: R }
    BoxImmPerson:{
      .match[R](m: mut BoxMatcher[Person, R]): R -> m#,
      .break(x: Person): Person -> this.match[Person]{ x },
    }
    BoxReadPerson:{
      .match[R](m: mut BoxMatcher[read Person, R]): R -> m#,
      .break(x: read Person): read Person -> this.match[read Person]{ x },
    }
    
    
    Ex:{
      .nums(o: BoxImmPerson): BoxReadPerson -> {'adapted
        .match(m) -> o.match(m),
        .break(x) -> o.break(x),
        },
      }
    """); }
  @Test void contravarianceBoxMatcherNoAdaptExtensionMethod() { ok("""
    package test
    Nat:{} Str:{}
    Person:{ read .name: Str, read .age: Nat, }
    Student:Person{ read .grades: Nat }
    BoxMatcher[T,R]:{ mut #: R }
    BoxExtension[T,R]:{ mut #(self: T): R }
    
    BoxPerson:{
//      .match[R](m: mut BoxMatcher[Person, R]): R -> m#,
      .extend[R](ext: mut BoxExtension[BoxPerson, R]): R -> ext#this,
    }
    BoxStudent:{
//      .match[R](m: mut BoxMatcher[Student, R]): R -> m#,
      .extend[R](ext: mut BoxExtension[BoxStudent, R]): R -> ext#this,
    }
    
    Ex:{
      .nums(o: BoxStudent): BoxPerson -> {'adapted
//        .match(m) -> o.match(m),
        .extend(ext) -> o.extend(ext),
        },
      }
    """); }
}
