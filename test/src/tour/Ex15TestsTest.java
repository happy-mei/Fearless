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

  @Test void nestedTestsWithPrintReporter() { ok(new Res("Hello, world!", """
    # Test Results
    ## <unnamed>
    Empty test
        
    ### Printing suite
    Printing test
        
    non-Printing test
        
    #### nested
    test1
        
    test2
        
    ##### more nested
    test3
        
    test4
        
    ###### more nested
    test5
        
    **more nested** \s
    test6
        
    **more nested** \s
    test7
        
    **more nested** \s
    test8
    ### top level
    test1
    """, 0), "test.Test", """
    package test
    alias base.test.Main as TestMain, alias base.test.ResultPrinters as ResultPrinters,
    
    Test: TestMain{system, runner -> runner
      .test("Empty test", {sys -> Void})
      .suite("Printing suite", {suite -> suite
        .test("Printing test", {sys -> FIO#sys.println("Hello, world!")})
        .test("non-Printing test", {sys -> Void})
        .suite("nested", {suite' -> suite'
          .test("test1", {sys -> Void})
          .test("test2", {sys -> Void})
          .suite("more nested", {suite'' -> suite''
            .test("test3", {sys -> Void})
            .test("test4", {sys -> Void})
            .suite("more nested", {suite3 -> suite3
              .test("test5", {sys -> Void})
              .suite("more nested", {suite4 -> suite4
                .test("test6", {sys -> Void})
                .suite("more nested", {suite5 -> suite5
                  .test("test7", {sys -> Void})
                  .suite("more nested", {suite6 -> suite6
                    .test("test8", {sys -> Void})
                    })
                  })
                })
              })
            })
          })
        })
      .suite("top level", {suite -> suite
        .test("test1", {sys -> Void})
        })
      .withReporter(ResultPrinters#(FIO#system))
      .run
      }
    """, Base.mutBaseAliases); }
}
