# The Fearless Programming Language

See https://fearlang.org for more.

## Repo structure
- `compiler` is the maven/intellij project containing the Fearless compiler itself.
- `assets` is for static assets used in the compiler, i.e. the source code of the standard library and runtime files.
- `artefacts` is for dynamically generated components that are used in the compiler. For example, the compiled libraries for natively implemented parts of the Fearless runtime.
- `native-runtime` is a rust library that provides a number of methods used in the Fearless runtime.
- `grammar` is a maven/intellij project for building Antlr4 gramar that we use to parse Fearless source code.
- `compiler-tests` includes some test programs that are used by the unit tests that live in `compiler/test`.

## Active branches
- `main` should be stable for use at any time with all tests passing.
	- Currently this is only true for macOS and Linux systems due to a weird interaction with Windows and ProcessBuilder
- `NewColonColonGrammar` has a number of syntax changes for Fearless, such as our `{::}` sugar.
- `vpf2` is Very Parallel Fearless.
- `guidev` has a GUI library
