import * as rt from "../rt/main.js";
export class base$$Todo_0 {
  static $self = new base$$Todo_0();

  $exclamation$imm() {
  return base$$Todo_0.$exclamation$imm$fun(this);
}


$exclamation$imm(msg_m$) {
  return base$$Todo_0.$exclamation$imm$fun(msg_m$, this);
}

  static $exclamation$imm$fun($this) {
  return (function() {
  console.error("Program aborted at:\n" + new Error().stack);
  if (typeof process !== "undefined") process.exit(1);
  else throw new Error("Program aborted");
})()
;
}

    static $exclamation$imm$fun(msg_m$, $this) {
  return (function() {
  console.error("Program aborted at:\n" + new Error().stack);
  if (typeof process !== "undefined") process.exit(1);
  else throw new Error("Program aborted");
})()
;
}

}
