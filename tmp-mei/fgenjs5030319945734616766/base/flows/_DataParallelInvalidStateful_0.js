import * as rt from "../rt/main.js";
export class base$$flows$$_DataParallelInvalidStateful_0 {
  static $self = new base$$flows$$_DataParallelInvalidStateful_0();

  $exclamation$imm() {
  return base$$flows$$_DataParallelInvalidStateful_0.$exclamation$imm$fun(this);
}

  static $exclamation$imm$fun($this) {
  return (function() {
  console.error("Program aborted at:\n" + new Error().stack);
  if (typeof process !== "undefined") process.exit(1);
  else throw new Error("Program aborted");
})()
;
}

}
