package tour;

import org.junit.jupiter.api.Test;
import utils.Base;
import utils.RunOutput.Res;

import static tour.TourHelper.run;
import static codegen.java.RunJavaProgramTests.*;

public class Ex15TestsTest {
  @Test void runTests() { run("""
    package test
    alias base.test.Main as TestMain,
    
    Test: TestMain{_, runner -> runner
      .test("Empty test", {sys -> Void})
      .test("Printing test", {sys -> FIO#sys.println("Hello, world!")})
      .run
      }
    //prints Hello, world!
    """); }
  @Test void runTestsWithSuite() { run("""
    package test
    alias base.test.Main as TestMain,
    
    Test: TestMain{_, runner -> runner
      .test("Empty test", {sys -> Void})
      .suite("Printing suite", {suite -> suite
        .test("Printing test", {sys -> FIO#sys.println("Hello, world!")})
        })
      .run
      }
    //prints Hello, world!
    """); }
  @Test void runTestsWithPrintReporter() { ok(new Res("Hello, world!", """
    # Test Results
    ## <unnamed>
    Empty test

    ### Printing suite
    Printing test
    """, 0), "test.Test", """
    package test
    alias base.test.Main as TestMain, alias base.test.ResultPrinters as ResultPrinters,
    
    Test: TestMain{system, runner -> runner
      .test("Empty test", {sys -> Void})
      .suite("Printing suite", {suite -> suite
        .test("Printing test", {sys -> FIO#sys.println("Hello, world!")})
        })
      .withReporter(ResultPrinters#(FIO#system))
      .run
      }
    """, Base.mutBaseAliases); }
}
