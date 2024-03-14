package tour;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static tour.TourHelper.*;
public class Ex01HelloWorldTest {
/*
----A tour of Fearless via the standard library----
A Fearless project is a folder containing files with extension *.fear.
Those files can be at top level or inside folders.
The organization of files in folders is for the benefit of the programmers and
have no impact on the semantic of fearless.
To start a Fearless application, we specify the fully qualified name of a type.
In the example below, we would need to run
>fearless myFolder test.Test
-------------------------*/@Test void helloWorld() { run("""
    package test
    Test:Main {sys -> FIO#sys.println("Hello, World!")}
    //prints Hello, World!
    """); }/*--------------------------------------------
As you can see, the code above is a very minimal Hello World program.
- In the first line we declare that our file belongs to the package 'test'.
Note how there is no need the files inside the package 'test' to be all contained inside a 'test' folder.
We will omit the `package test` line in all other examples.
- We then declare the type Test. Test implements Main. All runnable types must implement Main and have zero
abstract methods.
- In the body of Test we implement main by `sys -> FIO#sys.println("Hello, World!")`,
  - `sys` is a parameter name (we can freely choose those) that will refer to a mutable System object.
  The System object is our starting point for any kind of interaction with the real world.
  We call those objects that allows us to interact with the real world 'Object Capabilities'.
  In this example, we call `FIO#sys` to produce an `IO` object capability, and then we call the method `.println` on it.
  The syntax `FIO#sys` calls the method `#` on the object `FIO` passing `sys` as a parameter. You can read `FIO` as 'Factory for InputOutput'.
  Many types in Fearless start with 'F' for 'Factory/Function'.
  They serve the role of constructors in other languages.
-------------------------*/@Test void helloWorldBlockVar() { run("""
    Test:Main {sys -> Block#
      .var io = {FIO#sys}
      .return {io.println("Hello, World!")}
      }
    //prints Hello, World!
    """); }/*--------------------------------------------
In this other example we declare a local variable `io` and then we use it to call the `.println` method.
We use `Block` to open a statements block.
The `.var` method is used to declare the local variable `io`, that is initialized with the result of
executing `FIO#sys`.
The `.return` method concludes the block of statements.
In this case we need to return Void.
We could alternatively write
-------------------------*/@Test void helloWorldBlockVar2() { run("""
    Test:Main {sys -> Block#
      .var io = {FIO#sys}
      .do {io.println("Hello, World!")}
      .return {Void}
      }
    //prints Hello, World!
    """); }/*--------------------------------------------
The method `.do` just do some action in the middle of a block.
It is like declaring a local variable of type `Void` whose name is never used.
-------------------------*/@Disabled /*TODO: unimplemented lib code*/ @Test void fsReadHello() { run("""
    Test:Main {sys -> Block#
      .var[mut IO] io = {FIO#sys}
      .var[mut File] file = {io.file(Path#"test.txt")}
      .var content = {file.read!}
      .return {io.println(content)}
      }
    //prints ContentOfTextDotTxt
    """); }/*--------------------------------------------
Here we show how to read and write files.
We use `io` to open a  `mut File file`.
Note how we use `[..]` to specify the type of the local variable. The type can often be inferred,
we write it down here explicitly for clarity.
'file.read' reads the content of the file, but it does not directly return a string, since the operation
may fail. Instead, it returns an `Either[Str]` type.
The method `!` of either gets the string out (in the positive case), or throws an error in the case the
file could not be read.
-------------------------*/@Disabled /*TODO: unimplemented lib code*/ @Test void fsWriteHello() { run("""
    Test:Main {sys -> Block#
      .var   io = {FIO#sys}
      .var file = {io.file(Path#"test.txt")}
      .var    c = {"ContentOfTextDotTxt"}
      .return {file.write(c)!}
      }
    """); }/*--------------------------------------------
Consider this other example, writing on the file.
`file.write` does not throw an error if the operation fails, but returns an `Either[Void]`.
In most languages this would be bad API design, since the error could be ignored if the `Either[Void]`
is simply discarded. This concern is less valid in Fearless, where discarding values by accident is unlikely.
If the user forgot to use the `!` and just wrote
`.return {file.write(c)}`, the code would have not compiled.
To return for that block, we need a `Void` value, but `file.write(c)` produces an `Either[Void]`.

Overall, `IO` handles file system interactions (local IO) while Network IO is handled by another capability.
*/
}