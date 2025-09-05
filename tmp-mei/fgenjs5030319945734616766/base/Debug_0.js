import { base$$Sealed_0 } from "../base/Sealed_0.js";
import * as rt from "../rt/main.js";

export class base$$Debug_0 extends base$$Sealed_0 {
  static $self = new base$$Debug_0();

  println$imm(x_m$) {
  return base$$Debug_0.println$imm$fun(x_m$, this);
}


identify$imm(x_m$) {
  return base$$Debug_0.identify$imm$fun(x_m$, this);
}


$hash$imm(x_m$) {
  return base$$Debug_0.$hash$imm$fun(x_m$, this);
}

  static $hash$imm$fun(x_m$, $this) {
  return (function() {
  console.error("Program aborted at:\n" + new Error().stack);
  if (typeof process !== "undefined") process.exit(1);
  else throw new Error("Program aborted");
})()
;
}

    static println$imm$fun(x_m$, $this) {
  return (function() {
  console.error("Program aborted at:\n" + new Error().stack);
  if (typeof process !== "undefined") process.exit(1);
  else throw new Error("Program aborted");
})()
;
}

    static identify$imm$fun(x_m$, $this) {
  return (function() {
  console.error("Program aborted at:\n" + new Error().stack);
  if (typeof process !== "undefined") process.exit(1);
  else throw new Error("Program aborted");
})()
;
}

}
