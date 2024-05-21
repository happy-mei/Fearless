package tour;

import org.junit.jupiter.api.Test;
import utils.Base;
import utils.RunOutput;

import static codegen.java.RunJavaProgramTests.ok;

public class Ex14RandomTest {
  static final String RNG_ALIASES = """
    package test
    alias base.rng.FRandom as FRandom,
    alias base.rng.Random as Random,
    """;
  @Test void generatesNumber() { ok(new RunOutput.Res("570564682", "", 0), """
    package test
    Test:Main {sys -> FIO#sys.println((FRandom#1337).nat.str)}
    """, Base.mutBaseAliases, RNG_ALIASES); }

  @Test void generatesFloat() { ok(new RunOutput.Res("0.26568988443617236", "", 0), """
    package test
    Test:Main {sys -> FIO#sys.println((FRandom#1337).float.str)}
    """, Base.mutBaseAliases, RNG_ALIASES); }

  @Test void generatesNumberWithRandomSeed() { ok(new RunOutput.Res("", "", 0), """
    package test
    Test:Main {sys -> Block#
      .let[mut RandomSeed] seeder = {FRandomSeed#sys}
      .let rng1 = {FRandom#(seeder#)}
      .let rng2 = {FRandom#(seeder#)}
      // I mean, this technically has a _tiny_ chance of failing but it's so small that
      // a bug in the random number generator is more likely than this test failing due to a genuine collision
      .assert {(rng1.nat == (rng2.nat)).not}
      .return {{}}
      }
    """, Base.mutBaseAliases, RNG_ALIASES); }

  @Test void generatesMultipleNumbers() { ok(new RunOutput.Res("570564682\n1499355484\n1372376065\n1209872585\n241596240", "", 0), """
    package test
    Test:Main {sys -> Rng#(FRandom#1337, FIO#sys, Count.nat(5))}
    Rng: {#(rng: mut Random, io: mut IO, n: mut Count[Nat]): Void -> Block#
      .loop {n.get == 0 ? {.then -> ControlFlow.break, .else -> Block#(io.println(rng.nat.str), n--, ControlFlow.continue)}}
      .return {{}}
      }
    """, Base.mutBaseAliases, RNG_ALIASES); }

  @Test void generatesMultipleNumbersInRange() { ok(new RunOutput.Res("10 18 17 16 7 14 15 18 17 6", "", 0), """
    package test
    Test:Main {sys -> Rng#(FRandom#1337, FIO#sys, Count.nat(10))}
    Rng: {#(rng: mut Random, io: mut IO, n: mut Count[Nat]): Void -> Block#
      .loop {n.get == 0 ? {.then -> ControlFlow.break, .else -> Block#(io.print(rng.nat(5, 25).str+" "), n--, ControlFlow.continue)}}
      .return {{}}
      }
    """, Base.mutBaseAliases, RNG_ALIASES); }

  @Test void generatesNumberDifferentSeed() { ok(new RunOutput.Res("570564682\n1717387703", "", 0), """
    package test
    Test:Main {sys -> Block#
      .do {FIO#sys.println((FRandom#1337).nat.str)}
      .do {FIO#sys.println((FRandom#50000000).nat.str)}
      .return {{}}
      }
    """, Base.mutBaseAliases, RNG_ALIASES); }

  @Test void generateRandomSeed() { ok(new RunOutput.Res("[###]", "", 0), """
    package test
    Test:Main {sys -> FIO#sys.println(FRandomSeed#sys#.str)}
    """, Base.mutBaseAliases, RNG_ALIASES); }
}
