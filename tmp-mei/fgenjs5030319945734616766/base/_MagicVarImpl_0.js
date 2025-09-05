import * as rt from "../rt/main.js";
export class base$$_MagicVarImpl_0 {
  static $self = new base$$_MagicVarImpl_0();

  $hash$imm(x_m$) {
  return base$$_MagicVarImpl_0.$hash$imm$fun(x_m$, this);
}

  static $hash$imm$fun(x_m$, $this) {
  return (function() {
  console.error("Program aborted at:\n" + new Error().stack);
  if (typeof process !== "undefined") process.exit(1);
  else throw new Error("Program aborted");
})()
;
}

}
