import { base$$InfoVisitor_1 } from "../base/InfoVisitor_1.js";
import * as rt from "../rt/main.js";

export class base$$_InfoToJson_0 extends base$$InfoVisitor_1 {
  static $self = new base$$_InfoToJson_0();

  list$imm(info_m$) {
  return base$$_InfoToJson_0.list$imm$fun(info_m$, this);
}


msg$imm(info_m$) {
  return base$$_InfoToJson_0.msg$imm$fun(info_m$, this);
}


map$imm(info_m$) {
  return base$$_InfoToJson_0.map$imm$fun(info_m$, this);
}

  static msg$imm$fun(info_m$, $this) {
  return (function() {
  console.error("Program aborted at:\n" + new Error().stack);
  if (typeof process !== "undefined") process.exit(1);
  else throw new Error("Program aborted");
})()
;
}

    static list$imm$fun(info_m$, $this) {
  return (function() {
  console.error("Program aborted at:\n" + new Error().stack);
  if (typeof process !== "undefined") process.exit(1);
  else throw new Error("Program aborted");
})()
;
}

    static map$imm$fun(info_m$, $this) {
  return (function() {
  console.error("Program aborted at:\n" + new Error().stack);
  if (typeof process !== "undefined") process.exit(1);
  else throw new Error("Program aborted");
})()
;
}

}
