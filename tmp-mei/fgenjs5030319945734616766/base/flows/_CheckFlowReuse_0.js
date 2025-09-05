import * as rt from "../rt/main.js";
export class base$$flows$$_CheckFlowReuse_0 {
  static $self = new base$$flows$$_CheckFlowReuse_0();

  $hash$imm(isTail_m$) {
  return base$$flows$$_CheckFlowReuse_0.$hash$imm$fun(isTail_m$, this);
}

  static $hash$imm$fun(isTail_m$, $this) {
  return (function() {
  console.error("Program aborted at:\n" + new Error().stack);
  if (typeof process !== "undefined") process.exit(1);
  else throw new Error("Program aborted");
})()
;
}

}
