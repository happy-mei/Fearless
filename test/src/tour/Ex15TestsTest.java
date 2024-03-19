package tour;

import org.junit.jupiter.api.Test;

import static tour.TourHelper.run;

public class Ex15TestsTest {
  @Test void runTests() { run("""
    package test
    alias base.test.Main as TestMain,
    
    Test: TestMain{runner -> runner
      .test("Empty test", {sys -> Void})
      .test("Printing test", {sys -> FIO#sys.println("Hello, world!")})
      .run
      }
    //prints Hello, world!
    """); }
  @Test void runTestsWithSuite() { run("""
    package test
    alias base.test.Main as TestMain,
    
    Test: TestMain{runner -> runner
      .test("Empty test", {sys -> Void})
      .suite("Printing suite", {suite -> suite
        .test("Printing test", {sys -> FIO#sys.println("Hello, world!")})
        })
      .run
      }
    //prints Hello, world!
    """); }
}
