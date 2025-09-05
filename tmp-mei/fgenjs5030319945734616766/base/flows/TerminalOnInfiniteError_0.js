import * as rt from "../rt/main.js";
export class base$$flows$$TerminalOnInfiniteError_0 {
  static $self = new base$$flows$$TerminalOnInfiniteError_0();

  $hash$imm() {
  return base$$flows$$TerminalOnInfiniteError_0.$hash$imm$fun(this);
}

  static $hash$imm$fun($this) {
  return (function() {
  console.error("Program aborted at:\n" + new Error().stack);
  if (typeof process !== "undefined") process.exit(1);
  else throw new Error("Program aborted");
})()
;
}

}
