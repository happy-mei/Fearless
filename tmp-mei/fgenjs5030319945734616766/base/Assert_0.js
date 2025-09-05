import { base$$Sealed_0 } from "../base/Sealed_0.js";
import * as rt from "../rt/main.js";

export class base$$Assert_0 extends base$$Sealed_0 {
  static $self = new base$$Assert_0();

  $exclamation$imm(assertion_m$) {
  return base$$Assert_0.$exclamation$imm$fun(assertion_m$, this);
}


$exclamation$imm(assertion_m$, cont_m$) {
  return base$$Assert_0.$exclamation$imm$fun(assertion_m$, cont_m$, this);
}


$exclamation$imm(assertion_m$, msg_m$, cont_m$) {
  return base$$Assert_0.$exclamation$imm$fun(assertion_m$, msg_m$, cont_m$, this);
}


_fail$imm() {
  return base$$Assert_0._fail$imm$fun(this);
}


_fail$imm(msg_m$) {
  return base$$Assert_0._fail$imm$fun(msg_m$, this);
}

  static $exclamation$imm$fun(assertion_m$, $this) {
  return (function() {
  console.error("Program aborted at:\n" + new Error().stack);
  if (typeof process !== "undefined") process.exit(1);
  else throw new Error("Program aborted");
})()
;
}

    static $exclamation$imm$fun(assertion_m$, cont_m$, $this) {
  return (function() {
  console.error("Program aborted at:\n" + new Error().stack);
  if (typeof process !== "undefined") process.exit(1);
  else throw new Error("Program aborted");
})()
;
}

    static $exclamation$imm$fun(assertion_m$, msg_m$, cont_m$, $this) {
  return (function() {
  console.error("Program aborted at:\n" + new Error().stack);
  if (typeof process !== "undefined") process.exit(1);
  else throw new Error("Program aborted");
})()
;
}

    static _fail$imm$fun($this) {
  return (function() {
  console.error("Program aborted at:\n" + new Error().stack);
  if (typeof process !== "undefined") process.exit(1);
  else throw new Error("Program aborted");
})()
;
}

    static _fail$imm$fun(msg_m$, $this) {
  return (function() {
  console.error("Program aborted at:\n" + new Error().stack);
  if (typeof process !== "undefined") process.exit(1);
  else throw new Error("Program aborted");
})()
;
}

}
