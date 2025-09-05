import * as rt from "../rt/main.js";
export class base$$Error_0 {
  static $self = new base$$Error_0();

  $exclamation$imm(info_m$) {
  return base$$Error_0.$exclamation$imm$fun(info_m$, this);
}


msg$imm(msg_m$) {
  return base$$Error_0.msg$imm$fun(msg_m$, this);
}

  static $exclamation$imm$fun(info_m$, $this) {
  return (function() {
  console.error("Program aborted at:\n" + new Error().stack);
  if (typeof process !== "undefined") process.exit(1);
  else throw new Error("Program aborted");
})()
;
}

    static msg$imm$fun(msg_m$, $this) {
  return (function() {
  console.error("Program aborted at:\n" + new Error().stack);
  if (typeof process !== "undefined") process.exit(1);
  else throw new Error("Program aborted");
})()
;
}

}
