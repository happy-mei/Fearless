package program.typesystem;

import org.junit.jupiter.api.Test;

import static program.typesystem.RunTypeSystem.ok;

public class TestInlineDecs {
  @Test void personFactory() { ok("""
    package test
    FPerson:{ #(name: Str, age: UInt): Person -> Person:{
      .name: Str -> name,
      .age: UInt -> age,
      }}
    Ex:{
      .create: Person -> FPerson#(Bob, TwentyFour),
      .name(p: Person): Str -> p.name,
      }
    """, """
    package test
    Str:{} Bob:Str{}
    UInt:{} TwentyFour:UInt{}
    """); }
}
