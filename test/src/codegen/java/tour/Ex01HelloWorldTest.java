package codegen.java.tour;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import utils.Base;

import static utils.RunOutput.Res;
import static codegen.java.RunJavaProgramTests.*;

public class Ex01HelloWorldTest {
  @Test void helloWorld() { ok(new Res("Hello, World!", "", 0), "test.Test", """
    package test
    Test:Main {sys -> FIO#sys.println("Hello, World!")}
    """, Base.mutBaseAliases); }
  @Test void helloWorldBlock() { ok(new Res("Hello, World!", "", 0), "test.Test", """
    package test
    Test:Main {sys -> Block#
      .return {FIO#sys.println("Hello, World!")}
      }
    """, Base.mutBaseAliases); }
  @Test void helloWorldBlockVar() { ok(new Res("Hello, World!", "", 0), "test.Test", """
    package test
    Test:Main {sys -> Block#
      .var io = {FIO#sys}
      .return {io.println("Hello, World!")}
      }
    """, Base.mutBaseAliases); }
  // Local IO (i.e. FS) is handled by IO. Network IO is handled by another capability.
  @Disabled // TODO: unimplemented lib code
  @Test void fsReadWriteHello() { ok(new Res("Hello, World!", "", 0), "test.Test", """
    package test
    Test:Main {sys -> Block#
      .var io = {FIO#sys}
      .var file = {io.file(Path#"test.txt")}
      .do {file.write("Hello, World!")!} // ! unwraps the result/either to the positive case, or throws
      .var content = {file.read()!}
      .return {io.println(content)}
      }
    """, Base.mutBaseAliases); }
}
