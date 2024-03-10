package tour;

import org.junit.jupiter.api.Test;
import utils.Base;
import utils.RunOutput;

import static codegen.java.RunJavaProgramTests.ok;

public class Ex11UtilityTraitsTest {
  @Test void flowSumStr() { ok(new RunOutput.Res("30", "", 0), "test.Test", """
    package test
    Test:Main {sys -> Block#
      .var[mut Person] personMut = {FPerson#("Bob")}
      .var[read Person] personRead = {Foo#{personMut}}
      .assert{personMut.name == (personRead.name)}
      .do{personMut.name("Alice")}
      .assert{personRead.name == "Alice"}
      .return{{}}
      }
    Foo:{ #(f: read F[read Person]): read Person -> f# }
    FPerson:{ #(name: Str): Person -> Block#
      .var nameRef = {Ref#name}
      .return {Person:{
        read .name: Str -> nameRef.get,
        mut .name(newName: Str): Void -> nameRef.set(newName),
        }}
      }
    """, Base.mutBaseAliases);}
}
